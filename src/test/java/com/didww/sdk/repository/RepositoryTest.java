package com.didww.sdk.repository;

import com.didww.sdk.BaseTest;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.resource.BaseResource;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.Order;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class RepositoryTest extends BaseTest {

    @Test
    void testUpdateWithNullIdThrowsClientException() {
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setName("No ID trunk");
        // id is null

        assertThatThrownBy(() -> client.voiceInTrunks().update(trunk))
                .isInstanceOf(DidwwClientException.class)
                .hasMessageContaining("Resource ID is null");
    }

    @Test
    void testAllResourcesExtendBaseResource() {
        assertThat(new VoiceInTrunk()).isInstanceOf(BaseResource.class);
        assertThat(new VoiceInTrunkGroup()).isInstanceOf(BaseResource.class);
        assertThat(new Did()).isInstanceOf(BaseResource.class);
        assertThat(new Order()).isInstanceOf(BaseResource.class);
    }

    @Test
    void testGetIdViaBaseResource() {
        VoiceInTrunk trunk = VoiceInTrunk.build("test-uuid-123");

        BaseResource resource = trunk;
        assertThat(resource.getId()).isEqualTo("test-uuid-123");
    }

    @Test
    void testBuildDoesNotMarkIdAsDirty() {
        VoiceInTrunk trunk = VoiceInTrunk.build("test-uuid-123");

        assertThat(trunk.getId()).isEqualTo("test-uuid-123");
        assertThat(trunk.hasDirtyFields()).isFalse();
        assertThat(trunk.isFieldDirty("id")).isFalse();
    }

    @Test
    void testIncludedResourceHasDirtyTrackingEnabled() {
        String didId = "21d0b02c-b556-4d3e-acbf-504b78295dbe";
        wireMock.stubFor(get(urlPathEqualTo("/v3/dids/" + didId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("dids/show_with_included_trunk.json"))));

        Did did = client.dids().find(didId).getData();
        VoiceInTrunk includedTrunk = did.getVoiceInTrunk();
        assertThat(includedTrunk).isNotNull();
        assertThat(includedTrunk.hasDirtyFields()).isFalse();

        // Included resource should have dirty tracking enabled
        // so that mutations are tracked for subsequent updates
        includedTrunk.setName("changed");
        assertThat(includedTrunk.isFieldDirty("name")).isTrue();
    }

    @Test
    void testUpdateUsesIdFromResource() {
        String id = "41b94706-325e-4704-a433-d65105758836";
        String fixture = loadFixture("voice_in_trunks/create.json");

        wireMock.stubFor(patch(urlPathEqualTo("/v3/voice_in_trunks/" + id))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(fixture)));

        VoiceInTrunk trunk = VoiceInTrunk.build(id);
        trunk.setName("updated");

        VoiceInTrunk updated = client.voiceInTrunks().update(trunk).getData();
        assertThat(updated.getId()).isEqualTo(id);

        wireMock.verify(patchRequestedFor(urlPathEqualTo("/v3/voice_in_trunks/" + id)));
    }
}
