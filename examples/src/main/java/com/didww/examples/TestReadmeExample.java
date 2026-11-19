package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.Encrypt;
import com.didww.sdk.auth.AccessToken;
import com.didww.sdk.auth.DidwwScope;
import com.didww.sdk.auth.TokenStore;
import com.didww.sdk.callback.RequestValidator;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwAuthException;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.http.QueryParams;
import com.didww.sdk.prices.PriceFile;
import com.didww.sdk.prices.PriceFileReader;
import com.didww.sdk.prices.PriceLookup;
import com.didww.sdk.prices.PricesService;
import com.didww.sdk.prices.SmsInPrice;
import com.didww.sdk.prices.SmsOutPrice;
import com.didww.sdk.prices.VoiceInPrice;
import com.didww.sdk.prices.VoiceOutPrice;
import com.didww.sdk.resource.Address;
import com.didww.sdk.resource.AddressRequirement;
import com.didww.sdk.resource.AddressRequirementValidation;
import com.didww.sdk.resource.AddressVerification;
import com.didww.sdk.resource.AvailableDid;
import com.didww.sdk.resource.Balance;
import com.didww.sdk.resource.BaseResource;
import com.didww.sdk.resource.CapacityPool;
import com.didww.sdk.resource.Country;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.DidGroup;
import com.didww.sdk.resource.DidHistory;
import com.didww.sdk.resource.DidReservation;
import com.didww.sdk.resource.EmergencyCallingService;
import com.didww.sdk.resource.EmergencyRequirement;
import com.didww.sdk.resource.EmergencyRequirementValidation;
import com.didww.sdk.resource.EmergencyVerification;
import com.didww.sdk.resource.EncryptedFile;
import com.didww.sdk.resource.Export;
import com.didww.sdk.resource.Identity;
import com.didww.sdk.resource.Order;
import com.didww.sdk.resource.PermanentSupportingDocument;
import com.didww.sdk.resource.Proof;
import com.didww.sdk.resource.ProofEntity;
import com.didww.sdk.resource.ProofType;
import com.didww.sdk.resource.PublicKey;
import com.didww.sdk.resource.Region;
import com.didww.sdk.resource.SenderIdVerification;
import com.didww.sdk.resource.SharedCapacityGroup;
import com.didww.sdk.resource.SmsTrunk;
import com.didww.sdk.resource.SmsTrunkGroup;
import com.didww.sdk.resource.SmsTrunkRegenerateCredential;
import com.didww.sdk.resource.StockKeepingUnit;
import com.didww.sdk.resource.SupportingDocumentTemplate;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.didww.sdk.resource.VoiceOutTrunk;
import com.didww.sdk.resource.authenticationmethod.CredentialsAndIpAuthenticationMethod;
import com.didww.sdk.resource.configuration.SipConfiguration;
import com.didww.sdk.resource.enums.CallbackMethod;
import com.didww.sdk.resource.enums.CliFormat;
import com.didww.sdk.resource.enums.Codec;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.ExportServiceType;
import com.didww.sdk.resource.enums.ExportType;
import com.didww.sdk.resource.enums.IceMode;
import com.didww.sdk.resource.enums.IdentityType;
import com.didww.sdk.resource.enums.NumberType;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.enums.OrderStatus;
import com.didww.sdk.resource.enums.Pop;
import com.didww.sdk.resource.enums.SenderIdVerificationStatus;
import com.didww.sdk.resource.enums.SmsBodyType;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.didww.sdk.resource.enums.TransportProtocol;
import com.didww.sdk.resource.enums.VoiceOutCodec;
import com.didww.sdk.resource.orderitem.AvailableDidOrderItem;
import com.didww.sdk.resource.orderitem.CapacityOrderItem;
import com.didww.sdk.resource.orderitem.DidOrderItem;
import com.didww.sdk.resource.orderitem.ReservationDidOrderItem;
import com.didww.sdk.resource.smstrunkconfiguration.GenericSmsTrunkConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpOutConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsTrunkConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Runs the Java code snippets of the SDK README (README.md in the repository root) against the sandbox:
 * one check per snippet, or per section where its snippets belong together. A check that fails is
 * recorded with its error and the run goes on.
 *
 * What a check creates is deleted, and what it changes on existing resources (the routing and
 * description of a DID, the SMS trunk of a Sender ID verification) is restored, in a finally block,
 * also when the check fails. Exports, address verifications and emergency verifications cannot be
 * deleted, and DIDs ordered for the verifications stay on the account; an order is cancelled while it
 * is still pending. Each verification is printed on a line starting with "REVIEW" for DIDWW staff to
 * approve or reject.
 *
 * Only the production client snippet is skipped. A check is also reported as skipped when the sandbox
 * account cannot run it (no DID, no price plan) or when the environment variable it needs is not set:
 *   - DIDWW_README_PROXY_URL: an HTTP proxy, such as http://proxy.example.com:3128, for the HTTP client snippet;
 *   - DIDWW_README_REDIS_URL: a Redis server, such as redis://localhost:6379/0, for the token store snippet.
 * The installation, development and release snippets are build files and shell commands and are not
 * run. Lists are limited to their first page, and created resources get a random name suffix.
 *
 * Prints "Results: N passed, M failed" and exits with status 1 when a check failed.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.TestReadmeExample
 */
public class TestReadmeExample {

    /** The body of a check. It registers a cleanup step as soon as it creates or changes a resource. */
    @FunctionalInterface
    private interface Check {
        void run(Cleanup cleanup) throws Exception;
    }

    @FunctionalInterface
    private interface Step {
        void run() throws Exception;
    }

    /** Thrown by a check that cannot run on this account; the check is reported as skipped. */
    private static final class Skipped extends RuntimeException {
        private static final long serialVersionUID = 1L;

        Skipped(String reason) {
            super(reason);
        }
    }

    /** The cleanup steps of one check, run in reverse order of registration. */
    private static final class Cleanup {
        private final Deque<String> names = new ArrayDeque<>();
        private final Deque<Step> steps = new ArrayDeque<>();

        void add(String name, Step step) {
            names.push(name);
            steps.push(step);
        }

        // Runs every step and returns the errors; a resource that is already gone (404) is no error.
        List<String> runAll() {
            List<String> errors = new ArrayList<>();
            while (!steps.isEmpty()) {
                String name = names.pop();
                try {
                    steps.pop().run();
                    print("cleanup: " + name);
                } catch (DidwwApiException e) {
                    if (e.getHttpStatus() == 404) {
                        print("cleanup: " + name + " (already gone)");
                    } else {
                        errors.add(name + ": " + describe(e));
                    }
                } catch (Exception e) {
                    errors.add(name + ": " + describe(e));
                }
            }
            return errors;
        }
    }

    /** The DID fields a check changes and restores at the end. */
    private enum DidField { DESCRIPTION, CAPACITY_LIMIT, VOICE_ROUTING, SMS_ROUTING }

    private static final String SUFFIX = UUID.randomUUID().toString().substring(0, 8);

    private static DidwwClient client;
    private static final Map<String, Country> countriesByIso = new HashMap<>();
    private static int nameCount;
    private static int passed;
    private static int failed;
    private static final List<String> failures = new ArrayList<>();
    private static final List<String> skips = new ArrayList<>();

    public static void main(String[] args) {
        client = ExampleClientFactory.fromEnv();

        usage();
        configuration();
        authentication();
        readOnlyResources();
        dids();
        voiceInTrunks();
        voiceOutTrunks();
        smsTrunks();
        senderIdVerifications();
        ordersReservationsAndCapacity();
        identitiesAndAddresses();
        exports();
        emergencyAndDidHistory();
        prices();
        filtering();
        dirtyPatch();
        fileEncryption();
        webhook();
        errorHandling();
        dateFields();

        System.out.println("\n" + "=".repeat(60));
        if (!skips.isEmpty()) {
            System.out.println("Skipped (" + skips.size() + "):");
            skips.forEach(s -> System.out.println("  - " + s));
        }
        if (!failures.isEmpty()) {
            System.out.println("Failures:");
            failures.forEach(f -> System.out.println("  - " + f));
        }
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.out.println("=".repeat(60));
        System.exit(failed > 0 ? 1 : 0);
    }

    // ─── Usage ───────────────────────────────────────────────

    private static void usage() {
        section("Usage");
        check("Check balance, list DID groups with stock keeping units", cleanup -> {
            Balance balance = client.balance().find().getData();
            require(balance.getTotalBalance() != null, "the total balance is missing");
            print("Balance: " + balance.getTotalBalance());

            QueryParams params = QueryParams.builder()
                    .include("stock_keeping_units")
                    .filter("area_name", "Acapulco")
                    .build();
            List<DidGroup> didGroups = client.didGroups().list(params).getData();
            print("DID groups in Acapulco: " + didGroups.size());
        });
    }

    // ─── Configuration ───────────────────────────────────────

