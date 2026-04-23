package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.repository.ApiResponse;
import com.didww.sdk.resource.Address;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.Country;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.DidGroupType;
import com.didww.sdk.resource.EmergencyCallingService;
import com.didww.sdk.resource.EmergencyRequirement;
import com.didww.sdk.resource.EmergencyRequirementValidation;
import com.didww.sdk.resource.EmergencyVerification;
import com.didww.sdk.resource.Identity;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.StockKeepingUnit;
import com.didww.sdk.resource.orderitem.AvailableDidOrderItem;
import com.didww.sdk.resource.orderitem.OrderItem;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * End-to-end Emergency Calling Service purchase flow (2026-04-16).
 *
 * Steps:
 * 0. Find an address, then order an available DID with emergency feature in the same country.
 * 1. Find the newly ordered DID (not yet emergency-enabled).
 * 2. Look up emergency requirements for that DID's country + DID group type.
 * 3. Find an existing identity on the account.
 * 4. Find an existing address for that identity.
 * 5. Validate the (emergency_requirement, address, identity) triple.
 * 6. Create an emergency verification with the address and DID.
 * 7. Fetch the created verification to confirm its status.
 * 8. Fetch the auto-created emergency_calling_service via the verification.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.EmergencyScenarioExample
 */
public class EmergencyScenarioExample {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) throws InterruptedException {
        DidwwClient client = ExampleClientFactory.fromEnv();

        // === Step 0: Order an available DID with emergency feature ===
        System.out.println("=== Step 0: Order an available DID with emergency feature ===");

        // Find an address first so we know what country to order in
        QueryParams addressListParams = QueryParams.builder()
                .include("country")
                .page(1, 1)
                .build();
        List<Address> addressList = client.addresses().list(addressListParams).getData();
        if (addressList.isEmpty()) {
            System.out.println("No addresses on this account. Please create an address first. Exiting.");
            return;
        }
        Address addressForOrder = addressList.get(0);
        Country addressCountry = addressForOrder.getCountry();
        System.out.println("  Using address country: "
                + (addressCountry != null ? addressCountry.getName() : "unknown")
                + " (" + (addressCountry != null ? addressCountry.getId() : "?") + ")");

        // Find an available DID with emergency feature in that country
        QueryParams availableParams = QueryParams.builder()
                .filter("did_group.features", "emergency")
                .filter("country.id", addressCountry != null ? addressCountry.getId() : "")
                .include("did_group", "did_group.stock_keeping_units")
                .page(1, 1)
                .build();
        List<AvailableDid> availableDids = client.availableDids().list(availableParams).getData();
        if (availableDids.isEmpty()) {
            System.out.println("No available DIDs with emergency feature in this country. Exiting.");
            return;
        }

        AvailableDid availableDid = availableDids.get(0);
        DidGroup didGroup = availableDid.getDidGroup();
        List<StockKeepingUnit> skus = didGroup != null ? didGroup.getStockKeepingUnits() : null;
        if (skus == null || skus.isEmpty()) {
            System.out.println("No SKU found for this DID group. Exiting.");
            return;
        }
        StockKeepingUnit sku = skus.get(0);

        System.out.println("  Available DID: " + availableDid.getNumber());
        System.out.println("  DID Group: " + (didGroup != null ? didGroup.getAreaName() : "?"));

        AvailableDidOrderItem orderItem = new AvailableDidOrderItem();
        orderItem.setAvailableDidId(availableDid.getId());
        orderItem.setSkuId(sku.getId());

        Order order = new Order();
        order.setItems(Collections.singletonList(orderItem));

        Order createdOrder = client.orders().create(order).getData();
        System.out.println("  Order: " + createdOrder.getId() + " - " + createdOrder.getStatus());

        // Wait for order to complete
        for (int i = 0; i < 10; i++) {
            if (createdOrder.isCompleted()) break;
            Thread.sleep(5000);
            createdOrder = client.orders().find(createdOrder.getId()).getData();
        }
        if (!createdOrder.isCompleted()) {
            System.out.println("  Order did not complete (status: " + createdOrder.getStatus() + "). Exiting.");
            return;
        }
        System.out.println("  Order completed");

