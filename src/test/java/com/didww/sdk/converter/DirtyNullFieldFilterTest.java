package com.didww.sdk.converter;

import com.didww.sdk.resource.VoiceInTrunk;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirtyNullFieldFilterTest {

    private final ObjectMapper objectMapper = DidwwResourceConverter.createObjectMapper();

    @AfterEach
    void tearDown() {
        DirtySerializationContext.disableDirtyOnlyMode();
    }

    @Test
    void testDirtyOnlyModeAlwaysIncludesId() throws Exception {
        // Simulate deserialization: id is set while tracking is disabled
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setId("test-id-123");

        // Enable tracking after deserialization
        trunk.enableDirtyTracking();

        // Only name is dirty
        trunk.setName("updated name");

        DirtySerializationContext.enableDirtyOnlyMode();
        JsonNode node = objectMapper.valueToTree(trunk);

        // id should be present even though it was not marked dirty
        assertThat(node.has("id")).isTrue();
        assertThat(node.get("id").asText()).isEqualTo("test-id-123");

        // dirty field should be present
        assertThat(node.has("name")).isTrue();
        assertThat(node.get("name").asText()).isEqualTo("updated name");

        // non-dirty fields should be omitted
        assertThat(node.has("priority")).isFalse();
        assertThat(node.has("weight")).isFalse();
    }

    @Test
    void testDirtyOnlyModeOmitsNonDirtyFields() throws Exception {
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setId("test-id");
        trunk.enableDirtyTracking();
        trunk.setName("only this");

        DirtySerializationContext.enableDirtyOnlyMode();
        JsonNode node = objectMapper.valueToTree(trunk);

        assertThat(node.has("name")).isTrue();
        assertThat(node.has("description")).isFalse();
        assertThat(node.has("cli_format")).isFalse();
    }

    @Test
    void testDirtyNullFieldIsSerializedAsNull() throws Exception {
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.enableDirtyTracking();
        trunk.setName("test");
        trunk.setDescription(null);

        JsonNode node = objectMapper.valueToTree(trunk);

        assertThat(node.has("description")).isTrue();
        assertThat(node.get("description").isNull()).isTrue();
    }
}
