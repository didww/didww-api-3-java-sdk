package com.didww.sdk.repository;

import com.didww.sdk.resource.BaseResource;
import com.didww.sdk.BaseTest;
import com.didww.sdk.DidwwClient;
import com.didww.sdk.exception.DidwwApiException;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.resource.VoiceInTrunk;
import com.didww.sdk.resource.VoiceInTrunkGroup;
import com.didww.sdk.resource.Did;
import com.didww.sdk.resource.Order;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
    void testErrorBodyIsLimitedInSize() {
        StringBuilder big = new StringBuilder("{\"errors\":[{\"detail\":\"real error\"}],\"pad\":\"");
        for (int i = 0; i < (2 << 20); i++) {
            big.append('x');
        }
        big.append("\"}");
        wireMock.stubFor(get(urlPathEqualTo("/v3/countries/1")).willReturn(aResponse().withStatus(404)
                .withHeader("Content-Type", "application/vnd.api+json").withBody(big.toString())));

        DidwwApiException e = catchThrowableOfType(() -> client.countries().find("1"), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(404);
        assertThat(e.getErrors()).as("the cut body is not parsed").isEmpty();
        assertThat(e.getMessage()).hasSize(1 << 20);
    }

    @Test
    void testErrorBodyIsReadWithinADeadline() {
        DidwwClient slow = clientBuilder().readTimeout(Duration.ofSeconds(1)).build();
        wireMock.stubFor(get(urlPathEqualTo("/v3/countries/1")).willReturn(aResponse().withStatus(503)
                .withBody(new String(new char[200]).replace('\0', 'x'))
                .withChunkedDribbleDelay(50, 6000)));
        long start = System.nanoTime();

        DidwwApiException e = catchThrowableOfType(() -> slow.countries().find("1"), DidwwApiException.class);

        assertThat(e.getHttpStatus()).isEqualTo(503);
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(4));
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
        VoiceInTrunk trunk = new VoiceInTrunk().withId("test-uuid-123");

        BaseResource resource = trunk;
        assertThat(resource.getId()).isEqualTo("test-uuid-123");
    }

    @Test
    void testWithIdDoesNotMarkIdAsDirty() {
        VoiceInTrunk trunk = new VoiceInTrunk().withId("test-uuid-123");

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

        VoiceInTrunk trunk = new VoiceInTrunk().withId(id);
        trunk.setName("updated");

        VoiceInTrunk updated = client.voiceInTrunks().update(trunk).getData();
        assertThat(updated.getId()).isEqualTo(id);

        wireMock.verify(patchRequestedFor(urlPathEqualTo("/v3/voice_in_trunks/" + id)));
    }
}
