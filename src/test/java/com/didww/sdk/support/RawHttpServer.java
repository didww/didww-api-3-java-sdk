package com.didww.sdk.support;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A scripted server on 127.0.0.1 for cases WireMock cannot produce exactly: a
 * body shorter than its Content-Length, TLS alerts, a connection closed during
 * the TLS handshake, a body sent in steps. It counts accepted connections and
 * records the request heads it read.
 */
public final class RawHttpServer implements Closeable {
    private static final byte[] TLS_HANDSHAKE_FAILURE_ALERT = {0x15, 0x03, 0x03, 0x00, 0x02, 0x02, 0x28};

    /** Handles one accepted connection. */
    @FunctionalInterface
    public interface Handler {
        void handle(Connection connection) throws Exception;
    }

    /** One accepted connection. */
    public static final class Connection {
        private final RawHttpServer server;
        private final Socket socket;
        private final InputStream in;
        private final OutputStream out;

        private Connection(RawHttpServer server, Socket socket) throws IOException {
            this.server = server;
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        }

        public Socket socket() {
            return socket;
        }

        public InputStream in() {
            return in;
        }

        /** Reads the request line and headers, records them and returns them. */
        public String readHead() throws IOException {
            ByteArrayOutputStream head = new ByteArrayOutputStream();
            int matched = 0;
            byte[] end = {'\r', '\n', '\r', '\n'};
            int b;
            while (matched < end.length && (b = in.read()) != -1) {
                head.write(b);
                matched = b == end[matched] ? matched + 1 : (b == '\r' ? 1 : 0);
            }
            String text = head.toString(StandardCharsets.ISO_8859_1);
            server.heads.add(text);
            return text;
        }

        /** Reads a request head and a body of the Content-Length it names. */
        public String readRequest() throws IOException {
            String head = readHead();
            for (String line : head.split("\r\n")) {
                int colon = line.indexOf(':');
                if (colon > 0 && line.substring(0, colon).trim().equalsIgnoreCase("Content-Length")) {
                    long length = Long.parseLong(line.substring(colon + 1).trim());
                    for (long i = 0; i < length && in.read() != -1; i++) {
                        // Discard the body.
                    }
                }
            }
            return head;
        }

        /** Reads one TLS record, such as the ClientHello, so that closing does not reset the connection. */
        public void readTlsRecord() throws IOException {
            byte[] header = new byte[5];
            readFully(header);
            int length = ((header[3] & 0xff) << 8) | (header[4] & 0xff);
            readFully(new byte[length]);
        }

        private void readFully(byte[] buffer) throws IOException {
            int read = 0;
            while (read < buffer.length) {
                int n = in.read(buffer, read, buffer.length - read);
                if (n < 0) {
                    throw new IOException("connection closed");
                }
                read += n;
            }
        }

        public void write(byte[] bytes) throws IOException {
            out.write(bytes);
            out.flush();
        }

        public void write(String text) throws IOException {
            write(text.getBytes(StandardCharsets.ISO_8859_1));
        }

        /** Waits until the client closes the connection or {@code millis} pass. */
        public void drain(int millis) {
            try {
                socket.setSoTimeout(millis);
                while (in.read() != -1) {
                    // Discard.
                }
            } catch (IOException ignored) {
                // Closed or timed out.
            }
        }
    }

    private final ServerSocket serverSocket;
    private final Handler handler;
    private final AtomicInteger accepts = new AtomicInteger();
    private final List<String> heads = Collections.synchronizedList(new ArrayList<>());
    private final Thread acceptThread;

    private RawHttpServer(Handler handler) throws IOException {
        this.serverSocket = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        this.handler = handler;
        this.acceptThread = new Thread(this::acceptLoop, "raw-http-server-" + serverSocket.getLocalPort());
        this.acceptThread.setDaemon(true);
    }

    public static RawHttpServer start(Handler handler) throws IOException {
        RawHttpServer server = new RawHttpServer(handler);
        server.acceptThread.start();
        return server;
    }

    /** Answers every request with {@code response} as raw bytes, then closes the connection. */
    public static RawHttpServer respondingWith(byte[] response) throws IOException {
        return start(connection -> {
            connection.readRequest();
            connection.write(response);
            connection.drain(200);
        });
    }

    /** Answers the ClientHello of every connection with a fatal TLS handshake_failure alert. */
    public static RawHttpServer tlsAlert() throws IOException {
        return start(connection -> {
            connection.readTlsRecord();
            connection.write(TLS_HANDSHAKE_FAILURE_ALERT);
            connection.drain(5000);
        });
    }

    /** Closes every connection after reading the ClientHello. */
    public static RawHttpServer closingDuringHandshake() throws IOException {
        return start(Connection::readTlsRecord);
    }

    public int port() {
        return serverSocket.getLocalPort();
    }

    public String url() {
        return "http://127.0.0.1:" + port();
    }

    public String httpsUrl() {
        return "https://127.0.0.1:" + port();
    }

    public int accepts() {
        return accepts.get();
    }

    public List<String> heads() {
        synchronized (heads) {
            return new ArrayList<>(heads);
        }
    }

    private void acceptLoop() {
        while (!serverSocket.isClosed()) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                return;
            }
            accepts.incrementAndGet();
            Thread worker = new Thread(() -> serve(socket), "raw-http-connection");
            worker.setDaemon(true);
            worker.start();
        }
    }

    private void serve(Socket socket) {
        try (Socket s = socket) {
            handler.handle(new Connection(this, s));
        } catch (Exception ignored) {
            // The client went away.
        }
    }

    @Override
    public void close() throws IOException {
        serverSocket.close();
    }
}
