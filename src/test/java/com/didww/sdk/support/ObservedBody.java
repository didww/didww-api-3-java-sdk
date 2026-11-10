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
 * memory and runs {@code atEnd} when the SDK reaches the end of the data,
 * that is while the download is still in progress.
 */
public final class ObservedBody {

    private ObservedBody() {
    }

    public static Interceptor interceptor(String path, Runnable atEnd) {
        return chain -> {
            Response response = chain.proceed(chain.request());
            if (response.code() != 200 || !chain.request().url().encodedPath().equals(path)) {
                return response;
            }
            ResponseBody original = response.body();
            byte[] bytes = original.bytes();
            ForwardingSource source = new ForwardingSource(new Buffer().write(bytes)) {
                private boolean observed;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long read = super.read(sink, byteCount);
                    if (read < 0 && !observed) {
                        observed = true;
                        atEnd.run();
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
