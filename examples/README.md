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
| [`com.didww.examples.OrdersCapacityExample`](src/main/java/com/didww/examples/OrdersCapacityExample.java) | Purchases capacity by creating a capacity order item. |
| [`com.didww.examples.OrdersAvailableDidsExample`](src/main/java/com/didww/examples/OrdersAvailableDidsExample.java) | Orders an available DID using included DID group SKU. |
| [`com.didww.examples.OrdersReservationDidsExample`](src/main/java/com/didww/examples/OrdersReservationDidsExample.java) | Reserves a DID and then places an order from that reservation. |
| [`com.didww.examples.OrdersAllItemTypesExample`](src/main/java/com/didww/examples/OrdersAllItemTypesExample.java) | Creates a DID order with all item types: by SKU, by available DID, and by reservation. Fetches ordered DIDs. |
| [`com.didww.examples.UploadFileExample`](src/main/java/com/didww/examples/UploadFileExample.java) | Creates (or reads) a file, encrypts it, and uploads to `encrypted_files`. |
| [`com.didww.examples.IdentityAddressProofsExample`](src/main/java/com/didww/examples/IdentityAddressProofsExample.java) | Creates identity and address, encrypts and uploads files, attaches proofs to both. |

## Upload file example

```bash
DIDWW_API_KEY=your_api_key FILE_PATH=/absolute/path/to/file.jpeg \
  ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
```

## Troubleshooting

If `DIDWW_API_KEY` is missing, examples fail fast with:

`DIDWW_API_KEY environment variable is required`
