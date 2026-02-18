# Examples

All examples now read the API key from the `DIDWW_API_KEY` environment variable.

## Prerequisites

- Java 11+
- Gradle wrapper (`./gradlew`)
- DIDWW API key for sandbox or production account

## Environment variables

- `DIDWW_API_KEY` (required): your DIDWW API key
- `FILE_PATH` (required only for `UploadFileExample`): file to encrypt

## Run an example

Use the `runExample` Gradle task with `-PexampleClass`:

```bash
DIDWW_API_KEY=your_api_key ./gradlew runExample -PexampleClass=com.didww.examples.BalanceExample
```

## Available example classes

- `com.didww.examples.BalanceExample`
- `com.didww.examples.CountriesExample`
- `com.didww.examples.RegionsExample`
- `com.didww.examples.DidGroupsExample`
- `com.didww.examples.DidsExample`
- `com.didww.examples.TrunksExample`
- `com.didww.examples.SharedCapacityGroupsExample`
- `com.didww.examples.OrdersExample`
- `com.didww.examples.OrdersSkuExample`
- `com.didww.examples.OrdersCapacityExample`
- `com.didww.examples.OrdersAvailableDidsExample`
- `com.didww.examples.OrdersReservationDidsExample`
- `com.didww.examples.UploadFileExample`

## Upload file example

```bash
DIDWW_API_KEY=your_api_key FILE_PATH=/absolute/path/to/file.jpeg \
  ./gradlew runExample -PexampleClass=com.didww.examples.UploadFileExample
```

## Troubleshooting

If `DIDWW_API_KEY` is missing, examples fail fast with:

`DIDWW_API_KEY environment variable is required`
