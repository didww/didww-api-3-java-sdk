# Examples

All examples read the API key from the `DIDWW_API_KEY` environment variable and run against the sandbox. `WebhookExample` sends no request and needs no API key.

## Prerequisites

- Java 11+
- Gradle wrapper (`./gradlew`)
- DIDWW API key for sandbox account

## Environment variables

- `DIDWW_API_KEY` (required): your DIDWW API key
- `FILE_PATH` (optional for `UploadFileExample`): file to encrypt. Generates a sample PDF if not set
- `XDG_CACHE_HOME` (optional for `PricesDownloadExample`, Linux and other Unix systems): user cache directory, `~/.cache` if not set

## Run an example

Use the `runExample` Gradle task with `-PexampleClass`:

```bash
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.BalanceExample
```

### Pass an argument

Some examples take an optional argument. Pass it with Gradle's `--args`; relative paths resolve against the project directory:

```bash
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.PricesLookupExample --args="18005550123"
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.PricesDownloadExample --args="/path/to/dir"
```

## Available example classes

All classes are in the package `com.didww.examples`.

| Class | Description |
|---|---|
| [`BalanceExample`](src/main/java/com/didww/examples/BalanceExample.java) | Prints the account balance: total balance, balance and credit. |
| [`CountriesExample`](src/main/java/com/didww/examples/CountriesExample.java) | Lists countries, filters them, and finds one country by ID. |
| [`RegionsExample`](src/main/java/com/didww/examples/RegionsExample.java) | Lists the regions of a country with filters and the included country, and finds one region. |
| [`DidGroupsExample`](src/main/java/com/didww/examples/DidGroupsExample.java) | Lists DID groups with their included SKUs, number type and demo flag, and shows group details. |
| [`DidHistoryExample`](src/main/java/com/didww/examples/DidHistoryExample.java) | Lists DID history events (last 90 days) with action and date filters. |
| [`DidsExample`](src/main/java/com/didww/examples/DidsExample.java) | Lists DIDs and updates the last ordered one: trunk, capacity pool, capacity limit and description. |
| [`DidTrunkAssignmentExample`](src/main/java/com/didww/examples/DidTrunkAssignmentExample.java) | Assigns a DID to a trunk, then to a trunk group (the trunk is cleared: they are exclusive), and restores the DID's routing. |
| [`DidSmsRoutingExample`](src/main/java/com/didww/examples/DidSmsRoutingExample.java) | Routes a DID's inbound SMS to an SMS trunk, then to an SMS trunk group (exclusive), and restores the routing. |
| [`DidReservationsExample`](src/main/java/com/didww/examples/DidReservationsExample.java) | Reserves an available DID, lists and finds reservations, and deletes the reservation. |
| [`CapacityPoolsExample`](src/main/java/com/didww/examples/CapacityPoolsExample.java) | Lists capacity pools with their included shared capacity groups and qty-based pricings, and finds one pool. |
| [`SharedCapacityGroupsExample`](src/main/java/com/didww/examples/SharedCapacityGroupsExample.java) | Creates a shared capacity group in a capacity pool, lists the groups, renames it and deletes it. |
| [`OrdersExample`](src/main/java/com/didww/examples/OrdersExample.java) | Lists the most recent orders, orders a DID by SKU and cancels (deletes) that order. |
| [`OrdersSkuExample`](src/main/java/com/didww/examples/OrdersSkuExample.java) | Orders DIDs by SKU and quantity (`did_order_items` with `sku_id` and `qty`). |
| [`OrdersAvailableDidsExample`](src/main/java/com/didww/examples/OrdersAvailableDidsExample.java) | Orders a specific available DID with an SKU of its included DID group. |
| [`OrdersReservationDidsExample`](src/main/java/com/didww/examples/OrdersReservationDidsExample.java) | Reserves a DID and orders it from the reservation. |
| [`OrdersNanpaExample`](src/main/java/com/didww/examples/OrdersNanpaExample.java) | Orders a NANPA DID by NPA/NXX prefix. |
| [`OrdersCapacityExample`](src/main/java/com/didww/examples/OrdersCapacityExample.java) | Buys capacity channels in a capacity pool. |
| [`OrdersAllItemTypesExample`](src/main/java/com/didww/examples/OrdersAllItemTypesExample.java) | Places one order with the three DID item variants (by SKU, by available DID, by reservation) and lists its DIDs; capacity needs an order of its own. |
| [`OrdersEmergencyExample`](src/main/java/com/didww/examples/OrdersEmergencyExample.java) | Reads emergency orders, which DIDWW creates: recent orders with emergency order items, then the order of an emergency calling service. |
| [`TrunksExample`](src/main/java/com/didww/examples/TrunksExample.java) | Lists Voice IN trunks, creates a SIP trunk (point of presence, codecs, ICE mode) and a PSTN trunk, finds, updates and deletes them. |
| [`VoiceInTrunkGroupsExample`](src/main/java/com/didww/examples/VoiceInTrunkGroupsExample.java) | Creates two trunks and a trunk group with them, lists groups, renames the group and deletes it with its trunks. |
| [`VoiceInTrunkSipRegistrationExample`](src/main/java/com/didww/examples/VoiceInTrunkSipRegistrationExample.java) | SIP registration flow: create with `enabledSipRegistration=true`, rename, disable by setting `host`, re-enable by toggling the flag. The SDK keeps `host`, `port` and `useDidInRuri` aligned with the server's validation rules. |
| [`VoiceOutTrunksExample`](src/main/java/com/didww/examples/VoiceOutTrunksExample.java) | Creates, updates and deletes a Voice OUT trunk with codecs and ICE mode (requires account configuration). |
| [`SmsTrunksExample`](src/main/java/com/didww/examples/SmsTrunksExample.java) | Lists SMS trunks, creates and updates an HTTP IN trunk, regenerates the credentials of an HTTP OUT trunk, deletes both. |
| [`SmsTrunkGroupsExample`](src/main/java/com/didww/examples/SmsTrunkGroupsExample.java) | Creates two inbound SMS trunks, groups them by priority, lists groups, deletes the group (and its trunks). |
| [`SenderIdVerificationsExample`](src/main/java/com/didww/examples/SenderIdVerificationsExample.java) | Lists Sender ID verifications, renames an active one and routes it through a sending SMS trunk, then restores its name and trunk. |
| [`IdentityAddressProofsExample`](src/main/java/com/didww/examples/IdentityAddressProofsExample.java) | Creates an identity and an address, uploads encrypted files, creates proofs for both, prints the verified flags, deletes them. |
| [`AddressVerificationsExample`](src/main/java/com/didww/examples/AddressVerificationsExample.java) | Lists address verifications with status predicates and reject details. |
| [`UploadFileExample`](src/main/java/com/didww/examples/UploadFileExample.java) | Encrypts a file with the DIDWW public keys, uploads it and prints the encrypted file ID. |
| [`ExportsExample`](src/main/java/com/didww/examples/ExportsExample.java) | Creates and lists CDR exports with from/to datetime range filters (UTC). |
| [`ExportsSmsExample`](src/main/java/com/didww/examples/ExportsSmsExample.java) | Creates `sms_in` and `sms_out` exports with their filters and reads the columns of a completed one. |
| [`EmergencyRequirementsExample`](src/main/java/com/didww/examples/EmergencyRequirementsExample.java) | Lists emergency requirements per country and number type. |
| [`EmergencyRequirementValidationsExample`](src/main/java/com/didww/examples/EmergencyRequirementValidationsExample.java) | Validates an address and identity against an emergency requirement. |
| [`EmergencyVerificationsExample`](src/main/java/com/didww/examples/EmergencyVerificationsExample.java) | Lists emergency verifications with status predicates. |
| [`EmergencyCallingServicesExample`](src/main/java/com/didww/examples/EmergencyCallingServicesExample.java) | Lists emergency calling services and filters them by status. |
| [`EmergencyScenarioExample`](src/main/java/com/didww/examples/EmergencyScenarioExample.java) | End to end: find a DID, check requirements, validate, create a verification, get the calling service. |
| [`PricesLookupExample`](src/main/java/com/didww/examples/PricesLookupExample.java) `[number]` | Looks up Voice IN, Voice OUT, SMS OUT (A2P, P2P, OTP) and SMS IN prices for a number. |
| [`PricesDownloadExample`](src/main/java/com/didww/examples/PricesDownloadExample.java) `[dir]` | Downloads the Voice IN price file, fetches it again with If-Modified-Since (304), reads the first rows. Default directory: `didww-prices` in the user cache directory, created for the current user only. |
| [`IncludesExample`](src/main/java/com/didww/examples/IncludesExample.java) | Reads regions with their country, DID groups with SKUs, available DIDs with a nested include and DIDs with their trunk; a relationship that is not included reads as `null`. |
| [`WebhookExample`](src/main/java/com/didww/examples/WebhookExample.java) | Signs a sample callback the way DIDWW does and validates it with `RequestValidator`: `valid: true`, then `valid: false` for a tampered payload. |
| [`TestReadmeExample`](src/main/java/com/didww/examples/TestReadmeExample.java) | Runs the code snippets of the SDK README against the sandbox, one check per snippet, cleans up, prints `Results: N passed, M failed` and exits with status 1 on a failure. Production-only and staff-reviewed snippets are reported as skipped. |

`SenderIdVerificationsExample`, `DidSmsRoutingExample`, `DidTrunkAssignmentExample` and `TestReadmeExample` change existing account state and restore it at the end; `DidReservationsExample`, `SharedCapacityGroupsExample`, `TrunksExample`, `VoiceInTrunkGroupsExample`, `SmsTrunksExample`, `SmsTrunkGroupsExample` and `TestReadmeExample` delete what they create. Both happen also when a step fails. Orders and exports cannot be deleted.

## Upload file example

```bash
DIDWW_API_KEY=your_api_key FILE_PATH=/absolute/path/to/file.jpeg \
  ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
```

## Troubleshooting

If `DIDWW_API_KEY` is missing, examples fail fast with:

`DIDWW_API_KEY environment variable is required`
