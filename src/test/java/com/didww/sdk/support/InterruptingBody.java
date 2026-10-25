package com.didww.sdk.support;

import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.ForwardingSource;
import okio.Okio;

import java.io.IOException;

/**
 * Replaces the 200 body of a request to one path with the same bytes from
 * memory, which interrupts the reading thread on its first read with data, or
 * at the end of the data. Reads from memory never check the interrupt flag, so
 * the first to see it is whatever the body is written to.
 */
public final class InterruptingBody {

    private InterruptingBody() {
    }

    public static Interceptor interceptor(String path, boolean atEnd) {
        return chain -> {
            Response response = chain.proceed(chain.request());
            if (response.code() != 200 || !chain.request().url().encodedPath().equals(path)) {
                return response;
            }
            ResponseBody original = response.body();
            byte[] bytes = original.bytes();
            ForwardingSource source = new ForwardingSource(new Buffer().write(bytes)) {
                private boolean interrupted;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long read = super.read(sink, byteCount);
                    if (!interrupted && (atEnd ? read < 0 : read > 0)) {
                        interrupted = true;
                        Thread.currentThread().interrupt();
                    }
                    return read;
                }
            };
            return response.newBuilder()
                    .body(ResponseBody.create(Okio.buffer(source), original.contentType(), bytes.length))
                    .build();
        };
    }
}