    private static void configuration() {
        section("Configuration");
        skip("Production client", "production environment: these checks run against the sandbox only");

        check("Sandbox client with read scope, logger and connect timeout", cleanup -> {
            DidwwClient sandbox = ExampleClientFactory.builder()
                    .scope(DidwwScope.READ)
                    .logger(System.getLogger("com.didww.sdk"))
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            print("Balance: " + sandbox.balance().find().getData().getTotalBalance());
        });

        // The README's proxy.example.com is replaced by DIDWW_README_PROXY_URL; the calls go to the sandbox
        check("HTTP client: own OkHttpClient.Builder with a proxy", cleanup -> {
            URI proxyUrl = URI.create(requireEnv("DIDWW_README_PROXY_URL"));
            int proxyPort = proxyUrl.getPort() != -1 ? proxyUrl.getPort() : 80;
            Set<Proxy> proxiesUsed = ConcurrentHashMap.newKeySet();

            OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyPort)))
                    .connectTimeout(Duration.ofSeconds(10))
                    .readTimeout(Duration.ofSeconds(60))
                    // Not in the README: records the proxy of each connection, to show the calls went through it
                    .eventListener(new EventListener() {
                        @Override
                        public void connectStart(Call call, InetSocketAddress address, Proxy proxy) {
                            proxiesUsed.add(proxy);
                        }
                    });
            DidwwClient proxied = ExampleClientFactory.builder()
                    .httpClientBuilder(httpBuilder)
                    .build();
            print("Balance through the proxy: " + proxied.balance().find().getData().getTotalBalance());
            require(!proxiesUsed.isEmpty() && proxiesUsed.stream().allMatch(p -> p.type() == Proxy.Type.HTTP),
                    "the calls did not go through the proxy: " + proxiesUsed);
        });
    }

    // ─── Authentication ──────────────────────────────────────

    private static void authentication() {
        section("Authentication");
        check("Scope: a read token reads, a write is refused with 403", cleanup -> {
            DidwwClient readOnly = ExampleClientFactory.builder()
                    .scope(DidwwScope.READ)
                    .build();
            print("Balance with a read token: " + readOnly.balance().find().getData().getTotalBalance());

            VoiceInTrunkGroup group = new VoiceInTrunkGroup();
            group.setName(uniqueName("README read scope"));
            try {
                String id = readOnly.voiceInTrunkGroups().create(group).getData().getId();
                cleanup.add("delete voice IN trunk group " + id, () -> client.voiceInTrunkGroups().delete(id));
                throw new IllegalStateException("a write with a read token was accepted");
            } catch (DidwwApiException e) {
                require(e.getHttpStatus() == 403, "expected 403, got " + e.getHttpStatus());
                print("Write refused: HTTP " + e.getHttpStatus());
            }
        });

        check("Token cache: own TokenStore, AccessToken JSON", cleanup -> {
            InMemoryTokenStore store = new InMemoryTokenStore();
            DidwwClient cached = ExampleClientFactory.builder()
                    .tokenStore(store)
                    .build();
            cached.balance().find();
            require(!store.keys().isEmpty(), "no token was stored");
            for (String key : store.keys()) {
                require(key.startsWith("didww:token:v1:"), "unexpected key format");
            }

            // A store can persist the token as JSON; toString() never shows the access token
            AccessToken token = store.get(store.keys().iterator().next());
            ObjectMapper mapper = new ObjectMapper();
            AccessToken restored = mapper.readValue(mapper.writeValueAsString(token), AccessToken.class);
            require(restored.getAccessToken().equals(token.getAccessToken()), "the token did not survive JSON");
            require(!restored.toString().contains(token.getAccessToken()), "toString() shows the access token");
            print("Stored " + store.keys().size() + " token(s): " + restored);
        });

        // The README's myRedisStore: two clients with their own store over one Redis share the token
        check("Token cache: a TokenStore over Redis, shared by two clients", cleanup -> {
            String redisUrl = requireEnv("DIDWW_README_REDIS_URL");
            String namespace = "didww-readme-java-" + SUFFIX + ":"; // keeps this run apart from others
            RedisTokenStore firstStore = new RedisTokenStore(redisUrl, namespace);
            cleanup.add("delete the tokens of this run from Redis", firstStore::deleteWritten);
            DidwwClient first = ExampleClientFactory.builder()
                    .tokenStore(firstStore)
                    .build();
            first.balance().find();
            require(!firstStore.written().isEmpty(), "the first client stored no token in Redis");

            RedisTokenStore secondStore = new RedisTokenStore(redisUrl, namespace);
            cleanup.add("delete the tokens the second client stored in Redis", secondStore::deleteWritten);
            for (Map.Entry<String, AccessToken> entry : firstStore.written().entrySet()) {
                AccessToken stored = secondStore.get(entry.getKey());
                require(stored != null && stored.getAccessToken().equals(entry.getValue().getAccessToken()),
                        "the token read back from Redis differs from the one stored");
            }
            DidwwClient second = ExampleClientFactory.builder()
                    .tokenStore(secondStore)
                    .build();
            second.balance().find();
            require(secondStore.written().isEmpty(), "the second client minted a token instead of reusing the stored one");
            print("The second client reused the token stored in Redis by the first");
        });

        check("Logging through System.Logger", cleanup -> {
            DidwwClient logged = ExampleClientFactory.builder()
                    .logger(System.getLogger("com.didww.sdk"))
                    .build();
            print("Balance: " + logged.balance().find().getData().getTotalBalance());
        });
    }

    // ─── Read-only resources ─────────────────────────────────

    private static void readOnlyResources() {
        section("Read-only resources");
        check("Countries: list and find", cleanup -> {
            List<Country> countries = client.countries().list(firstPage(5)).getData();
            require(!countries.isEmpty(), "no countries");
            Country country = client.countries().find(countries.get(0).getId()).getData();
            require(country.getId().equals(countries.get(0).getId()), "find returned another country");
            print("Found " + country.getName());
        });
        check("Regions", cleanup -> listed("regions", client.regions().list(firstPage(5)).getData()));
        check("Cities", cleanup -> listed("cities", client.cities().list(firstPage(5)).getData()));
        check("Areas", cleanup -> listed("areas", client.areas().list(firstPage(5)).getData()));
        check("NANPA prefixes", cleanup -> listed("NANPA prefixes", client.nanpaPrefixes().list(firstPage(5)).getData()));
        check("DID groups: toll-free, with stock keeping units", cleanup -> {
            QueryParams dgParams = QueryParams.builder()
                    .include("stock_keeping_units")
                    .filter("number_type", "toll_free")
                    .page(1, 5)
                    .build();
            List<DidGroup> groups = client.didGroups().list(dgParams).getData();
            for (DidGroup group : groups) {
                require(group.getNumberType() == NumberType.TOLL_FREE, "not toll-free: " + group.getId());
                print(group.getPrefix() + " " + group.getAreaName() + ", demo=" + group.getIsDemo()
                        + ", " + sizeOf(group.getStockKeepingUnits()) + " SKU(s)");
            }
        });
        check("Available DIDs with DID group and stock keeping units", cleanup -> {
            QueryParams adParams = QueryParams.builder()
                    .include("did_group.stock_keeping_units")
                    .page(1, 3)
                    .build();
            for (AvailableDid available : client.availableDids().list(adParams).getData()) {
                require(available.getDidGroup() != null, "the DID group was not included");
                print(available.getNumber() + ", " + sizeOf(available.getDidGroup().getStockKeepingUnits()) + " SKU(s)");
            }
        });
        check("Proof types", cleanup -> listed("proof types", client.proofTypes().list().getData()));
        check("Public keys", cleanup -> {
            List<PublicKey> publicKeys = client.publicKeys().list().getData();
            require(!publicKeys.isEmpty(), "no public keys");
            listed("public keys", publicKeys);
        });
        check("Address requirements", cleanup ->
                listed("address requirements", client.addressRequirements().list(firstPage(5)).getData()));
        check("Supporting document templates", cleanup ->
                listed("supporting document templates", client.supportingDocumentTemplates().list().getData()));
        check("Balance", cleanup -> {
            Balance balance = client.balance().find().getData();
            print("Total " + balance.getTotalBalance() + ", balance " + balance.getBalance()
                    + ", credit " + balance.getCredit());
        });
    }

    // ─── DIDs ────────────────────────────────────────────────

    private static void dids() {
        section("DIDs");
        check("DIDs: list, update description, capacity limit and trunk (restored)", cleanup -> {
            List<Did> dids = client.dids().list(firstPage(5)).getData();
            print(dids.size() + " DID(s) on the first page");
            Did original = firstDid();
            VoiceInTrunk trunk = new VoiceInTrunk().withId(createSipTrunk(cleanup, "README DID update"));
            restoreDidAfterwards(cleanup, original,
                    EnumSet.of(DidField.DESCRIPTION, DidField.CAPACITY_LIMIT, DidField.VOICE_ROUTING));

            Did did = client.dids().find(original.getId()).getData();
            did.setDescription("Updated");
            did.setCapacityLimit(20);
            did.setVoiceInTrunk(trunk);
            client.dids().update(did);

            Did updated = findDidWithRouting(original.getId());
            require("Updated".equals(updated.getDescription()), "the description was not updated");
            require(Integer.valueOf(20).equals(updated.getCapacityLimit()), "the capacity limit was not updated");
            require(trunk.getId().equals(idOf(updated.getVoiceInTrunk())), "the trunk was not assigned");
            print("Updated DID " + updated.getNumber());
        });

        check("SMS routing: SMS trunk, SMS trunk group, unroute (restored)", cleanup -> {
            Did original = firstDid();
            String didId = original.getId();
            String trunkId = createHttpInSmsTrunk(cleanup, "README SMS routing");
            String memberId = createHttpInSmsTrunk(cleanup, "README SMS routing member");
            SmsTrunkGroup group = new SmsTrunkGroup();
            group.setName(uniqueName("README SMS routing group"));
            group.setSmsTrunks(Collections.singletonList(new SmsTrunk().withId(memberId)));
            String groupId = client.smsTrunkGroups().create(group).getData().getId();
            cleanup.add("delete SMS trunk group " + groupId, () -> client.smsTrunkGroups().delete(groupId));
            restoreDidAfterwards(cleanup, original, EnumSet.of(DidField.SMS_ROUTING));

            // sends sms_trunk and sms_trunk_group: null
            Did route = new Did().withId(didId);
            route.setSmsTrunk(new SmsTrunk().withId(trunkId));
            client.dids().update(route);

            List<Did> routed = client.dids().list(QueryParams.builder()
                    .filter("sms_trunk.id", trunkId)
                    .include("sms_trunk", "sms_trunk_group")
                    .build()).getData();
            require(routed.stream().anyMatch(d -> didId.equals(d.getId())), "the DID is not routed to the SMS trunk");

            // sends sms_trunk_group and sms_trunk: null
            Did toGroup = new Did().withId(didId);
            toGroup.setSmsTrunkGroup(new SmsTrunkGroup().withId(groupId));
            client.dids().update(toGroup);
            Did grouped = findDidWithRouting(didId);
            require(grouped.getSmsTrunk() == null && groupId.equals(idOf(grouped.getSmsTrunkGroup())),
                    "the DID is not routed to the SMS trunk group only");

            // unroute: sends sms_trunk: null and sms_trunk_group: null
            Did unroute = new Did().withId(didId);
            unroute.setSmsTrunk(null);
            unroute.setSmsTrunkGroup(null);
            client.dids().update(unroute);
            Did unrouted = findDidWithRouting(didId);
            require(unrouted.getSmsTrunk() == null && unrouted.getSmsTrunkGroup() == null, "the DID is still routed");
        });
    }

    // ─── Voice IN trunks and trunk groups ────────────────────

    private static void voiceInTrunks() {
        section("Voice IN trunks");
        check("Voice IN trunks: create a SIP trunk, update and delete it", cleanup -> {
            VoiceInTrunk trunk = new VoiceInTrunk();
            trunk.setName(uniqueName("README SIP Trunk"));
            trunk.setPriority(1);
            trunk.setWeight(100);
            trunk.setCliFormat(CliFormat.E164);
            trunk.setRingingTimeout(30);
            trunk.setPop(Pop.FRA);

            SipConfiguration sip = new SipConfiguration();
            sip.setHost("sip.example.com");
            sip.setPort(5060);
            sip.setCodecs(Arrays.asList(Codec.PCMA, Codec.PCMU, Codec.TELEPHONE_EVENT));
            sip.setIceMode(IceMode.ACCEPT);
            sip.setTransportProtocolId(TransportProtocol.UDP);
            trunk.setConfiguration(sip);

            VoiceInTrunk created = client.voiceInTrunks().create(trunk).getData();
            cleanup.add("delete voice IN trunk " + created.getId(), () -> client.voiceInTrunks().delete(created.getId()));
            require(created.getPop() == Pop.FRA, "pop is " + created.getPop());

            created.setDescription("Updated");
            VoiceInTrunk updated = client.voiceInTrunks().update(created).getData();
            require("Updated".equals(updated.getDescription()), "the description was not updated");
        });

        check("Voice IN trunks: change the point of presence", cleanup -> {
            String trunkId = createSipTrunk(cleanup, "README POP");
            VoiceInTrunk popUpdate = new VoiceInTrunk().withId(trunkId);
            popUpdate.setPop(Pop.AMS);
            VoiceInTrunk updated = client.voiceInTrunks().update(popUpdate).getData();
            require(updated.getPop() == Pop.AMS, "pop is " + updated.getPop());
        });

        check("SIP registration: create with registration, disable it by setting the host", cleanup -> {
            SipConfiguration sip = new SipConfiguration();
            sip.setEnabledSipRegistration(true);
            sip.setUseDidInRuri(true);
            sip.setCnamLookup(true);

            VoiceInTrunk trunk = new VoiceInTrunk();
            trunk.setName(uniqueName("README Office (registered)"));
            trunk.setConfiguration(sip);
            VoiceInTrunk createdTrunk = client.voiceInTrunks().create(trunk).getData();
            String trunkId = createdTrunk.getId();
            cleanup.add("delete voice IN trunk " + trunkId, () -> client.voiceInTrunks().delete(trunkId));
            SipConfiguration created = (SipConfiguration) createdTrunk.getConfiguration();
            require(created.getIncomingAuthUsername() != null && created.getIncomingAuthPassword() != null,
                    "the registration credentials were not generated");
            print("Registration credentials generated (not printed)");

            SipConfiguration disable = new SipConfiguration();
            disable.setHost("sip.example.com");
            VoiceInTrunk update = new VoiceInTrunk().withId(trunkId);
            update.setConfiguration(disable);
            VoiceInTrunk disabled = client.voiceInTrunks().update(update).getData();
            SipConfiguration disabledSip = (SipConfiguration) disabled.getConfiguration();
            require(!Boolean.TRUE.equals(disabledSip.getEnabledSipRegistration()), "SIP registration is still enabled");
        });

        check("Voice IN trunk groups: create", cleanup -> {
            VoiceInTrunkGroup group = new VoiceInTrunkGroup();
            group.setName(uniqueName("README Primary Group"));
            group.setCapacityLimit(50);
            VoiceInTrunkGroup created = client.voiceInTrunkGroups().create(group).getData();
            cleanup.add("delete voice IN trunk group " + created.getId(),
                    () -> client.voiceInTrunkGroups().delete(created.getId()));
            require(Integer.valueOf(50).equals(created.getCapacityLimit()), "capacity limit is " + created.getCapacityLimit());
        });
    }

    // ─── Voice OUT trunks ────────────────────────────────────

    private static void voiceOutTrunks() {
        section("Voice OUT trunks (need account configuration)");
        check("Voice OUT trunks: create with credentials and IP authentication", cleanup -> {
            // Without the account configuration the API answers every Voice OUT trunk request with 403,
            // a list included; any error of the create below still fails the check
            try {
                client.voiceOutTrunks().list(firstPage(1));
            } catch (DidwwApiException e) {
                if (e.getHttpStatus() != 403) {
                    throw e;
                }
                throw new Skipped("Voice OUT trunks are not enabled for the account (HTTP 403)");
            }

            // 203.0.113.0/24 is documentation address space
            CredentialsAndIpAuthenticationMethod auth = new CredentialsAndIpAuthenticationMethod();
            auth.setAllowedSipIps(Collections.singletonList("203.0.113.0/24"));

            VoiceOutTrunk voTrunk = new VoiceOutTrunk();
            voTrunk.setName(uniqueName("README Outbound Trunk"));
            voTrunk.setAuthenticationMethod(auth);
            voTrunk.setDefaultDstAction(DefaultDstAction.ALLOW_ALL);
            voTrunk.setOnCliMismatchAction(OnCliMismatchAction.REJECT_CALL);
            voTrunk.setCodecs(Arrays.asList(VoiceOutCodec.OPUS, VoiceOutCodec.PCMA, VoiceOutCodec.TELEPHONE_EVENT));
            VoiceOutTrunk created = client.voiceOutTrunks().create(voTrunk).getData();
            cleanup.add("delete voice OUT trunk " + created.getId(), () -> client.voiceOutTrunks().delete(created.getId()));

            require(created.getAuthenticationMethod() instanceof CredentialsAndIpAuthenticationMethod,
                    "unexpected authentication method " + created.getAuthenticationMethod());
            CredentialsAndIpAuthenticationMethod createdAuth = (CredentialsAndIpAuthenticationMethod) created.getAuthenticationMethod();
            require(createdAuth.getUsername() != null && createdAuth.getPassword() != null,
                    "the credentials were not generated");
            require(Arrays.asList(VoiceOutCodec.OPUS, VoiceOutCodec.PCMA, VoiceOutCodec.TELEPHONE_EVENT)
                    .equals(created.getCodecs()), "codecs are " + created.getCodecs());
        });
    }

    // ─── SMS trunks and SMS trunk groups ─────────────────────

    private static void smsTrunks() {
        section("SMS trunks");
        check("SMS trunks: create HTTP IN, update the URL, clear attributes, list by type", cleanup -> {
            HttpInConfiguration httpIn = new HttpInConfiguration();
            httpIn.setUrl("https://example.com/sms");
            httpIn.setHttpMethod(SmsHttpMethod.POST);
            httpIn.setBodyType(SmsBodyType.JSON);
            httpIn.setBody("{\"text\":\"{SMS_TEXT}\",\"from\":\"{SMS_SRC_ADDR}\",\"to\":\"{SMS_DST_ADDR}\"}");

            SmsTrunk trunk = new SmsTrunk();
            trunk.setName(uniqueName("README Inbound webhook"));
            trunk.setConfiguration(httpIn);
            SmsTrunk created = client.smsTrunks().create(trunk).getData();
            String trunkId = created.getId();
            cleanup.add("delete SMS trunk " + trunkId, () -> client.smsTrunks().delete(trunkId));

            // On update the configuration type must stay the same; attributes left out keep their values
            HttpInConfiguration newUrl = new HttpInConfiguration();
            newUrl.setUrl("https://example.com/sms/v2");
            SmsTrunk update = new SmsTrunk().withId(trunkId);
            update.setConfiguration(newUrl);
            client.smsTrunks().update(update);

            // Nullable attributes are cleared with a generic configuration of the same type
            ObjectNode attributes = JsonNodeFactory.instance.objectNode();
            attributes.put("http_method", "get");
            attributes.putNull("body_type");
            attributes.putNull("body");
            SmsTrunk clear = new SmsTrunk().withId(trunkId);
            clear.setConfiguration(new GenericSmsTrunkConfiguration("http_in", attributes));
            SmsTrunkConfiguration cleared = client.smsTrunks().update(clear).getData().getConfiguration();
            require(cleared instanceof HttpInConfiguration, "unexpected configuration " + cleared);
            HttpInConfiguration result = (HttpInConfiguration) cleared;
            require("https://example.com/sms/v2".equals(result.getUrl()), "the URL is " + result.getUrl());
            require(result.getHttpMethod() == SmsHttpMethod.GET && result.getBody() == null,
                    "the body attributes were not cleared");

            List<SmsTrunk> trunks = client.smsTrunks().list(QueryParams.builder()
                    .filter("configuration.type", "http_in,sms_to_email")
                    .include("sms_trunk_group")
                    .build()).getData();
            for (SmsTrunk listed : trunks) {
                String type = listed.getConfiguration() != null ? listed.getConfiguration().getType() : null;
                require("http_in".equals(type) || "sms_to_email".equals(type), "unexpected type " + type);
            }
            print(trunks.size() + " http_in and sms_to_email trunk(s)");
        });

        check("SMS trunks: regenerate the credentials of an HTTP OUT trunk", cleanup -> {
            String outTrunkId = createHttpOutSmsTrunk(cleanup, "README HTTP OUT");
            SmsTrunkRegenerateCredential regenerate = new SmsTrunkRegenerateCredential();
            regenerate.setSmsTrunk(new SmsTrunk().withId(outTrunkId));
            client.smsTrunkRegenerateCredentials().create(regenerate);

            // The response carries no credentials: read the trunk again
            SmsTrunk outTrunk = client.smsTrunks().find(outTrunkId).getData();
            SmsTrunkConfiguration configuration = outTrunk.getConfiguration();
            require(configuration instanceof HttpOutConfiguration, "unexpected configuration " + configuration);
            HttpOutConfiguration http = (HttpOutConfiguration) configuration;
            require(http.getAuthName() != null && http.getAuthPassword() != null, "no credentials");
            print("Credentials regenerated (not printed); submit URL: " + outTrunk.getMetaHost());
        });

        check("SMS trunk groups: create, add a trunk, remove every trunk", cleanup -> {
            String firstId = createHttpInSmsTrunk(cleanup, "README SMS group member");
            String secondId = createHttpInSmsTrunk(cleanup, "README SMS group member");

            SmsTrunk member = new SmsTrunk().withId(firstId);
            SmsTrunkGroup group = new SmsTrunkGroup();
            group.setName(uniqueName("README Inbound SMS"));
            group.setSmsTrunks(Collections.singletonList(member));
            SmsTrunkGroup created = client.smsTrunkGroups().create(group).getData();
            String groupId = created.getId();
            // Deleting a group also deletes the trunks still in it
            cleanup.add("delete SMS trunk group " + groupId, () -> client.smsTrunkGroups().delete(groupId));
            print("Created with " + created.getMetaSmsTrunksCount() + " trunk(s)");
            require(smsGroupMembers(groupId).size() == 1, "the group does not hold one trunk");

            // setSmsTrunks replaces the membership: build it from the included sms_trunks
            SmsTrunkGroup current = client.smsTrunkGroups().find(groupId,
                    QueryParams.builder().include("sms_trunks").build()).getData();
            List<SmsTrunk> members = new ArrayList<>();
            if (current.getSmsTrunks() != null) {
                members.addAll(current.getSmsTrunks());
            }
            members.add(new SmsTrunk().withId(secondId));
            SmsTrunkGroup update = new SmsTrunkGroup().withId(groupId);
            update.setSmsTrunks(members);
            client.smsTrunkGroups().update(update);
            require(smsGroupMembers(groupId).size() == 2, "the group does not hold two trunks");

            // An empty list removes every trunk from the group
            SmsTrunkGroup empty = new SmsTrunkGroup().withId(groupId);
            empty.setSmsTrunks(Collections.emptyList());
            client.smsTrunkGroups().update(empty);
            require(smsGroupMembers(groupId).isEmpty(), "the group still holds trunks");
        });
    }

    // ─── Sender ID verifications ─────────────────────────────

    private static void senderIdVerifications() {
        section("Sender ID verifications");
        check("Sender ID verifications: list active ones with their SMS trunk", cleanup -> {
            for (SenderIdVerification v : activeVerifications()) {
                require(v.isActive(), v.getReference() + " is not active");
                String trunk = v.getSmsTrunk() != null ? v.getSmsTrunk().getName() : "no SMS trunk";
                print(v.getReference() + " " + v.getName() + " " + v.getSenderIds() + " via " + trunk);
            }
        });

        check("Sender ID verifications: route through an SMS trunk, unroute (restored)", cleanup -> {
            List<SenderIdVerification> verifications = activeVerifications();
            if (verifications.isEmpty()) {
                throw new Skipped("no active Sender ID verification on the account");
            }
            SenderIdVerification verification = verifications.get(0);
            String id = verification.getId();
            String originalTrunkId = idOf(verification.getSmsTrunk());
            String trunkId = createHttpOutSmsTrunk(cleanup, "README Sender ID");
            cleanup.add("restore the SMS trunk of " + verification.getReference(), () -> {
                SenderIdVerification restore = new SenderIdVerification().withId(id);
                restore.setSmsTrunk(originalTrunkId != null ? new SmsTrunk().withId(originalTrunkId) : null);
                client.senderIdVerifications().update(restore);
            });

            // http_out, smpp_esme or smpp_smsc trunks only
            SenderIdVerification route = new SenderIdVerification().withId(id);
            route.setSmsTrunk(new SmsTrunk().withId(trunkId));
            client.senderIdVerifications().update(route);
            require(trunkId.equals(idOf(findVerification(id).getSmsTrunk())), "the trunk was not assigned");

            // Outbound messages stop until a trunk is assigned again
            SenderIdVerification unroute = new SenderIdVerification().withId(id);
            unroute.setSmsTrunk(null);
            client.senderIdVerifications().update(unroute);
            require(findVerification(id).getSmsTrunk() == null, "the trunk is still assigned");
        });
    }

    // ─── Orders, DID reservations, shared capacity groups ────

    private static void ordersReservationsAndCapacity() {
        section("Orders, DID reservations, shared capacity groups");
        check("Orders: order by SKU, build the other item types", cleanup -> {
            DidOrderItem item = new DidOrderItem();
            item.setSkuId(findSkuId());
            item.setQty(1); // the README orders 2
            Order order = new Order();
            order.setItems(Arrays.asList(item));
            Order created = client.orders().create(order).getData();
            print("Order " + created.getId() + ": " + created.getStatus());
            cancelIfPending(cleanup, created);

            // The other item types; each order holds items of one type (DID items, or capacity items)
            AvailableDidOrderItem availableItem = new AvailableDidOrderItem();
            availableItem.setAvailableDidId("available-did-uuid");
            availableItem.setSkuId("sku-uuid");
            ReservationDidOrderItem reservationItem = new ReservationDidOrderItem();
            reservationItem.setDidReservationId("reservation-uuid");
            reservationItem.setSkuId("sku-uuid");
            CapacityOrderItem capacityItem = new CapacityOrderItem();
            capacityItem.setCapacityPoolId("pool-uuid");
            capacityItem.setQty(1);
            require("did_order_items".equals(availableItem.getType())
                    && "did_order_items".equals(reservationItem.getType())
                    && "capacity_order_items".equals(capacityItem.getType()), "unexpected order item types");
        });

        check("DID reservations: reserve an available DID, delete the reservation", cleanup -> {
            List<AvailableDid> available = client.availableDids().list(firstPage(1)).getData();
            if (available.isEmpty()) {
                throw new Skipped("no available DID");
            }
            DidReservation reservation = new DidReservation();
            reservation.setDescription("Reserved for client");
            reservation.setAvailableDid(available.get(0));
            DidReservation created = client.didReservations().create(reservation).getData();
            cleanup.add("delete DID reservation " + created.getId(), () -> client.didReservations().delete(created.getId()));
            print("Reserved " + available.get(0).getNumber() + " until " + created.getExpiresAt());
        });

        check("Shared capacity groups: create in a capacity pool", cleanup -> {
            List<CapacityPool> pools = client.capacityPools().list(firstPage(1)).getData();
            if (pools.isEmpty()) {
                throw new Skipped("no capacity pool on the account");
            }
            SharedCapacityGroup scg = new SharedCapacityGroup();
            scg.setName(uniqueName("README Shared Group"));
            scg.setSharedChannelsCount(1); // the README asks for 20; the sandbox pool may have fewer channels
            scg.setCapacityPool(pools.get(0));
            SharedCapacityGroup created = client.sharedCapacityGroups().create(scg).getData();
            cleanup.add("delete shared capacity group " + created.getId(),
                    () -> client.sharedCapacityGroups().delete(created.getId()));
            print("Created " + created.getName() + " in " + pools.get(0).getName());
        });
    }

    // ─── Identities, addresses, address verifications ────────

    private static void identitiesAndAddresses() {
        section("Identities and addresses");
        check("Identities and addresses: create a personal identity and its address", cleanup -> {
            Country country = country("US");
            Identity identity = new Identity();
            identity.setFirstName("John");
            identity.setLastName("Doe");
            identity.setPhoneNumber("12125551234");
            identity.setIdentityType(IdentityType.PERSONAL);
            identity.setCountry(country);
            Identity createdIdentity = client.identities().create(identity).getData();
            cleanup.add("delete identity " + createdIdentity.getId(),
                    () -> client.identities().delete(createdIdentity.getId()));

            Address address = new Address();
            address.setCityName("New York");
            address.setPostalCode("10001");
            address.setAddress("123 Main St");
            address.setIdentity(createdIdentity);
            address.setCountry(country);
            Address createdAddress = client.addresses().create(address).getData();
            cleanup.add("delete address " + createdAddress.getId(),
                    () -> client.addresses().delete(createdAddress.getId()));
            print("Identity " + createdIdentity.getId() + ", address " + createdAddress.getId());
        });

        // A verification needs a DID of a group with an address requirement (some Ukrainian local groups,
        // such as Kiev, have one) and an identity and address that meet that requirement.
        check("Address verifications: create for a Ukrainian DID, update its external reference", cleanup -> {
            Did did = didWithAddressRequirement();
            DidGroup group = client.didGroups().find(did.getDidGroup().getId(),
                    QueryParams.builder().include("country", "address_requirement").build()).getData();
            AddressRequirement requirement = addressRequirement(group.getAddressRequirement().getId());
            print("DID " + did.getNumber() + " in " + group.getAreaName() + ", requirement " + requirement.getId());

            // Once the verification exists the API refuses to delete its address and identity (409).
            // The files of the proofs are not deleted either: the proofs hold them until they expire.
            AtomicBoolean verificationCreated = new AtomicBoolean(false);
            ProofFiles files = new ProofFiles(cleanup);
            Identity identity = identityFor(requirement, group.getCountry(), files, cleanup, verificationCreated);
            Address address = addressFor(requirement, identity, group, files, cleanup, verificationCreated);

            AddressRequirementValidation validation = new AddressRequirementValidation();
            validation.setAddressRequirement(new AddressRequirement().withId(requirement.getId()));
            validation.setAddress(new Address().withId(address.getId()));
            validation.setIdentity(new Identity().withId(identity.getId()));
            client.addressRequirementValidations().create(validation); // 422 with the reasons when not met

            AddressVerification verification = new AddressVerification();
            verification.setCallbackUrl("http://example.com/callback");
            verification.setCallbackMethod(CallbackMethod.GET);
            verification.setAddress(new Address().withId(address.getId()));
            verification.setDids(Collections.singletonList(new Did().withId(did.getId())));
            if (Boolean.TRUE.equals(requirement.getServiceDescriptionRequired())) {
                verification.setServiceDescription("README check of the DIDWW Java SDK");
            }
            AddressVerification created = client.addressVerifications().create(verification).getData();
            verificationCreated.set(true);
            System.out.println("REVIEW address_verification " + created.getId() + " " + created.getReference());

            AddressVerification update = new AddressVerification().withId(created.getId());
            update.setExternalReferenceId("readme-java-" + SUFFIX);
            AddressVerification updated = client.addressVerifications().update(update).getData();
            require(("readme-java-" + SUFFIX).equals(updated.getExternalReferenceId()),
                    "the external reference was not updated");
            print("Status " + updated.getStatus() + "; the DID, identity and address stay with the verification");
        });
    }

    // ─── Exports ─────────────────────────────────────────────

    private static void exports() {
        section("Exports (cannot be deleted)");
        check("Exports: create CDR IN and SMS OUT exports, read one back", cleanup -> {
            // Dates are "YYYY-MM-DD HH:MM:SS" in UTC: from is inclusive, to is exclusive and not in the future
            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            String from = today.minusDays(1) + " 00:00:00";
            String to = today + " 00:00:00";

            Map<String, Object> cdrFilters = new HashMap<>();
            cdrFilters.put("from", from);
            cdrFilters.put("to", to);
            Export export = new Export();
            export.setExportType(ExportType.CDR_IN);
            export.setFilters(cdrFilters);
            Export created = client.exports().create(export).getData();
            print("CDR IN export " + created.getId() + ": " + created.getStatus());

            Map<String, Object> smsFilters = new HashMap<>();
            smsFilters.put("from", from);
            smsFilters.put("to", to);
            smsFilters.put("service_type", ExportServiceType.A2P); // sent as "a2p"
            // The README also filters by SMS trunk; this check does so when the account has a sending one
            String sendingTrunkId = sendingSmsTrunkId();
            if (sendingTrunkId != null) {
                smsFilters.put("sms_trunk.id", Arrays.asList(sendingTrunkId));
            }
            Export smsOut = new Export();
            smsOut.setExportType(ExportType.SMS_OUT);
            smsOut.setFilters(smsFilters);
            Export createdSms = client.exports().create(smsOut).getData();

            // The export is pending until its file is generated; read it again later
            Export again = client.exports().find(createdSms.getId()).getData();
            require(again.getExportType() == ExportType.SMS_OUT, "unexpected export type " + again.getExportType());
            print("SMS OUT export " + again.getId() + ": " + again.getStatus());
        });

        check("Exports: download a completed export", cleanup -> {
            List<Export> done = client.exports().list(QueryParams.builder()
                    .filter("status", "completed")
                    .page(1, 1)
                    .build()).getData();
            if (done.isEmpty()) {
                throw new Skipped("no completed export on the account yet");
            }
            Export completed = done.get(0);
            require(completed.isCompleted(), "export " + completed.getId() + " is " + completed.getStatus());

            Path dir = privateTempDirectory("didww-readme-export-", cleanup);
            client.downloadExport(completed.getUrl(), dir.resolve("export.csv.gz"));           // gzipped CSV, as served
            client.downloadAndDecompressExport(completed.getUrl(), dir.resolve("export.csv")); // plain CSV
            try (OutputStream out = Files.newOutputStream(dir.resolve("copy.csv.gz"))) {
                client.downloadExport(completed.getUrl(), out);                                // any OutputStream
            }
            print("Downloaded export " + completed.getId() + ": " + Files.size(dir.resolve("export.csv.gz"))
                    + " bytes gzipped, " + Files.size(dir.resolve("export.csv")) + " bytes plain");
        });
    }

    // ─── Emergency services, DID history ─────────────────────

    private static void emergencyAndDidHistory() {
        section("Emergency services and DID history");
        check("Emergency services: list requirements and calling services", cleanup -> {
            List<EmergencyRequirement> emergReqs = client.emergencyRequirements().list(firstPage(5)).getData();
            List<EmergencyCallingService> emergServices = client.emergencyCallingServices().list(firstPage(5)).getData();
            print(emergReqs.size() + " requirement(s), " + emergServices.size() + " calling service(s)");
        });

        // As EmergencyScenarioExample: an emergency-capable Ukrainian DID without an emergency calling service,
        // and an address of the account that meets the emergency requirement of the DID's number type
        check("Emergency services: create an emergency verification", cleanup -> {
            Did did = emergencyDid();
            DidGroup group = client.didGroups().find(did.getDidGroup().getId(),
                    QueryParams.builder().include("country").build()).getData();
            List<EmergencyRequirement> requirements = client.emergencyRequirements().list(QueryParams.builder()
                    .filter("country.id", group.getCountry().getId())
                    .filter("number_type", EnumWire.value(group.getNumberType()))
                    .build()).getData();
            require(!requirements.isEmpty(), "no emergency requirement for " + group.getCountry().getName());
            Address address = emergencyAddress(requirements.get(0), group.getCountry());
            print("DID " + did.getNumber() + ", address " + address.getId());

            EmergencyVerification emergVerification = new EmergencyVerification();
            emergVerification.setCallbackUrl("https://example.com/callback");
            emergVerification.setCallbackMethod("post");
            emergVerification.setAddress(address);
            emergVerification.setDids(Arrays.asList(did));
            EmergencyVerification created = client.emergencyVerifications().create(emergVerification).getData();
            System.out.println("REVIEW emergency_verification " + created.getId() + " " + created.getReference());
            print("Status " + created.getStatus() + "; the verification and its DID stay on the account");
        });

        check("DID history: list", cleanup -> {
            List<DidHistory> history = client.didHistory().list(firstPage(5)).getData();
            for (DidHistory entry : history) {
                print(entry.getDidNumber() + " " + entry.getAction() + " " + entry.getCreatedAt());
            }
        });
    }

    // ─── Prices ──────────────────────────────────────────────

    private static void prices() {
        section("Prices");
        check("Price lookup: Voice IN", cleanup -> {
            PriceLookup<VoiceInPrice> result = priced(() -> client.voiceInPrices().lookup(Map.of(
                    "number", "18005550123",
                    "src_country_iso", "US")));
            print(result.getTotalRecords() + " matching price(s)");
            for (VoiceInPrice price : result.getRows()) {
                if (price.getRate() != null) {
                    print(price.getDestination() + " " + price.getSourceType() + ": " + price.getRate() + " USD/min");
                }
            }
        });
        check("Price lookup: Voice OUT", cleanup -> {
            PriceLookup<VoiceOutPrice> voiceOut = priced(() -> client.voiceOutPrices().lookup(Map.of("number", "493012345678")));
            print("Category: " + voiceOut.getRows().get(0).getCategory());
        });
        check("Price lookup: SMS OUT A2P", cleanup -> {
            PriceLookup<SmsOutPrice> a2p = priced(() -> client.smsOutA2pPrices().lookup(Map.of("dst_number", "4915123456789")));
            print(a2p.getTotalRecords() + " matching price(s), first " + a2p.getRows().get(0).getPrice() + " USD");
        });
        check("Price lookup: SMS IN", cleanup -> {
            PriceLookup<SmsInPrice> smsIn = priced(() -> client.smsInPrices().lookup(Map.of("dst_country_iso", "US")));
            print(smsIn.getTotalRecords() + " matching price(s)");
        });

        check("Price files: fetch, read rows, head, download", cleanup -> {
            PricesService<VoiceInPrice> prices = client.voiceInPrices();
            Path dir = privateTempDirectory("didww-readme-prices-", cleanup);
            Path path = dir.resolve("voice_in_prices.csv.gz");

            // Download unless the copy at the path is current
            PriceFile file = priced(() -> prices.fetch(path));
            print(file.isNotModified()
                    ? "answered 304, the file was kept"
                    : "downloaded " + file.getBytesWritten() + " bytes, last modified " + file.getLastModified());

            // Read the rows, keyed by the header line
            try (Stream<Map<String, String>> rows = PriceFileReader.rows(path)) {
                rows.limit(5).forEach(row -> print(row.get("Destination") + ": " + row.get("Rate")));
            }

            // Lower-level calls
            PriceFile status = prices.head(file.getLastModified());
            print("HEAD: not modified " + status.isNotModified() + ", file name " + status.getFileName());
            PriceFile again = prices.downloadToFile(path);
            require(again.getBytesWritten() > 0, "downloadToFile wrote nothing");
            try (OutputStream out = Files.newOutputStream(dir.resolve("copy.csv.gz"))) {
                PriceFile copy = prices.download(out);
                require(copy.getBytesWritten() > 0, "download wrote nothing");
            }
        });
    }

    // ─── Filtering, sorting, pagination ──────────────────────

    private static void filtering() {
        section("Filtering, sorting and pagination");
        check("Regions filtered by country and name, with country, sorted, paged", cleanup -> {
            QueryParams params = QueryParams.builder()
                    .filter("country.id", country("US").getId())
                    .filter("name", "Arizona")
                    .include("country")
                    .sort("name")
                    .page(1, 25)
                    .build();
            List<Region> regions = client.regions().list(params).getData();
            require(!regions.isEmpty(), "Arizona was not found");
            require(regions.get(0).getCountry() != null, "the country was not included");
            print(regions.get(0).getName() + ", " + regions.get(0).getCountry().getName());
        });
    }

    // ─── Dirty PATCH serialization ───────────────────────────

    private static void dirtyPatch() {
        section("Dirty PATCH serialization");
        check("Dirty PATCH: update a fetched DID (restored)", cleanup -> {
            Did original = firstDid();
            restoreDidAfterwards(cleanup, original, EnumSet.of(DidField.DESCRIPTION));
            Did did = client.dids().find(original.getId()).getData();
            did.setDescription("Updated description");
            Did updated = client.dids().update(did).getData();
            require("Updated description".equals(updated.getDescription()), "the description was not updated");
        });

        check("Dirty PATCH: build a resource for update with withId", cleanup -> {
            String trunkId = createSipTrunk(cleanup, "README withId");
            String newName = uniqueName("README New name");
            VoiceInTrunk trunk = new VoiceInTrunk().withId(trunkId);
            trunk.setName(newName);
            VoiceInTrunk updated = client.voiceInTrunks().update(trunk).getData();
            require(newName.equals(updated.getName()), "the name was not updated");
        });

        check("Dirty PATCH: clear a field with explicit null (restored)", cleanup -> {
            Did original = firstDid();
            restoreDidAfterwards(cleanup, original, EnumSet.of(DidField.DESCRIPTION));
            Did did = new Did().withId(original.getId());
            did.setDescription(null);
            Did updated = client.dids().update(did).getData();
            require(updated.getDescription() == null, "the description was not cleared");
        });

        check("Dirty PATCH: clear a relationship (restored)", cleanup -> {
            Did original = firstDid();
            String trunkId = createSipTrunk(cleanup, "README clear relationship");
            restoreDidAfterwards(cleanup, original, EnumSet.of(DidField.VOICE_ROUTING));
            Did assign = new Did().withId(original.getId());
            assign.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
            client.dids().update(assign);

            Did did = new Did().withId(original.getId());
            did.setVoiceInTrunk(null);
            client.dids().update(did);
            require(findDidWithRouting(original.getId()).getVoiceInTrunk() == null, "the trunk is still assigned");
        });

        check("Dirty PATCH: update an included resource", cleanup -> {
            Did original = firstDid();
            String trunkId = createSipTrunk(cleanup, "README included trunk");
            restoreDidAfterwards(cleanup, original, EnumSet.of(DidField.VOICE_ROUTING));
            Did assign = new Did().withId(original.getId());
            assign.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
            client.dids().update(assign);

            Did did = client.dids().find(original.getId(), QueryParams.builder().include("voice_in_trunk").build()).getData();
            VoiceInTrunk trunk = did.getVoiceInTrunk();
            require(trunk != null && trunkId.equals(trunk.getId()), "the trunk was not included");
            trunk.setDescription("Updated via include");
            VoiceInTrunk updated = client.voiceInTrunks().update(trunk).getData();
            require("Updated via include".equals(updated.getDescription()), "the description was not updated");
        });
    }

    // ─── File encryption ─────────────────────────────────────

    private static void fileEncryption() {
        section("File encryption");
        check("File encryption: Encrypt instance and static methods", cleanup -> {
            byte[] fileBytes = "README encryption check".getBytes(StandardCharsets.UTF_8);

            // Instance-based: fetches public keys from the API
            Encrypt enc = new Encrypt(client);
            String fingerprint = enc.getFingerprint();
            byte[] encrypted = enc.encrypt(fileBytes);

            // Static: provide your own public keys
            String[] publicKeyPems = client.publicKeys().list().getData().stream()
                    .map(PublicKey::getKey)
                    .toArray(String[]::new);
            byte[] encryptedWithKeys = Encrypt.encryptWithKeys(fileBytes, publicKeyPems);
            String fingerprintOfKeys = Encrypt.calculateFingerprint(publicKeyPems);

            require(encrypted.length > fileBytes.length && encryptedWithKeys.length > fileBytes.length,
                    "nothing was encrypted");
            require(fingerprint.equals(fingerprintOfKeys), "the two fingerprints differ");
            print("Encrypted " + fileBytes.length + " bytes to " + encrypted.length + " bytes");
        });

        check("File encryption: upload an encrypted file", cleanup -> {
            Path pdf = privateTempDirectory("didww-readme-upload-", cleanup).resolve("document.pdf");
            UploadFileExample.createSamplePdf(pdf, "README upload check");

            Encrypt enc = new Encrypt(client);
            byte[] encrypted = enc.encrypt(Files.readAllBytes(pdf));
            String encryptedFileId = client.uploadEncryptedFile(
                    encrypted,          // encrypted bytes
                    "document.pdf.enc", // uploaded file name
                    enc.getFingerprint(),
                    "document.pdf"      // description
            );
            cleanup.add("delete encrypted file " + encryptedFileId,
                    () -> client.encryptedFiles().delete(encryptedFileId));
            require(encryptedFileId != null && !encryptedFileId.isEmpty(), "no encrypted file ID");
            print("Encrypted file ID: " + encryptedFileId);
        });
    }

    // ─── Webhook signature validation ────────────────────────

    private static void webhook() {
        section("Webhook signature validation");
        check("RequestValidator: a genuine signature validates, a tampered payload does not", cleanup -> {
            String secret = "README-callback-secret";
            RequestValidator validator = new RequestValidator(secret);
            String requestUrl = "https://example.com/didww/callbacks";
            Map<String, String> payloadMap = new LinkedHashMap<>();
            payloadMap.put("id", "1dd7a68b-e235-402b-8912-fe73ee14243a");
            payloadMap.put("status", "completed");
            payloadMap.put("type", "orders");
            String signature = WebhookExample.sign(secret, requestUrl, payloadMap); // as DIDWW signs a callback

            require(validator.validate(requestUrl, payloadMap, signature), "a genuine signature was refused");
            Map<String, String> tampered = new LinkedHashMap<>(payloadMap);
            tampered.put("status", "canceled");
            require(!validator.validate(requestUrl, tampered, signature), "a tampered payload was accepted");
            require("X-DIDWW-Signature".equals(RequestValidator.HEADER_NAME), "unexpected header name");
        });
    }

    // ─── Error handling ──────────────────────────────────────

    private static void errorHandling() {
        section("Error handling");
        check("DidwwApiException for a trunk that does not exist", cleanup -> {
            try {
                client.voiceInTrunks().find("nonexistent");
                throw new IllegalStateException("no error for a trunk that does not exist");
            } catch (DidwwApiException e) {
                print("HTTP Status: " + e.getHttpStatus());
                for (DidwwApiException.ApiError error : e.getErrors()) {
                    print("Error: " + error.getDetail());
                }
                require(e.getHttpStatus() >= 400 && e.getHttpStatus() < 500, "expected a 4xx status");
            }
        });

        check("API rate limit (429) handling", cleanup -> {
            try {
                client.dids().list(firstPage(1));
                print("Not rate limited");
            } catch (DidwwApiException e) {
                if (e.getHttpStatus() != 429) {
                    throw e;
                }
                print("API rate limit, retry after " + e.getRetryAfter());
            }
        });

        // The README ends this snippet with a catch-all for every SDK error; it is left out here, as it would
        // turn any API error into a pass
        check("Token service and prices error kinds", cleanup -> {
            try {
                client.smsOutA2pPrices().lookup(Map.of("dst_number", "4915123456789"));
                print("Price found");
            } catch (DidwwAuthException e) {
                if (e.getKind() == AuthErrorKind.RATE_LIMITED) {
                    print("Token service rate limit, retry after " + e.getRetryAfter());
                } else if (e.getKind() == AuthErrorKind.INVALID_CLIENT) {
                    print("API key refused: " + e.getMessage());
                } else {
                    throw e;
                }
            } catch (DidwwPricesException e) {
                switch (e.getKind()) {
                    case PRICE_NOT_FOUND:
                        print("No price for this number");
                        break;
                    case TOO_MANY_REQUESTS:
                        print("Prices rate limit, retry after " + e.getRetryAfter());
                        break;
                    case PRICE_PLAN_NOT_ASSIGNED:
                        throw new Skipped("no SMS price plan on the account: " + e.getMessage());
                    default:
                        throw e;
                }
            }
        });
    }

    // ─── Date and datetime fields ────────────────────────────

    private static void dateFields() {
        section("Date and datetime fields");
        check("DID datetimes and identity birth date", cleanup -> {
            Did did = client.dids().find(firstDid().getId()).getData();
            OffsetDateTime createdAt = did.getCreatedAt();
            OffsetDateTime expiresAt = did.getExpiresAt();
            require(createdAt != null, "created_at is missing");
            print("DID created " + createdAt + ", expires " + expiresAt);

            List<Identity> identities = client.identities().list(firstPage(1)).getData();
            if (!identities.isEmpty()) {
                Identity identity = client.identities().find(identities.get(0).getId()).getData();
                LocalDate birthDate = identity.getBirthDate();
                print("Identity birth date " + birthDate);
            }
        });
    }

    // ─── Check runner ────────────────────────────────────────

    private static void section(String title) {
        System.out.println("\n== " + title + " ==");
    }

    // Runs one check; its cleanup steps run in the finally block, also when the check fails.
    private static void check(String name, Check body) {
        Cleanup cleanup = new Cleanup();
        Exception error = null;
        List<String> cleanupErrors;
        try {
            body.run(cleanup);
        } catch (Exception e) {
            error = e;
        } finally {
            cleanupErrors = cleanup.runAll();
        }

        if (error == null && cleanupErrors.isEmpty()) {
            passed++;
            System.out.println("  PASS " + name);
        } else if (error instanceof Skipped && cleanupErrors.isEmpty()) {
            skip(name, error.getMessage());
        } else {
            List<String> messages = new ArrayList<>();
            if (error != null && !(error instanceof Skipped)) {
                messages.add(describe(error));
            }
            cleanupErrors.forEach(e -> messages.add("cleanup failed: " + e));
            failed++;
            failures.add(name + ": " + String.join("; ", messages));
            System.out.println("  FAIL " + name + ": " + String.join("; ", messages));
        }
    }

    private static void skip(String name, String reason) {
        skips.add(name + ": " + reason);
        System.out.println("  SKIP " + name + ": " + reason);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
    }

    private static void print(String line) {
        System.out.println("    " + line);
    }

    // ─── Helpers ─────────────────────────────────────────────

    private static QueryParams firstPage(int size) {
        return QueryParams.builder().page(1, size).build();
    }

    private static void listed(String what, List<?> items) {
        print(items.size() + " " + what);
    }

    private static int sizeOf(List<?> list) {
        return list != null ? list.size() : 0;
    }

    private static String idOf(BaseResource resource) {
        return resource != null ? resource.getId() : null;
    }

    private static String uniqueName(String label) {
        return label + " " + SUFFIX + "-" + (++nameCount);
    }

    // Converts "this account has no price plan or no price" into a skipped check.
    private static <T> T priced(Supplier<T> call) {
        try {
            return call.get();
        } catch (DidwwPricesException e) {
            if (e.getKind() == PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED || e.getKind() == PricesErrorKind.PRICE_NOT_FOUND) {
                throw new Skipped(e.getKind() + ": " + e.getMessage());
            }
            throw e;
        }
    }

    private static Country country(String iso) {
        return countriesByIso.computeIfAbsent(iso, code -> {
            List<Country> found = client.countries().list(QueryParams.builder().filter("iso", code).build()).getData();
            require(!found.isEmpty(), "country " + code + " not found");
            return found.get(0);
        });
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new Skipped("set " + name + " to run this check");
        }
        return value;
    }

    // ─── Helpers of the verification checks ──────────────────

    // An active DID without an address verification in a Ukrainian local DID group that has an address
    // requirement; when the account has none, one is ordered from such a group. The requirement shows only
    // with include=address_requirement, and whether a DID awaits registration depends on account rules,
    // so neither that flag nor the group's needs_registration is used.
    private static Did didWithAddressRequirement() throws InterruptedException {
        List<DidGroup> groups = client.didGroups().list(QueryParams.builder()
                .filter("country.id", country("UA").getId())
                .filter("number_type", EnumWire.value(NumberType.LOCAL))
                .include("address_requirement")
                .page(1, 100)
                .build()).getData();
        List<String> groupIds = new ArrayList<>();
        for (DidGroup group : groups) {
            if (group.getAddressRequirement() != null) {
                groupIds.add(group.getId());
            }
        }
        require(!groupIds.isEmpty(), "no Ukrainian local DID group with an address requirement");

        for (String groupId : groupIds) {
            List<Did> dids = client.dids().list(QueryParams.builder()
                    .filter("did_group.id", groupId)
                    .filter("terminated", "false")
                    .include("did_group", "address_verification")
                    .page(1, 25)
                    .build()).getData();
            for (Did did : dids) {
                if (did.getAddressVerification() == null && did.getDidGroup() != null) {
                    return did;
                }
            }
        }
        for (String groupId : groupIds) {
            QueryParams available = QueryParams.builder()
                    .filter("did_group.id", groupId)
                    .include("did_group.stock_keeping_units")
                    .page(1, 1)
                    .build();
            if (!client.availableDids().list(available).getData().isEmpty()) {
                return orderAvailableDid(available);
            }
        }
        throw new IllegalStateException("no available DID in the Ukrainian local groups with an address requirement");
    }

    // An emergency-capable Ukrainian DID without an identity, emergency calling service or emergency
    // verification; when the account has none, one is ordered. A DID bound to an identity (by an earlier
    // verification) can be verified only with an address of that identity, so it is not reused.
    private static Did emergencyDid() throws InterruptedException {
        String ukraine = country("UA").getId();
        List<Did> dids = client.dids().list(QueryParams.builder()
                .filter("country.id", ukraine)
                .filter("did_group.features", "emergency")
                .filter("emergency_enabled", "false")
                .filter("terminated", "false")
                .include("did_group", "identity", "emergency_calling_service", "emergency_verification")
                .page(1, 25)
                .build()).getData();
        for (Did did : dids) {
            if (did.getIdentity() == null && did.getEmergencyCallingService() == null
                    && did.getEmergencyVerification() == null && did.getDidGroup() != null) {
                return did;
            }
        }
        return orderAvailableDid(QueryParams.builder()
                .filter("country.id", ukraine)
                .filter("did_group.features", "emergency")
                .include("did_group.stock_keeping_units")
                .page(1, 1)
                .build());
    }

    // Orders the first available DID of the list, waits up to two minutes for the order (the sandbox can
    // take more than one) and returns the DID.
    private static Did orderAvailableDid(QueryParams availableParams) throws InterruptedException {
        List<AvailableDid> available = client.availableDids().list(availableParams).getData();
        require(!available.isEmpty(), "no available DID to order");
        AvailableDid availableDid = available.get(0);
        List<StockKeepingUnit> skus = availableDid.getDidGroup() != null
                ? availableDid.getDidGroup().getStockKeepingUnits() : null;
        require(skus != null && !skus.isEmpty(), "no SKU for available DID " + availableDid.getNumber());

        AvailableDidOrderItem item = new AvailableDidOrderItem();
        item.setAvailableDidId(availableDid.getId());
        item.setSkuId(skus.get(0).getId());
        Order order = new Order();
        order.setItems(Collections.singletonList(item));
        Order created = client.orders().create(order).getData();
        print("Ordered " + availableDid.getNumber() + " (order " + created.getId() + "); the DID stays on the account");
        for (int attempt = 0; attempt < 24 && !created.isCompleted(); attempt++) {
            Thread.sleep(5000);
            created = client.orders().find(created.getId()).getData();
        }
        require(created.isCompleted(), "order " + created.getId() + " is " + created.getStatus());

        List<Did> dids = client.dids().list(QueryParams.builder()
                .filter("order.id", created.getId())
                .include("did_group")
                .build()).getData();
        require(!dids.isEmpty(), "no DID in order " + created.getId());
        return dids.get(0);
    }

    // The address requirement with its proof types and documents.
    private static AddressRequirement addressRequirement(String requirementId) {
        return client.addressRequirements().find(requirementId, QueryParams.builder()
                .include("personal_proof_types", "business_proof_types", "address_proof_types",
                        "personal_permanent_document", "business_permanent_document",
                        "personal_onetime_document", "business_onetime_document")
                .build()).getData();
    }

    // Creates an identity with the type, mandatory fields, proofs and permanent document the requirement asks for.
    private static Identity identityFor(AddressRequirement requirement, Country country, ProofFiles files,
                                        Cleanup cleanup, AtomicBoolean referenced) {
        boolean business = requirement.getIdentityType() == IdentityType.BUSINESS
                || (requirement.getIdentityType() == IdentityType.ANY
                && requirement.getPersonalOnetimeDocument() != null && requirement.getBusinessOnetimeDocument() == null);
        SupportingDocumentTemplate onetime = business
                ? requirement.getBusinessOnetimeDocument() : requirement.getPersonalOnetimeDocument();
        require(onetime == null, "the requirement asks for a one-time document (" + onetime
                + "), which an address verification of the API cannot carry");

        Identity identity = new Identity();
        identity.setIdentityType(business ? IdentityType.BUSINESS : IdentityType.PERSONAL);
        identity.setPhoneNumber("380487000000");
        identity.setCountry(country);
        if (business) {
            identity.setCompanyName(uniqueName("README Check"));
        } else {
            identity.setFirstName("John");
            identity.setLastName("Doe");
        }
        List<String> mandatory = business
                ? requirement.getBusinessMandatoryFields() : requirement.getPersonalMandatoryFields();
        for (String field : mandatory != null ? mandatory : Collections.<String>emptyList()) {
            fillIdentityField(identity, field, country);
        }
        Identity created = client.identities().create(identity).getData();
        String identityId = created.getId();
        cleanup.add("delete identity " + identityId + " unless a verification references it", () -> {
            if (referenced.get()) {
                print("identity " + identityId + " stays: the address verification references it");
            } else {
                client.identities().delete(identityId);
            }
        });

        createProofs(created, business ? requirement.getBusinessProofTypes() : requirement.getPersonalProofTypes(),
                business ? requirement.getBusinessProofQty() : requirement.getPersonalProofQty(), files, "identity");
        SupportingDocumentTemplate template = business
                ? requirement.getBusinessPermanentDocument() : requirement.getPersonalPermanentDocument();
        if (template != null) {
            PermanentSupportingDocument document = new PermanentSupportingDocument();
            document.setIdentity(new Identity().withId(identityId));
            document.setTemplate(new SupportingDocumentTemplate().withId(template.getId()));
            document.setFiles(Collections.singletonList(files.upload()));
            client.permanentSupportingDocuments().create(document);
            print("Permanent document " + template.getName() + " for the identity");
        }
        return created;
    }

    private static void fillIdentityField(Identity identity, String field, Country country) {
        switch (field) {
            case "birth_date":
                identity.setBirthDate(LocalDate.of(1990, 1, 15));
                break;
            case "birth_country":
                identity.setBirthCountry(country);
                break;
            case "country":
                identity.setCountry(country);
                break;
            case "id_number":
                identity.setIdNumber("AB123456");
                break;
            case "personal_tax_id":
                identity.setPersonalTaxId("1234567890");
                break;
            case "contact_email":
                identity.setContactEmail("readme-check@example.com");
                break;
            case "tax_id":
                identity.setVatId("UA12345678");
                break;
            case "company_reg_number":
                identity.setCompanyRegNumber("12345678");
                break;
            default:
                print("mandatory field " + field + " is not filled in by this check");
        }
    }

    // Creates an address of the identity in the DID's country, with the proofs the requirement asks for.
    // The API sets no city or area on a new address, so the DID's city is not compared with it.
    private static Address addressFor(AddressRequirement requirement, Identity identity, DidGroup group,
                                      ProofFiles files, Cleanup cleanup, AtomicBoolean referenced) {
        Address address = new Address();
        address.setCityName(group.getAreaName() != null ? group.getAreaName() : "Odesa");
        address.setPostalCode("65000");
        address.setAddress("1 Deribasivska St");
        address.setIdentity(new Identity().withId(identity.getId()));
        address.setCountry(group.getCountry());
        Address created = client.addresses().create(address).getData();
        String addressId = created.getId();
        cleanup.add("delete address " + addressId + " unless a verification references it", () -> {
            if (referenced.get()) {
                print("address " + addressId + " stays: the address verification references it");
            } else {
                client.addresses().delete(addressId);
            }
        });
        createProofs(created, requirement.getAddressProofTypes(), requirement.getAddressProofQty(), files, "address");
        return created;
    }

    // Creates qty proofs of different types for the identity or address, each with an uploaded file.
    private static void createProofs(ProofEntity entity, List<ProofType> types, Integer qty, ProofFiles files,
                                     String label) {
        int needed = qty != null ? qty : 0;
        List<ProofType> available = types != null ? types : Collections.<ProofType>emptyList();
        require(available.size() >= needed, needed + " " + label + " proof(s) needed, " + available.size() + " type(s) listed");
        for (int i = 0; i < needed; i++) {
            Proof proof = new Proof();
            proof.setEntity(entity);
            proof.setProofType(new ProofType().withId(available.get(i).getId()));
            proof.setFiles(Collections.singletonList(files.upload()));
            client.proofs().create(proof);
            print("Proof " + available.get(i).getName() + " for the " + label);
        }
    }

    // The first address of the account in the country, with its identity, that meets the emergency requirement.
    private static Address emergencyAddress(EmergencyRequirement requirement, Country country) {
        List<Address> addresses = client.addresses().list(QueryParams.builder()
                .filter("country.id", country.getId())
                .include("identity")
                .page(1, 10)
                .build()).getData();
        List<String> refusals = new ArrayList<>();
        for (Address address : addresses) {
            if (address.getIdentity() == null) {
                continue;
            }
            EmergencyRequirementValidation validation = new EmergencyRequirementValidation();
            validation.setEmergencyRequirement(new EmergencyRequirement().withId(requirement.getId()));
            validation.setAddress(new Address().withId(address.getId()));
            validation.setIdentity(new Identity().withId(address.getIdentity().getId()));
            try {
                client.emergencyRequirementValidations().create(validation);
                return address;
            } catch (DidwwApiException e) {
                if (e.getHttpStatus() != 422) {
                    throw e;
                }
                refusals.add(address.getId() + ": " + e.getMessage());
            }
        }
        throw new IllegalStateException("no address in " + country.getName()
                + " meets the emergency requirement " + requirement.getId() + " " + refusals);
    }

    /** Encrypts a sample PDF and uploads it, once for each proof or document that needs a file. */
    private static final class ProofFiles {
        private final Encrypt enc = new Encrypt(client);
        private final byte[] pdf;

        ProofFiles(Cleanup cleanup) throws Exception {
            Path file = privateTempDirectory("didww-readme-proof-", cleanup).resolve("proof.pdf");
            UploadFileExample.createSamplePdf(file, "README address verification check");
            pdf = Files.readAllBytes(file);
        }

        EncryptedFile upload() {
            String id = client.uploadEncryptedFile(enc.encrypt(pdf), "proof.pdf.enc", enc.getFingerprint(), "proof.pdf");
            return new EncryptedFile().withId(id);
        }
    }

    // The first DID that is not terminated, with its voice and SMS routing included.
    private static Did firstDid() {
        QueryParams params = QueryParams.builder()
                .filter("terminated", "false")
                .include("voice_in_trunk", "voice_in_trunk_group", "sms_trunk", "sms_trunk_group")
                .page(1, 1)
                .build();
        List<Did> dids = client.dids().list(params).getData();
        if (dids.isEmpty()) {
            throw new Skipped("no DID on the account; order one first");
        }
        return dids.get(0);
    }

    private static Did findDidWithRouting(String didId) {
        QueryParams params = QueryParams.builder()
                .include("voice_in_trunk", "voice_in_trunk_group", "sms_trunk", "sms_trunk_group")
                .build();
        return client.dids().find(didId, params).getData();
    }

    // Registers a cleanup step that puts the given fields of the DID back as they were.
    private static void restoreDidAfterwards(Cleanup cleanup, Did original, Set<DidField> fields) {
        String didId = original.getId();
        String description = original.getDescription();
        Integer capacityLimit = original.getCapacityLimit();
        String trunkId = idOf(original.getVoiceInTrunk());
        String trunkGroupId = idOf(original.getVoiceInTrunkGroup());
        String smsTrunkId = idOf(original.getSmsTrunk());
        String smsTrunkGroupId = idOf(original.getSmsTrunkGroup());
        cleanup.add("restore " + fields + " of DID " + original.getNumber(), () -> {
            Did restore = new Did().withId(didId);
            if (fields.contains(DidField.DESCRIPTION)) {
                restore.setDescription(description);
            }
            if (fields.contains(DidField.CAPACITY_LIMIT)) {
                restore.setCapacityLimit(capacityLimit);
            }
            if (fields.contains(DidField.VOICE_ROUTING)) {
                if (trunkId != null) {
                    restore.setVoiceInTrunk(new VoiceInTrunk().withId(trunkId));
                } else if (trunkGroupId != null) {
                    restore.setVoiceInTrunkGroup(new VoiceInTrunkGroup().withId(trunkGroupId));
                } else {
                    restore.setVoiceInTrunk(null); // sends trunk and trunk group as null
                }
            }
            if (fields.contains(DidField.SMS_ROUTING)) {
                if (smsTrunkId != null) {
                    restore.setSmsTrunk(new SmsTrunk().withId(smsTrunkId));
                } else if (smsTrunkGroupId != null) {
                    restore.setSmsTrunkGroup(new SmsTrunkGroup().withId(smsTrunkGroupId));
                } else {
                    restore.setSmsTrunk(null);
                    restore.setSmsTrunkGroup(null);
                }
            }
            client.dids().update(restore);
        });
    }

    private static String createSipTrunk(Cleanup cleanup, String label) {
        SipConfiguration sip = new SipConfiguration();
        sip.setHost("sip.example.com");
        sip.setPort(5060);
        sip.setCodecs(Arrays.asList(Codec.PCMA, Codec.PCMU, Codec.TELEPHONE_EVENT));
        sip.setTransportProtocolId(TransportProtocol.UDP);
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName(uniqueName(label));
        trunk.setConfiguration(sip);
        String trunkId = client.voiceInTrunks().create(trunk).getData().getId();
        cleanup.add("delete voice IN trunk " + trunkId, () -> client.voiceInTrunks().delete(trunkId));
        return trunkId;
    }

    // An inbound SMS trunk that forwards messages with GET, which takes no body
    private static String createHttpInSmsTrunk(Cleanup cleanup, String label) {
        HttpInConfiguration http = new HttpInConfiguration();
        http.setUrl("https://example.com/sms");
        http.setHttpMethod(SmsHttpMethod.GET);
        SmsTrunk trunk = new SmsTrunk();
        trunk.setName(uniqueName(label));
        trunk.setConfiguration(http);
        String trunkId = client.smsTrunks().create(trunk).getData().getId();
        cleanup.add("delete SMS trunk " + trunkId, () -> client.smsTrunks().delete(trunkId));
        return trunkId;
    }

    // A sending SMS trunk; DIDWW generates its credentials
    private static String createHttpOutSmsTrunk(Cleanup cleanup, String label) {
        HttpOutConfiguration http = new HttpOutConfiguration();
        http.setCallbackUrl("https://example.com/sms/status");
        http.setAllowedIps(Collections.singletonList("203.0.113.10"));
        SmsTrunk trunk = new SmsTrunk();
        trunk.setName(uniqueName(label));
        trunk.setConfiguration(http);
        String trunkId = client.smsTrunks().create(trunk).getData().getId();
        cleanup.add("delete SMS trunk " + trunkId, () -> client.smsTrunks().delete(trunkId));
        return trunkId;
    }

    private static String sendingSmsTrunkId() {
        for (SmsTrunk trunk : client.smsTrunks().list(firstPage(10)).getData()) {
            if (Boolean.TRUE.equals(trunk.getAllowOut())) {
                return trunk.getId();
            }
        }
        return null;
    }

    private static List<SmsTrunk> smsGroupMembers(String groupId) {
        SmsTrunkGroup group = client.smsTrunkGroups().find(groupId,
                QueryParams.builder().include("sms_trunks").build()).getData();
        return group.getSmsTrunks() != null ? group.getSmsTrunks() : Collections.emptyList();
    }

    private static List<SenderIdVerification> activeVerifications() {
        return client.senderIdVerifications().list(QueryParams.builder()
                .filter("status", EnumWire.value(SenderIdVerificationStatus.ACTIVE))
                .include("sms_trunk")
                .build()).getData();
    }

    private static SenderIdVerification findVerification(String id) {
        return client.senderIdVerifications().find(id, QueryParams.builder().include("sms_trunk").build()).getData();
    }

    // The first SKU of a DID group with numbers in stock: a group without refuses the order
    private static String findSkuId() {
        QueryParams params = QueryParams.builder()
                .filter("is_available", "true")
                .include("stock_keeping_units")
                .page(1, 10)
                .build();
        for (DidGroup group : client.didGroups().list(params).getData()) {
            if (group.getStockKeepingUnits() != null && !group.getStockKeepingUnits().isEmpty()) {
                return group.getStockKeepingUnits().get(0).getId();
            }
        }
        throw new Skipped("no DID group with stock keeping units");
    }

    // Orders cannot be deleted; a pending one is cancelled. A refused cancel fails the check, unless the
    // order is no longer pending by then (it completed meanwhile), which the reloaded order tells.
    private static void cancelIfPending(Cleanup cleanup, Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            print("Order " + order.getId() + " is " + order.getStatus() + " and stays");
            return;
        }
        cleanup.add("cancel order " + order.getId(), () -> {
            try {
                client.orders().delete(order.getId());
            } catch (DidwwApiException e) {
                OrderStatus status = client.orders().find(order.getId()).getData().getStatus();
                if (status == OrderStatus.PENDING) {
                    throw e;
                }
                print("order " + order.getId() + " is " + status + " and can no longer be cancelled");
            }
        });
    }

    // A new temporary directory that only the current user can open, deleted with its files at cleanup.
    // Where the file system has no POSIX permissions (Windows), the temporary directory is per user already.
    private static Path privateTempDirectory(String prefix, Cleanup cleanup) throws IOException {
        Path dir;
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            dir = Files.createTempDirectory(prefix, PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwx------")));
        } else {
            dir = Files.createTempDirectory(prefix); // NOSONAR the per-user temporary directory, see above
        }
        cleanup.add("delete " + dir, () -> deleteDirectory(dir));
        return dir;
    }

    private static void deleteDirectory(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
                Files.deleteIfExists(path);
            }
        }
    }

    /** A TokenStore kept in memory, as the README's interface describes it; a real one would use Redis. */
    private static final class InMemoryTokenStore implements TokenStore {
        private final Map<String, AccessToken> tokens = new ConcurrentHashMap<>();
        private final Map<String, Instant> expiries = new ConcurrentHashMap<>();

        @Override
        public AccessToken get(String key) {
            Instant expiry = expiries.get(key);
            if (expiry != null && Instant.now().isAfter(expiry)) {
                delete(key);
                return null;
            }
            return tokens.get(key);
        }

        @Override
        public void set(String key, AccessToken token, Duration ttl) {
            tokens.put(key, token);
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                expiries.remove(key);
            } else {
                expiries.put(key, Instant.now().plus(ttl));
            }
        }

        @Override
        public void delete(String key) {
            tokens.remove(key);
            expiries.remove(key);
        }

        Set<String> keys() {
            return tokens.keySet();
        }
    }

    /**
     * A TokenStore over Redis, the README's myRedisStore. It holds the JSON of each AccessToken under the
     * SDK's key with a namespace in front. A minimal Redis client keeps the example free of a Redis library:
     * each call opens a connection with short timeouts, sends one command and closes it. The URL is
     * redis://[[user]:password@]host[:port][/database].
     */
    private static final class RedisTokenStore implements TokenStore {
        private static final int TIMEOUT_MILLIS = 3000;

        private final URI url;
        private final String namespace;
        private final ObjectMapper mapper = new ObjectMapper();
        private final Map<String, AccessToken> written = new ConcurrentHashMap<>();

        RedisTokenStore(String url, String namespace) {
            this.url = URI.create(url);
            this.namespace = namespace;
        }

        @Override
        public AccessToken get(String key) {
            Object json = command("GET", namespace + key);
            if (json == null) {
                return null;
            }
            try {
                return mapper.readValue((String) json, AccessToken.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void set(String key, AccessToken token, Duration ttl) {
            String json;
            try {
                json = mapper.writeValueAsString(token);
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
            if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
                command("SET", namespace + key, json, "PX", Long.toString(ttl.toMillis()));
            } else {
                command("SET", namespace + key, json);
            }
            written.put(key, token);
        }

        @Override
        public void delete(String key) {
            command("DEL", namespace + key);
        }

        // The tokens this store has written, by key
        Map<String, AccessToken> written() {
            return written;
        }

        void deleteWritten() {
            for (String key : written.keySet()) {
                delete(key);
            }
        }

        // Sends one command, after AUTH and SELECT when the URL has a password or a database, and returns
        // the reply: a String, a Long, or null.
        private Object command(String... args) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(url.getHost(), url.getPort() != -1 ? url.getPort() : 6379),
                        TIMEOUT_MILLIS);
                socket.setSoTimeout(TIMEOUT_MILLIS);
                OutputStream out = new BufferedOutputStream(socket.getOutputStream());
                InputStream in = new BufferedInputStream(socket.getInputStream());
                String userInfo = url.getRawUserInfo();
                if (userInfo != null) {
                    int colon = userInfo.indexOf(':');
                    String user = colon >= 0 ? decode(userInfo.substring(0, colon)) : "";
                    String password = decode(colon >= 0 ? userInfo.substring(colon + 1) : userInfo);
                    if (user.isEmpty()) {
                        send(out, in, "AUTH", password);
                    } else {
                        send(out, in, "AUTH", user, password);
                    }
                }
                String path = url.getPath();
                if (path != null && path.length() > 1) {
                    send(out, in, "SELECT", path.substring(1));
                }
                return send(out, in, args);
            } catch (IOException e) {
                throw new UncheckedIOException("Redis " + args[0] + " failed", e);
            }
        }

        private static String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        private static Object send(OutputStream out, InputStream in, String... args) throws IOException {
            ByteArrayOutputStream request = new ByteArrayOutputStream();
            request.write(("*" + args.length + "\r\n").getBytes(StandardCharsets.UTF_8));
            for (String arg : args) {
                byte[] bytes = arg.getBytes(StandardCharsets.UTF_8);
                request.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
                request.write(bytes);
                request.write("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            request.writeTo(out);
            out.flush();
            return reply(in);
        }

        private static Object reply(InputStream in) throws IOException {
            String line = readLine(in);
            if (line.isEmpty()) {
                throw new IOException("Empty Redis reply");
            }
            String rest = line.substring(1);
            switch (line.charAt(0)) {
                case '+':
                    return rest;
                case ':':
                    return Long.parseLong(rest);
                case '-':
                    throw new IOException("Redis error: " + rest);
                case '$':
                    int length = Integer.parseInt(rest);
                    if (length < 0) {
                        return null;
                    }
                    byte[] data = in.readNBytes(length + 2); // the value and its CRLF
                    if (data.length < length + 2) {
                        throw new EOFException("Redis reply cut short");
                    }
                    return new String(data, 0, length, StandardCharsets.UTF_8);
                default:
                    throw new IOException("Unexpected Redis reply type " + line.charAt(0));
            }
        }

        private static String readLine(InputStream in) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            for (int b = in.read(); b != '\n'; b = in.read()) {
                if (b == -1) {
                    throw new EOFException("Redis closed the connection");
                }
                line.write(b);
            }
            String text = line.toString(StandardCharsets.UTF_8);
            return text.endsWith("\r") ? text.substring(0, text.length() - 1) : text;
        }
    }
}
