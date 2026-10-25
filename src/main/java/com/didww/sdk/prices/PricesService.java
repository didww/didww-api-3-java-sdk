package com.didww.sdk.prices;

import com.didww.sdk.auth.Audience;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.http.JsonApiMediaType;
import com.didww.sdk.internal.AtomicFile;
import com.didww.sdk.internal.Bodies;
import com.didww.sdk.internal.Jitter;
import com.didww.sdk.internal.Sleeper;
import com.didww.sdk.internal.Streams;
import com.didww.sdk.internal.Urls;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.ProtocolException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;

/**
 * One price list of a prices service: its price file, a gzipped CSV, and its
 * lookup. Obtain it with the price accessors of {@link com.didww.sdk.DidwwClient},
 * such as {@code voiceInPrices()}. Requests carry a token for the audience of
 * the service host. Without a price plan of the service every call fails with
 * {@link PricesErrorKind#PRICE_PLAN_NOT_ASSIGNED}.
 *
 * <p>A failed call raises {@link DidwwPricesException} (see
 * {@link DidwwPricesException#getKind()}), {@link com.didww.sdk.exception.DidwwAuthException}
 * when no token could be obtained, or {@link IllegalArgumentException} for
 * invalid filters. {@link #lookup(Map)} raises {@link DidwwClientException}
 * when a 200 answer cannot be parsed or holds no prices. A destination stream
 * or file that cannot be written raises {@link UncheckedIOException} with the
 * destination's {@link IOException} as its cause.
 *
 * <p>Redirects are never followed: a 3xx answer raises
 * {@link PricesErrorKind#UNEXPECTED_STATUS}. A 503 answer to a file request
 * whose Retry-After is at most a minute is retried up to twice, after that
 * Retry-After (at least a second) plus a small random delay; any other answer,
 * including 429, is not retried. (OkHttp itself sends a request once more,
 * without waiting, when a 503 carries {@code Retry-After: 0}.) Error bodies are
 * read up to 1 MiB and within the read timeout of the client.
 *
 * <p>An interrupted thread ends the call with
 * {@link PricesErrorKind#CONNECTION_ERROR} and keeps its interrupt flag set.
 * A call timeout set on the client's {@link okhttp3.OkHttpClient.Builder} also
 * bounds the download of large files.
 *
 * @param <T> the row type of the price list
 */
public final class PricesService<T> {
    private static final String CODE_PRICE_PLAN_NOT_ASSIGNED = "price_plan_not_assigned";
    private static final String CODE_PRICE_NOT_FOUND = "price_not_found";
    private static final int MAX_UNAVAILABLE_RETRIES = 2;
    private static final long MAX_UNAVAILABLE_RETRY_AFTER_SECONDS = 60;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final PriceList<T> list;
    private Sleeper sleeper = Sleeper.SYSTEM;
    private DoubleSupplier random = Jitter.SECURE_RANDOM;
    private Long errorBodyDeadlineMillis;

