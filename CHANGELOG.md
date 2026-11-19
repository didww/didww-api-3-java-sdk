# Changelog

## [5.0.0] - 2026-11-19

Targets DIDWW API `2026-11-19`. The client is still configured with an API key: the SDK mints and caches the bearer tokens itself. For API `2026-04-16`, stay on 4.x (`4.1.4`, branch `release-4.1`). The README section "Migrating from 4.x" describes each change.

### Breaking Changes
- Authentication: every request carries `Authorization: Bearer <token>`. The SDK mints the token from the API key with `POST /token` on the token service (`auth.didww.com`, sandbox `auth-sandbox.didww.com`), one token per audience, and caches it. The `Api-Key` header is sent only to the token service, so outgoing traffic must reach it. `X-DIDWW-API-Version` is no longer sent: the version is requested in the token. The token service throws the new `DidwwAuthException` from any call that needs a token.
- `DidwwClient.DEFAULT_API_VERSION` is `2026-11-19`. `build()` throws `IllegalArgumentException` for an API version that is not a date or is earlier than `2026-11-19`, an API key that is empty or holds a control or non-ASCII character, and a service URL that is not a usable http(s) URL.
- `DidGroup`, `AddressRequirement`, `EmergencyRequirement` and `EmergencyCallingService` have `getNumberType()` (`NumberType`) instead of the `did_group_type` relationship; filter with `number_type`, and with `did_group.number_type` on available DIDs.
- `VoiceInTrunk#setPop` and `getPop` use the enum `com.didww.sdk.resource.enums.Pop`, an attribute sent in lowercase (`fra`, `ams`…), no longer a relationship.
- `SipConfiguration#setCodecs` and `getCodecs` (codec names in priority order, `codecs`) replace `setCodecIds` and `getCodecIds` (`codec_ids`). `Codec` is a class with a constant per codec instead of an enum: `getValue()` returns the name, and `values()`, `valueOf`, `name()` and `switch` no longer apply.
- The mandatory fields of `AddressRequirement` and `EmergencyRequirement` are field names as the API spells them (`birth_date`, `tax_id`…) and never `null`; `ProofType#getEntityType()` is lowercase (`address`, `business`, `personal`).
- Decimal numbers read into untyped maps or nodes (`ApiResponse#getMeta()`, the `source` and `meta` of API errors, `Export#getFilters()`, `GenericAuthenticationMethod` attributes, the maps of `HttpInConfiguration`) are `BigDecimal` with their exact value, not `Double`.
- HTTP defaults: connections open within 10 s, reads and writes wait up to 60 s, there is no call timeout, redirects are not followed, and no cookies, HTTP cache or `Authenticator` are used, also with your own `httpClientBuilder`, whose timeouts are kept.
- Export downloads succeed only on a 200 from the API or environment origin, stream the file and replace a `Path` destination only once it is complete. Any other status throws `DidwwApiException`, and a destination that cannot be written throws `UncheckedIOException`, where 4.x threw `DidwwClientException`.
- `uploadEncryptedFile` goes through the configured HTTP client with the bearer token and throws `DidwwApiException` on an error status, where 4.x threw `DidwwClientException`.
- `DidwwApiException` and `DidwwClientException` extend the new abstract `DidwwException`, still a `RuntimeException`.
- `client.balance()` returns `BalanceRepository`, which replaces the generic `SingletonRepository`; `find()` is unchanged.

### Added
- Token handling: the builder methods `apiVersion`, `scope` (`DidwwScope.READ` or `FULL`), `tokenStore`, `logger` (`java.lang.System.Logger`, silent by default), `authUrl`, `voiceInPricesUrl`, `voiceOutPricesUrl` and `smsPricesUrl`. Tokens are cached in a `TokenStore`, by default one `MemoryTokenStore` shared by the clients of the process; implement `TokenStore` to share them between processes, and `AccessToken` serializes with Jackson. The SDK asks for one-hour tokens and replaces a token once after a 401. After a token service 429 it mints no token until `Retry-After` has passed, and it retries the token service's 5xx answers and connection failures with backoff.
- Prices: `voiceInPrices()`, `voiceOutPrices()`, `smsOutA2pPrices()`, `smsOutP2pPrices()`, `smsOutOtpPrices()` and `smsInPrices()`, with lookups that return typed rows (`BigDecimal` prices), price file downloads that use `If-Modified-Since`, `PriceFileReader` to read a downloaded file, and `DidwwPricesException` with `PricesErrorKind`.
- SMS: `SmsTrunk` with the configurations `sms_to_email`, `http_in`, `http_out`, `smpp_esme` and `smpp_smsc` (and `GenericSmsTrunkConfiguration` for other types), `SmsTrunkGroup`, `SmsTrunkRegenerateCredential`, `SenderIdVerification`, the `sms_trunk` and `sms_trunk_group` routing of a DID, and `sms_in` and `sms_out` exports with the `ExportTrunkType` and `ExportServiceType` filters.
- Voice OUT trunks: `codecs` (`VoiceOutCodec`), `iceMode`, `rtcpMuxMode` and `rtcpFeedbackMode`. Inbound SIP trunks: `iceMode`, `rtcpMuxMode` and `rtcpFeedbackMode`, and the codecs `AMR` and `AMR_WB`. Codecs and media modes the SDK does not know are kept as read, such as `Codec.of("evs")`, and sent back unchanged.
- `DidwwApiException#getRetryAfter()`: the `Retry-After` of the answer, for a 429 among others. A 429 is not retried automatically.
- `DidGroup#getIsDemo()` and the company website of a business `Identity`.
- `downloadExport` and `downloadAndDecompressExport` overloads that write to an `OutputStream`.
- `DidwwCredentials#toString()` masks the API key. The SDK's exceptions and the `toString()` of tokens, token stores and SMS trunk configurations never show the API key, a token or generated credentials.
- `FILTERS.md`, the filter keys of every list endpoint; README sections on authentication, logging, prices, SMS, error handling and migrating from 4.x; 41 examples, including `TestReadmeExample`, which runs the README snippets against the sandbox.