        // === Step 1: Find the newly ordered DID ===
        System.out.println("\n=== Step 1: Find the newly ordered DID ===");
        QueryParams didParams = QueryParams.builder()
                .filter("did_group.features", "emergency")
                .filter("emergency_enabled", "false")
                .include("did_group", "did_group.country", "did_group.did_group_type", "emergency_calling_service")
                .sort("-created_at")
                .page(1, 10)
                .build();
        List<Did> dids = client.dids().list(didParams).getData();

        // Pick a DID that is not yet assigned to an ECS
        Did did = null;
        for (Did d : dids) {
            if (d.getEmergencyCallingService() == null) {
                did = d;
                break;
            }
        }
        if (did == null) {
            System.out.println("No available DID without an existing Emergency Calling Service. Exiting.");
            return;
        }

        DidGroup dg = did.getDidGroup();
        Country country = dg != null ? dg.getCountry() : null;
        DidGroupType dgt = dg != null ? dg.getDidGroupType() : null;

        System.out.println("  DID:            " + did.getNumber() + " (" + did.getId() + ")");
        System.out.println("  DID Group:      " + (dg != null ? dg.getId() : "?"));
        System.out.println("  Country:        " + (country != null ? country.getName() : "?")
                + " (" + (country != null ? country.getId() : "?") + ")");
        System.out.println("  DID Group Type: " + (dgt != null ? dgt.getName() : "?")
                + " (" + (dgt != null ? dgt.getId() : "?") + ")");

        // === Step 2: Get emergency requirements ===
        System.out.println("\n=== Step 2: Get emergency requirements for country + did_group_type ===");
        QueryParams reqParams = QueryParams.builder()
                .filter("country.id", country != null ? country.getId() : "")
                .filter("did_group_type.id", dgt != null ? dgt.getId() : "")
                .build();
        List<EmergencyRequirement> requirements = client.emergencyRequirements().list(reqParams).getData();

        if (requirements.isEmpty()) {
            System.out.println("No emergency requirements found. Exiting.");
            return;
        }

        EmergencyRequirement requirement = requirements.get(0);
        System.out.println("  Emergency Requirement: " + requirement.getId());
        System.out.println("  Identity type:         " + requirement.getIdentityType());
        System.out.println("  Address area level:    " + requirement.getAddressAreaLevel());
        System.out.println("  Estimated setup time:  " + requirement.getEstimateSetupTime());
        System.out.println("  Setup price:           " + requirement.getMetaSetupPrice());
        System.out.println("  Monthly price:         " + requirement.getMetaMonthlyPrice());

        // === Step 3: Find an existing identity ===
        System.out.println("\n=== Step 3: Find an existing identity ===");
        QueryParams identityParams = QueryParams.builder()
                .page(1, 1)
                .build();
        List<Identity> identities = client.identities().list(identityParams).getData();

        if (identities.isEmpty()) {
            System.out.println("No identities found. Create an identity first. Exiting.");
            return;
        }

        Identity identity = identities.get(0);
        System.out.println("  Identity: " + identity.getId());
        System.out.println("  Type:     " + identity.getIdentityType());

        // === Step 4: Find an existing address ===
        System.out.println("\n=== Step 4: Find an existing address ===");
        QueryParams addrParams = QueryParams.builder()
                .page(1, 1)
                .build();
        List<Address> addresses = client.addresses().list(addrParams).getData();

        if (addresses.isEmpty()) {
            System.out.println("No addresses found. Create an address first. Exiting.");
            return;
        }

        Address address = addresses.get(0);
        System.out.println("  Address: " + address.getId());

        // === Step 5: Validate the order setup ===
        System.out.println("\n=== Step 5: Validate emergency requirement (requirement + address + identity) ===");
        EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
        validation.setEmergencyRequirement(new EmergencyRequirement().withId(requirement.getId()));
        validation.setAddress(new Address().withId(address.getId()));
        validation.setIdentity(new Identity().withId(identity.getId()));

        try {
            client.emergencyRequirementValidations().create(validation);
            System.out.println("  Validation passed -- this combination can be used for emergency calling.");
        } catch (Exception e) {
            System.out.println("  Validation failed: " + e.getMessage());
            System.out.println("  The address/identity does not satisfy the emergency requirement.");
            return;
        }

