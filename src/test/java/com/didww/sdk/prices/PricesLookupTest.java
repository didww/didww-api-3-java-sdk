package com.didww.sdk.prices;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.SdkVersion;
import com.didww.sdk.exception.AuthErrorKind;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwAuthException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.exception.DidwwException;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class PricesLookupTest extends PricesTestBase {
    private static final String UNAUTHORIZED_BODY =
            "{\"errors\":[{\"title\":\"Unauthorized\",\"code\":\"401\",\"status\":\"401\"}]}";
    private static final Map<String, String> NUMBER = Collections.singletonMap("number", "18005550123");

    private static Map<String, String> filters(String... pairs) {
        Map<String, String> filters = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            filters.put(pairs[i], pairs[i + 1]);
        }
        return filters;
    }

    private static PricesService<?> service(DidwwClient client, String name) {
        Map<String, Function<DidwwClient, PricesService<?>>> services = new HashMap<>();
        services.put("voiceIn", DidwwClient::voiceInPrices);
        services.put("voiceOut", DidwwClient::voiceOutPrices);
        services.put("smsOutA2p", DidwwClient::smsOutA2pPrices);
        services.put("smsOutP2p", DidwwClient::smsOutP2pPrices);
        services.put("smsOutOtp", DidwwClient::smsOutOtpPrices);
        services.put("smsIn", DidwwClient::smsInPrices);
        return services.get(name).apply(client);
    }

    private void stubLookup(String path, int status, String body) {
        wireMock.stubFor(get(urlPathEqualTo(path)).willReturn(jsonApi(status, body)));
    }

    @Test
    void testVoiceInPricesLookup() {
        stubLookup(VOICE_IN_LOOKUP, 200, fixture("prices/voice_in/lookup/200.json"));

        PriceLookup<VoiceInPrice> result = client.voiceInPrices().lookup(NUMBER);

        List<LoggedRequest> requests = priceRequests();
        assertThat(requests).hasSize(1);
        LoggedRequest request = requests.get(0);
        assertThat(request.getMethod().getName()).isEqualTo("GET");
        assertThat(request.getUrl()).isEqualTo("/v3/lookup/prices?filter%5Bnumber%5D=18005550123");
        assertThat(request.getHeader("Accept")).isEqualTo("application/vnd.api+json");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer " + TEST_ACCESS_TOKEN);
        assertThat(request.getHeader("User-Agent")).isEqualTo(SdkVersion.userAgent());
        assertThat(request.containsHeader("Api-Key")).isFalse();
        assertThat(request.containsHeader("X-DIDWW-API-Version")).isFalse();
        assertThat(mintAudiences()).containsExactly(VOICE_IN);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(result.getRows()).hasSize(2);
        VoiceInPrice priced = result.getRows().get(0);
        VoiceInPrice rejected = result.getRows().get(1);
        assertThat(priced.getId()).isEqualTo("df73d9e4-46ce-4af0-817e-f090dc8144f1");
        assertThat(priced.getDestination()).isEqualTo("1800");
        assertThat(priced.getCountry()).isEqualTo("United States");
        assertThat(priced.getSrcCountryName()).isNull();
        assertThat(priced.getSourceType()).isEqualTo("cellular");
        assertThat(priced.getRate()).isEqualTo(new BigDecimal("0.025"));
        assertThat(priced.getConnectionFee()).isEqualTo(new BigDecimal("0.0"));
        assertThat(priced.getInitialInterval()).isEqualTo(60);
        assertThat(priced.getNextInterval()).isEqualTo(60);
        assertThat(priced.getRejectCalls()).isFalse();
        assertThat(rejected.getId()).isEqualTo("cb1335e1-6cfd-4921-8724-153d9ed67ac3");
        assertThat(rejected.getSrcCountryName()).isEqualTo("United Kingdom");
        assertThat(rejected.getRate()).isNull();
        assertThat(rejected.getConnectionFee()).isNull();
        assertThat(rejected.getRejectCalls()).isTrue();
    }

    @Test
    void testVoiceInPricesLookupWithSourceCountry() {
        stubLookup(VOICE_IN_LOOKUP, 200, fixture("prices/voice_in/lookup/200.json"));

        client.voiceInPrices().lookup(filters("src_country_iso", "GB", "number", "448001234567"));

        assertThat(priceRequests().get(0).getUrl())
                .as("filters are sorted by name")
                .isEqualTo("/v3/lookup/prices?filter%5Bnumber%5D=448001234567&filter%5Bsrc_country_iso%5D=GB");
    }

    @Test
    void testPricesLookupEscapesFilterValues() {
        stubLookup(VOICE_IN_LOOKUP, 200, fixture("prices/voice_in/lookup/200.json"));

        client.voiceInPrices().lookup(filters("number", "+1 800&x=1"));

        assertThat(priceRequests().get(0).getUrl())
                .isEqualTo("/v3/lookup/prices?filter%5Bnumber%5D=%2B1%20800%26x%3D1");
        wireMock.verify(getRequestedFor(urlPathEqualTo(VOICE_IN_LOOKUP))
                .withQueryParam("filter[number]", equalTo("+1 800&x=1")));
    }

    @Test
    void testVoiceOutPricesLookup() {
        stubLookup(VOICE_IN_LOOKUP, 200, fixture("prices/voice_out/lookup/200.json"));

        PriceLookup<VoiceOutPrice> result = client.voiceOutPrices().lookup(filters("number", "493012345678"));

        assertThat(priceRequests().get(0).getUrl()).isEqualTo("/v3/lookup/prices?filter%5Bnumber%5D=493012345678");
        assertThat(mintAudiences()).containsExactly(VOICE_OUT);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        VoiceOutPrice scheduled = result.getRows().get(0);
        VoiceOutPrice plain = result.getRows().get(1);
        assertThat(scheduled.getId()).isEqualTo("7b82cac2-c42a-4fa9-8da4-ccba2843068f");
        assertThat(scheduled.getCategory()).isEqualTo(VoiceOutPriceCategory.INTERNATIONAL);
        assertThat(scheduled.getCountry()).isEqualTo("Germany");
        assertThat(scheduled.getNetwork()).isEqualTo("Fixed");
        assertThat(scheduled.getPrefix()).isEqualTo("4930");
        assertThat(scheduled.getRate()).isEqualTo(new BigDecimal("0.0045"));
        assertThat(scheduled.getConnectionFee()).isEqualTo(new BigDecimal("0.0"));
        assertThat(scheduled.getInitialIncrement()).isEqualTo(1);
        assertThat(scheduled.getRecurringIncrement()).isEqualTo(1);
        assertThat(scheduled.getNextRate()).isEqualTo(new BigDecimal("0.005"));
        assertThat(scheduled.getNextConnectionFee()).isEqualTo(new BigDecimal("0.0"));
        assertThat(scheduled.getNextInitialIncrement()).isEqualTo(30);
        assertThat(scheduled.getNextRecurringIncrement()).isEqualTo(6);
        assertThat(scheduled.getEffectiveAt().toInstant()).isEqualTo(Instant.parse("2026-11-01T00:00:00Z"));
        assertThat(scheduled.getComment()).isEqualTo("Increase and Billing increment change");
        assertThat(scheduled.getRejectCalls()).isFalse();
        assertThat(plain.getCategory()).isEqualTo(VoiceOutPriceCategory.ORIGIN_BASED);
        assertThat(plain.getNextRate()).isNull();
        assertThat(plain.getNextConnectionFee()).isNull();
        assertThat(plain.getNextInitialIncrement()).isNull();
        assertThat(plain.getNextRecurringIncrement()).isNull();
        assertThat(plain.getEffectiveAt()).isNull();
        assertThat(plain.getComment()).isNull();
    }

    @Test
    void testVoiceOutPricesLookupUnknownCategoryIsNull() {
        stubLookup(VOICE_IN_LOOKUP, 200,
                "{\"data\":[{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{\"category\":\"Premium\",\"prefix\":\"49\"}}]}");

        VoiceOutPrice row = client.voiceOutPrices().lookup(filters("number", "4930")).getRows().get(0);

        assertThat(row.getCategory()).isNull();
        assertThat(row.getPrefix()).isEqualTo("49");
    }

    static Stream<Arguments> smsServices() {
        return Stream.of(
                Arguments.of("smsOutA2p", "/v3/sms_out/a2p/lookup/prices"),
                Arguments.of("smsOutP2p", "/v3/sms_out/p2p/lookup/prices"),
                Arguments.of("smsOutOtp", "/v3/sms_out/otp/lookup/prices"),
                Arguments.of("smsIn", "/v3/sms_in/lookup/prices"));
    }

    @ParameterizedTest
    @MethodSource("smsServices")
    void testSmsPricesLookup(String name, String path) {
        stubLookup(path, 200, fixture(name.equals("smsIn") ? "prices/sms_in/lookup/200.json"
                : "prices/sms_out/lookup/200.json"));

        service(client, name).lookup(filters("dst_number", "4915123456789"));
        service(client, name).lookup(filters("dst_country_iso", "FR"));
        service(client, name).lookup(filters("dst_number", "4915123456789", "dst_country_iso", "DE"));

        List<LoggedRequest> requests = priceRequests();
        assertThat(requests).extracting(LoggedRequest::getUrl).containsExactly(
                path + "?filter%5Bdst_number%5D=4915123456789",
                path + "?filter%5Bdst_country_iso%5D=FR",
                path + "?filter%5Bdst_country_iso%5D=DE&filter%5Bdst_number%5D=4915123456789");
        assertThat(requests).allSatisfy(r ->
                assertThat(r.getHeader("Authorization")).isEqualTo("Bearer " + TEST_ACCESS_TOKEN));
        assertThat(mintAudiences()).containsExactly(SMS);
    }

    @Test
    void testSmsOutPricesLookupRows() {
        stubLookup("/v3/sms_out/a2p/lookup/prices", 200, fixture("prices/sms_out/lookup/200.json"));

        PriceLookup<SmsOutPrice> result = client.smsOutA2pPrices().lookup(filters("dst_number", "4915123456789"));

        assertThat(result.getTotalRecords()).isEqualTo(2);
        SmsOutPrice priced = result.getRows().get(0);
        SmsOutPrice rejected = result.getRows().get(1);
        assertThat(priced.getId()).isEqualTo("d8e4d372-42a6-4bb6-80ec-6c29c49aa9fd");
        assertThat(priced.getSourcePrefix()).isNull();
        assertThat(priced.getDestinationPrefix()).isEqualTo("49151");
        assertThat(priced.getCountry()).isEqualTo("Germany");
        assertThat(priced.getMcc()).isEqualTo(262);
        assertThat(priced.getMccName()).isEqualTo("Germany");
        assertThat(priced.getMnc()).isEqualTo(1);
        assertThat(priced.getMncName()).isEqualTo("Telekom Deutschland");
        assertThat(priced.getRejectSms()).isFalse();
        assertThat(priced.getPrice()).as("the scale of the answer is kept").isEqualTo(new BigDecimal("0.075000"));
        assertThat(priced.getPrice().toPlainString()).isEqualTo("0.075000");
        assertThat(rejected.getRejectSms()).isTrue();
        assertThat(rejected.getPrice()).isNull();
    }

    @Test
    void testSmsInPricesLookupRows() {
        stubLookup("/v3/sms_in/lookup/prices", 200, fixture("prices/sms_in/lookup/200.json"));

        PriceLookup<SmsInPrice> result = client.smsInPrices().lookup(filters("dst_number", "12025550123"));

        assertThat(result.getTotalRecords()).isEqualTo(1);
        assertThat(result.getRows()).hasSize(1);
        SmsInPrice price = result.getRows().get(0);
        assertThat(price.getId()).isEqualTo("e93f925e-25ee-4060-bd50-a790b4945d3f");
        assertThat(price.getSourcePrefix()).isNull();
        assertThat(price.getSourceNumberLengthMin()).isEqualTo(1);
        assertThat(price.getSourceNumberLengthMax()).isEqualTo(15);
        assertThat(price.getDestinationPrefix()).isEqualTo("1");
        assertThat(price.getDestinationNumberLengthMin()).isEqualTo(11);
        assertThat(price.getDestinationNumberLengthMax()).isEqualTo(11);
        assertThat(price.getCountry()).isEqualTo("United States");
        assertThat(price.getPrice()).isEqualTo(new BigDecimal("0.005000"));
    }

    @Test
    void testLookupRowsAreUnmodifiable() {
        stubLookup(VOICE_IN_LOOKUP, 200, fixture("prices/voice_in/lookup/200.json"));

        List<VoiceInPrice> rows = client.voiceInPrices().lookup(NUMBER).getRows();

        org.assertj.core.api.Assertions.assertThatThrownBy(rows::clear)
                .isInstanceOf(UnsupportedOperationException.class);
    }

    static Stream<Arguments> filterValidationCases() {
        return Stream.of(
                Arguments.of("voiceIn", filters("src_country_iso", "GB"), "Filter number is required on lookup/prices"),
                Arguments.of("voiceIn", filters("number", ""), "Filter number is required on lookup/prices"),
                Arguments.of("voiceIn", filters("number", null), "Filter number is required on lookup/prices"),
                Arguments.of("voiceIn", filters("number", "18005550123", "dst_country_iso", "US"),
                        "Filter dst_country_iso is not allowed on lookup/prices (allowed: number, src_country_iso)"),
                Arguments.of("voiceOut", filters("number", "493012345678", "src_country_iso", "DE"),
                        "Filter src_country_iso is not allowed on lookup/prices (allowed: number)"),
                Arguments.of("voiceOut", filters("number", "493012345678", "b", "1", "a", "1"),
                        "Filter a, b is not allowed on lookup/prices (allowed: number)"),
                Arguments.of("voiceOut", filters("a", "1"),
                        "Filter a is not allowed on lookup/prices (allowed: number)"),
                Arguments.of("smsOutA2p", filters(),
                        "One of filters dst_number, dst_country_iso is required on sms_out/a2p/lookup/prices"),
                Arguments.of("smsOutP2p", null,
                        "One of filters dst_number, dst_country_iso is required on sms_out/p2p/lookup/prices"),
                Arguments.of("smsOutOtp", filters("dst_number", " \t"),
                        "One of filters dst_number, dst_country_iso is required on sms_out/otp/lookup/prices"),
                Arguments.of("smsIn", filters("number", "12025550123"),
                        "Filter number is not allowed on sms_in/lookup/prices (allowed: dst_number, dst_country_iso)"),
                Arguments.of("smsIn", filters(null, "1"),
                        "One of filters dst_number, dst_country_iso is required on sms_in/lookup/prices"));
    }

    @ParameterizedTest
    @MethodSource("filterValidationCases")
    void testPricesLookupFilterValidation(String name, Map<String, String> filters, String message) {
        PricesService<?> service = service(client, name);

        IllegalArgumentException e = catchThrowableOfType(() -> service.lookup(filters),
                IllegalArgumentException.class);

        assertThat(e).hasMessage(message);
        assertThat(priceRequests()).isEmpty();
        assertThat(mints()).isZero();
    }

    static Stream<Arguments> lookupErrorCases() {
        return Stream.of(
                Arguments.of(400, null, "prices/errors/filter_not_allowed.json", null,
                        PricesErrorKind.BAD_REQUEST, "102", null, "Prices service: src_country_iso is not allowed."),
                Arguments.of(403, "{\"errors\":[{\"title\":\"Insufficient scope\",\"code\":\"403\",\"status\":\"403\"}]}",
                        null, null, PricesErrorKind.ACCESS_DENIED, "403", null, "Prices service: Insufficient scope"),
                Arguments.of(404, null, "prices/errors/price_not_found.json", null,
                        PricesErrorKind.PRICE_NOT_FOUND, "price_not_found", null,
                        "Prices service: No effective price covers this number in the assigned plan."),
                Arguments.of(404, null, "prices/errors/price_plan_not_assigned.json", null,
                        PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED, "price_plan_not_assigned", null,
                        "Prices service: No price plan is assigned to this account."),
                Arguments.of(404, "{\"errors\":[{\"title\":\"Not found\",\"code\":\"not_found\"}]}", null, null,
                        PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED, "not_found", null, "Prices service: Not found"),
                Arguments.of(404, "", null, null,
                        PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED, "price_plan_not_assigned", null,
                        "Prices service responded with status 404"),
                Arguments.of(406, "{\"errors\":[{\"title\":\"Not acceptable\",\"code\":\"406\",\"status\":\"406\"}]}",
                        null, null, PricesErrorKind.NOT_ACCEPTABLE, "406", null, "Prices service: Not acceptable"),
                Arguments.of(429, "{\"errors\":[{\"title\":\"Too many requests\",\"code\":\"too_many_requests\"}]}",
                        null, "7", PricesErrorKind.TOO_MANY_REQUESTS, "too_many_requests", 7,
                        "Prices service: Too many requests"),
                Arguments.of(503, null, "prices/errors/serving_failure.json", "1",
                        PricesErrorKind.SERVICE_UNAVAILABLE, "serving_failure", 1,
                        "Prices service: The price file is not available yet."),
                Arguments.of(500, "", null, null,
                        PricesErrorKind.UNEXPECTED_STATUS, null, null, "Prices service responded with status 500"),
                Arguments.of(500, "<html>oops</html>", null, " 5 ",
                        PricesErrorKind.UNEXPECTED_STATUS, null, 5, "Prices service responded with status 500"),
                Arguments.of(500, "{\"errors\":[\"not an object\",{\"title\":\"Kept\",\"code\":102}]}", null, "-1",
                        PricesErrorKind.UNEXPECTED_STATUS, "102", null, "Prices service: Kept"),
                Arguments.of(500, "{\"errors\":{\"title\":\"Not a list\"}}", null, "+3",
                        PricesErrorKind.UNEXPECTED_STATUS, null, null, "Prices service responded with status 500"),
                Arguments.of(304, "", null, null,
                        PricesErrorKind.UNEXPECTED_STATUS, null, null, "Prices service responded with status 304"),
                Arguments.of(204, "", null, null,
                        PricesErrorKind.UNEXPECTED_STATUS, null, null, "Prices service responded with status 204"));
    }

    @ParameterizedTest
    @MethodSource("lookupErrorCases")
    void testPricesLookupErrors(int status, String body, String fixture, String retryAfter, PricesErrorKind kind,
                                String code, Integer retryAfterSeconds, String message) {
        String content = fixture != null ? fixture(fixture) : body;
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).willReturn(retryAfter != null
                ? jsonApi(status, content).withHeader("Retry-After", retryAfter)
                : jsonApi(status, content)));

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().lookup(NUMBER),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(kind);
        assertThat(e.getHttpStatus()).isEqualTo(status);
        assertThat(e.getCode()).isEqualTo(code);
        assertThat(e.getRetryAfter()).isEqualTo(retryAfterSeconds != null ? Duration.ofSeconds(retryAfterSeconds) : null);
        assertThat(e).hasMessage(message).isInstanceOf(DidwwException.class).hasNoCause();
        assertThat(priceRequests()).as("lookups are never retried").hasSize(1);
        assertThat(mints()).isEqualTo(1);
    }

    @Test
    void testPricesLookupErrorObjects() {
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).willReturn(jsonApi(400,
                "{\"errors\":[{\"title\":\"Invalid\",\"detail\":\"number is invalid\",\"code\":\"100\",\"status\":\"400\","
                        + "\"source\":{\"parameter\":\"filter[number]\"}},{\"title\":\"Also bad\"}]}")));

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().lookup(NUMBER),
                DidwwPricesException.class);

        assertThat(e).hasMessage("Prices service: number is invalid; Also bad");
        assertThat(e.getErrors()).hasSize(2);
        DidwwApiException.ApiError first = e.getErrors().get(0);
        assertThat(first.getTitle()).isEqualTo("Invalid");
        assertThat(first.getStatus()).isEqualTo("400");
        assertThat(first.getSource()).containsEntry("parameter", "filter[number]");
        assertThat(e.getErrors().get(1).getDetail()).isNull();
    }

    @Test
    void testPricesLookupDoesNotFollowRedirects() {
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).willReturn(aResponse().withStatus(302)
                .withHeader("Location", "/v3/elsewhere")));
        wireMock.stubFor(get(urlPathEqualTo("/v3/elsewhere")).willReturn(jsonApi(200,
                fixture("prices/voice_in/lookup/200.json"))));

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().lookup(NUMBER),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.UNEXPECTED_STATUS);
        assertThat(e.getHttpStatus()).isEqualTo(302);
        assertThat(priceRequests()).hasSize(1);
        wireMock.verify(0, getRequestedFor(urlPathEqualTo("/v3/elsewhere")));
    }

    @Test
    void testPricesLookupRetriesRefusedTokenOnce() {
        stubTwoTokens(VOICE_IN);
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).withHeader("Authorization", equalTo("Bearer token-1"))
                .willReturn(jsonApi(401, UNAUTHORIZED_BODY)));
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).withHeader("Authorization", equalTo("Bearer token-2"))
                .willReturn(jsonApi(200, fixture("prices/voice_in/lookup/200.json"))));

        PriceLookup<VoiceInPrice> result = client.voiceInPrices().lookup(NUMBER);

        assertThat(result.getTotalRecords()).isEqualTo(2);
        assertThat(priceRequests()).extracting(r -> r.getHeader("Authorization"))
                .containsExactly("Bearer token-1", "Bearer token-2");
        assertThat(mintAudiences()).containsExactly(VOICE_IN, VOICE_IN);
    }

    @Test
    void testPricesLookupNotAuthorizedAfterRetry() {
        wireMock.stubFor(get(urlPathEqualTo(VOICE_IN_LOOKUP)).willReturn(jsonApi(401, UNAUTHORIZED_BODY)));

        DidwwPricesException e = catchThrowableOfType(() -> client.voiceInPrices().lookup(NUMBER),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.NOT_AUTHORIZED);
        assertThat(e.getHttpStatus()).isEqualTo(401);
        assertThat(e.getCode()).isEqualTo("401");
        assertThat(priceRequests()).hasSize(2);
        assertThat(mints()).isEqualTo(2);
    }

    @Test
    void testPricesLookupConnectionError() {
        DidwwClient refused = builder().voiceInPricesUrl(closedUrl()).build();

        DidwwPricesException e = catchThrowableOfType(() -> refused.voiceInPrices().lookup(NUMBER),
                DidwwPricesException.class);

        assertThat(e.getKind()).isEqualTo(PricesErrorKind.CONNECTION_ERROR);
        assertThat(e.getHttpStatus()).isNull();
        assertThat(e).hasCauseInstanceOf(IOException.class);
        assertThat(e.getMessage()).startsWith("Prices service: ").doesNotContain(TEST_ACCESS_TOKEN, TEST_API_KEY);
    }

    @Test
    void testPricesLookupReturnsTokenErrorAsIs() {
        wireMock.stubFor(post(urlEqualTo("/token")).withRequestBody(matchingJsonPath("$.aud[0]", equalTo(VOICE_IN)))
                .willReturn(aResponse().withStatus(401).withHeader("Content-Type", "application/json")
                        .withBody(fixture("token/invalid_client.json"))));

        DidwwAuthException e = catchThrowableOfType(() -> client.voiceInPrices().lookup(NUMBER),
                DidwwAuthException.class);

        assertThat(e.getKind()).isEqualTo(AuthErrorKind.INVALID_CLIENT);
        assertThat(priceRequests()).isEmpty();
    }

    @Test
    void testPricesExceptionMessage() {
        DidwwApiException.ApiError bad = new DidwwApiException.ApiError();
        bad.setTitle("Bad");
        bad.setDetail("number is invalid");
        DidwwApiException.ApiError alsoBad = new DidwwApiException.ApiError();
        alsoBad.setTitle("Also bad");
        DidwwApiException.ApiError empty = new DidwwApiException.ApiError();
        empty.setDetail("");

        assertThat(new DidwwPricesException(PricesErrorKind.BAD_REQUEST, 400, null,
                Arrays.asList(bad, empty, alsoBad), null, null))
                .hasMessage("Prices service: number is invalid; Also bad");
        assertThat(new DidwwPricesException(PricesErrorKind.UNEXPECTED_STATUS, 202, null, null, null, null))
                .hasMessage("Prices service responded with status 202");
        assertThat(new DidwwPricesException(PricesErrorKind.CONNECTION_ERROR, null, null, null, null,
                new IOException("connection refused")))
                .hasMessage("Prices service: connection refused");
        assertThat(new DidwwPricesException(PricesErrorKind.CONNECTION_ERROR, 200, null, null, null,
                new java.io.EOFException()))
                .hasMessage("Prices service: java.io.EOFException");
        assertThat(new DidwwPricesException(PricesErrorKind.CONNECTION_ERROR, null, null, null, null, null).getErrors())
                .isEmpty();
    }

    @Test
    void testServiceToStringHidesUserInfo() {
        DidwwClient withUserInfo = builder().voiceInPricesUrl("https://user:secret@api-vin.example.com/").build();

        assertThat(withUserInfo.voiceInPrices().toString())
                .isEqualTo("PricesService{https://api-vin.example.com/v3/lookup/prices}");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<html>maintenance</html>",
            "",
            "[]",
            "{}",
            "{\"data\":null}",
            "{\"data\":[],\"meta\":{\"total_records\":0}}",
            "{\"data\":{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{}}}",
            "{\"data\":[1]}",
            "{\"data\":[{\"id\":\"1\",\"type\":\"other\",\"attributes\":{}}]}",
            "{\"data\":[{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{}}],\"meta\":[]}",
            "{\"data\":[{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{}}],\"meta\":{\"total_records\":\"2\"}}",
            "{\"data\":[{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{}}],\"meta\":{\"total_records\":1.5}}"
    })
    void testPricesLookupUnparsableBody(String body) {
        stubLookup(VOICE_IN_LOOKUP, 200, body);

        DidwwClientException e = catchThrowableOfType(() -> client.voiceOutPrices().lookup(filters("number", "442071234567")),
                DidwwClientException.class);

        assertThat(e.getMessage()).startsWith("Failed to parse price lookup");
        assertThat(priceRequests()).hasSize(1);
    }

    @Test
    void testPricesLookupWithoutMeta() {
        stubLookup(VOICE_IN_LOOKUP, 200, "{\"data\":[{\"id\":\"1\",\"type\":\"prices\",\"attributes\":{\"prefix\":\"49\"}}]}");

        PriceLookup<VoiceOutPrice> result = client.voiceOutPrices().lookup(filters("number", "4930"));

        assertThat(result.getRows()).extracting(VoiceOutPrice::getPrefix).containsExactly("49");
        assertThat(result.getTotalRecords()).isZero();
    }

    @Test
    void testPricesServicesCallTheirOwnHost() {
        wireMock.stubFor(head(urlPathMatching("/.*/prices\\.csv\\.gz")).willReturn(priceFile(voiceInFixture(),
                LAST_MODIFIED_HEADER)));
        DidwwClient hosts = builder()
                .voiceInPricesUrl(origin() + "/vin")
                .voiceOutPricesUrl(origin() + "/vout/v3")
                .smsPricesUrl(origin() + "/sms/v3/")
                .build();

        hosts.voiceInPrices().head();
        hosts.voiceOutPrices().head();
        hosts.smsOutA2pPrices().head();
        hosts.smsOutP2pPrices().head();
        hosts.smsOutOtpPrices().head();
        hosts.smsInPrices().head();

        List<LoggedRequest> requests = priceRequests();
        assertThat(requests).extracting(LoggedRequest::getUrl).containsExactly(
                "/vin/v3/prices.csv.gz",
                "/vout/v3/prices.csv.gz",
                "/sms/v3/sms_out/a2p/prices.csv.gz",
                "/sms/v3/sms_out/p2p/prices.csv.gz",
                "/sms/v3/sms_out/otp/prices.csv.gz",
                "/sms/v3/sms_in/prices.csv.gz");
        assertThat(requests.stream().map(r -> r.getMethod().getName()).collect(Collectors.toSet()))
                .containsExactly("HEAD");
        assertThat(mintAudiences()).containsExactly(VOICE_IN, VOICE_OUT, SMS);
    }

    @Test
    void testPriceListPaths() {
        assertThat(client.voiceInPrices().getPriceList()).isSameAs(PriceList.VOICE_IN);
        assertThat(PriceList.VOICE_IN.getFilePath()).isEqualTo("prices.csv.gz");
        assertThat(PriceList.SMS_OUT_P2P.getLookupPath()).isEqualTo("sms_out/p2p/lookup/prices");
        assertThat(PriceList.SMS_IN.getFilePath()).isEqualTo("sms_in/prices.csv.gz");
    }
}
