# Changelog

## [4.1.0] - 2026-05-04

### Added
- Complete the API 2026-04-16 `SipConfiguration` attribute set: `enabledSipRegistration`, `useDidInRuri`, `cnamLookup`, `networkProtocolPriority`, `diversionInjectMode`, plus the server-generated read-only `incomingAuthUsername` / `incomingAuthPassword`.
- `SipConfiguration#toString` and `CredentialsAndIpAuthenticationMethod#toString` redact credential fields with `[FILTERED]` so default logging / debugger / unhandled exception traces never expose plaintext credentials. Wire payload is unaffected.
- `SipConfiguration` auto-cascades server-enforced field dependencies on assignment: `setEnabledSipRegistration(true)` clears `host` / `port`, `setEnabledSipRegistration(false)` forces `useDidInRuri = false`, and `setHost(<non-null>)` flips both. Jackson populates the private fields via reflection during deserialization, so server responses are not clobbered.

### Breaking Changes
- Renamed `Order#isCancelled` to `Order#isCanceled` for wire-format consistency
  (server wire value is `"canceled"`, single L). Safe because `isCancelled` was
  not in the released 3.0.0 tag — it was added on `feat/api-2026-04-16`.