        // === Step 6: Create an emergency verification ===
        System.out.println("\n=== Step 6: Create emergency verification ===");
        String suffix = String.format("%08x", RANDOM.nextInt());
        EmergencyVerification verification = new EmergencyVerification();
        verification.setCallbackUrl("https://example.com/webhooks/emergency");
        verification.setCallbackMethod("post");
        verification.setExternalReferenceId("java-scenario-" + suffix);
        verification.setAddress(new Address().withId(address.getId()));
        verification.setDids(Collections.singletonList(new Did().withId(did.getId())));

        try {
            EmergencyVerification created = client.emergencyVerifications().create(verification).getData();
            System.out.println("  Created verification: " + created.getId());
            System.out.println("  Reference:            " + created.getReference());
            System.out.println("  Status:               " + created.getStatus());
            System.out.println("  External Reference:   " + created.getExternalReferenceId());

            // === Step 7: Fetch the created verification ===
            System.out.println("\n=== Step 7: Fetch the created verification ===");
            QueryParams verifyParams = QueryParams.builder()
                    .include("address", "emergency_calling_service", "dids")
                    .build();
            EmergencyVerification fetched = client.emergencyVerifications()
                    .find(created.getId(), verifyParams).getData();
            System.out.println("  Verification: " + fetched.getId());
            System.out.println("  Status:       " + fetched.getStatus());
            System.out.println("  isPending:    " + fetched.isPending());
            System.out.println("  isApproved:   " + fetched.isApproved());
            System.out.println("  isRejected:   " + fetched.isRejected());
            if (fetched.getRejectReasons() != null && !fetched.getRejectReasons().isEmpty()) {
                System.out.println("  Reject reasons: " + String.join(", ", fetched.getRejectReasons()));
            }
            if (fetched.getRejectComment() != null) {
                System.out.println("  Reject comment: " + fetched.getRejectComment());
            }

            // === Step 8: Get the emergency calling service ===
            System.out.println("\n=== Step 8: Fetch emergency calling service ===");
            if (fetched.getEmergencyCallingService() != null) {
                printEmergencyCallingService(client, fetched.getEmergencyCallingService().getId());
            } else {
                System.out.println("  No emergency_calling_service linked yet (may be created asynchronously).");
                System.out.println("  Check back after the verification is approved.");
            }
        } catch (Exception e) {
            System.out.println("Failed to create verification: " + e.getMessage());
        }

        System.out.println("\nDone! Emergency calling service flow completed.");
    }

    private static void printEmergencyCallingService(DidwwClient client, String serviceId) {
        QueryParams params = QueryParams.builder()
                .include("country", "did_group_type", "dids", "address", "emergency_verification")
                .build();
        EmergencyCallingService svc = client.emergencyCallingServices().find(serviceId, params).getData();
        System.out.println("  Service:        " + svc.getId());
        System.out.println("  Name:           " + svc.getName());
        System.out.println("  Reference:      " + svc.getReference());
        System.out.println("  Status:         " + svc.getStatus());
        System.out.println("  isActive:       " + svc.isActive());
        System.out.println("  isNewStatus:    " + svc.isNewStatus());
        System.out.println("  isInProcess:    " + svc.isInProcess());
        if (svc.getCountry() != null) {
            System.out.println("  Country:        " + svc.getCountry().getName());
        }
        if (svc.getDidGroupType() != null) {
            System.out.println("  DID Group Type: " + svc.getDidGroupType().getName());
        }
        System.out.println("  Setup price:    " + svc.getMetaSetupPrice());
        System.out.println("  Monthly price:  " + svc.getMetaMonthlyPrice());
        System.out.println("  Activated at:   " + svc.getActivatedAt());
        if (svc.getRenewDate() != null) {
            System.out.println("  Renews:         " + svc.getRenewDate());
        }
        if (svc.getDids() != null && !svc.getDids().isEmpty()) {
            String numbers = svc.getDids().stream()
                    .map(Did::getNumber)
                    .collect(Collectors.joining(", "));
            System.out.println("  Attached DIDs:  " + numbers);
        }
    }
}
