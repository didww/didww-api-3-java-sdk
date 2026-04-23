# Changelog

## Unreleased

### Breaking Changes
- Renamed `Order#isCancelled` to `Order#isCanceled` for wire-format consistency
  (server wire value is `"canceled"`, single L). Safe because `isCancelled` was
  not in the released 3.0.0 tag — it was added on `feat/api-2026-04-16`.
