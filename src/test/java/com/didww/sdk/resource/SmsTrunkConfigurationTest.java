package com.didww.sdk.resource;

import com.didww.sdk.converter.DidwwResourceConverter;
import com.didww.sdk.converter.SmsTrunkConfigurationDeserializer;
import com.didww.sdk.converter.SmsTrunkConfigurationSerializer;
import com.didww.sdk.resource.enums.SmsServerLocation;
import com.didww.sdk.resource.smstrunkconfiguration.GenericSmsTrunkConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpInConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.HttpOutConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppEsmeConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmppSmscConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsToEmailConfiguration;
import com.didww.sdk.resource.smstrunkconfiguration.SmsTrunkConfiguration;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsTrunkConfigurationTest {

    private final ObjectMapper mapper = DidwwResourceConverter.createObjectMapper();

    static class Holder {
        @JsonProperty("configuration")
        @JsonDeserialize(using = SmsTrunkConfigurationDeserializer.class)
        @JsonSerialize(using = SmsTrunkConfigurationSerializer.class)
        public SmsTrunkConfiguration configuration;
    }

    static class LeakyHttpOutConfiguration extends HttpOutConfiguration {
        @JsonAnyGetter
        public Map<String, Object> extra() {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("auth_name", "leaked-name");
            extra.put("auth_password", "leaked-password");
            return extra;
        }
    }

    static class LeakySmppEsmeConfiguration extends SmppEsmeConfiguration {
        @JsonAnyGetter
        public Map<String, Object> extra() {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("system_id", "leaked-id");
            extra.put("password", "leaked-password");
            return extra;
        }
    }

    private SmsTrunkConfiguration read(String configurationJson) throws Exception {
        return mapper.readValue("{\"configuration\":" + configurationJson + "}", Holder.class).configuration;
    }

    private JsonNode write(SmsTrunkConfiguration configuration) {
        Holder holder = new Holder();
        holder.configuration = configuration;
        return mapper.valueToTree(holder).get("configuration");
    }

    private String writeString(SmsTrunkConfiguration configuration) throws Exception {
        Holder holder = new Holder();
        holder.configuration = configuration;
        return mapper.writeValueAsString(holder);
    }

    @Test
    void testDispatchesKnownTypes() throws Exception {
        assertThat(read("{\"type\":\"sms_to_email\",\"attributes\":{\"send_to\":\"a@example.com\"}}"))
                .isInstanceOf(SmsToEmailConfiguration.class);
        assertThat(read("{\"type\":\"http_in\",\"attributes\":{\"url\":\"https://example.com\"}}"))
                .isInstanceOf(HttpInConfiguration.class);
        assertThat(read("{\"type\":\"http_out\",\"attributes\":{}}")).isInstanceOf(HttpOutConfiguration.class);
        assertThat(read("{\"type\":\"smpp_esme\"}")).isInstanceOf(SmppEsmeConfiguration.class);
        assertThat(read("{\"type\":\"smpp_smsc\",\"attributes\":null}")).isInstanceOf(SmppSmscConfiguration.class);
    }

    @Test
    void testUnknownTypeBecomesGeneric() throws Exception {
        SmsTrunkConfiguration cfg = read("{\"type\":\"future_transport\",\"attributes\":{\"endpoint\":\"https://example.com\",\"cleared\":null}}");

        assertThat(cfg).isInstanceOf(GenericSmsTrunkConfiguration.class);
        GenericSmsTrunkConfiguration generic = (GenericSmsTrunkConfiguration) cfg;
        assertThat(generic.getType()).isEqualTo("future_transport");
        assertThat(generic.getAttributes().get("endpoint").asText()).isEqualTo("https://example.com");
        assertThat(generic.getAttributes().get("cleared").isNull()).isTrue();

        GenericSmsTrunkConfiguration empty = (GenericSmsTrunkConfiguration) read("{\"type\":\"future_transport\"}");
        assertThat(empty.getAttributes()).isEmpty();
    }

    @Test
    void testNullConfigurationReadsAsNull() throws Exception {
        assertThat(read("null")).isNull();
    }

    @Test
    void testMissingTypeThrows() {
        assertThatThrownBy(() -> read("{\"attributes\":{}}")).hasMessageContaining("Missing 'type'");
        assertThatThrownBy(() -> read("{\"type\":null,\"attributes\":{}}")).hasMessageContaining("Missing 'type'");
    }

    @Test
    void testRoundTrip() throws Exception {
        String[] inputs = {
                "{\"type\":\"sms_to_email\",\"attributes\":{\"send_to\":\"sms@example.com\",\"subject\":\"Inbound SMS\",\"message\":\"Hi\"}}",
                "{\"type\":\"http_in\",\"attributes\":{\"url\":\"https://example.com/sms\",\"http_method\":\"put\",\"query_parameters\":{\"source\":\"didww\"},"
                        + "\"headers\":{\"X-Source\":\"didww\"},\"body_type\":\"html_url_encoded\",\"body\":{\"text\":\"{SMS_TEXT}\"},\"follow_redirect\":true,\"validate_https_certificate\":false}}",
                "{\"type\":\"http_out\",\"attributes\":{\"callback_url\":\"https://example.com/status\",\"allowed_ips\":[\"203.0.113.10\"]}}",
                "{\"type\":\"smpp_esme\",\"attributes\":{\"system_type\":\"cmt\",\"allowed_ips\":[\"203.0.113.10\"],\"server_location\":\"ams\"}}",
                "{\"type\":\"smpp_smsc\",\"attributes\":{\"host\":\"smsc.example.com\",\"tx_port\":2775,\"rx_port\":2776,\"transceiver_mode\":false,"
                        + "\"connection_timeout\":0,\"use_ssl\":true,\"system_id\":\"didww\",\"system_type\":\"cmt\",\"password\":\"secret\",\"server_location\":\"nyc\"}}",
                "{\"type\":\"future_transport\",\"attributes\":{\"endpoint\":\"https://example.com/future\",\"nested\":{\"a\":[1,2]}}}",
        };
        for (String input : inputs) {
            assertThat(write(read(input))).isEqualTo(mapper.readTree(input));
        }
    }

    @Test
    void testReadOnlyCredentialsAreNotWritten() throws Exception {
        SmsTrunkConfiguration esme = read("{\"type\":\"smpp_esme\",\"attributes\":{\"server_location\":\"nyc\",\"system_id\":\"esme1234\",\"password\":\"gen12345\"}}");
        assertThat(((SmppEsmeConfiguration) esme).getPassword()).isEqualTo("gen12345");
        assertThat(write(esme)).isEqualTo(mapper.readTree("{\"type\":\"smpp_esme\",\"attributes\":{\"server_location\":\"nyc\"}}"));

        SmsTrunkConfiguration httpOut = read("{\"type\":\"http_out\",\"attributes\":{\"callback_url\":null,\"allowed_ips\":null,\"auth_name\":\"n\",\"auth_password\":\"p\"}}");
        assertThat(write(httpOut)).isEqualTo(mapper.readTree("{\"type\":\"http_out\",\"attributes\":{}}"));
    }

    @Test
    void testReadOnlyCredentialsOfSubclassesAreNotWritten() {
        LeakyHttpOutConfiguration httpOut = new LeakyHttpOutConfiguration();
        httpOut.setCallbackUrl("https://example.com/status");
        assertThat(mapper.<JsonNode>valueToTree(httpOut).has("auth_password")).isTrue();
        JsonNode httpOutAttributes = write(httpOut).get("attributes");
        assertThat(httpOutAttributes.has("auth_name")).isFalse();
        assertThat(httpOutAttributes.has("auth_password")).isFalse();
        assertThat(httpOutAttributes.get("callback_url").asText()).isEqualTo("https://example.com/status");

        LeakySmppEsmeConfiguration esme = new LeakySmppEsmeConfiguration();
        esme.setServerLocation(SmsServerLocation.AMS);
        assertThat(mapper.<JsonNode>valueToTree(esme).has("password")).isTrue();
        JsonNode esmeAttributes = write(esme).get("attributes");
        assertThat(esmeAttributes.has("system_id")).isFalse();
        assertThat(esmeAttributes.has("password")).isFalse();
        assertThat(esmeAttributes.get("server_location").asText()).isEqualTo("ams");
    }

    @Test
    void testSmppSmscCredentialsAreWritable() {
        SmppSmscConfiguration smsc = new SmppSmscConfiguration();
        smsc.setHost("smsc.example.com");
        smsc.setSystemId("didww");
        smsc.setPassword("secret");

        JsonNode attributes = write(smsc).get("attributes");

        assertThat(attributes.get("system_id").asText()).isEqualTo("didww");
        assertThat(attributes.get("password").asText()).isEqualTo("secret");
    }

    @Test
    void testToStringMasksSecretsButWireKeepsThem() throws Exception {
        HttpOutConfiguration httpOut = (HttpOutConfiguration) read(
                "{\"type\":\"http_out\",\"attributes\":{\"callback_url\":\"https://example.com/status\",\"auth_name\":\"generated-name\",\"auth_password\":\"generated-password\"}}");
        assertThat(httpOut.toString()).contains("https://example.com/status", "[FILTERED]")
                .doesNotContain("generated-name", "generated-password");

        SmppEsmeConfiguration esme = (SmppEsmeConfiguration) read(
                "{\"type\":\"smpp_esme\",\"attributes\":{\"server_location\":\"nyc\",\"system_id\":\"esme1234\",\"password\":\"gen12345\"}}");
        assertThat(esme.toString()).contains("NYC", "[FILTERED]").doesNotContain("esme1234", "gen12345");

        SmppSmscConfiguration smsc = new SmppSmscConfiguration();
        smsc.setHost("smsc.example.com");
        smsc.setSystemId("didww");
        smsc.setPassword("secret");
        assertThat(smsc.toString()).contains("smsc.example.com", "didww", "[FILTERED]").doesNotContain("secret");
        assertThat(writeString(smsc)).contains("\"password\":\"secret\"");

        HttpInConfiguration httpIn = new HttpInConfiguration();
        httpIn.setUrl("https://example.com/sms");
        httpIn.setHeaders(Collections.singletonMap("Authorization", "Bearer abc123"));
        assertThat(httpIn.toString()).contains("https://example.com/sms", "[FILTERED]")
                .doesNotContain("Authorization", "Bearer abc123");
        assertThat(writeString(httpIn)).contains("Bearer abc123");
        assertThat(new HttpInConfiguration().toString()).contains("headers=null");
    }

    @Test
    void testGenericToStringMasksValuesAndShowsNamesAndNulls() {
        ObjectNode attributes = mapper.createObjectNode();
        attributes.put("url", "https://example.com/sms");
        attributes.putObject("headers").put("Authorization", "Bearer abc123");
        attributes.putNull("body");
        GenericSmsTrunkConfiguration generic = new GenericSmsTrunkConfiguration("http_in", attributes);

        assertThat(generic.toString())
                .isEqualTo("GenericSmsTrunkConfiguration(type=http_in, attributes={body=null, headers=[FILTERED], url=[FILTERED]})");
        assertThat(write(generic).get("attributes")).isEqualTo(attributes);
        assertThat(write(generic).get("attributes").get("body").isNull()).isTrue();
    }

    @Test
    void testGenericKeepsExactNumbers() throws Exception {
        String input = "{\"configuration\":{\"type\":\"future_transport\",\"attributes\":{\"id\":9007199254740993,"
                + "\"nested\":{\"ids\":[123456789012345678901234567890]},\"ratio\":1.10,\"whole\":100.0,\"small\":0.1}}}";

        Holder holder = mapper.readValue(input, Holder.class);
        GenericSmsTrunkConfiguration generic = (GenericSmsTrunkConfiguration) holder.configuration;

        assertThat(generic.getAttributes().get("id").asLong()).isEqualTo(9007199254740993L);
        assertThat(generic.getAttributes().get("ratio").decimalValue()).isEqualTo(new BigDecimal("1.10"));
        assertThat(mapper.writeValueAsString(holder)).isEqualTo(input);
    }

    @Test
    void testGenericBuiltByHandKeepsDecimalScale() throws Exception {
        GenericSmsTrunkConfiguration generic = new GenericSmsTrunkConfiguration("future_transport");
        generic.getAttributes().put("ratio", new BigDecimal("1.10"));

        assertThat(writeString(generic)).contains("\"ratio\":1.10");
    }

    @Test
    void testGenericRequiresTypeAndAttributes() {
        assertThatThrownBy(() -> new GenericSmsTrunkConfiguration(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new GenericSmsTrunkConfiguration("x", null)).isInstanceOf(NullPointerException.class);
    }
}
