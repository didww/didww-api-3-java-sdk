# Examples

All examples now read the API key from the `DIDWW_API_KEY` environment variable.

## Prerequisites

- Java 11+
- Gradle wrapper (`./gradlew`)
- DIDWW API key for sandbox account

## Environment variables

- `DIDWW_API_KEY` (required): your DIDWW API key
- `FILE_PATH` (optional for `UploadFileExample`): file to encrypt. Generates a sample PDF if not set

## Run an example

Use the `runExample` Gradle task with `-PexampleClass`:

```bash
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.BalanceExample
```

## Available example classes

| Class | Description |
|---|---|
| [`com.didww.examples.BalanceExample`](src/main/java/com/didww/examples/BalanceExample.java) | Fetches and prints current account balance and credit. |
| [`com.didww.examples.CountriesExample`](src/main/java/com/didww/examples/CountriesExample.java) | Lists countries, demonstrates filtering, and fetches one country by ID. |
| [`com.didww.examples.RegionsExample`](src/main/java/com/didww/examples/RegionsExample.java) | Lists regions with filters/includes and fetches a specific region. |
| [`com.didww.examples.DidGroupsExample`](src/main/java/com/didww/examples/DidGroupsExample.java) | Fetches DID groups with included SKUs and shows group details. |
| [`com.didww.examples.DidsExample`](src/main/java/com/didww/examples/DidsExample.java) | Updates DID routing/capacity by assigning trunk and capacity pool. |
| [`com.didww.examples.TrunksExample`](src/main/java/com/didww/examples/TrunksExample.java) | Lists trunks, creates SIP and PSTN trunks, updates and deletes them. |
| [`com.didww.examples.SharedCapacityGroupsExample`](src/main/java/com/didww/examples/SharedCapacityGroupsExample.java) | Creates a shared capacity group in a capacity pool. |
| [`com.didww.examples.OrdersExample`](src/main/java/com/didww/examples/OrdersExample.java) | Lists orders and creates/cancels a DID order using live SKU lookup. |
| [`com.didww.examples.OrdersSkuExample`](src/main/java/com/didww/examples/OrdersSkuExample.java) | Creates a DID order by SKU resolved from DID groups. |
| [`com.didww.examples.OrdersNanpaExample`](src/main/java/com/didww/examples/OrdersNanpaExample.java) | Orders a DID number by NPA/NXX prefix. |
| [`com.didww.examples.OrdersCapacityExample`](src/main/java/com/didww/examples/OrdersCapacityExample.java) | Purchases capacity by creating a capacity order item. |
| [`com.didww.examples.OrdersAvailableDidsExample`](src/main/java/com/didww/examples/OrdersAvailableDidsExample.java) | Orders an available DID using included DID group SKU. |
| [`com.didww.examples.OrdersReservationDidsExample`](src/main/java/com/didww/examples/OrdersReservationDidsExample.java) | Reserves a DID and then places an order from that reservation. |
| [`com.didww.examples.OrdersAllItemTypesExample`](src/main/java/com/didww/examples/OrdersAllItemTypesExample.java) | Creates a DID order with all item types: by SKU, by available DID, and by reservation. Fetches ordered DIDs. |
| [`com.didww.examples.UploadFileExample`](src/main/java/com/didww/examples/UploadFileExample.java) | Creates (or reads) a file, encrypts it, and uploads to `encrypted_files`. |
| [`com.didww.examples.IdentityAddressProofsExample`](src/main/java/com/didww/examples/IdentityAddressProofsExample.java) | Creates identity and address, encrypts and uploads files, attaches proofs to both. |
| [`com.didww.examples.VoiceInTrunkGroupsExample`](src/main/java/com/didww/examples/VoiceInTrunkGroupsExample.java) | CRUD for trunk groups with trunk relationships. |
| [`com.didww.examples.VoiceInTrunkSipRegistrationExample`](src/main/java/com/didww/examples/VoiceInTrunkSipRegistrationExample.java) | End-to-end SIP registration flow: create with `enabledSipRegistration=true`, rename, disable by setting `host`, re-enable by toggling the flag. The SDK keeps the dependent fields (`host`, `port`, `useDidInRuri`) aligned with the server's validation rules automatically. |
| [`com.didww.examples.VoiceOutTrunksExample`](src/main/java/com/didww/examples/VoiceOutTrunksExample.java) | CRUD for voice out trunks (requires account config). |
| [`com.didww.examples.DidTrunkAssignmentExample`](src/main/java/com/didww/examples/DidTrunkAssignmentExample.java) | Demonstrates exclusive trunk/trunk group assignment on DIDs. |
| [`com.didww.examples.DidReservationsExample`](src/main/java/com/didww/examples/DidReservationsExample.java) | Creates, lists, finds and deletes DID reservations. |
| [`com.didww.examples.ExportsExample`](src/main/java/com/didww/examples/ExportsExample.java) | Creates and lists CDR exports with from/to datetime range filters. |
| [`com.didww.examples.CapacityPoolsExample`](src/main/java/com/didww/examples/CapacityPoolsExample.java) | Lists capacity pools with included shared capacity groups. |

### 2026-04-16 Examples

| Class | Description |
|---|---|
| [`com.didww.examples.DidHistoryExample`](src/main/java/com/didww/examples/DidHistoryExample.java) | Lists DID ownership history (last 90 days) with action/date filters. |
| [`com.didww.examples.EmergencyRequirementsExample`](src/main/java/com/didww/examples/EmergencyRequirementsExample.java) | Lists emergency requirements per country/DID group type. |
| [`com.didww.examples.EmergencyCallingServicesExample`](src/main/java/com/didww/examples/EmergencyCallingServicesExample.java) | Lists and filters emergency calling services by status. |
| [`com.didww.examples.EmergencyVerificationsExample`](src/main/java/com/didww/examples/EmergencyVerificationsExample.java) | Lists emergency verifications with status predicates. |
| [`com.didww.examples.EmergencyRequirementValidationsExample`](src/main/java/com/didww/examples/EmergencyRequirementValidationsExample.java) | Validates address+identity against an emergency requirement. |
| [`com.didww.examples.AddressVerificationsExample`](src/main/java/com/didww/examples/AddressVerificationsExample.java) | Lists address verifications with status predicates and reject details. |
| [`com.didww.examples.OrdersEmergencyExample`](src/main/java/com/didww/examples/OrdersEmergencyExample.java) | Inspects emergency orders with EmergencyOrderItem details. |
| [`com.didww.examples.EmergencyScenarioExample`](src/main/java/com/didww/examples/EmergencyScenarioExample.java) | End-to-end: find DID → check requirements → validate → create verification → get service. |

## Upload file example

```bash
DIDWW_API_KEY=your_api_key FILE_PATH=/absolute/path/to/file.jpeg \
  ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
```

## Troubleshooting

If `DIDWW_API_KEY` is missing, examples fail fast with:

`DIDWW_API_KEY environment variable is required`
