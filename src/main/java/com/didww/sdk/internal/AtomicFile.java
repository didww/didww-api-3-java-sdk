package com.didww.sdk.internal;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;

/**
 * Internal helper, not part of the SDK API: writes a file through a temporary
 * file next to it, which replaces the target only on {@link #commit(FileTime)}.
 * The temporary file is created with the default permissions of new files
 * (the umask on POSIX systems), so the target gets them too.
 */
public final class AtomicFile implements Closeable {
    private static final int MAX_NAME_ATTEMPTS = 8;

    private final Path target;
    private final Path temp;
    private final FileChannel channel;
    private final OutputStream out;
    private boolean committed;

    private AtomicFile(Path target, Path temp, FileChannel channel) {
        this.target = target;
        this.temp = temp;
        this.channel = channel;
        this.out = new BufferedOutputStream(Channels.newOutputStream(channel), 64 * 1024);
    }

    /**
     * Creates a new temporary file next to {@code target}.
     *
     * @param target the file to write
     * @return the open file
     * @throws IOException if the temporary file cannot be created
     */
    public static AtomicFile open(Path target) throws IOException {
        Path name = target.getFileName();
        if (name == null) {
            throw new IOException("Not a file path: " + target);
        }
        FileAlreadyExistsException last = null;
        for (int i = 0; i < MAX_NAME_ATTEMPTS; i++) {
            Path temp = target.resolveSibling("." + name + "." + Jitter.randomHex(8) + ".tmp");
            try {
                FileChannel channel = FileChannel.open(temp, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                return new AtomicFile(target, temp, channel);
            } catch (FileAlreadyExistsException e) {
                last = e;
            }
        }
        throw last;
    }

    /**
     * Returns the stream that writes the temporary file.
     *
     * @return the output stream
     */
    public OutputStream stream() {
        return out;
    }

    /**
     * Flushes the temporary file to the disk, sets its modification time when
     * {@code lastModified} is not {@code null}, and moves it over the target.
     *
     * @param lastModified the modification time to set, or {@code null}
     * @throws IOException if the file cannot be written or moved
     */
    public void commit(FileTime lastModified) throws IOException {
        out.flush();
        channel.force(true);
        out.close();
        if (lastModified != null) {
            Files.setLastModifiedTime(temp, lastModified);
        }
        try {
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
        committed = true;
    }

    /**
     * Removes the temporary file unless it was committed.
     *
     * @throws IOException if the temporary file cannot be removed
     */
    @Override
    public void close() throws IOException {
        if (committed) {
            return;
        }
        try {
            out.close();
        } catch (IOException ignored) {
            // The file is removed anyway.
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
