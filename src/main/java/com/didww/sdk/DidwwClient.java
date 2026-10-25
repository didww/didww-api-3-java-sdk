package com.didww.sdk;

import com.didww.sdk.auth.Audience;
import com.didww.sdk.auth.BearerTokenInterceptor;
import com.didww.sdk.auth.DidwwScope;
import com.didww.sdk.auth.MemoryTokenStore;
import com.didww.sdk.auth.TokenProvider;
import com.didww.sdk.auth.TokenStore;
import com.didww.sdk.converter.DidwwResourceConverter;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.internal.AtomicFile;
import com.didww.sdk.internal.Bodies;
import com.didww.sdk.internal.MultiMemberGzipInputStream;
import com.didww.sdk.internal.Streams;
import com.didww.sdk.internal.Urls;
import com.didww.sdk.prices.PriceList;
import com.didww.sdk.prices.PricesService;
import com.didww.sdk.prices.SmsInPrice;
import com.didww.sdk.prices.SmsOutPrice;
import com.didww.sdk.prices.VoiceInPrice;
import com.didww.sdk.prices.VoiceOutPrice;
import com.didww.sdk.repository.BalanceRepository;
import com.didww.sdk.repository.ReadOnlyRepository;
import com.didww.sdk.repository.Repository;
import com.didww.sdk.resource.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ResourceConverter;
import okhttp3.Authenticator;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipException;

/**
 * The DIDWW API client. It is immutable after {@link Builder#build()} and safe
 * for concurrent use; build one per API key and environment and share it.
 *
 * <p>Every request is authenticated with a short-lived bearer token minted from
 * the API key by the token service ({@code auth.didww.com} or
 * {@code auth-sandbox.didww.com}); the API key itself is sent to the token
 * service only. Redirects are not followed.
 */
public class DidwwClient {
    /**
     * The API version requested when no other is configured. Earlier versions
     * are not supported by this major version of the SDK.
     */
    public static final String DEFAULT_API_VERSION = "2026-10-22";

    private static final String API_VERSION_PATTERN = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
    private static final MediaType OCTET_STREAM = MediaType.parse("application/octet-stream");

    private final DidwwCredentials credentials;
    private final String baseUrl;
    private final String authUrl;
    private final String voiceInPricesUrl;
    private final String voiceOutPricesUrl;
    private final String smsPricesUrl;
    private final String apiVersion;
    private final DidwwScope scope;
    private final TokenStore tokenStore;
    private final System.Logger logger;
    private final TokenProvider tokenProvider;
    // Authenticates with bearer tokens; JSON:API, uploads, exports and prices.
    private final OkHttpClient httpClient;
    // Without bearer tokens; the token requests.
    private final OkHttpClient tokenHttpClient;
    private final ResourceConverter converter;
    private final ObjectMapper objectMapper;

