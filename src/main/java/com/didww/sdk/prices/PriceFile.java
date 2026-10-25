package com.didww.sdk.prices;

import java.time.Instant;

/**
 * The price file metadata of a HEAD or download answer.
 */
public final class PriceFile {
    private final int statusCode;
    private final Instant lastModified;
    private final Long contentLength;
    private final String fileName;
    private final long bytesWritten;

    PriceFile(int statusCode, Instant lastModified, Long contentLength, String fileName, long bytesWritten) {
        this.statusCode = statusCode;
        this.lastModified = lastModified;
        this.contentLength = contentLength;
        this.fileName = fileName;
        this.bytesWritten = bytesWritten;
    }

    /**
     * Returns 200, or 304 when the file has not changed since the
     * If-Modified-Since date sent.
     *
     * @return the HTTP status
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the Last-Modified of the file.
     *
     * @return the time, or {@code null} when absent or not an IMF-fixdate
     */
    public Instant getLastModified() {
        return lastModified;
    }

    /**
     * Returns the size of the file in bytes, from Content-Length.
     *
     * @return the size, or {@code null} when unknown, for example on 304
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * Returns the file name from Content-Disposition, for example
     * {@code voice_in_prices.csv.gz}, as sent: check it before using it as a
     * path.
     *
     * @return the file name, or {@code null} when absent
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the bytes written by a download.
     *
     * @return the number of bytes; 0 for HEAD and 304
     */
    public long getBytesWritten() {
        return bytesWritten;
    }

    /**
     * Returns whether the file has not changed since the If-Modified-Since
     * date sent (304).
     *
     * @return true on 304
     */
    public boolean isNotModified() {
        return statusCode == 304;
    }

    @Override
    public String toString() {
        return "PriceFile{statusCode=" + statusCode + ", lastModified=" + lastModified
                + ", contentLength=" + contentLength + ", fileName=" + fileName
                + ", bytesWritten=" + bytesWritten + "}";
    }
}
