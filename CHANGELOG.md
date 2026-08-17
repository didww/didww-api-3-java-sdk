# Changelog

## [4.0.1] - 2026-08-17

### Fixed
- Reading server-provided `meta` off `EmergencyRequirement`, `EmergencyCallingService` and
  `DidHistory` no longer throws `ClassCastException` when a value arrives as a JSON number.
  `GET /v3/emergency_requirements` sends `setup_price` as a number while `monthly_price` is a
  string, and the `Map<String, String>` field enforced nothing: jsonapi-converter deserializes by
  `field.getType()`, which drops the type arguments, so Jackson stored `Integer` values. Values are
  now coerced on parse through `MetaMap`. `getMeta()` keeps its `Map<String, String>` signature and
  its JVM descriptor, so no consumer change or rebuild is required.

## [4.0.0] - 2026-04-24

### Breaking Changes
- Renamed `Order#isCancelled` to `Order#isCanceled` for wire-format consistency
  (server wire value is `"canceled"`, single L). Safe because `isCancelled` was
  not in the released 3.0.0 tag — it was added on `feat/api-2026-04-16`.