### Removed
- `client.pops()`, `client.didGroupTypes()`, `com.didww.sdk.resource.Pop` and `com.didww.sdk.resource.DidGroupType`, with the `did_group_type` relationship and include.
- The public `com.didww.sdk.http.ApiKeyInterceptor`.

### Fixed
- OkHttp is published as an API dependency (`compile` scope), so code that passes an `OkHttpClient.Builder` to `httpClientBuilder` compiles without declaring OkHttp itself.
- A DID read with `order`, `did_group`, `emergency_verification`, `identity` or `emergency_calling_service` included can be updated: these relationships are no longer sent back, which the API refused. `setEmergencyCallingService(null)` still unassigns the emergency calling service.

## [4.1.4] - 2026-10-07

Publishes the 4.1.1 changes. The 4.1.1, 4.1.2 and 4.1.3 tags could not be built on JitPack, so they are not available there; use 4.1.4. No code changes since 4.1.1.

### Changed
- `jitpack.yml` installs JDK 11 through SDKMAN: the JitPack build image has no JDK 11 behind `jdk: openjdk11`.

## [4.1.3] - 2026-10-07

Publishes the 4.1.1 changes. The 4.1.1 and 4.1.2 tags could not be built on JitPack, so they are not available there; use 4.1.3. No code changes since 4.1.1.

### Added
- `jitpack.yml` pins the JitPack build to JDK 11, matching `sourceCompatibility`.

## [4.1.2] - 2026-10-07

Publishes the 4.1.1 changes. The 4.1.1 tag could not be built on JitPack, so 4.1.1 is not available there; use 4.1.2. No code changes since 4.1.1.

## [4.1.1] - 2026-10-07

### Changed
- `RequestValidator` is keyed with the callback secret enabled in the DIDWW User Panel (**APIs → DIDWW API 3 → Callback Secrets**); its constructor parameter is renamed to `callbackSecret`. Java has no named arguments, so existing code keeps working, and the signature algorithm is unchanged.

## [4.1.0] - 2026-08-18

### Added
- Complete the API 2026-04-16 `SipConfiguration` attribute set: `enabledSipRegistration`, `useDidInRuri`, `cnamLookup`, `networkProtocolPriority`, `diversionInjectMode`, plus the server-generated read-only `incomingAuthUsername` / `incomingAuthPassword`.
- `SipConfiguration#toString` and `CredentialsAndIpAuthenticationMethod#toString` redact credential fields with `[FILTERED]` so default logging / debugger / unhandled exception traces never expose plaintext credentials. Wire payload is unaffected.
- `SipConfiguration` auto-cascades server-enforced field dependencies on assignment: `setEnabledSipRegistration(true)` clears `host` / `port`, `setEnabledSipRegistration(false)` forces `useDidInRuri = false`, and `setHost(<non-null>)` flips both. Jackson populates the private fields via reflection during deserialization, so server responses are not clobbered.

### Fixed
- Reading server-provided `meta` off `EmergencyRequirement`, `EmergencyCallingService` and
  `DidHistory` no longer throws `ClassCastException` when a value arrives as a JSON number.
  `GET /v3/emergency_requirements` sends `setup_price` as a number while `monthly_price` is a
  string, and the `Map<String, String>` field enforced nothing: jsonapi-converter deserializes by
  `field.getType()`, which drops the type arguments, so Jackson stored `Integer` values. Values are
  now coerced on parse through `MetaMap`. `getMeta()` keeps its `Map<String, String>` signature and
  its JVM descriptor, so no consumer change or rebuild is required.

### Breaking Changes
- Renamed `Order#isCancelled` to `Order#isCanceled` for wire-format consistency
  (server wire value is `"canceled"`, single L). Safe because `isCancelled` was
  not in the released 3.0.0 tag — it was added on `feat/api-2026-04-16`.
