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
        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setId("test-uuid-123");

        BaseResource resource = trunk;
        assertThat(resource.getId()).isEqualTo("test-uuid-123");
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

        VoiceInTrunk trunk = new VoiceInTrunk();
        trunk.setId(id);
        trunk.setName("updated");

        VoiceInTrunk updated = client.voiceInTrunks().update(trunk).getData();
        assertThat(updated.getId()).isEqualTo(id);

        wireMock.verify(patchRequestedFor(urlPathEqualTo("/v3/voice_in_trunks/" + id)));
    }
}
