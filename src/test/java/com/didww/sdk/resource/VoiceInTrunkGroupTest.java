package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class VoiceInTrunkGroupTest extends BaseTest {

    @Test
    void testListVoiceInTrunkGroups() {
        wireMock.stubFor(get(urlPathEqualTo("/v3/voice_in_trunk_groups"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunk_groups/index.json"))));

        ApiResponse<List<VoiceInTrunkGroup>> response = client.voiceInTrunkGroups().list();
        List<VoiceInTrunkGroup> groups = response.getData();

        assertThat(groups).isNotEmpty();

        VoiceInTrunkGroup first = groups.get(0);
        assertThat(first.getId()).isEqualTo("837c5764-a6c3-456f-aa37-71fc8f8ca07b");
        assertThat(first.getName()).isEqualTo("sample trunk group");
    }

    @Test
    void testCreateVoiceInTrunkGroup() {
        wireMock.stubFor(post(urlPathEqualTo("/v3/voice_in_trunk_groups"))
                .withRequestBody(equalToJson(loadFixture("voice_in_trunk_groups/create_request.json"), true, false))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/vnd.api+json")
                        .withBody(loadFixture("voice_in_trunk_groups/create.json"))));

        VoiceInTrunkGroup group = new VoiceInTrunkGroup();
        group.setName("trunk group sample with 2 trunks");
        group.setCapacityLimit(1000);
        group.setVoiceInTrunks(Arrays.asList(
                VoiceInTrunk.build("7c15bca2-7f17-46fb-9486-7e2a17158c7e"),
                VoiceInTrunk.build("b07a4cab-48c6-4b3a-9670-11b90b81bdef")
        ));

        ApiResponse<VoiceInTrunkGroup> response = client.voiceInTrunkGroups().create(group);
        VoiceInTrunkGroup created = response.getData();

        assertThat(created.getId()).isEqualTo("b2319703-ce6c-480d-bb53-614e7abcfc96");
        assertThat(created.getName()).isEqualTo("trunk group sample with 2 trunks");
        assertThat(created.getCapacityLimit()).isEqualTo(1000);
    }

    @Test
    void testDeleteVoiceInTrunkGroup() {
        String id = "b2319703-ce6c-480d-bb53-614e7abcfc96";
        wireMock.stubFor(delete(urlPathEqualTo("/v3/voice_in_trunk_groups/" + id))
                .willReturn(aResponse().withStatus(204)));

        client.voiceInTrunkGroups().delete(id);

        wireMock.verify(deleteRequestedFor(urlPathEqualTo("/v3/voice_in_trunk_groups/" + id)));
    }
}
