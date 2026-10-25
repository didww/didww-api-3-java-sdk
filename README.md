Java client for DIDWW API v3.

![Tests](https://github.com/didww/didww-api-3-java-sdk/actions/workflows/ci.yml/badge.svg)
![Coverage](https://img.shields.io/endpoint?url=https://didww.github.io/didww-api-3-java-sdk/badge.json)
![Java](https://img.shields.io/badge/java-11%2B-blue)

## About DIDWW API v3

The DIDWW API provides a simple yet powerful interface that allows you to fully integrate your own applications with DIDWW services. An extensive set of actions may be performed using this API, such as ordering and configuring phone numbers, setting capacity, creating SIP trunks and retrieving CDRs and other operational data.

The DIDWW API v3 is a fully compliant implementation of the [JSON API specification](http://jsonapi.org/format/).

This SDK uses [jsonapi-converter](https://github.com/jasminb/jsonapi-converter) for JSON:API serialization and deserialization.

Read more https://doc.didww.com/api

## API Version

This SDK targets DIDWW API version `2026-10-22` (`DidwwClient.DEFAULT_API_VERSION`). The version is not sent as a header: it is requested in the `api_version` of every access token the SDK mints (see [Authentication](#authentication)).

| SDK Version | Branch | DIDWW API Version |
|-------------|--------|-------------------|
| **5.x** | [`main`](https://github.com/didww/didww-api-3-java-sdk) | [`2026-10-22`](https://doc.didww.com/api3/2026-10-22/index.html) |
| **4.x** | [`release-4.1`](https://github.com/didww/didww-api-3-java-sdk/tree/release-4.1) | [`2026-04-16`](https://doc.didww.com/api3/2026-04-16/index.html) |
| **3.x** | [`2022-05-10`](https://github.com/didww/didww-api-3-java-sdk/tree/2022-05-10) | [`2022-05-10`](https://doc.didww.com/api3/2022-05-10/index.html) |

`DidwwClient.Builder#apiVersion` requests a later version. `build()` throws an `IllegalArgumentException` for a value that is not a `YYYY-MM-DD` date and for a version earlier than `2026-10-22`. The resources of earlier API versions are not in this release: use 4.x from the [`release-4.1`](https://github.com/didww/didww-api-3-java-sdk/tree/release-4.1) branch for API `2026-04-16` (see [Migrating from 4.x](#migrating-from-4x)).

## Requirements

- Java 11+

The SDK depends on [OkHttp](https://square.github.io/okhttp/) 4, [Jackson](https://github.com/FasterXML/jackson) 2 and jsonapi-converter. It adds no logging dependency (see [Logging](#logging)).

## Installation

This SDK is distributed via [JitPack](https://jitpack.io/#didww/didww-api-3-java-sdk). JitPack builds the library directly from GitHub, so no manual publishing is required.

Browse all available versions (tags, branches, commits):
[https://jitpack.io/#didww/didww-api-3-java-sdk](https://jitpack.io/#didww/didww-api-3-java-sdk)

### Gradle

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.didww:didww-api-3-java-sdk:5.0.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.didww</groupId>
        <artifactId>didww-api-3-java-sdk</artifactId>
        <version>5.0.0</version>
    </dependency>
</dependencies>
```

For API `2026-04-16`, stay on 4.x, for example `com.github.didww:didww-api-3-java-sdk:4.1.4`.

### Using a branch or commit

You can also consume the SDK from a specific branch or commit via JitPack:

```kotlin
dependencies {
    // specific branch (append -SNAPSHOT)
    implementation("com.github.didww:didww-api-3-java-sdk:main-SNAPSHOT")

    // specific commit hash
    // implementation("com.github.didww:didww-api-3-java-sdk:<commit-hash>")
}
```

## Usage

```java
import com.didww.sdk.*;
import com.didww.sdk.resource.*;
import com.didww.sdk.http.QueryParams;
import java.util.List;

DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.SANDBOX))
    .build();

// Check balance
Balance balance = client.balance().find().getData();
System.out.println("Balance: " + balance.getTotalBalance());

// List DID groups with stock keeping units
QueryParams params = QueryParams.builder()
    .include("stock_keeping_units")
    .filter("area_name", "Acapulco")
    .build();
List<DidGroup> didGroups = client.didGroups().list(params).getData();
```

`DidwwClient` is immutable once built and safe for concurrent use: build one per API key and environment and reuse it. Every call is synchronous.

For more examples visit [examples](examples/src/main/java/com/didww/examples/).

For details on obtaining your API key please visit https://doc.didww.com/api3/configuration.html

## Examples

- Source code: [examples/src/main/java/com/didww/examples](examples/src/main/java/com/didww/examples)
- How to run: [examples/README.md](examples/README.md)

## Configuration

```java
import com.didww.sdk.auth.DidwwScope;
import java.time.Duration;

// Production
DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .build();

// Sandbox, read-only tokens, logging through System.Logger, a shorter connect timeout
DidwwClient sandbox = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.SANDBOX))
    .scope(DidwwScope.READ)
    .logger(System.getLogger("com.didww.sdk"))
    .connectTimeout(Duration.ofSeconds(5))
    .build();
```

| Builder method | Default | Description |
|----------------|---------|-------------|
| `credentials(credentials)` | required | `new DidwwCredentials(apiKey, environment)`, with `DidwwEnvironment.PRODUCTION` or `DidwwEnvironment.SANDBOX`. The environment selects the API, token service and price hosts. See [Environments](#environments). |
| `apiVersion(version)` | `DidwwClient.DEFAULT_API_VERSION` | API version requested in every token. See [API Version](#api-version). |
| `scope(scope)` | none | `DidwwScope.READ` or `DidwwScope.FULL`. Without it no scope is requested and the token service grants `full`. See [Scope](#scope). |
| `tokenStore(store)` | one `MemoryTokenStore` shared by every client of the process built without its own | Cache for access tokens. See [Token cache](#token-cache). |
| `logger(logger)` | none: nothing is logged | `java.lang.System.Logger` for the token warnings and debug events. See [Logging](#logging). |
| `httpClientBuilder(builder)` | an OkHttp client shared by the SDK | `OkHttpClient.Builder` used as the base of every call. See [HTTP client](#http-client-proxy-tls-timeouts). |
| `connectTimeout(timeout)` | 10 s, or the one of your `httpClientBuilder` | Time allowed to open the TCP connection (to the proxy when one is set). The TLS handshake and the tunnel through an HTTP proxy are bounded by the read timeout, as in OkHttp. |
| `readTimeout(timeout)` | 60 s, or the one of your `httpClientBuilder` | Longest wait for a proxy tunnel, the TLS handshake, the response headers and between two reads of a body. |
| `baseUrl(url)` | API URL of the environment | JSON:API base URL only, including `/v3`. |
| `authUrl(url)` | Token service of the environment | Token service base URL. |
| `voiceInPricesUrl(url)`, `voiceOutPricesUrl(url)`, `smsPricesUrl(url)` | Price hosts of the environment | Price service base URLs, with or without the `/v3` suffix. |

The API key is sent only to the token service. An overridden URL, for example to send the traffic through a proxy, does not change the token audiences (see [Authentication](#authentication)).

`build()` throws a `NullPointerException` without credentials (and `new DidwwCredentials(...)` one for a `null` API key or environment), and an `IllegalArgumentException`, before any request, for:

- an empty API key, or one with a character outside printable ASCII (a control character, such as a trailing newline or a tab, or a non-ASCII character, such as a non-breaking space);
- an API version that is not a `YYYY-MM-DD` date, or that is earlier than `2026-10-22`;
- a base, token service or prices URL that is not an http or https URL with a host, contains a space or a control character, or has a query or fragment.

`DidwwCredentials#toString()` masks the API key.

### Environments

| Environment | API | Token service | Voice IN prices | Voice OUT prices | SMS prices |
|-------------|-----|---------------|-----------------|------------------|------------|
| `DidwwEnvironment.PRODUCTION` | `https://api.didww.com/v3` | `https://auth.didww.com` | `https://api-vin.didww.com/v3` | `https://api-vout.didww.com/v3` | `https://api-sms.didww.com/v3` |
| `DidwwEnvironment.SANDBOX` | `https://sandbox-api.didww.com/v3` | `https://auth-sandbox.didww.com` | `https://api-vin-sandbox.didww.com/v3` | `https://api-vout-sandbox.didww.com/v3` | `https://api-sms-sandbox.didww.com/v3` |

A firewall that limits outgoing traffic must allow the token service, and the price hosts when you use [Prices](#prices).

### HTTP client (proxy, TLS, timeouts)

Without `httpClientBuilder` every client of the process shares one OkHttp client, so they share its connection pool and dispatcher. It opens TCP connections within 10 s, waits up to 60 s for a proxy tunnel, the TLS handshake, the response headers and between two reads or writes of a body, and has no call timeout, so a large price or export file is not cut off while its data keeps arriving.

You can pass your own `OkHttpClient.Builder`, for example for a proxy or a custom trust store:

```java
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import okhttp3.OkHttpClient;

OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.example.com", 8080)))
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(60));

DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .httpClientBuilder(httpBuilder)
    .build();
```

The SDK copies the builder and leaves yours unchanged. The copy is used for every call: the token service, the JSON:API, export download, encrypted file upload and prices. Your proxy, proxy authenticator, TLS settings, interceptors, connection pool and dispatcher are kept. On the copy the SDK:

- follows no redirects (`followRedirects(false)` and `followSslRedirects(false)`), whatever your builder says, so that neither a token nor the API key is sent to another URL;
- uses `CookieJar.NO_COOKIES`, no HTTP cache and no `Authenticator`, so no state is carried between calls or API keys;
- adds the bearer token to every request, with an interceptor placed after yours.

Your builder's timeouts are kept as you set them; OkHttp's own defaults are 10 s each, and `connectTimeout` and `readTimeout` of the `DidwwClient.Builder` override them. **A `callTimeout` on your builder bounds the whole call, body included, so it also cuts off the download of a large price or export file.** Prefer a read timeout.

Your interceptors see the `Api-Key` header of the token requests, and a network interceptor also sees the `Authorization` header of the other requests. Do not log them: with OkHttp's `HttpLoggingInterceptor`, call `redactHeader("Api-Key")` and `redactHeader("Authorization")`, and keep in mind that OkHttp's `Request` and `Headers` `toString()` print the `Api-Key` header in clear.

Price and export files are requested with `Accept-Encoding: identity` and written exactly as served (gzipped); OkHttp's transparent decompression still applies to JSON:API answers. Each attempt of a token request is limited to 10 s, independently of your timeouts. An error answer is read up to 1 MiB, within the read timeout of the client (60 s when it has none).

OkHttp itself sends a request once more, without waiting and with the same token, when it is answered 503 with `Retry-After: 0`, or 408 without a positive `Retry-After`. The SDK accepts this; other 503 answers are not resent by OkHttp (see [Price files](#price-files) for the SDK's own 503 retry).

## Authentication

Every request carries `Authorization: Bearer <token>`. The SDK mints the tokens itself with `POST /token` on the token service, sending your API key in the `Api-Key` header. The API key is sent nowhere else. No code change is needed for this: `new DidwwCredentials("YOUR_API_KEY", environment)` works as before.

- **Audience.** One token per audience, minted on the first call to a service and reused until it expires. The audience is the production hostname of the service, in the sandbox too:

  | Calls | Token audience |
  |-------|----------------|
  | JSON:API resources, export download, encrypted file upload | `api.didww.com` |
  | Voice IN prices | `api-vin.didww.com` |
  | Voice OUT prices | `api-vout.didww.com` |
  | SMS OUT (A2P, P2P, OTP) and SMS IN prices | `api-sms.didww.com` |

  `client.publicKeys().list()` is sent without a token.
- **Expiry and 401.** A cached token lives for the `expires_in` granted; the SDK adds no local expiry margin. When a request is answered 401, the SDK drops that token, mints a new one and sends the request once more. A second 401 drops the new token too and is not retried: it becomes the error of the call (see [Error Handling](#error-handling)). A 403, such as `insufficient_scope`, is never retried.
- **Concurrency.** Concurrent calls of one `DidwwClient` that need the same token wait for a single mint, which runs on the thread of the first caller. A waiting thread that is interrupted stops waiting with `AuthErrorKind.CONNECTION_ERROR` and keeps its interrupt flag; the mint goes on for the other callers.
- **Token service errors** are thrown as `DidwwAuthException` from any call that needs a new token (kinds in [Error Handling](#error-handling)):
  - A 429 throws `AuthErrorKind.RATE_LIMITED` with `getRetryAfter()` (the `Retry-After` seconds, at least 1 s). Every further mint for the same token service and API key in the process fails the same way without a request until that time has passed, plus a small random delay so that clients do not retry in step. Such a refusal reports the time still left, so retry in a loop rather than once.
  - A 5xx answer, a connection failure or a timeout is retried with exponential backoff and full jitter (0.5 s base, 8 s cap, 4 attempts in all), then throws `SERVER_ERROR` or `CONNECTION_ERROR`.
  - A 400, a 401 or a TLS failure is thrown at once, without a retry.

### Scope

```java
import com.didww.sdk.auth.DidwwScope;

DidwwClient readOnly = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .scope(DidwwScope.READ) // GET and HEAD only; DidwwScope.FULL allows every method
    .build();
// Without scope(...) no scope is sent and the token service grants full.
```

A `READ` token is enough for the price lookups and price files. A write with a `READ` token is answered 403, which the JSON:API repositories throw as a `DidwwApiException` (see [Error Handling](#error-handling)).

The SDK compares each token it receives with what it asked for:

- a different `scope` (for example `full` granted for a `read` request) or a different `api_version` is logged as a warning through the logger (see [Logging](#logging)), and the token is used;
- a token with an empty access token, one that is not a valid header value, or one whose `aud` does not include the requested audience, throws `AuthErrorKind.INVALID_GRANT` and is not cached.

### Token cache

Tokens are cached in a `com.didww.sdk.auth.TokenStore`, keyed by the token service URL, the API key (as a SHA-256 hash), the audience, the API version and the scope. The key is an opaque string starting with `didww:token:v1:`. Each entry is stored for the token's `expires_in`.

The default is one `MemoryTokenStore` in process memory, shared by every client of the process built without `tokenStore(...)`; since the key covers the API key, clients of different accounts never see each other's tokens. To share tokens between processes, pass your own implementation, for example one backed by Redis:

```java
public interface TokenStore {
    AccessToken get(String key);                              // null when there is none or it has expired
    void set(String key, AccessToken token, Duration ttl);    // zero or negative ttl: no expiry
    void delete(String key);
}
```

```java
DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .tokenStore(myRedisStore)
    .build();
```

`AccessToken` serializes with Jackson under its wire names (`access_token`, `token_type`, `expires_in`, `aud`, `scope`, `api_version`, `minted_at`), the access token included, so that a store can persist it, for example with `new ObjectMapper().writeValueAsString(token)` and `readValue(json, AccessToken.class)`. Its `toString()` never shows the access token. A shared store holds live bearer tokens, so protect it like the API key. Processes that share a store can each mint once when a token is missing. A store must be safe for concurrent use.

A store that throws a `RuntimeException` does not fail the call: the error is logged as a warning, and the call goes on with a newly minted token (a failed write leaves the token uncached). Each call to a store other than `MemoryTokenStore` is limited to 10 seconds; the SDK runs it on a daemon thread and stops waiting after that, but an abandoned call keeps its thread until it returns, so bound the store's own I/O as well, for example with connect and read timeouts on the Redis client. After a 401 the refused token is deleted from the store only while it is still the stored one, so that a token another process has just stored survives; a store that keeps returning the refused token, for example after a failed delete, does not get it reused.

### Logging

`logger(...)` takes a `java.lang.System.Logger`, so the SDK needs no logging dependency. Following the usual library convention, the default logs nothing, not even the mismatch warnings, until you pass a logger:

```java
DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .logger(System.getLogger("com.didww.sdk"))
    .build();
```

`System.getLogger` logs through `java.util.logging`, unless a `System.LoggerFinder` routes it elsewhere, such as SLF4J's `org.slf4j:slf4j-jdk-platform-logging` or Log4j's `org.apache.logging.log4j:log4j-jpl`.

- `WARNING`: `DIDWW token scope mismatch` and `DIDWW token api_version mismatch` (followed by the requested and granted values), and token store failures (`DIDWW token store read failed`, `write failed`, `write skipped`, `delete failed`, with the exception).
- `DEBUG`: `DIDWW token minted`, with `aud`, `scope`, `api_version`, `expires_in` and `duration_ms`.

Neither the token nor the API key is ever logged. A logger that throws does not fail the call.

### SDK design decisions

- **Audience strategy.** One token per audience, minted on first use, rather than one token naming every service. An account that never calls the prices services never requests their audiences, so a refused audience cannot break DID management, and every token is valid for one service only.
- **Cache store.** In-process memory by default, shared by the clients of the process, because a Java service usually keeps its clients for its lifetime. A shared store is opt-in through `tokenStore(...)`. Lifetime is decided by the server through the 401 refresh, not by a local expiry margin.
- **Logger.** `System.Logger`, silent by default, so that the SDK does not pick a logging framework for you.
- **HTTP defaults.** Connect and read timeouts but no call timeout, so that large files are not cut off; a call timeout you set is kept as given. No redirects, so that credentials stay on the configured hosts; no cookies, cache or authenticator, so that no state is carried between calls; no decompression of files, so that they are written as served.
- **Gzip handling.** Price and export files are streamed to your `OutputStream` or file exactly as served, still gzipped, without being held in memory. The SDK never decompresses a price file on its own: `PriceFileReader` reads a downloaded file when you want rows, and `downloadAndDecompressExport` writes an export as plain CSV.
- **Upload retry.** The 401 retry sends the request body a second time. `uploadEncryptedFile` takes the encrypted content as a `byte[]` and builds the multipart body in memory, so the retry always resends the complete body.
- **Blocking waits.** Calls are synchronous: the token backoff and the price file 503 retry wait on the calling thread. An interrupt ends them with `CONNECTION_ERROR` and keeps the thread's interrupt flag set.

## Resources

### Read-Only Resources

```java
import com.didww.sdk.*;
import com.didww.sdk.resource.*;
import com.didww.sdk.http.QueryParams;

DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.SANDBOX))
    .build();

// Countries
List<Country> countries = client.countries().list().getData();
Country country = client.countries().find("uuid").getData();

// Regions
List<Region> regions = client.regions().list().getData();

// Cities
List<City> cities = client.cities().list().getData();

// Areas
List<Area> areas = client.areas().list().getData();

// NANPA Prefixes
List<NanpaPrefix> prefixes = client.nanpaPrefixes().list().getData();

// DID Groups (with stock keeping units), filtered by number type
QueryParams dgParams = QueryParams.builder()
    .include("stock_keeping_units")
    .filter("number_type", "toll_free")
    .build();
List<DidGroup> groups = client.didGroups().list(dgParams).getData();

// Available DIDs (with DID group and stock keeping units)
QueryParams adParams = QueryParams.builder().include("did_group.stock_keeping_units").build();
List<AvailableDid> available = client.availableDids().list(adParams).getData();

// Proof Types
List<ProofType> proofTypes = client.proofTypes().list().getData();

// Public Keys
List<PublicKey> publicKeys = client.publicKeys().list().getData();

// Address Requirements
List<AddressRequirement> requirements = client.addressRequirements().list().getData();

// Supporting Document Templates
List<SupportingDocumentTemplate> templates = client.supportingDocumentTemplates().list().getData();

// Balance (singleton)
Balance balance = client.balance().find().getData();
```

`DidGroup`, `AddressRequirement`, `EmergencyRequirement` and `EmergencyCallingService` carry `getNumberType()`, one of the `NumberType` values (`LOCAL`, `NATIONAL`, `MOBILE`, `TOLL_FREE`, `SHARED_COST`, `GLOBAL`; in filters use the wire form, such as `toll_free`). `DidGroup#getIsDemo()` flags demo groups. `NumberType` replaces the `DidGroupType` resource and the `did_group_type` relationship, which API `2026-10-22` no longer has. The `pops` resource is gone too: a Voice IN trunk carries its point of presence in the `pop` attribute (see [Voice In Trunks](#voice-in-trunks)).

### DIDs

```java
// List DIDs
List<Did> dids = client.dids().list().getData();

// Update DID - assign trunk and capacity
Did did = client.dids().find("uuid").getData();
did.setDescription("Updated");
did.setCapacityLimit(20);
did.setVoiceInTrunk(trunk);
client.dids().update(did);
```

#### SMS routing

A DID's inbound messages go to an SMS trunk or to an SMS trunk group. The two are exclusive: `setSmsTrunk` with a trunk also sends `sms_trunk_group` as `null` in the same request, and `setSmsTrunkGroup` with a group sends `sms_trunk` as `null`, as `setVoiceInTrunk` and `setVoiceInTrunkGroup` do. Setting one of them to `null` sends only that one as `null`; set both to unroute the DID.

```java
// sends sms_trunk and sms_trunk_group: null
Did route = new Did().withId("did-uuid");
route.setSmsTrunk(new SmsTrunk().withId("sms-trunk-uuid"));
client.dids().update(route);

// sends sms_trunk_group and sms_trunk: null
Did toGroup = new Did().withId("did-uuid");
toGroup.setSmsTrunkGroup(new SmsTrunkGroup().withId("sms-trunk-group-uuid"));
client.dids().update(toGroup);

// unroute: sends sms_trunk: null and sms_trunk_group: null
Did unroute = new Did().withId("did-uuid");
unroute.setSmsTrunk(null);
unroute.setSmsTrunkGroup(null);
client.dids().update(unroute);

List<Did> routed = client.dids().list(QueryParams.builder()
    .filter("sms_trunk.id", "sms-trunk-uuid")
    .include("sms_trunk", "sms_trunk_group")
    .build()).getData(); // did.getSmsTrunk(), did.getSmsTrunkGroup()
```

An SMS trunk cannot be deleted while DIDs are routed to it, nor an SMS trunk group while DIDs are routed to it or to one of its trunks: unroute those DIDs, or route them elsewhere, first.

### Voice In Trunks

```java
import com.didww.sdk.resource.configuration.*;
import com.didww.sdk.resource.enums.*;

// Create SIP trunk
VoiceInTrunk trunk = new VoiceInTrunk();
trunk.setName("My SIP Trunk");
trunk.setPriority(1);
trunk.setWeight(100);
trunk.setCliFormat(CliFormat.E164);
trunk.setRingingTimeout(30);
trunk.setPop(Pop.FRA); // optional; null lets DIDWW choose

SipConfiguration sip = new SipConfiguration();
sip.setHost("sip.example.com");
sip.setPort(5060);
sip.setCodecIds(Arrays.asList(Codec.PCMU, Codec.PCMA));
sip.setTransportProtocolId(TransportProtocol.UDP);
trunk.setConfiguration(sip);

VoiceInTrunk created = client.voiceInTrunks().create(trunk).getData();

// Update trunk
created.setDescription("Updated");
client.voiceInTrunks().update(created);

// Delete trunk
client.voiceInTrunks().delete(created.getId());
```

`pop` is a plain attribute, one of the `com.didww.sdk.resource.enums.Pop` values (`NYC`, `FRA`, `LAC`, `MIA`, `SG`, `HK`, `AMS`). Leave it `null` to let DIDWW choose; `setPop(null)` on a built or fetched trunk sends `"pop": null`. A value this SDK does not know reads as `null`. It is no longer a relationship and cannot be included.

```java
VoiceInTrunk popUpdate = new VoiceInTrunk().withId("trunk-uuid");
popUpdate.setPop(Pop.AMS);
client.voiceInTrunks().update(popUpdate);
```

#### SIP Registration (2026-04-16)

A Voice In Trunk can also authenticate inbound calls via SIP registration
credentials generated by DIDWW. The SDK auto-cascades the dependent fields
the server requires:

* `setEnabledSipRegistration(true)` clears any previously-set `host` /
  `port` (the server rejects them with 422 otherwise);
* `setHost(<non-null>)` flips `enabledSipRegistration` to `false` and
  forces `useDidInRuri = false` so the server accepts the disable PATCH.

The server generates `incomingAuthUsername` and `incomingAuthPassword` and
surfaces them in the response when SIP registration is enabled (read-only
— the SDK exposes only getters).

```java
SipConfiguration sip = new SipConfiguration();
sip.setEnabledSipRegistration(true);
sip.setUseDidInRuri(true);
sip.setCnamLookup(true);

VoiceInTrunk trunk = new VoiceInTrunk();
trunk.setName("Office (registered)");
trunk.setConfiguration(sip);
SipConfiguration created = (SipConfiguration)
        client.voiceInTrunks().create(trunk).getData().getConfiguration();
// created.getIncomingAuthUsername() — server-generated
// created.getIncomingAuthPassword() — server-generated
```

To disable SIP registration on an existing trunk, just set `host` — the
cascade flips `enabledSipRegistration` and `useDidInRuri` to `false`
automatically:

```java
SipConfiguration disable = new SipConfiguration();
disable.setHost("sip.example.com");

VoiceInTrunk update = new VoiceInTrunk().withId("trunk-uuid");
update.setConfiguration(disable);
client.voiceInTrunks().update(update);
```

### Voice In Trunk Groups

```java
VoiceInTrunkGroup group = new VoiceInTrunkGroup();
group.setName("Primary Group");
group.setCapacityLimit(50);
VoiceInTrunkGroup created = client.voiceInTrunkGroups().create(group).getData();
```

### Voice Out Trunks

> **Note:** Voice Out Trunks require additional account configuration. Contact DIDWW support to enable.
> The `REPLACE_CLI` and `RANDOMIZE_CLI` values of `OnCliMismatchAction` also require account configuration.

Voice Out Trunks use a polymorphic `authenticationMethod` (2026-04-16). Three types are supported:

- **`credentials_and_ip`** -- default method; `username` and `password` are server-generated and returned in the response.
- **`twilio`** -- requires a `twilioAccountSid`.
- **`ip_only`** -- read-only; can only be configured by DIDWW staff upon request. Cannot be set via the API.

```java
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.authenticationmethod.CredentialsAndIpAuthenticationMethod;

// NOTE: 203.0.113.0/24 is RFC 5737 TEST-NET-3 documentation space.
// Replace with the real CIDR of your SIP infrastructure.
CredentialsAndIpAuthenticationMethod auth = new CredentialsAndIpAuthenticationMethod();
auth.setAllowedSipIps(Collections.singletonList("203.0.113.0/24"));

VoiceOutTrunk voTrunk = new VoiceOutTrunk();
voTrunk.setName("My Outbound Trunk");
voTrunk.setAuthenticationMethod(auth);
voTrunk.setDefaultDstAction(DefaultDstAction.ALLOW_ALL);
voTrunk.setOnCliMismatchAction(OnCliMismatchAction.REJECT_CALL);
VoiceOutTrunk created = client.voiceOutTrunks().create(voTrunk).getData();
// created.getAuthenticationMethod().getUsername() -- server-generated
// created.getAuthenticationMethod().getPassword() -- server-generated
```

### SMS Trunks

An SMS trunk delivers inbound messages, accepts outbound ones, or both. Its configuration is one of five classes in `com.didww.sdk.resource.smstrunkconfiguration`:

| Type | Class | Notes |
|------|-------|-------|
| `sms_to_email` | `SmsToEmailConfiguration` | Inbound messages are forwarded as email. `sendTo`, `subject` and `message` are required. |
| `http_in` | `HttpInConfiguration` | Inbound messages are sent as an HTTP request to your `url`. `httpMethod` is one of the `SmsHttpMethod` values (`GET`, `POST`, `PUT`); `POST` and `PUT` also need `bodyType` (an `SmsBodyType` value) and `body`, which `GET` refuses. Query parameters, headers and body may use the placeholders `{SMS_TEXT}`, `{SMS_TEXT_BASE64_ENCODED}`, `{SMS_SRC_ADDR}`, `{SMS_DST_ADDR}` and `{SMS_TIME}`. |
| `http_out` | `HttpOutConfiguration` | You submit outbound messages over HTTP. `authName` and `authPassword` are generated. The submit URL is `trunk.getMetaHost()` (from the trunk's meta). |
| `smpp_esme` | `SmppEsmeConfiguration` | You bind to DIDWW as an SMPP ESME. `serverLocation` is required; `systemId` and `password` are generated. |
| `smpp_smsc` | `SmppSmscConfiguration` | DIDWW binds to your SMSC with your `host`, `systemId`, `password` and `serverLocation`. `txPort` defaults to 2775 and `rxPort` to 2776. |

`SmsTrunkConfiguration#getType()` returns the wire type. The read-only `getAllowIn()` and `getAllowOut()` tell which directions a trunk handles. `serverLocation` is `SmsServerLocation.NYC` or `SmsServerLocation.AMS`; the sandbox accepts `nyc` only. A configuration type this SDK does not know is read as a `GenericSmsTrunkConfiguration` and sent back unchanged; its numbers keep their exact value.

```java
import com.didww.sdk.resource.smstrunkconfiguration.*;
import com.didww.sdk.resource.enums.SmsBodyType;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

HttpInConfiguration httpIn = new HttpInConfiguration();
httpIn.setUrl("https://example.com/sms");
httpIn.setHttpMethod(SmsHttpMethod.POST);
httpIn.setBodyType(SmsBodyType.JSON);
httpIn.setBody("{\"text\":\"{SMS_TEXT}\",\"from\":\"{SMS_SRC_ADDR}\",\"to\":\"{SMS_DST_ADDR}\"}");

SmsTrunk trunk = new SmsTrunk();
trunk.setName("Inbound webhook");
trunk.setConfiguration(httpIn);
SmsTrunk created = client.smsTrunks().create(trunk).getData();

// On update the configuration type must stay the same; attributes left out keep their values.
HttpInConfiguration newUrl = new HttpInConfiguration();
newUrl.setUrl("https://example.com/sms/v2");
SmsTrunk update = new SmsTrunk().withId(created.getId());
update.setConfiguration(newUrl);
client.smsTrunks().update(update);

// Nullable attributes are cleared with a generic configuration of the same type: null is sent as null
ObjectNode attributes = JsonNodeFactory.instance.objectNode();
attributes.put("http_method", "get");
attributes.putNull("body_type");
attributes.putNull("body");
SmsTrunk clear = new SmsTrunk().withId(created.getId());
clear.setConfiguration(new GenericSmsTrunkConfiguration("http_in", attributes));
client.smsTrunks().update(clear);

// List by configuration type, with the group
List<SmsTrunk> trunks = client.smsTrunks().list(QueryParams.builder()
    .filter("configuration.type", "http_in,sms_to_email")
    .include("sms_trunk_group")
    .build()).getData();
```

Changes made inside a configuration object are not tracked: call `setConfiguration(...)` again to send it in the next update. The configuration is sent as a whole, and a `null` field is left out, so on update it keeps its stored value. A typed configuration therefore cannot clear a nullable attribute: the `queryParameters`, `headers`, `bodyType` and `body` of `http_in`, the `callbackUrl` and `allowedIps` of `http_out`, the `systemType` and `allowedIps` of `smpp_esme` and the `systemType` of `smpp_smsc`. Send such a change as a `GenericSmsTrunkConfiguration` of the same type, as above: its `putNull(...)` attributes are sent as `null`.

`setOutgoingDids(...)` replaces the whole `outgoing_dids` list of the trunk: list every DID to keep (include `outgoing_dids` to read the current list), or pass an empty list to clear it. `outgoing_dids` must stay empty while `allow_any_did_as_source` is on, so `setAllowAnyDidAsSource(true)` also clears an `outgoing_dids` list that was loaded or set before, and a trunk read with `outgoing_dids` included sends an empty list. `incoming_dids` and `sender_id_verifications` can be included but are read-only and never sent back.

The generated credentials of `http_out` and `smpp_esme` trunks are read-only and are never sent in a request. The `toString()` of a configuration masks them, the `password` of an `SmppSmscConfiguration`, and the `headers` of an `HttpInConfiguration`, which may carry the credentials of your endpoint. A `GenericSmsTrunkConfiguration` masks every attribute value, since its fields are not known; only the attribute names and explicit nulls are shown. The getters return the real values. To rotate the generated credentials:

```java
SmsTrunkRegenerateCredential regenerate = new SmsTrunkRegenerateCredential();
regenerate.setSmsTrunk(new SmsTrunk().withId(outTrunkId));
client.smsTrunkRegenerateCredentials().create(regenerate);

// The response carries no credentials: read the trunk again
SmsTrunk outTrunk = client.smsTrunks().find(outTrunkId).getData();
SmsTrunkConfiguration configuration = outTrunk.getConfiguration();
if (configuration instanceof HttpOutConfiguration) {
    HttpOutConfiguration http = (HttpOutConfiguration) configuration;
    // http.getAuthName() and http.getAuthPassword() are the new credentials
} else if (configuration instanceof SmppEsmeConfiguration) {
    SmppEsmeConfiguration esme = (SmppEsmeConfiguration) configuration;
    // esme.getSystemId() and esme.getPassword() are the new credentials
}
```

### SMS Trunk Groups

An SMS trunk group spreads a DID's inbound messages over its trunks by their `priority` (the higher value first). `http_out` trunks cannot join a group. Deleting a group also deletes the trunks in it.

```java
SmsTrunk member = new SmsTrunk().withId("sms-trunk-uuid");
SmsTrunkGroup group = new SmsTrunkGroup();
group.setName("Inbound SMS");
group.setSmsTrunks(Collections.singletonList(member));
SmsTrunkGroup created = client.smsTrunkGroups().create(group).getData();
System.out.println(created.getMetaSmsTrunksCount()); // 1
```

`setSmsTrunks(...)` replaces the whole membership of the group: list every trunk to keep. Build the list from the included `sms_trunks`:

```java
SmsTrunkGroup current = client.smsTrunkGroups().find("sms-trunk-group-uuid",
    QueryParams.builder().include("sms_trunks").build()).getData();
List<SmsTrunk> members = new ArrayList<>();
if (current.getSmsTrunks() != null) {
    members.addAll(current.getSmsTrunks());
}
members.add(new SmsTrunk().withId("another-sms-trunk-uuid"));

SmsTrunkGroup update = new SmsTrunkGroup().withId("sms-trunk-group-uuid");
update.setSmsTrunks(members);
client.smsTrunkGroups().update(update);

// An empty list removes every trunk from the group
SmsTrunkGroup empty = new SmsTrunkGroup().withId("sms-trunk-group-uuid");
empty.setSmsTrunks(Collections.emptyList());
client.smsTrunkGroups().update(empty);
```

### Sender ID Verifications

A Sender ID verification covers the sender IDs your outbound messages use. It is created and reviewed outside the API: the API lists and reads verifications, and changes only their `name` and the SMS trunk their messages are routed through. A closed verification cannot be changed. `getStatus()` is one of the `SenderIdVerificationStatus` values and `getSenderType()` one of the `SenderType` values; `isActive()`, `isClosed()` and the other `is*()` methods test the status. `getMetaSetupPrice()` and `getMetaMonthlyPrice()` return the prices of the meta as decimal strings.

```java
List<SenderIdVerification> verifications = client.senderIdVerifications().list(QueryParams.builder()
    .filter("status", "active")
    .include("sms_trunk")
    .build()).getData();

// http_out, smpp_esme or smpp_smsc trunks only
SenderIdVerification route = new SenderIdVerification().withId(verifications.get(0).getId());
route.setSmsTrunk(new SmsTrunk().withId("sms-trunk-uuid"));
client.senderIdVerifications().update(route);

// Outbound messages stop until a trunk is assigned again
SenderIdVerification unroute = new SenderIdVerification().withId(verifications.get(0).getId());
unroute.setSmsTrunk(null);
client.senderIdVerifications().update(unroute);
```

Deleting an SMS trunk leaves the verifications routed through it without a trunk. The other relationships (`countries`, `dids`, `order`, `identity`, `address`) can be included but are read-only and never sent back.

### Orders

```java
import com.didww.sdk.resource.orderitem.*;

// Order by SKU
Order order = new Order();
DidOrderItem item = new DidOrderItem();
item.setSkuId("sku-uuid");
item.setQty(2);
order.setItems(Arrays.asList(item));
Order created = client.orders().create(order).getData();

// Order available DID
AvailableDidOrderItem availableItem = new AvailableDidOrderItem();
availableItem.setAvailableDidId("available-did-uuid");
availableItem.setSkuId("sku-uuid");

// Order reserved DID
ReservationDidOrderItem reservationItem = new ReservationDidOrderItem();
reservationItem.setDidReservationId("reservation-uuid");
reservationItem.setSkuId("sku-uuid");

// Order capacity
CapacityOrderItem capacityItem = new CapacityOrderItem();
capacityItem.setCapacityPoolId("pool-uuid");
capacityItem.setQty(1);
```

### DID Reservations

```java
DidReservation reservation = new DidReservation();
reservation.setDescription("Reserved for client");
reservation.setAvailableDid(availableDid);
DidReservation created = client.didReservations().create(reservation).getData();

// Delete reservation
client.didReservations().delete(created.getId());
```

### Shared Capacity Groups

```java
SharedCapacityGroup scg = new SharedCapacityGroup();
scg.setName("Shared Group");
scg.setSharedChannelsCount(20);
scg.setCapacityPool(capacityPool);
SharedCapacityGroup created = client.sharedCapacityGroups().create(scg).getData();
```

### Identities

```java
import com.didww.sdk.resource.enums.IdentityType;

Identity identity = new Identity();
identity.setFirstName("John");
identity.setLastName("Doe");
identity.setPhoneNumber("12125551234");
identity.setIdentityType(IdentityType.PERSONAL);
identity.setCountry(country);
Identity created = client.identities().create(identity).getData();
```

`setCompanyWebsite(...)` belongs to business identities; a personal identity refuses it, and it is left out of a create request while `null`.

### Addresses

```java
Address address = new Address();
address.setCityName("New York");
address.setPostalCode("10001");
address.setAddress("123 Main St");
address.setIdentity(identity);
address.setCountry(country);
Address created = client.addresses().create(address).getData();
```

### Address Verifications

```java
AddressVerification verification = new AddressVerification();
verification.setCallbackUrl("http://example.com/callback");
verification.setCallbackMethod(CallbackMethod.GET);
AddressVerification created = client.addressVerifications().create(verification).getData();
```

### Exports

```java
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import com.didww.sdk.resource.enums.ExportType;

// Dates are "YYYY-MM-DD HH:MM:SS" in UTC: from is inclusive, to is exclusive and not in the future
LocalDate today = LocalDate.now(ZoneOffset.UTC);
String from = today.minusDays(1) + " 00:00:00";
String to = today + " 00:00:00";

Map<String, Object> cdrFilters = new HashMap<>();
cdrFilters.put("from", from);
cdrFilters.put("to", to);
Export export = new Export();
export.setExportType(ExportType.CDR_IN);
export.setFilters(cdrFilters);
Export created = client.exports().create(export).getData();

// SMS export
Map<String, Object> smsFilters = new HashMap<>();
smsFilters.put("from", from);
smsFilters.put("to", to);
smsFilters.put("service_type", "a2p");
smsFilters.put("sms_trunk.id", Arrays.asList("sms-trunk-uuid"));
Export smsOut = new Export();
smsOut.setExportType(ExportType.SMS_OUT);
smsOut.setFilters(smsFilters);
Export createdSms = client.exports().create(smsOut).getData();

// The export is pending until its file is generated; read it again later
Export completed = client.exports().find(createdSms.getId()).getData();
if (completed.isCompleted()) {
    client.downloadExport(completed.getUrl(), Path.of("/tmp/export.csv.gz"));           // gzipped CSV, as served
    client.downloadAndDecompressExport(completed.getUrl(), Path.of("/tmp/export.csv")); // plain CSV

    try (OutputStream out = Files.newOutputStream(Path.of("/tmp/copy.csv.gz"))) {
        client.downloadExport(completed.getUrl(), out);                                // any OutputStream
    }
}
```

The export downloads send the token only to the origin (scheme, host and port) of the JSON:API base URL or of the environment, so export URLs, which the API builds on the environment's host, are still downloaded when `baseUrl(...)` points elsewhere; that request goes directly to the environment's host. A URL on any other origin throws a `DidwwClientException` and nothing is sent. Only a 200 answer is written, as it arrives, without holding the file in memory; redirects are not followed, and any other status, a 3xx or a 204 included, throws a `DidwwApiException` (see [Error Handling](#error-handling)). The `Path` overloads write to a temporary file next to the destination, which replaces it only once the whole file has arrived (or has been decompressed); the `OutputStream` overloads write into your stream, which is not closed and may hold part of the file after a failure.

`from` and `to` are required for every export type (`cdr_in`, `cdr_out`, `sms_in`, `sms_out`). The other filter keys depend on the type:

- `cdr_in`: `did_number`, `source_number`, `source_number_out`, `source_country_iso`;
- `cdr_out`: `voice_out_trunk.id`, `source`, `cli`, `destination`;
- `sms_in` and `sms_out`: `source_address_eq`, `source_address_cont`, `destination_address_eq`, `destination_address_cont`, `status`, `sms_id`, `sms_trunk.id` (a string or a list of strings);
- `sms_in` only: `trunk_type`, one or more of the `ExportTrunkType` values in their wire form (`SMPP SMSC`, `SMPP ESME`, `Email`, `HTTP IN`);
- `sms_out` only: `service_type` (`a2p` or `p2p`) and `sender_id_verification.id` (a string or a list of strings).

The Javadoc of `Export#setFilters` lists the keys of every type.

### Emergency Services (2026-04-16)

```java
// List emergency requirements
List<EmergencyRequirement> emergReqs = client.emergencyRequirements().list().getData();

// Create emergency verification
EmergencyVerification emergVerification = new EmergencyVerification();
emergVerification.setCallbackUrl("https://example.com/callback");
emergVerification.setCallbackMethod("post");
emergVerification.setAddress(address);
emergVerification.setDids(Arrays.asList(did));
EmergencyVerification created = client.emergencyVerifications().create(emergVerification).getData();

// List emergency calling services
List<EmergencyCallingService> emergServices = client.emergencyCallingServices().list().getData();
```

### DID History (2026-04-16)

```java
// List DID history
List<DidHistory> history = client.didHistory().list().getData();
for (DidHistory entry : history) {
    System.out.println(entry.getAction() + " " + entry.getCreatedAt());
}
```

## Prices

Prices are served by three services outside the JSON:API (hosts in [Environments](#environments)). Each price list has a price file and a lookup:

| Accessor | Service | Price file | Lookup | Lookup rows |
|----------|---------|------------|--------|-------------|
| `client.voiceInPrices()` | Voice IN | `prices.csv.gz` | `lookup/prices` | `VoiceInPrice` |
| `client.voiceOutPrices()` | Voice OUT | `prices.csv.gz` | `lookup/prices` | `VoiceOutPrice` |
| `client.smsOutA2pPrices()` | SMS | `sms_out/a2p/prices.csv.gz` | `sms_out/a2p/lookup/prices` | `SmsOutPrice` |
| `client.smsOutP2pPrices()` | SMS | `sms_out/p2p/prices.csv.gz` | `sms_out/p2p/lookup/prices` | `SmsOutPrice` |
| `client.smsOutOtpPrices()` | SMS | `sms_out/otp/prices.csv.gz` | `sms_out/otp/lookup/prices` | `SmsOutPrice` |
| `client.smsInPrices()` | SMS | `sms_in/prices.csv.gz` | `sms_in/lookup/prices` | `SmsInPrice` |

Each accessor returns a `PricesService<T>`; it, the row classes and `PriceFileReader` are in package `com.didww.sdk.prices`. The account needs a price plan of that service: without one, every call throws `PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED`.

### Price lookups

```java
import com.didww.sdk.prices.*;

PriceLookup<VoiceInPrice> result = client.voiceInPrices().lookup(Map.of(
    "number", "18005550123",
    "src_country_iso", "US"));
System.out.println(result.getTotalRecords());
for (VoiceInPrice price : result.getRows()) {
    if (price.getRate() != null) {
        System.out.println(price.getDestination() + " " + price.getSourceType() + ": " + price.getRate() + " USD/min");
    }
}

PriceLookup<VoiceOutPrice> voiceOut = client.voiceOutPrices().lookup(Map.of("number", "493012345678"));
System.out.println(voiceOut.getRows().get(0).getCategory()); // VoiceOutPriceCategory
PriceLookup<SmsOutPrice> a2p = client.smsOutA2pPrices().lookup(Map.of("dst_number", "4915123456789"));
PriceLookup<SmsInPrice> smsIn = client.smsInPrices().lookup(Map.of("dst_country_iso", "US"));
```

| Service | Filters |
|---------|---------|
| Voice IN | `number` (required), `src_country_iso` |
| Voice OUT | `number` (required); no other filter |
| SMS OUT and SMS IN | `dst_number`, `dst_country_iso`; at least one |

A missing or unknown filter throws an `IllegalArgumentException` before any request; `null` and blank values are dropped (`Map.of` refuses `null`, so use a `HashMap` for optional values). The server checks the value formats and answers a bad value with 400 (`PricesErrorKind.BAD_REQUEST`). A lookup is not retried, except once with a new token after a 401.

`lookup` returns a `PriceLookup<T>` with `getRows()` and `getTotalRecords()`. It always holds at least one row: no match throws `PricesErrorKind.PRICE_NOT_FOUND`, never an empty result. In the rows, prices and fees are `BigDecimal` in USD, VAT excluded, with the scale of the answer kept (compare them with `compareTo`, not `equals`); they are `null` when the row rejects the calls or messages (see `getRejectCalls()` and `getRejectSms()`). Intervals and increments are `Integer`, `SmsOutPrice#getMcc()` and `getMnc()` are `Integer`, `VoiceOutPrice#getEffectiveAt()` is an `OffsetDateTime` and `VoiceOutPrice#getCategory()` is one of the `VoiceOutPriceCategory` values (`INTERNATIONAL`, `ORIGIN_BASED`, `LOCAL`), or `null` for a category this SDK does not know.

The ID of an SMS OUT or SMS IN lookup row is derived from its content: it changes when the row or its price changes, so do not use it to track a destination over time. A Voice OUT ID stays the same when a re-import changes only the price. A Voice IN ID names the answering row of the plan.

### Price files

The files are gzipped CSV with a header row. The SDK writes them exactly as served and never decompresses them.

```java
import com.didww.sdk.prices.*;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

PricesService<VoiceInPrice> prices = client.voiceInPrices();
Path path = Path.of("/var/data/voice_in_prices.csv.gz");

// Download unless the copy at the path is current
PriceFile file = prices.fetch(path);
System.out.println(file.isNotModified()
    ? "answered 304, the file was kept"
    : "downloaded " + file.getBytesWritten() + " bytes, last modified " + file.getLastModified());

// Read the rows, keyed by the header line
try (Stream<Map<String, String>> rows = PriceFileReader.rows(path)) {
    rows.limit(5).forEach(row -> System.out.println(row.get("Destination") + ": " + row.get("Rate (USD, excl. VAT)")));
}

// Lower-level calls
PriceFile status = prices.head(file.getLastModified()); // sent as If-Modified-Since; null sends none
// status.isNotModified(), status.getContentLength() (null when unknown),
// status.getFileName() (from Content-Disposition, e.g. "voice_in_prices.csv.gz")

file = prices.downloadToFile(path);
try (OutputStream out = Files.newOutputStream(Path.of("/tmp/voice_in_prices.csv.gz"))) {
    file = prices.download(out); // any OutputStream; download(out, ifModifiedSince) sends If-Modified-Since
}
```

- `fetch(path)` sends the modification time of the file at `path` as `If-Modified-Since`, when the file is there; an unchanged price file is answered 304 and the file is kept. A file written by `downloadToFile` or `fetch` gets the `Last-Modified` of the response as its modification time, so the date matches the copy on disk. `fetch` trusts the modification time of any file at `path`: a file copied or changed by other means can be answered 304 and kept, so replace such a file with `downloadToFile`.
- `downloadToFile` and `fetch` stream the body into a temporary file next to `path`, unique to the call, which gets the permissions of a new file (the umask on POSIX systems), is flushed to the disk and replaces `path` atomically only on a complete 200. A 304 or an error leaves an existing file untouched, the temporary file is always removed, and concurrent downloads to one path do not mix their bytes. A directory that does not exist throws an `UncheckedIOException` without a request. **On Windows the replacement fails while `path` is open**, for example by a `PriceFileReader` stream that has not been closed.
- `download(destination)` and `download(destination, ifModifiedSince)` write the body into `destination` only on a 200, as it arrives, so after a transfer error `destination` may hold part of it. The stream is not closed. An `IOException` of `destination` is thrown as an `UncheckedIOException` with it as the cause.
- A body shorter than its `Content-Length` throws `PricesErrorKind.CONNECTION_ERROR`; it never replaces a file.
- `head` and the downloads return a `PriceFile`: `getStatusCode()`, `getLastModified()` (`null` when absent), `getContentLength()`, `getFileName()` (as sent: check it before using it as a path), `getBytesWritten()` and `isNotModified()`.
- `PriceFileReader.rows(path)` and `PriceFileReader.rows(inputStream)` gunzip a downloaded file and return a lazy `Stream` of rows, each a `Map<String, String>` keyed by the header line, as served (for example `Rate (USD, excl. VAT)`), in header order. A UTF-8 byte order mark and empty lines are skipped. Input that is not gzip (`ZipException`), gzip data cut short, an empty input included (`EOFException`), or a row whose field count differs from the header (an `IOException` whose message starts with `Price file:`) throws an `UncheckedIOException` with that cause while the stream is consumed, so rows before the error have already been passed on. Data after the end of the gzip data is ignored unless it starts another gzip member, so a later member that is cut short or corrupt raises the error too. Close the stream: `rows(path)` opens the file, `rows(inputStream)` leaves your stream open.
- Only `Last-Modified` and `If-Modified-Since` are used, in whole seconds. The service sends no `ETag`, and the SDK never sends `If-None-Match`, which would make the service ignore `If-Modified-Since`.
- A 503 on a price file is retried up to 2 times when its `Retry-After` is 60 seconds or less, after waiting that long (at least 1 s) plus a small random delay so that clients do not retry in step. Otherwise it throws `PricesErrorKind.SERVICE_UNAVAILABLE` with `getRetryAfter()`. Lookups and other statuses, 429 included, are not retried.
- A 404 `price_not_found` throws `PRICE_NOT_FOUND`; any other 404, `price_plan_not_assigned` included, and any 404 on `head`, which has no body, throws `PRICE_PLAN_NOT_ASSIGNED`. `DidwwPricesException#getCode()` holds the code of the answer, or `price_plan_not_assigned` for a 404 without one.
- Redirects are not followed. A status without its own kind in [Error Handling](#error-handling), such as a 3xx, a 202 or a 204, throws `PricesErrorKind.UNEXPECTED_STATUS`. An error is never turned into an empty file or an empty result.

## Filtering, Sorting, and Pagination

> See [`FILTERS.md`](FILTERS.md) for the list of `filter[KEY]` keys accepted by every list endpoint at API version `2026-10-22`.

```java
import com.didww.sdk.http.QueryParams;

QueryParams params = QueryParams.builder()
    .filter("country.id", "uuid")
    .filter("name", "Arizona")
    .include("country")
    .sort("name")
    .page(1, 25)
    .build();

List<Region> regions = client.regions().list(params).getData();
```

## Dirty PATCH Serialization

The SDK tracks which fields have been modified and sends only those fields in PATCH requests. This avoids overwriting server-side values that your code hasn't touched.

### Updating a fetched resource

When you fetch a resource and modify it, only the changed fields are sent:

```java
Did did = client.dids().find("uuid").getData();
did.setDescription("Updated description");
// PATCH payload includes only "description", not all attributes
client.dids().update(did);
```

A writable relationship loaded through an include is sent back on update as it was read. For a to-many relationship, such as the `outgoing_dids` of an SMS trunk or the `sms_trunks` of an SMS trunk group, that replaces the stored list with the list you read, so when you change other fields of such a resource, build the update from `withId(id)` (below) instead.

### Building a resource for update

Use `withId(id)` to create a lightweight resource for PATCH without fetching first:

```java
VoiceInTrunk trunk = new VoiceInTrunk().withId("trunk-uuid");
trunk.setName("New name");
// PATCH payload includes only "name"
client.voiceInTrunks().update(trunk);
```

### Clearing a field with explicit null

Calling a setter with `null` marks the field as dirty and includes an explicit `null` in the payload, which clears the server-side value:

```java
Did did = new Did().withId("uuid");
did.setDescription(null);
// PATCH payload includes "description": null
client.dids().update(did);
```

### Clearing a relationship

Calling a relationship setter with `null` sends `"data": null` for to-one or `"data": []` for to-many relationships:

```java
Did did = new Did().withId("uuid");
did.setVoiceInTrunk(null);
// PATCH payload includes: "relationships": { "voice_in_trunk": { "data": null } }
client.dids().update(did);
```

### Included resources

Dirty tracking is automatically enabled on included (sideloaded) resources, so you can fetch with includes and update a related resource directly:

```java
Did did = client.dids().find("uuid", QueryParams.builder().include("voice_in_trunk").build()).getData();
VoiceInTrunk trunk = did.getVoiceInTrunk();
trunk.setDescription("Updated via include");
client.voiceInTrunks().update(trunk);
```

## File Encryption

The SDK provides an `Encrypt` utility for encrypting files before upload, using RSA-OAEP + AES-256-CBC (matching DIDWW's encryption requirements).

```java
import com.didww.sdk.Encrypt;

// Instance-based: fetches public keys from the API
Encrypt enc = new Encrypt(client);
String fingerprint = enc.getFingerprint();
byte[] encrypted = enc.encrypt(fileBytes);

// Static: provide your own public keys
byte[] encryptedWithKeys = Encrypt.encryptWithKeys(fileBytes, publicKeyPems);
String fingerprintOfKeys = Encrypt.calculateFingerprint(publicKeyPems);
```

Upload encrypted file:

```java
String encryptedFileId = client.uploadEncryptedFile(
    encrypted,          // encrypted bytes
    "document.pdf.enc", // uploaded file name
    fingerprint,        // Encrypt#getFingerprint()
    "document.pdf"      // description
);
```

The upload goes through the configured HTTP client (proxy, TLS, interceptors) with a bearer token.

## Webhook Signature Validation

Validate incoming webhook callbacks from DIDWW using HMAC-SHA1 signature verification.

Initialize the validator with the callback secret that is enabled in the DIDWW User Panel (**APIs → DIDWW API 3 → Callback Secrets**). DIDWW signs every callback with it and sends callbacks only while a callback secret is enabled.

```java
import com.didww.sdk.callback.RequestValidator;

RequestValidator validator = new RequestValidator("YOUR_CALLBACK_SECRET");

// In your webhook handler:
boolean valid = validator.validate(
    requestUrl,    // full original URL
    payloadMap,    // Map<String, String> of payload key-value pairs
    signature      // value of X-DIDWW-Signature header
);
```

The signature header name is available as `RequestValidator.HEADER_NAME`.

## Error Handling

The SDK's own exceptions derive from `DidwwException`, an unchecked exception in package `com.didww.sdk.exception`. Their messages and causes never contain the API key or a token, and the `toString()` of the credentials, tokens, token stores and SMS trunk configurations never shows them either.

```java
import com.didww.sdk.exception.*;

try {
    client.voiceInTrunks().find("nonexistent");
} catch (DidwwApiException e) {
    System.out.println("HTTP Status: " + e.getHttpStatus());
    for (DidwwApiException.ApiError error : e.getErrors()) {
        System.out.println("Error: " + error.getDetail());
    }
} catch (DidwwClientException e) {
    System.out.println("Client error: " + e.getMessage());
}
```

| Exception | Thrown for |
|-----------|------------|
| `DidwwApiException` | An error status of the JSON:API repositories and of the export downloads, with `getHttpStatus()` and `getErrors()`. |
| `DidwwClientException` | A failure on the client side: a JSON:API call that fails in transport (the `IOException` is the cause) or whose resource cannot be serialized, an error status of `uploadEncryptedFile`, an upload or price lookup answer that cannot be parsed, a refused export URL, an export download that breaks off or cannot be decompressed. |
| `DidwwAuthException` | The token service, from any call that needs a new token. |
| `DidwwPricesException` | The prices services. |

`DidwwApiException` also reports the authentication failures of the API: a 401 still answered after the token was replaced once (`getHttpStatus()` 401), and a 403 such as `insufficient_scope` for a write with a `READ` token, which is never retried. `uploadEncryptedFile` reports any error status, including a 401 after the retry and a 403, as a `DidwwClientException` whose message holds the HTTP status. The export downloads do not follow redirects: any status other than 200, such as a 3xx or a 204, throws a `DidwwApiException`.

The token service and prices errors carry a kind, which `getKind()` tells apart:

```java
try {
    client.smsOutA2pPrices().lookup(Map.of("dst_number", "4915123456789"));
} catch (DidwwAuthException e) {
    if (e.getKind() == AuthErrorKind.RATE_LIMITED) {
        // Wait getRetryAfter() and try again; repeat while the kind is RATE_LIMITED.
        System.out.println("Token service rate limit, retry after " + e.getRetryAfter());
    } else if (e.getKind() == AuthErrorKind.INVALID_CLIENT) {
        System.out.println("API key refused: " + e.getMessage());
    } else {
        throw e;
    }
} catch (DidwwPricesException e) {
    switch (e.getKind()) {
        case PRICE_NOT_FOUND:
            System.out.println("No price for this number");
            break;
        case TOO_MANY_REQUESTS:
            System.out.println("Prices rate limit, retry after " + e.getRetryAfter());
            break;
        default:
            throw e;
    }
} catch (DidwwException e) {
    System.out.println("Error: " + e.getMessage());
}
```

**Token service** (`DidwwAuthException`, with `getKind()`, `getHttpStatus()`, `getError()`, `getErrorDescription()` and `getRetryAfter()`):

| `AuthErrorKind` | Thrown when |
|-----------------|-------------|
| `INVALID_CLIENT` | 401: the token service refused the API key. Not retried. |
| `INVALID_REQUEST` | 400, e.g. `invalid_request` or `invalid_scope`. |
| `RATE_LIMITED` | 429. `getRetryAfter()` is the server's wait. Further mints fail without a request until that time plus a small random delay has passed; such a refusal reports the time still left in `getRetryAfter()`, so retry in a loop rather than once. |
| `SERVER_ERROR` | 5xx on the last of the 4 attempts. |
| `CONNECTION_ERROR` | Connection failure or timeout on the last attempt, a TLS failure (not retried), or an interrupted thread (its interrupt flag stays set). `getHttpStatus()` is `null`. |
| `INVALID_GRANT` | The token is empty, not a valid header value, or does not cover the requested audience. |
| `UNEXPECTED_STATUS` | Any other status, such as a redirect. |

**Prices** (`DidwwPricesException`, with `getKind()`, `getHttpStatus()`, `getCode()`, `getErrors()` and `getRetryAfter()`):

| `PricesErrorKind` | Status |
|-------------------|--------|
| `BAD_REQUEST` | 400, e.g. a filter value of the wrong format |
| `NOT_AUTHORIZED` | 401 after the token was replaced once |
| `ACCESS_DENIED` | 403 |
| `PRICE_PLAN_NOT_ASSIGNED` | 404 `price_plan_not_assigned`, and any other 404 except `price_not_found`, including any 404 on `head`, which has no body |
| `PRICE_NOT_FOUND` | 404 `price_not_found`: no price matches the lookup |
| `NOT_ACCEPTABLE` | 406 |
| `TOO_MANY_REQUESTS` | 429, with `getRetryAfter()` when the service sends it in seconds. Not retried. |
| `SERVICE_UNAVAILABLE` | 503, with `getRetryAfter()`. Retried on price files only, see [Price files](#price-files). |
| `UNEXPECTED_STATUS` | Any other status, such as a redirect, a 202, a 204 or a 304 to a lookup |
| `CONNECTION_ERROR` | Connection failure, timeout, TLS failure, a response body that breaks off or is shorter than its `Content-Length`, or an interrupted thread (its interrupt flag stays set). Not retried. |

The prices services can also throw a `DidwwAuthException`, `lookup` an `IllegalArgumentException` for invalid filters and a `DidwwClientException` when a 200 answer is not a JSON:API document it can parse or holds no prices. A destination `OutputStream` or file that cannot be written, and the file system errors of `downloadToFile` and `fetch` (temporary file, times, move), throw an `UncheckedIOException` with the `IOException` as the cause. Outside `DidwwException`, the SDK also throws `IllegalArgumentException` from `build()` and `NullPointerException` for a required argument that is `null`.

## Trunk Configuration Types

| Type | Class |
|------|-------|
| SIP | `SipConfiguration` |
| PSTN | `PstnConfiguration` |

For the SMS trunk configuration types, see [SMS Trunks](#sms-trunks).

## Order Item Types

| Type | Class |
|------|-------|
| DID | `DidOrderItem` |
| Available DID | `AvailableDidOrderItem` |
| Reservation DID | `ReservationDidOrderItem` |
| Capacity | `CapacityOrderItem` |
| Generic | `GenericOrderItem` |

## All Supported Resources

| Resource | Repository | Operations |
|----------|-----------|------------|
| Country | `client.countries()` | list, find |
| Region | `client.regions()` | list, find |
| City | `client.cities()` | list, find |
| Area | `client.areas()` | list, find |
| NanpaPrefix | `client.nanpaPrefixes()` | list, find |
| DidGroup | `client.didGroups()` | list, find |
| AvailableDid | `client.availableDids()` | list, find |
| ProofType | `client.proofTypes()` | list, find |
| PublicKey | `client.publicKeys()` | list, find |
| AddressRequirement | `client.addressRequirements()` | list, find |
| SupportingDocumentTemplate | `client.supportingDocumentTemplates()` | list, find |
| Balance | `client.balance()` | find |
| Did | `client.dids()` | list, find, update, delete |
| VoiceInTrunk | `client.voiceInTrunks()` | list, find, create, update, delete |
| VoiceInTrunkGroup | `client.voiceInTrunkGroups()` | list, find, create, update, delete |
| VoiceOutTrunk | `client.voiceOutTrunks()` | list, find, create, update, delete |
| VoiceOutTrunkRegenerateCredential | `client.voiceOutTrunkRegenerateCredentials()` | create |
| SmsTrunk | `client.smsTrunks()` | list, find, create, update, delete |
| SmsTrunkGroup | `client.smsTrunkGroups()` | list, find, create, update, delete |
| SmsTrunkRegenerateCredential | `client.smsTrunkRegenerateCredentials()` | create |
| SenderIdVerification | `client.senderIdVerifications()` | list, find, update |
| DidReservation | `client.didReservations()` | list, find, create, delete |
| CapacityPool | `client.capacityPools()` | list, find, update |
| SharedCapacityGroup | `client.sharedCapacityGroups()` | list, find, create, update, delete |
| Order | `client.orders()` | list, find, create, delete |
| Export | `client.exports()` | list, find, create, update |
| Address | `client.addresses()` | list, find, create, update, delete |
| AddressVerification | `client.addressVerifications()` | list, find, create, update |
| Identity | `client.identities()` | list, find, create, update, delete |
| EncryptedFile | `client.encryptedFiles()` | list, find, delete |
| PermanentSupportingDocument | `client.permanentSupportingDocuments()` | create |
| Proof | `client.proofs()` | create |
| AddressRequirementValidation | `client.addressRequirementValidations()` | create |
| DidHistory | `client.didHistory()` | list |
| EmergencyRequirement | `client.emergencyRequirements()` | list, find |
| EmergencyRequirementValidation | `client.emergencyRequirementValidations()` | create |
| EmergencyCallingService | `client.emergencyCallingServices()` | list, find, create, update, delete |
| EmergencyVerification | `client.emergencyVerifications()` | list, find, create, update |
| StockKeepingUnit | include on `didGroups` | — |
| QtyBasedPricing | include on `capacityPools` | — |
| VoiceInPrice | `client.voiceInPrices()` | lookup, head, download, downloadToFile, fetch |
| VoiceOutPrice | `client.voiceOutPrices()` | lookup, head, download, downloadToFile, fetch |
| SmsOutPrice | `client.smsOutA2pPrices()`, `client.smsOutP2pPrices()`, `client.smsOutOtpPrices()` | lookup, head, download, downloadToFile, fetch |
| SmsInPrice | `client.smsInPrices()` | lookup, head, download, downloadToFile, fetch |

> **Note:** `StockKeepingUnit` and `QtyBasedPricing` have no standalone API endpoints.
> Access them via `include` on `didGroups` and `capacityPools` respectively.
> The price rows are served by the prices services outside the JSON:API, see [Prices](#prices).

## Date and Datetime Fields

The SDK distinguishes between date-only and datetime fields:

- **Datetime fields** — deserialized as `OffsetDateTime`:
  - `getCreatedAt()` — present on most resources
  - `getUpdatedAt()` — `SmsTrunk`, `SmsTrunkGroup`
  - `getExpiresAt()` — `Did`, `DidReservation`, `Proof`, `EncryptedFile` (nullable)
  - `getActivatedAt()` — `EmergencyCallingService`, `SenderIdVerification` (nullable)
  - `getCanceledAt()` — `EmergencyCallingService` (nullable)
  - `getClosedAt()` — `SenderIdVerification` (nullable)
  - `getEffectiveAt()` — `VoiceOutPrice` price lookup rows (nullable)
- **Date-only fields** — deserialized as `LocalDate`:
  - `Identity.getBirthDate()`
- **Date-only fields kept as strings** — remain as `String`:
  - `CapacityPool.getRenewDate()`, `EmergencyCallingService.getRenewDate()` — `"YYYY-MM-DD"` (nullable)
  - `SenderIdVerification.getRenewDate()` — `"YYYY-MM-DD"`, `null` once the verification is closed
  - `DidOrderItem.getBilledFrom()`, `DidOrderItem.getBilledTo()`
- **String fields** (not numeric):
  - `EmergencyRequirement.getEstimateSetupTime()` — e.g. `"7-14 days"`, `"1"`
  - `EmergencyRequirement.getRequirementRestrictionMessage()` — nullable

**Important changes from previous API versions:**
- `getExpireAt()` renamed to `getExpiresAt()` on `DidReservation` and `EncryptedFile`
- `getRenewDate()` returns a date-only string, NOT an `OffsetDateTime`
- `getEstimateSetupTime()` returns a `String`, NOT an integer

```java
Did did = client.dids().find("uuid").getData();
System.out.println(did.getCreatedAt());   // 2024-01-15T10:00:00Z  (OffsetDateTime)
System.out.println(did.getExpiresAt());   // null or 2025-01-15T10:00:00Z

Identity identity = client.identities().find("uuid").getData();
System.out.println(identity.getBirthDate());  // 1990-05-20  (LocalDate)
```

## Enums

The SDK provides enum classes in `com.didww.sdk.resource.enums`:

`CallbackMethod`, `IdentityType`, `OrderStatus`, `ExportType`, `ExportStatus`, `CliFormat`,
`OnCliMismatchAction`\*, `MediaEncryptionMode`, `DefaultDstAction`, `VoiceOutTrunkStatus`,
`EmergencyCallingServiceStatus`, `EmergencyVerificationStatus`, `DiversionRelayPolicy`,
`TransportProtocol`, `Codec`, `RxDtmfFormat`, `TxDtmfFormat`, `SstRefreshMethod`,
`ReroutingDisconnectCode`, `Feature`, `AreaLevel`, `AddressVerificationStatus`, `StirShakenMode`,
`NumberType`, `Pop`, `SenderIdVerificationStatus`, `SenderType`, `SmsHttpMethod`, `SmsBodyType`,
`SmsServerLocation`, `ExportTrunkType`

\* `REPLACE_CLI` and `RANDOMIZE_CLI` require account configuration.

`ExportType` includes `SMS_IN` and `SMS_OUT`. `VoiceOutPriceCategory` is in `com.didww.sdk.prices`, and `DidwwScope` in `com.didww.sdk.auth`. A value the API sends that this SDK does not know reads as `null`.

The wire value of a string enum is declared with Jackson's `@JsonProperty` only: `name()` is not the wire form (`NumberType.TOLL_FREE.name()` is `TOLL_FREE`, the API value is `toll_free`). Write the wire value in filters, such as `filter("number_type", "toll_free")` or `filter("status", "changes_required")`.

## Migrating from 4.x

5.0.0 targets API `2026-10-22` only. To stay on API `2026-04-16`, keep 4.x (for example `4.1.4`, branch [`release-4.1`](https://github.com/didww/didww-api-3-java-sdk/tree/release-4.1)).

Most code keeps working: `DidwwClient.builder().credentials(new DidwwCredentials(apiKey, environment)).build()` is unchanged, and the SDK mints the access tokens itself. The breaking changes:

- **Authentication.** Every request carries a bearer token minted from the API key (see [Authentication](#authentication)). The `Api-Key` header is sent only to the token service, so outgoing traffic must reach `auth.didww.com` (`auth-sandbox.didww.com` in the sandbox). The token service can throw a `DidwwAuthException` from any call. The public `com.didww.sdk.http.ApiKeyInterceptor` is removed.
- **API version.** `X-DIDWW-API-Version` is no longer sent: the version is requested in the token. The default is `2026-10-22`, and an earlier version makes `build()` throw an `IllegalArgumentException`.
- **Validation.** `build()` throws an `IllegalArgumentException` for an invalid API key, API version or service URL (see [Configuration](#configuration)), which 4.x accepted.
- **Removed resources.** `client.pops()`, `client.didGroupTypes()`, `com.didww.sdk.resource.Pop` and `com.didww.sdk.resource.DidGroupType` are removed, with the `did_group_type` relationship and include. `DidGroup`, `AddressRequirement`, `EmergencyRequirement` and `EmergencyCallingService` have `getNumberType()` instead of `getDidGroupType()`; filter with `number_type` instead of `did_group_type.id`, and with `did_group.number_type` on available DIDs.
- **Point of presence.** `VoiceInTrunk#setPop` takes the enum `com.didww.sdk.resource.enums.Pop` (for example `Pop.FRA`), and `getPop()` returns it; `pop` is an attribute, no longer a relationship, and cannot be included.
- **Numbers in untyped maps.** Decimal numbers that the SDK reads into a `Map<String, Object>` or a Jackson node, such as `ApiResponse#getMeta()`, `GenericAuthenticationMethod#getAttributes()` (an unknown Voice OUT trunk authentication method), the `source` and `meta` of the errors of a `DidwwApiException`, `Export#getFilters()` and the maps of `HttpInConfiguration`, are now `BigDecimal` with their exact value, not `Double`. Integers are unchanged.
- **HTTP defaults.** 4.x used OkHttp's defaults: 10 s timeouts and redirects followed. 5.x opens TCP connections within 10 s, waits up to 60 s for a proxy tunnel, the TLS handshake, the response headers and between two reads or writes, follows no redirects, sends and stores no cookies and ignores the `Authenticator` and cache of your builder (see [HTTP client](#http-client-proxy-tls-timeouts)). The timeouts of your own `httpClientBuilder` are kept as before.
- **Export download and file upload.** `uploadEncryptedFile` now goes through your configured HTTP client (proxy, TLS, interceptors) with the bearer token; 4.x used a separate default client. `downloadExport` and `downloadAndDecompressExport` download only from the API origin or the environment origin (a `DidwwClientException` otherwise, without a request), succeed only on a 200 (any other status, a 2xx included, now throws a `DidwwApiException` with the status and the parsed errors, where 4.x threw a `DidwwClientException`), follow no redirects, stream the file instead of reading it into memory, and replace the destination file only once it is complete; a destination file that cannot be written (missing directory, permissions, full disk) now throws an `UncheckedIOException` with the `IOException` as its cause, not a `DidwwClientException`; new overloads write to an `OutputStream`.
- **Exceptions.** `DidwwApiException` and `DidwwClientException` now extend the abstract `DidwwException` (still a `RuntimeException`), the base of the new `DidwwAuthException` and `DidwwPricesException`.

New in 5.0.0: [SMS trunks](#sms-trunks), [SMS trunk groups](#sms-trunk-groups), [Sender ID verifications](#sender-id-verifications), [DID SMS routing](#sms-routing), `sms_in` and `sms_out` [exports](#exports), [prices](#prices), `DidGroup#getIsDemo()`, `Identity` company website, and the `apiVersion`, `scope`, `tokenStore`, `logger` and service URL methods of the builder.

## Development

```bash
# Run the tests
./gradlew test

# Build the Javadoc
./gradlew javadoc

# Compile the examples
./gradlew compileExamplesJava

# Run an example (see examples/README.md)
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.BalanceExample
```

## Releasing a New Version

1. Update `version` in `build.gradle.kts` to the new version (e.g. `"5.0.0"`)
2. Commit and push to `main` (5.x), or to `release-4.1` for a 4.x patch release
3. Create and push a git tag matching the version (no `v` prefix):
   ```bash
   git tag 5.0.0
   git push origin 5.0.0
   ```
4. JitPack will automatically build and publish the new version from the tag

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/didww/didww-api-3-java-sdk

## License

The package is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