    /**
     * Internal, not part of the SDK API: use the price accessors of
     * {@link com.didww.sdk.DidwwClient}.
     *
     * @param httpClient the client that authenticates with bearer tokens and does not follow redirects
     * @param baseUrl    the base URL of the prices service, with or without {@code /v3}
     * @param list       the price list
     */
    public PricesService(OkHttpClient httpClient, String baseUrl, PriceList<T> list) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl must not be null");
        this.list = Objects.requireNonNull(list, "list must not be null");
    }

    void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    void setRandom(DoubleSupplier random) {
        this.random = random;
    }

    void setErrorBodyDeadline(Duration deadline) {
        this.errorBodyDeadlineMillis = deadline.toMillis();
    }

    /**
     * Returns the price list of this service.
     *
     * @return the price list
     */
    public PriceList<T> getPriceList() {
        return list;
    }

    /**
     * Returns the prices matching {@code filters}, such as
     * {@code Map.of("number", "18005550123")}. Null and blank values are
     * dropped. A missing or unknown filter raises
     * {@link IllegalArgumentException} before any request; the formats of the
     * values are checked by the service ({@link PricesErrorKind#BAD_REQUEST}).
     * A lookup is never retried, except once with a new token after a 401.
     *
     * @param filters the lookup filters by name (without {@code filter[...]}), may be {@code null}
     * @return the matching prices
     * @throws IllegalArgumentException if a filter is missing or not allowed
     * @throws DidwwPricesException     if the service answers with another status than 200, or cannot be reached
     * @throws DidwwClientException     if the 200 answer cannot be parsed or holds no prices
     */
    public PriceLookup<T> lookup(Map<String, String> filters) {
        Map<String, String> given = checkFilters(filters);
        HttpUrl.Builder url = HttpUrl.get(Urls.joinV3(baseUrl, list.getLookupPath())).newBuilder();
        for (Map.Entry<String, String> filter : given.entrySet()) {
            url.addQueryParameter("filter[" + filter.getKey() + "]", filter.getValue());
        }
        Request request = new Request.Builder()
                .url(url.build())
                .get()
                .header("Accept", JsonApiMediaType.VALUE)
                .tag(Audience.class, list.getAudience())
                .build();

        byte[] body;
        try (Response response = execute(request)) {
            checkNotRedirected(response, request);
            if (response.code() != 200) {
                throw error(response);
            }
            ResponseBody responseBody = response.body();
            try {
                body = responseBody != null ? responseBody.bytes() : new byte[0];
            } catch (IOException e) {
                throw connectionError(200, e);
            }
        }
        return parseLookup(body);
    }

    /**
     * Returns the metadata of the price file without its content.
     *
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200, or cannot be reached
     */
    public PriceFile head() {
        return head(null);
    }

    /**
     * Returns the metadata of the price file without its content. A given
     * {@code ifModifiedSince} is sent as If-Modified-Since, in whole seconds;
     * a file that has not changed since answers 304
     * ({@link PriceFile#isNotModified()}).
     *
     * @param ifModifiedSince the time of the copy at hand, or {@code null}
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200 or 304, or cannot be reached
     */
    public PriceFile head(Instant ifModifiedSince) {
        return requestFile("HEAD", ifModifiedSince, null);
    }

    /**
     * Writes the price file to {@code destination} exactly as served: gzipped,
     * never decompressed (see {@link PriceFileReader}). The stream is not
     * closed.
     *
     * @param destination where the file is written
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200, or the transfer fails
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public PriceFile download(OutputStream destination) {
        return download(destination, null);
    }

    /**
     * Writes the price file to {@code destination} exactly as served: gzipped,
     * never decompressed (see {@link PriceFileReader}). A given
     * {@code ifModifiedSince} is sent as If-Modified-Since, in whole seconds;
     * a 304 answer writes nothing. The stream is not closed.
     *
     * <p>Only a 200 answer is written, but {@code destination} may hold partial
     * data when the transfer fails; a body shorter than its Content-Length
     * raises {@link PricesErrorKind#CONNECTION_ERROR}.
     *
     * @param destination     where the file is written
     * @param ifModifiedSince the time of the copy at hand, or {@code null}
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200 or 304, or the transfer fails
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public PriceFile download(OutputStream destination, Instant ifModifiedSince) {
        Objects.requireNonNull(destination, "destination must not be null");
        return requestFile("GET", ifModifiedSince, destination);
    }

    /**
     * Downloads the price file to {@code path}. The file is written to a
     * temporary file next to {@code path}, flushed to the disk and moved over
     * {@code path} only when the whole file has arrived, so a failed download
     * leaves an existing file as it was. The new file gets the default
     * permissions of a new file (the umask on POSIX systems) and the
     * Last-Modified of the answer as its modification time.
     *
     * <p>On Windows the replacement fails while {@code path} is open, for
     * example by a {@link PriceFileReader}.
     *
     * @param path where the file is written
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200, or the transfer fails
     * @throws UncheckedIOException if the file cannot be written, for example in a missing directory
     *                              (then without a request)
     */
    public PriceFile downloadToFile(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        return downloadToFile(path, null);
    }

    /**
     * Downloads the price file to {@code path} like {@link #downloadToFile(Path)}
     * unless the file at {@code path} is current: the modification time of an
     * existing file is sent as If-Modified-Since, and a 304 answer leaves the
     * file untouched ({@link PriceFile#isNotModified()}). It trusts the
     * modification time of any file at {@code path}.
     *
     * @param path where the file is kept
     * @return the file metadata
     * @throws DidwwPricesException if the service answers with another status than 200 or 304, or the transfer fails
     * @throws UncheckedIOException if the file cannot be read or written
     */
    public PriceFile fetch(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        Instant ifModifiedSince;
        try {
            ifModifiedSince = Files.getLastModifiedTime(path).toInstant();
        } catch (NoSuchFileException e) {
            ifModifiedSince = null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return downloadToFile(path, ifModifiedSince);
    }

    private PriceFile downloadToFile(Path path, Instant ifModifiedSince) {
        try (AtomicFile file = AtomicFile.open(path)) {
            PriceFile result = requestFile("GET", ifModifiedSince, file.stream());
            if (!result.isNotModified()) {
                Instant lastModified = result.getLastModified();
                file.commit(lastModified != null ? FileTime.from(lastModified) : null);
            }
            return result;
        } catch (ClosedByInterruptException e) {
            throw connectionError(200, e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (UncheckedIOException e) {
            // The file channel closes when the thread is interrupted.
            if (e.getCause() instanceof ClosedByInterruptException) {
                throw connectionError(200, e.getCause());
            }
            throw e;
        }
    }

    private PriceFile requestFile(String method, Instant ifModifiedSince, OutputStream destination) {
        HttpUrl url = HttpUrl.get(Urls.joinV3(baseUrl, list.getFilePath()));
        for (int attempt = 1; ; attempt++) {
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .method(method, null)
                    .header("Accept-Encoding", "identity")
                    .tag(Audience.class, list.getAudience());
            if (ifModifiedSince != null) {
                builder.header("If-Modified-Since", HttpDates.format(ifModifiedSince));
            }
            Request request = builder.build();

            Duration wait;
            try (Response response = execute(request)) {
                checkNotRedirected(response, request);
                int status = response.code();
                if (status == 200 || status == 304) {
                    return priceFile(response, status == 200 ? destination : null);
                }
                wait = unavailableWait(response, attempt);
                if (wait == null) {
                    throw error(response);
                }
                Bodies.readLimitedQuietly(response.body(), Bodies.MAX_ERROR_BODY_SIZE, errorBodyDeadlineMillis());
            }
            try {
                sleeper.sleep(wait);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DidwwPricesException(PricesErrorKind.CONNECTION_ERROR, 503, null, null, null, e);
            }
        }
    }

    // The wait before retrying a 503 answer whose Retry-After is at most a
    // minute, or null when the answer is not retried.
    private Duration unavailableWait(Response response, int attempt) {
        if (response.code() != 503 || attempt > MAX_UNAVAILABLE_RETRIES) {
            return null;
        }
        Duration retryAfter = retryAfter(response);
        if (retryAfter == null || retryAfter.getSeconds() > MAX_UNAVAILABLE_RETRY_AFTER_SECONDS) {
            return null;
        }
        Duration atLeastOneSecond = retryAfter.getSeconds() < 1 ? Duration.ofSeconds(1) : retryAfter;
        return Jitter.spread(atLeastOneSecond, random);
    }

    private PriceFile priceFile(Response response, OutputStream destination) {
        int status = response.code();
        Long contentLength = status == 200 ? contentLength(response.header("Content-Length")) : null;
        long written = 0;
        if (destination != null) {
            ResponseBody body = response.body();
            if (body != null) {
                InputStream in = body.byteStream();
                written = Streams.copy(in, destination, e -> connectionError(200, e));
            }
            if (contentLength != null && written != contentLength) {
                throw connectionError(200, new ProtocolException(
                        "incomplete body: " + written + " of " + contentLength + " bytes"));
            }
        }
        return new PriceFile(status, HttpDates.parse(response.header("Last-Modified")), contentLength,
                ContentDisposition.fileName(response.header("Content-Disposition")), written);
    }

    private static Long contentLength(String header) {
        if (header == null) {
            return null;
        }
        String value = header.trim();
        return value.matches("\\d{1,18}") ? Long.valueOf(value) : null;
    }

    private Response execute(Request request) {
        try {
            return httpClient.newCall(request).execute();
        } catch (IOException e) {
            throw connectionError(null, e);
        }
    }

    // A user interceptor could follow a redirect: an answer from another URL
    // than the one sent is refused.
    private static void checkNotRedirected(Response response, Request request) {
        HttpUrl sent = request.url();
        HttpUrl answered = response.request().url();
        if (!Urls.sameOrigin(sent, answered) || !sent.encodedPath().equals(answered.encodedPath())) {
            throw new DidwwPricesException(PricesErrorKind.UNEXPECTED_STATUS, response.code(), null, null, null,
                    new ProtocolException("The request was redirected; redirects are not followed"));
        }
    }

    private static DidwwPricesException connectionError(Integer status, IOException cause) {
        return new DidwwPricesException(PricesErrorKind.CONNECTION_ERROR, status, null, null, null, cause);
    }

    private long errorBodyDeadlineMillis() {
        return errorBodyDeadlineMillis != null ? errorBodyDeadlineMillis : Bodies.errorBodyDeadlineMillis(httpClient);
    }

    // Maps an answer other than a success. A broken or slow body reads as
    // nothing: the status and Retry-After still decide the error.
    private DidwwPricesException error(Response response) {
        int status = response.code();
        byte[] body = Bodies.readLimitedQuietly(response.body(), Bodies.MAX_ERROR_BODY_SIZE, errorBodyDeadlineMillis());
        List<DidwwApiException.ApiError> errors = parseErrors(body);
        String code = errors.isEmpty() ? null : errors.get(0).getCode();
        PricesErrorKind kind;
        switch (status) {
            case 400:
                kind = PricesErrorKind.BAD_REQUEST;
                break;
            case 401:
                kind = PricesErrorKind.NOT_AUTHORIZED;
                break;
            case 403:
                kind = PricesErrorKind.ACCESS_DENIED;
                break;
            case 404:
                if (code == null || code.isEmpty()) {
                    code = CODE_PRICE_PLAN_NOT_ASSIGNED;
                }
                kind = CODE_PRICE_NOT_FOUND.equals(code)
                        ? PricesErrorKind.PRICE_NOT_FOUND
                        : PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED;
                break;
            case 406:
                kind = PricesErrorKind.NOT_ACCEPTABLE;
                break;
            case 429:
                kind = PricesErrorKind.TOO_MANY_REQUESTS;
                break;
            case 503:
                kind = PricesErrorKind.SERVICE_UNAVAILABLE;
                break;
            default:
                kind = PricesErrorKind.UNEXPECTED_STATUS;
                break;
        }
        return new DidwwPricesException(kind, status, code, errors, retryAfter(response), null);
    }

    // The Retry-After of an answer when given in seconds; an HTTP date is ignored.
    private static Duration retryAfter(Response response) {
        String header = response.header("Retry-After");
        if (header == null) {
            return null;
        }
        String value = header.trim();
        return value.matches("\\d{1,9}") ? Duration.ofSeconds(Long.parseLong(value)) : null;
    }

    // The error objects of a JSON:API errors document; none for any other body.
    private static List<DidwwApiException.ApiError> parseErrors(byte[] body) {
        List<DidwwApiException.ApiError> errors = new ArrayList<>();
        JsonNode root;
        try {
            root = MAPPER.readTree(body);
        } catch (IOException | RuntimeException e) {
            return errors;
        }
        JsonNode items = root != null && root.isObject() ? root.get("errors") : null;
        if (items == null || !items.isArray()) {
            return errors;
        }
        for (JsonNode item : items) {
            if (!item.isObject()) {
                continue;
            }
            DidwwApiException.ApiError error = new DidwwApiException.ApiError();
            error.setTitle(text(item, "title"));
            error.setDetail(text(item, "detail"));
            error.setStatus(text(item, "status"));
            error.setCode(text(item, "code"));
            error.setSource(object(item, "source"));
            error.setMeta(object(item, "meta"));
            errors.add(error);
        }
        return errors;
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value != null && value.isValueNode() && !value.isNull() ? value.asText() : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value != null && value.isObject() ? MAPPER.convertValue(value, Map.class) : null;
    }

    private Map<String, String> checkFilters(Map<String, String> filters) {
        Map<String, String> given = givenFilters(filters);

        List<String> allowed = list.allowed();
        List<String> unknown = new ArrayList<>();
        for (String name : given.keySet()) {
            if (!allowed.contains(name)) {
                unknown.add(name);
            }
        }
        List<String> missing = new ArrayList<>();
        for (String name : list.getRequired()) {
            if (!given.containsKey(name)) {
                missing.add(name);
            }
        }
        boolean anyOfGiven = false;
        for (String name : list.getAnyOf()) {
            anyOfGiven |= given.containsKey(name);
        }

        String lookupPath = list.getLookupPath();
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException("Filter " + String.join(", ", unknown) + " is not allowed on "
                    + lookupPath + " (allowed: " + String.join(", ", allowed) + ")");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Filter " + String.join(", ", missing) + " is required on " + lookupPath);
        }
        if (!list.getAnyOf().isEmpty() && !anyOfGiven) {
            throw new IllegalArgumentException("One of filters " + String.join(", ", list.getAnyOf())
                    + " is required on " + lookupPath);
        }
        return given;
    }

    // The filters with a name and a non-blank value, sorted by name.
    private static Map<String, String> givenFilters(Map<String, String> filters) {
        TreeMap<String, String> given = new TreeMap<>();
        if (filters == null) {
            return given;
        }
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            String value = filter.getValue();
            if (filter.getKey() != null && value != null && !value.isBlank()) {
                given.put(filter.getKey(), value);
            }
        }
        return given;
    }

    private PriceLookup<T> parseLookup(byte[] body) {
        JsonNode root;
        try {
            root = MAPPER.readTree(body);
        } catch (IOException e) {
            throw new DidwwClientException("Failed to parse price lookup: " + e.getMessage(), e);
        }
        if (root == null || !root.isObject()) {
            throw new DidwwClientException("Failed to parse price lookup: the answer is not a JSON:API document");
        }
        JsonNode data = root.get("data");
        if (data == null || data.isNull() || (data.isArray() && data.isEmpty())) {
            throw new DidwwClientException("Failed to parse price lookup: the answer holds no prices");
        }
        if (!data.isArray()) {
            throw new DidwwClientException("Failed to parse price lookup: data is not an array");
        }
        for (JsonNode item : data) {
            if (!item.isObject()) {
                throw new DidwwClientException("Failed to parse price lookup: data holds a value that is not a resource");
            }
        }

        List<T> rows;
        try {
            rows = list.getConverter().readDocumentCollection(body, list.getRowClass()).get();
        } catch (RuntimeException e) {
            throw new DidwwClientException("Failed to parse price lookup: " + e.getMessage(), e);
        }
        if (rows == null || rows.isEmpty() || rows.contains(null)) {
            throw new DidwwClientException("Failed to parse price lookup: the answer holds no prices");
        }
        return new PriceLookup<>(new ArrayList<>(rows), totalRecords(root.get("meta")));
    }

    private static int totalRecords(JsonNode meta) {
        if (meta == null || meta.isNull()) {
            return 0;
        }
        if (!meta.isObject()) {
            throw new DidwwClientException("Failed to parse price lookup meta: meta is not an object");
        }
        JsonNode total = meta.get("total_records");
        if (total == null || total.isNull()) {
            return 0;
        }
        if (!total.isIntegralNumber() || !total.canConvertToInt()) {
            throw new DidwwClientException("Failed to parse price lookup meta: total_records is not an integer");
        }
        return total.intValue();
    }

    @Override
    public String toString() {
        HttpUrl url = HttpUrl.get(Urls.joinV3(baseUrl, list.getLookupPath()));
        return "PricesService{" + url.newBuilder().username("").password("").build() + "}";
    }
}
