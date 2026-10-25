package com.didww.sdk.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccessTokenTest {
    private static final Instant MINTED_AT = Instant.parse("2026-10-22T08:00:00Z");

    private static AccessToken token() {
        return new AccessToken("secret-access-token", "Bearer", 900, Arrays.asList("api.didww.com"), "full",
                "2026-10-22", MINTED_AT);
    }

    @Test
    void testToStringHidesAccessToken() {
        String text = token().toString();

        assertThat(text)
                .doesNotContain("secret-access-token")
                .isEqualTo("AccessToken{accessToken=[FILTERED], tokenType=Bearer, expiresIn=900, "
                        + "audience=[api.didww.com], scope=full, apiVersion=2026-10-22, mintedAt=2026-10-22T08:00:00Z}");
    }

    @Test
    void testJsonRoundTripWithWireNames() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(token());
        JsonNode node = mapper.readTree(json);

        assertThat(node.get("access_token").asText()).isEqualTo("secret-access-token");
        assertThat(node.get("token_type").asText()).isEqualTo("Bearer");
        assertThat(node.get("expires_in").asInt()).isEqualTo(900);
        assertThat(node.get("aud").get(0).asText()).isEqualTo("api.didww.com");
        assertThat(node.get("scope").asText()).isEqualTo("full");
        assertThat(node.get("api_version").asText()).isEqualTo("2026-10-22");
        assertThat(node.get("minted_at").asText()).isEqualTo("2026-10-22T08:00:00Z");
        assertThat(node.size()).isEqualTo(7);

        AccessToken back = mapper.readValue(json, AccessToken.class);
        assertThat(back.getAccessToken()).isEqualTo("secret-access-token");
        assertThat(back.getExpiresIn()).isEqualTo(900);
        assertThat(back.getAudience()).containsExactly("api.didww.com");
        assertThat(back.getMintedAt()).isEqualTo(MINTED_AT);
    }

    @Test
    void testJsonWithSingleAudienceAndWithoutMintedAt() throws Exception {
        AccessToken token = new ObjectMapper().readValue(
                "{\"access_token\":\"t\",\"aud\":\"api-sms.didww.com\",\"expires_in\":60}", AccessToken.class);

        assertThat(token.getAudience()).containsExactly("api-sms.didww.com");
        assertThat(token.getTokenType()).isEmpty();
        assertThat(token.getMintedAt()).isNotNull();
    }

    @Test
    void testFromFixture() throws Exception {
        AccessToken token = new ObjectMapper().readValue(
                getClass().getClassLoader().getResourceAsStream("fixtures/token/create.json"), AccessToken.class);

        assertThat(token.getAccessToken()).isEqualTo("test-access-token");
        assertThat(token.getExpiresIn()).isEqualTo(900);
        assertThat(token.getAudience()).containsExactly("api.didww.com");
    }

    @Test
    void testImmutable() {
        List<String> audience = new ArrayList<>(Arrays.asList("api.didww.com"));
        AccessToken token = new AccessToken("t", "Bearer", 900, audience, "full", "2026-10-22", MINTED_AT);
        audience.add("api-sms.didww.com");

        assertThat(token.getAudience()).containsExactly("api.didww.com");
        assertThatThrownBy(() -> token.getAudience().add("x")).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testNullFieldsBecomeEmpty() {
        AccessToken token = new AccessToken(null, null, 0, null, null, null, null);

        assertThat(token.getAccessToken()).isEmpty();
        assertThat(token.getAudience()).isEmpty();
        assertThat(token.getMintedAt()).isNotNull();
        assertThat(token.toString()).contains("accessToken=null");
    }
}