    private DidwwClient(Builder builder) {
        this.credentials = Objects.requireNonNull(builder.credentials, "credentials must not be null");
        validateApiKey(credentials.getApiKey());
        DidwwEnvironment environment = credentials.getEnvironment();

        this.apiVersion = builder.apiVersion != null ? builder.apiVersion : DEFAULT_API_VERSION;
        validateApiVersion(apiVersion);
        this.scope = builder.scope;

        this.baseUrl = Urls.trimTrailingSlash(serviceUrl("API base",
                builder.baseUrl != null ? builder.baseUrl : environment.getBaseUrl()));
        this.authUrl = Urls.trimTrailingSlash(serviceUrl("Token service",
                builder.authUrl != null ? builder.authUrl : environment.getAuthUrl()));
        this.voiceInPricesUrl = serviceUrl("Voice IN prices",
                builder.voiceInPricesUrl != null ? builder.voiceInPricesUrl : environment.getVoiceInPricesUrl());
        this.voiceOutPricesUrl = serviceUrl("Voice OUT prices",
                builder.voiceOutPricesUrl != null ? builder.voiceOutPricesUrl : environment.getVoiceOutPricesUrl());
        this.smsPricesUrl = serviceUrl("SMS prices",
                builder.smsPricesUrl != null ? builder.smsPricesUrl : environment.getSmsPricesUrl());

        this.objectMapper = DidwwResourceConverter.createObjectMapper();
        this.converter = DidwwResourceConverter.create(objectMapper);
        this.tokenStore = builder.tokenStore != null ? builder.tokenStore : DefaultTokenStore.INSTANCE;
        this.logger = builder.logger;

        // Copy via build().newBuilder() to avoid mutating the caller's builder.
        OkHttpClient.Builder httpBuilder = builder.httpClientBuilder != null
                ? builder.httpClientBuilder.build().newBuilder()
                : SharedHttpClient.INSTANCE.newBuilder();
        if (builder.connectTimeout != null) {
            httpBuilder.connectTimeout(builder.connectTimeout);
        }
        if (builder.readTimeout != null) {
            httpBuilder.readTimeout(builder.readTimeout);
        }
        OkHttpClient base = httpBuilder
                .followRedirects(false)
                .followSslRedirects(false)
                .cookieJar(CookieJar.NO_COOKIES)
                .authenticator(Authenticator.NONE)
                .cache(null)
                .build();

        this.tokenHttpClient = base;
        this.tokenProvider = new TokenProvider(tokenHttpClient, authUrl, credentials.getApiKey(), apiVersion,
                scope, tokenStore, logger);
        this.httpClient = base.newBuilder()
                .addInterceptor(new BearerTokenInterceptor(tokenProvider))
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Read-only repositories
    public ReadOnlyRepository<Country> countries() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "countries", Country.class, objectMapper);
    }

    public ReadOnlyRepository<Region> regions() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "regions", Region.class, objectMapper);
    }

    public ReadOnlyRepository<City> cities() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "cities", City.class, objectMapper);
    }

    public ReadOnlyRepository<DidGroup> didGroups() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "did_groups", DidGroup.class, objectMapper);
    }

    public ReadOnlyRepository<AvailableDid> availableDids() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "available_dids", AvailableDid.class, objectMapper);
    }

    public ReadOnlyRepository<Area> areas() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "areas", Area.class, objectMapper);
    }

    public ReadOnlyRepository<NanpaPrefix> nanpaPrefixes() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "nanpa_prefixes", NanpaPrefix.class, objectMapper);
    }

    public ReadOnlyRepository<ProofType> proofTypes() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "proof_types", ProofType.class, objectMapper);
    }

    public ReadOnlyRepository<PublicKey> publicKeys() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "public_keys", PublicKey.class, objectMapper);
    }

    public ReadOnlyRepository<DidHistory> didHistory() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "did_history", DidHistory.class, objectMapper);
    }

    public Repository<EmergencyCallingService> emergencyCallingServices() {
        return new Repository<>(httpClient, converter, baseUrl, "emergency_calling_services", EmergencyCallingService.class, objectMapper);
    }

    public ReadOnlyRepository<EmergencyRequirement> emergencyRequirements() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "emergency_requirements", EmergencyRequirement.class, objectMapper);
    }

    public ReadOnlyRepository<AddressRequirement> addressRequirements() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "address_requirements", AddressRequirement.class, objectMapper);
    }

    public ReadOnlyRepository<SupportingDocumentTemplate> supportingDocumentTemplates() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "supporting_document_templates", SupportingDocumentTemplate.class, objectMapper);
    }

    // Singleton repository
    public BalanceRepository balance() {
        return new BalanceRepository(httpClient, converter, baseUrl, "balance", objectMapper);
    }

    // CRUD repositories
    public Repository<Did> dids() {
        return new Repository<>(httpClient, converter, baseUrl, "dids", Did.class, objectMapper);
    }

    public Repository<VoiceInTrunk> voiceInTrunks() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_in_trunks", VoiceInTrunk.class, objectMapper);
    }

    public Repository<VoiceInTrunkGroup> voiceInTrunkGroups() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_in_trunk_groups", VoiceInTrunkGroup.class, objectMapper);
    }

    public Repository<VoiceOutTrunk> voiceOutTrunks() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_out_trunks", VoiceOutTrunk.class, objectMapper);
    }

    public Repository<VoiceOutTrunkRegenerateCredential> voiceOutTrunkRegenerateCredentials() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_out_trunk_regenerate_credentials", VoiceOutTrunkRegenerateCredential.class, objectMapper);
    }

    public Repository<SmsTrunk> smsTrunks() {
        return new Repository<>(httpClient, converter, baseUrl, "sms_trunks", SmsTrunk.class, objectMapper);
    }

    public Repository<SmsTrunkGroup> smsTrunkGroups() {
        return new Repository<>(httpClient, converter, baseUrl, "sms_trunk_groups", SmsTrunkGroup.class, objectMapper);
    }

    public Repository<SmsTrunkRegenerateCredential> smsTrunkRegenerateCredentials() {
        return new Repository<>(httpClient, converter, baseUrl, "sms_trunk_regenerate_credentials", SmsTrunkRegenerateCredential.class, objectMapper);
    }

    /**
     * Sender ID verifications: list, find and update ({@code name}, {@code sms_trunk}). The API has no create
     * or delete for them.
     *
     * @return the sender ID verification repository
     */
    public Repository<SenderIdVerification> senderIdVerifications() {
        return new Repository<>(httpClient, converter, baseUrl, "sender_id_verifications", SenderIdVerification.class, objectMapper);
    }

    public Repository<DidReservation> didReservations() {
        return new Repository<>(httpClient, converter, baseUrl, "did_reservations", DidReservation.class, objectMapper);
    }

    public Repository<CapacityPool> capacityPools() {
        return new Repository<>(httpClient, converter, baseUrl, "capacity_pools", CapacityPool.class, objectMapper);
    }

    public Repository<SharedCapacityGroup> sharedCapacityGroups() {
        return new Repository<>(httpClient, converter, baseUrl, "shared_capacity_groups", SharedCapacityGroup.class, objectMapper);
    }

    public Repository<Order> orders() {
        return new Repository<>(httpClient, converter, baseUrl, "orders", Order.class, objectMapper);
    }

    public Repository<Export> exports() {
        return new Repository<>(httpClient, converter, baseUrl, "exports", Export.class, objectMapper);
    }

    public Repository<Address> addresses() {
        return new Repository<>(httpClient, converter, baseUrl, "addresses", Address.class, objectMapper);
    }

    public Repository<AddressVerification> addressVerifications() {
        return new Repository<>(httpClient, converter, baseUrl, "address_verifications", AddressVerification.class, objectMapper);
    }

    public Repository<Identity> identities() {
        return new Repository<>(httpClient, converter, baseUrl, "identities", Identity.class, objectMapper);
    }

    public Repository<EncryptedFile> encryptedFiles() {
        return new Repository<>(httpClient, converter, baseUrl, "encrypted_files", EncryptedFile.class, objectMapper);
    }

    public Repository<PermanentSupportingDocument> permanentSupportingDocuments() {
        return new Repository<>(httpClient, converter, baseUrl, "permanent_supporting_documents", PermanentSupportingDocument.class, objectMapper);
    }

    public Repository<Proof> proofs() {
        return new Repository<>(httpClient, converter, baseUrl, "proofs", Proof.class, objectMapper);
    }

    public Repository<EmergencyVerification> emergencyVerifications() {
        return new Repository<>(httpClient, converter, baseUrl, "emergency_verifications", EmergencyVerification.class, objectMapper);
    }

    public Repository<EmergencyRequirementValidation> emergencyRequirementValidations() {
        return new Repository<>(httpClient, converter, baseUrl, "emergency_requirement_validations", EmergencyRequirementValidation.class, objectMapper);
    }

    public Repository<AddressRequirementValidation> addressRequirementValidations() {
        return new Repository<>(httpClient, converter, baseUrl, "address_requirement_validations", AddressRequirementValidation.class, objectMapper);
    }

    /**
     * Uploads one encrypted file to /encrypted_files as multipart/form-data.
     *
     * <p>The upload goes through the configured HTTP client (proxy, TLS,
     * interceptors). The body is built in memory, so it is sent once more with
     * a new access token when the API refuses the current one.
     *
     * @param encryptedData encrypted file bytes to upload
     * @param fileName      original file name sent as the multipart part name
     * @param fingerprint   encryption key fingerprint identifying the public key used
     * @param description   optional human-readable description for the file (may be {@code null})
     * @return the encrypted file ID returned by the API
     * @throws DidwwClientException if the upload fails or the response is unexpected
     */
    public String uploadEncryptedFile(byte[] encryptedData,
                                      String fileName,
                                      String fingerprint,
                                      String description) {
        Objects.requireNonNull(encryptedData, "encryptedData must not be null");
        Objects.requireNonNull(fileName, "fileName must not be null");
        Objects.requireNonNull(fingerprint, "fingerprint must not be null");

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("encrypted_files[encryption_fingerprint]", fingerprint)
                .addFormDataPart(
                        "encrypted_files[file]",
                        fileName,
                        RequestBody.create(encryptedData, OCTET_STREAM)
                );

        if (description != null && !description.isEmpty()) {
            bodyBuilder.addFormDataPart("encrypted_files[description]", description);
        }

        Request request = new Request.Builder()
                .url(baseUrl + "/encrypted_files")
                .post(bodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                byte[] errorBody = Bodies.readLimitedQuietly(response.body(), Bodies.MAX_ERROR_BODY_SIZE,
                        Bodies.errorBodyDeadlineMillis(httpClient));
                String detail = errorBody.length > 0 ? " " + new String(errorBody, StandardCharsets.UTF_8) : "";
                throw new DidwwClientException("Failed to upload encrypted file: HTTP " + response.code() + detail);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root != null ? root.get("data") : null;
            if (dataNode == null || !dataNode.has("id")) {
                throw new DidwwClientException("Unexpected encrypted_files upload response: " + responseBody);
            }

            return dataNode.get("id").asText();
        } catch (IOException e) {
            throw new DidwwClientException("Failed to upload encrypted file", e);
        }
    }

    /**
     * Downloads an export file and writes it to {@code destination} unchanged
     * (gzip-compressed CSV). The stream is not closed.
     *
     * <p>The access token is sent only to the origin (scheme, host and port) of
     * the JSON:API base URL or of the environment, as the API builds export URLs
     * on the environment's host even when {@link Builder#baseUrl(String)} points
     * elsewhere: a URL on any other origin raises {@link DidwwClientException}
     * without a request. Only a 200 answer is written, as it arrives; redirects
     * are not followed, and any other status, including 3xx and 204, raises
     * {@link DidwwApiException}.
     *
     * @param url         URL of the export file ({@code Export.getUrl()})
     * @param destination where the file is written
     * @throws DidwwClientException if the URL is refused, or the download fails or is cut short
     * @throws DidwwApiException    if the API answers with another status than 200
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public void downloadExport(String url, OutputStream destination) {
        Objects.requireNonNull(destination, "destination must not be null");
        download(url, false, (in, readFailure) -> Streams.copy(in, destination, readFailure));
    }

    /**
     * Downloads an export file to {@code destination} unchanged (gzip-compressed
     * CSV). The URL is checked and the answer handled as in
     * {@link #downloadExport(String, OutputStream)}. The file is written to a
     * temporary file next to {@code destination}, which replaces it only once
     * the whole file has arrived.
     *
     * @param url         URL of the export file ({@code Export.getUrl()})
     * @param destination local path where the file will be written
     * @throws DidwwClientException if the URL is refused, or the download fails or is cut short
     * @throws DidwwApiException    if the API answers with another status than 200
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public void downloadExport(String url, Path destination) {
        Objects.requireNonNull(destination, "destination must not be null");
        download(url, false, (in, readFailure) -> writeFile(in, destination, readFailure));
    }

    /**
     * Downloads a gzip-compressed export file (.csv.gz) and writes the
     * decompressed CSV to {@code destination}, as it arrives. The stream is not
     * closed. The URL is checked and the answer handled as in
     * {@link #downloadExport(String, OutputStream)}.
     *
     * @param url         URL of the gzip-compressed export file
     * @param destination where the decompressed CSV is written
     * @throws DidwwClientException if the URL is refused, or the download or the decompression fails
     * @throws DidwwApiException    if the API answers with another status than 200
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public void downloadAndDecompressExport(String url, OutputStream destination) {
        Objects.requireNonNull(destination, "destination must not be null");
        download(url, true, (in, readFailure) -> Streams.copy(in, destination, readFailure));
    }

    /**
     * Downloads a gzip-compressed export file (.csv.gz) and writes the
     * decompressed CSV to {@code destination} through a temporary file next to
     * it, which replaces it only once the whole file has been decompressed. The
     * URL is checked and the answer handled as in
     * {@link #downloadExport(String, OutputStream)}.
     *
     * @param url         URL of the gzip-compressed export file
     * @param destination local path where the decompressed CSV will be written
     * @throws DidwwClientException if the URL is refused, or the download or the decompression fails
     * @throws DidwwApiException    if the API answers with another status than 200
     * @throws UncheckedIOException if {@code destination} cannot be written
     */
    public void downloadAndDecompressExport(String url, Path destination) {
        Objects.requireNonNull(destination, "destination must not be null");
        download(url, true, (in, readFailure) -> writeFile(in, destination, readFailure));
    }

    private void download(String url, boolean decompress, BodyConsumer consumer) {
        HttpUrl exportUrl = checkExportUrl(url);
        Request request = new Request.Builder()
                .url(exportUrl)
                .get()
                .header("Accept-Encoding", "identity")
                .tag(Audience.class, Audience.API)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() != 200) {
                throw unexpectedStatus(response);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new DidwwClientException("Empty response body for export download");
            }
            Function<IOException, RuntimeException> readFailure = e -> readFailure(e, decompress);
            InputStream in = body.byteStream();
            if (!decompress) {
                consumer.accept(in, readFailure);
                return;
            }
            InputStream gzip;
            try {
                gzip = new MultiMemberGzipInputStream(in, 64 * 1024);
            } catch (IOException e) {
                throw readFailure.apply(e);
            }
            try (InputStream source = gzip) {
                consumer.accept(source, readFailure);
            }
        } catch (IOException e) {
            throw new DidwwClientException("Failed to download export", e);
        }
    }

    private static void writeFile(InputStream in, Path destination,
                                  Function<IOException, RuntimeException> readFailure) {
        try (AtomicFile file = AtomicFile.open(destination)) {
            Streams.copy(in, file.stream(), readFailure);
            file.commit(null);
        } catch (ClosedByInterruptException e) {
            throw interrupted(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof ClosedByInterruptException) {
                throw interrupted(e.getCause());
            }
            throw e;
        }
    }

    // The file channel closes when the thread is interrupted; the flag stays set.
    private static DidwwClientException interrupted(IOException e) {
        return new DidwwClientException("Export download was interrupted", e);
    }

    private static DidwwClientException readFailure(IOException e, boolean decompress) {
        String reason = e.getMessage() != null ? e.getMessage()
                : e instanceof EOFException ? "the download was cut short" : null;
        String failure = decompress && (e instanceof ZipException || e instanceof EOFException)
                ? "Failed to decompress export data" : "Failed to read export data";
        return new DidwwClientException(reason != null ? failure + ": " + reason : failure, e);
    }

    // Refuses an export URL whose origin is neither the origin of the JSON:API
    // base URL nor that of the environment, so that the token is not sent elsewhere.
    HttpUrl checkExportUrl(String url) {
        HttpUrl exportUrl = url != null ? HttpUrl.parse(url) : null;
        HttpUrl base = HttpUrl.parse(baseUrl);
        HttpUrl environment = HttpUrl.parse(credentials.getEnvironment().getBaseUrl());
        if (exportUrl != null && (Urls.sameOrigin(exportUrl, base) || Urls.sameOrigin(exportUrl, environment))) {
            return exportUrl;
        }
        String shown = exportUrl != null ? Urls.origin(exportUrl) : "that is not an absolute http or https URL";
        throw new DidwwClientException("Export URL " + shown + " does not match the API base URL "
                + Urls.origin(base) + " or the environment URL " + Urls.origin(environment));
    }

    private DidwwApiException unexpectedStatus(Response response) {
        byte[] bytes = Bodies.readLimitedQuietly(response.body(), Bodies.MAX_ERROR_BODY_SIZE,
                Bodies.errorBodyDeadlineMillis(httpClient));
        String body = new String(bytes, StandardCharsets.UTF_8);
        List<DidwwApiException.ApiError> errors = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(bytes);
            JsonNode errorsNode = root != null ? root.get("errors") : null;
            if (errorsNode != null && errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    if (errorNode.isObject()) {
                        errors.add(objectMapper.treeToValue(errorNode, DidwwApiException.ApiError.class));
                    }
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // Not a JSON:API errors document.
        }
        if (errors.isEmpty()) {
            DidwwApiException.ApiError error = new DidwwApiException.ApiError();
            error.setTitle("Unexpected status");
            error.setDetail(body.isEmpty() ? null : body);
            errors = Collections.singletonList(error);
        }
        return new DidwwApiException(response.code(), errors);
    }

    private static void validateApiKey(String apiKey) {
        if (apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key is required");
        }
        for (int i = 0; i < apiKey.length(); i++) {
            char c = apiKey.charAt(i);
            if (c < 0x20 || c > 0x7e) {
                throw new IllegalArgumentException("API key contains characters not allowed in an HTTP header");
            }
        }
    }

    private static void validateApiVersion(String apiVersion) {
        if (!apiVersion.matches(API_VERSION_PATTERN)) {
            throw new IllegalArgumentException("API version must be a YYYY-MM-DD date, got \"" + apiVersion + "\"");
        }
        if (apiVersion.compareTo(DEFAULT_API_VERSION) < 0) {
            throw new IllegalArgumentException("API version " + apiVersion + " is not supported: this SDK requires "
                    + DEFAULT_API_VERSION + " or later; use didww-api-3-java-sdk 4.x (branch release-4.1) for 2026-04-16");
        }
    }

    private static String serviceUrl(String name, String url) {
        if (!Urls.isServiceUrl(url)) {
            String shown = url != null && url.indexOf('@') >= 0 ? "a URL with user info" : "\"" + url + "\"";
            throw new IllegalArgumentException(name + " URL must be an http or https URL without a query or fragment, got "
                    + shown);
        }
        return url;
    }

    public DidwwCredentials getCredentials() {
        return credentials;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Returns the token service base URL.
     *
     * @return the token service URL
     */
    public String getAuthUrl() {
        return authUrl;
    }

    /**
     * Returns the API version requested for access tokens.
     *
     * @return the API version
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Returns the scope requested for access tokens.
     *
     * @return the scope, or {@code null} when none is requested
     */
    public DidwwScope getScope() {
        return scope;
    }

    /**
     * Returns the Voice IN price list (file and lookup). Lookup filters:
     * {@code number} (required), {@code src_country_iso}.
     *
     * @return the Voice IN prices service
     */
    public PricesService<VoiceInPrice> voiceInPrices() {
        return new PricesService<>(httpClient, voiceInPricesUrl, PriceList.VOICE_IN);
    }

    /**
     * Returns the Voice OUT price list (file and lookup). Lookup filters:
     * {@code number} (required).
     *
     * @return the Voice OUT prices service
     */
    public PricesService<VoiceOutPrice> voiceOutPrices() {
        return new PricesService<>(httpClient, voiceOutPricesUrl, PriceList.VOICE_OUT);
    }

    /**
     * Returns the SMS OUT A2P price list (file and lookup). Lookup filters:
     * one of {@code dst_number} and {@code dst_country_iso}, or both.
     *
     * @return the SMS OUT A2P prices service
     */
    public PricesService<SmsOutPrice> smsOutA2pPrices() {
        return new PricesService<>(httpClient, smsPricesUrl, PriceList.SMS_OUT_A2P);
    }

    /**
     * Returns the SMS OUT P2P price list (file and lookup). Lookup filters:
     * one of {@code dst_number} and {@code dst_country_iso}, or both.
     *
     * @return the SMS OUT P2P prices service
     */
    public PricesService<SmsOutPrice> smsOutP2pPrices() {
        return new PricesService<>(httpClient, smsPricesUrl, PriceList.SMS_OUT_P2P);
    }

    /**
     * Returns the SMS OUT OTP price list (file and lookup). Lookup filters:
     * one of {@code dst_number} and {@code dst_country_iso}, or both.
     *
     * @return the SMS OUT OTP prices service
     */
    public PricesService<SmsOutPrice> smsOutOtpPrices() {
        return new PricesService<>(httpClient, smsPricesUrl, PriceList.SMS_OUT_OTP);
    }

    /**
     * Returns the SMS IN price list (file and lookup). Lookup filters:
     * one of {@code dst_number} and {@code dst_country_iso}, or both.
     *
     * @return the SMS IN prices service
     */
    public PricesService<SmsInPrice> smsInPrices() {
        return new PricesService<>(httpClient, smsPricesUrl, PriceList.SMS_IN);
    }

    String getVoiceInPricesUrl() {
        return voiceInPricesUrl;
    }

    String getVoiceOutPricesUrl() {
        return voiceOutPricesUrl;
    }

    String getSmsPricesUrl() {
        return smsPricesUrl;
    }

    TokenStore getTokenStore() {
        return tokenStore;
    }

    System.Logger getLogger() {
        return logger;
    }

    TokenProvider getTokenProvider() {
        return tokenProvider;
    }

    // Package-private for testing: the client that authenticates with bearer tokens.
    OkHttpClient getHttpClient() {
        return httpClient;
    }

    // Package-private for testing: the client of the token requests.
    OkHttpClient getTokenHttpClient() {
        return tokenHttpClient;
    }

    @FunctionalInterface
    private interface BodyConsumer {
        void accept(InputStream in, Function<IOException, RuntimeException> readFailure) throws IOException;
    }

    // Clients built without an httpClientBuilder share its connection pool and dispatcher.
    private static final class SharedHttpClient {
        private static final OkHttpClient INSTANCE = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
    }

    // The token store of every client built without its own.
    private static final class DefaultTokenStore {
        private static final TokenStore INSTANCE = new MemoryTokenStore();
    }

    public static class Builder {
        private DidwwCredentials credentials;
        private Duration connectTimeout;
        private Duration readTimeout;
        private String baseUrl;
        private String authUrl;
        private String voiceInPricesUrl;
        private String voiceOutPricesUrl;
        private String smsPricesUrl;
        private String apiVersion;
        private DidwwScope scope;
        private TokenStore tokenStore;
        private System.Logger logger;
        private OkHttpClient.Builder httpClientBuilder;

        public Builder credentials(DidwwCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the connect timeout. Without it the timeout is 10 seconds, or the
         * one of the {@link #httpClientBuilder(OkHttpClient.Builder)}.
         *
         * @param connectTimeout the connect timeout
         * @return this builder
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the read timeout: the longest wait for the response headers and
         * between two reads of a body. Without it the timeout is 60 seconds, or
         * the one of the {@link #httpClientBuilder(OkHttpClient.Builder)}.
         *
         * @param readTimeout the read timeout
         * @return this builder
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Overrides the JSON:API base URL of the environment. The token service
         * and the price hosts are not affected, and export downloads still
         * accept URLs on the environment's host.
         *
         * @param baseUrl base URL to use instead of the environment default
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Overrides the token service base URL of the environment.
         *
         * @param authUrl the token service URL
         * @return this builder
         */
        public Builder authUrl(String authUrl) {
            this.authUrl = authUrl;
            return this;
        }

        /**
         * Overrides the Voice IN prices base URL of the environment (with or without {@code /v3}).
         *
         * @param voiceInPricesUrl the Voice IN prices URL
         * @return this builder
         */
        public Builder voiceInPricesUrl(String voiceInPricesUrl) {
            this.voiceInPricesUrl = voiceInPricesUrl;
            return this;
        }

        /**
         * Overrides the Voice OUT prices base URL of the environment (with or without {@code /v3}).
         *
         * @param voiceOutPricesUrl the Voice OUT prices URL
         * @return this builder
         */
        public Builder voiceOutPricesUrl(String voiceOutPricesUrl) {
            this.voiceOutPricesUrl = voiceOutPricesUrl;
            return this;
        }

        /**
         * Overrides the SMS prices base URL of the environment (with or without {@code /v3}).
         *
         * @param smsPricesUrl the SMS prices URL
         * @return this builder
         */
        public Builder smsPricesUrl(String smsPricesUrl) {
            this.smsPricesUrl = smsPricesUrl;
            return this;
        }

        /**
         * Sets the API version requested for access tokens: a YYYY-MM-DD date
         * not earlier than {@link DidwwClient#DEFAULT_API_VERSION}, the default.
         *
         * @param apiVersion the API version
         * @return this builder
         */
        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        /**
         * Sets the scope requested for access tokens. Without it no scope is
         * requested and the token service grants full access.
         *
         * @param scope the scope, or {@code null}
         * @return this builder
         */
        public Builder scope(DidwwScope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * Sets the store that caches access tokens, for example one shared by
         * several processes. The store holds live bearer tokens and must be
         * protected accordingly. The default is a {@link MemoryTokenStore}
         * shared by every client of the process built without its own store.
         *
         * @param tokenStore the token store
         * @return this builder
         */
        public Builder tokenStore(TokenStore tokenStore) {
            this.tokenStore = tokenStore;
            return this;
        }

        /**
         * Sets the logger for token warnings (store failures, a granted scope or
         * API version other than the requested one) and debug events (a minted
         * token). The default logs nothing; pass
         * {@code System.getLogger("com.didww.sdk")} or any other
         * {@link System.Logger} to enable logging. Tokens and API keys are never logged.
         *
         * @param logger the logger, or {@code null} for none
         * @return this builder
         */
        public Builder logger(System.Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Sets a custom OkHttpClient.Builder for advanced configuration such as
         * proxy, SSL, interceptors, timeouts, etc. The builder is copied, not
         * changed. Its timeouts are kept (OkHttp's default read timeout is 10
         * seconds), and a call timeout set on it also bounds the download of
         * large files. On the copy the SDK turns off redirects, cookies, the
         * cache and the authenticator, and adds the bearer token authentication.
         *
         * @param httpClientBuilder custom OkHttpClient builder to use as the base
         * @return this builder
         */
        public Builder httpClientBuilder(OkHttpClient.Builder httpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            return this;
        }

        /**
         * Builds the client.
         *
         * @return the client
         * @throws NullPointerException     if no credentials were set
         * @throws IllegalArgumentException if the API key, the API version or a service URL is not valid
         */
        public DidwwClient build() {
            return new DidwwClient(this);
        }
    }
}
