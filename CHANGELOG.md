# Changelog

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
