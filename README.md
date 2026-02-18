Java client for DIDWW API v3.

## About DIDWW API v3

The DIDWW API provides a simple yet powerful interface that allows you to fully integrate your own applications with DIDWW services. An extensive set of actions may be performed using this API, such as ordering and configuring phone numbers, setting capacity, creating SIP trunks and retrieving CDRs and other operational data.

The DIDWW API v3 is a fully compliant implementation of the [JSON API specification](http://jsonapi.org/format/).

Read more https://doc.didww.com/api

This SDK targets DIDWW API v3 documentation version:
[https://doc.didww.com/api3/2022-05-10/index.html](https://doc.didww.com/api3/2022-05-10/index.html)

## Requirements

- Java 11+

## Installation

### Gradle

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // branch
    implementation("com.github.didww:didww-api-3-java-sdk:main-SNAPSHOT")

    // or exact commit
    // implementation("com.github.didww:didww-api-3-java-sdk:<commit-hash>")
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
        <version>main-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### JitPack (no release required)

You can consume the SDK directly from GitHub branch/commit via JitPack.

Gradle:

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // branch
    implementation("com.github.didww:didww-api-3-java-sdk:main-SNAPSHOT")

    // or exact commit
    // implementation("com.github.didww:didww-api-3-java-sdk:<commit-hash>")
}
```

## Usage

```java
import com.didww.sdk.*;
import com.didww.sdk.resource.*;
import com.didww.sdk.http.QueryParams;

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

For more examples visit [examples](examples/src/main/java/com/didww/examples/).

For details on obtaining your API key please visit https://doc.didww.com/api#introduction-api-keys

## Examples

- Source code: [examples/src/main/java/com/didww/examples](examples/src/main/java/com/didww/examples)
- How to run: [examples/README.md](examples/README.md)

## Configuration

```java
import java.time.Duration;

DidwwClient client = DidwwClient.builder()
    .credentials(new DidwwCredentials("YOUR_API_KEY", DidwwEnvironment.PRODUCTION))
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .build();
```

### Environments

| Environment | Base URL |
|-------------|----------|
| `DidwwEnvironment.PRODUCTION` | `https://api.didww.com/v3` |
| `DidwwEnvironment.SANDBOX` | `https://sandbox-api.didww.com/v3` |

## Resources

### Read-Only Resources

```java
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

// POPs (Points of Presence)
List<Pop> pops = client.pops().list().getData();

// DID Group Types
List<DidGroupType> types = client.didGroupTypes().list().getData();

// DID Groups
List<DidGroup> groups = client.didGroups().list().getData();

// Available DIDs
List<AvailableDid> available = client.availableDids().list().getData();

// Proof Types
List<ProofType> proofTypes = client.proofTypes().list().getData();

// Public Keys
List<PublicKey> publicKeys = client.publicKeys().list().getData();

// Requirements
List<Requirement> requirements = client.requirements().list().getData();

// Supporting Document Templates
List<SupportingDocumentTemplate> templates = client.supportingDocumentTemplates().list().getData();

// Balance (singleton)
Balance balance = client.balance().find().getData();
```

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

### Voice In Trunks

```java
import com.didww.sdk.resource.configuration.*;

// Create SIP trunk
VoiceInTrunk trunk = new VoiceInTrunk();
trunk.setName("My SIP Trunk");
trunk.setPriority(1);
trunk.setWeight(100);
trunk.setCliFormat("e164");
trunk.setRingingTimeout(30);

SipConfiguration sip = new SipConfiguration();
sip.setHost("sip.example.com");
sip.setPort(5060);
sip.setCodecIds(Arrays.asList(9, 10));
sip.setTransportProtocolId(1);
trunk.setConfiguration(sip);

VoiceInTrunk created = client.voiceInTrunks().create(trunk).getData();

// Update trunk
created.setDescription("Updated");
client.voiceInTrunks().update(created);

// Delete trunk
client.voiceInTrunks().delete(created.getId());
```

### Voice In Trunk Groups

```java
VoiceInTrunkGroup group = new VoiceInTrunkGroup();
group.setName("Primary Group");
group.setCapacityLimit(50);
VoiceInTrunkGroup created = client.voiceInTrunkGroups().create(group).getData();
```

### Voice Out Trunks

```java
VoiceOutTrunk voTrunk = new VoiceOutTrunk();
voTrunk.setName("My Outbound Trunk");
voTrunk.setAllowedSipIps(Arrays.asList("0.0.0.0/0"));
voTrunk.setDefaultDstAction("allow_all");
voTrunk.setOnCliMismatchAction("replace_cli");
VoiceOutTrunk created = client.voiceOutTrunks().create(voTrunk).getData();
```

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
Identity identity = new Identity();
identity.setFirstName("John");
identity.setLastName("Doe");
identity.setIdentityType("Personal");
Identity created = client.identities().create(identity).getData();
```

### Addresses

```java
Address address = new Address();
address.setCityName("New York");
address.setPostalCode("10001");
address.setAddress("123 Main St");
Address created = client.addresses().create(address).getData();
```

### Address Verifications

```java
AddressVerification verification = new AddressVerification();
verification.setCallbackUrl("http://example.com/callback");
verification.setCallbackMethod("GET");
AddressVerification created = client.addressVerifications().create(verification).getData();
```

### Exports

```java
import java.nio.file.Path;

Export export = new Export();
export.setExportType("cdr_in");
Export created = client.exports().create(export).getData();

// Download the export when completed
client.downloadExport(created.getUrl(), Path.of("/tmp/export.csv"));
```

## Filtering, Sorting, and Pagination

```java
import com.didww.sdk.http.QueryParams;

QueryParams params = QueryParams.builder()
    .filter("country.id", "uuid")
    .filter("name", "Arizona")
    .include("country")
    .sort("-created_at")
    .page(1, 25)
    .build();

List<Region> regions = client.regions().list(params).getData();
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
byte[] encrypted = Encrypt.encryptWithKeys(fileBytes, publicKeyPems);
String fingerprint = Encrypt.calculateFingerprint(publicKeyPems);
```

Upload encrypted file:

```java
List<String> encryptedFileIds = client.uploadEncryptedFile(
    encrypted,          // encrypted bytes
    "document.pdf.enc", // uploaded file name
    fingerprint,        // Encrypt#getFingerprint()
    "document.pdf"      // description
);
```

## Webhook Signature Validation

Validate incoming webhook callbacks from DIDWW using HMAC-SHA1 signature verification.

```java
import com.didww.sdk.callback.RequestValidator;

RequestValidator validator = new RequestValidator("YOUR_API_KEY");

// In your webhook handler:
boolean valid = validator.validate(
    requestUrl,    // full original URL
    payloadMap,    // Map<String, String> of payload key-value pairs
    signature      // value of X-DIDWW-Signature header
);
```

The signature header name is available as `RequestValidator.HEADER_NAME`.

## Error Handling

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

## Trunk Configuration Types

| Type | Class |
|------|-------|
| SIP | `SipConfiguration` |
| H.323 | `H323Configuration` |
| IAX2 | `Iax2Configuration` |
| PSTN | `PstnConfiguration` |

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
| Pop | `client.pops()` | list, find |
| DidGroupType | `client.didGroupTypes()` | list, find |
| DidGroup | `client.didGroups()` | list, find |
| AvailableDid | `client.availableDids()` | list, find |
| ProofType | `client.proofTypes()` | list, find |
| PublicKey | `client.publicKeys()` | list, find |
| Requirement | `client.requirements()` | list, find |
| SupportingDocumentTemplate | `client.supportingDocumentTemplates()` | list, find |
| Balance | `client.balance()` | find |
| Did | `client.dids()` | list, find, update, delete |
| VoiceInTrunk | `client.voiceInTrunks()` | list, find, create, update, delete |
| VoiceInTrunkGroup | `client.voiceInTrunkGroups()` | list, find, create, update, delete |
| VoiceOutTrunk | `client.voiceOutTrunks()` | list, find, create, update, delete |
| VoiceOutTrunkRegenerateCredential | `client.voiceOutTrunkRegenerateCredentials()` | create |
| DidReservation | `client.didReservations()` | list, find, create, delete |
| CapacityPool | `client.capacityPools()` | list, find, update |
| SharedCapacityGroup | `client.sharedCapacityGroups()` | list, find, create, update, delete |
| Order | `client.orders()` | list, find, create, delete |
| Export | `client.exports()` | list, find, create |
| Address | `client.addresses()` | list, find, create, update, delete |
| AddressVerification | `client.addressVerifications()` | list, find, create |
| Identity | `client.identities()` | list, find, create, update, delete |
| EncryptedFile | `client.encryptedFiles()` | list, find, delete |
| PermanentSupportingDocument | `client.permanentSupportingDocuments()` | create |
| Proof | `client.proofs()` | create |
| RequirementValidation | `client.requirementValidations()` | create |

## Contributing

Bug reports and pull requests are welcome on GitHub at https://github.com/didww/didww-api-3-java-sdk

## License

The package is available as open source under the terms of the [MIT License](https://opensource.org/licenses/MIT).
