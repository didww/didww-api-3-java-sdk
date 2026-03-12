package com.didww.sdk.resource;

import com.didww.sdk.BaseTest;
import com.didww.sdk.repository.ApiResponse;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class DidGroupTypeTest extends BaseTest {

    @Test
    void testListDidGroupTypes() {
        stubGetFixture("/v3/did_group_types", "did_group_types/index.json");

        ApiResponse<List<DidGroupType>> response = client.didGroupTypes().list();
        List<DidGroupType> didGroupTypes = response.getData();

        assertThat(didGroupTypes).isNotEmpty();
    }

    @Test
    void testFindDidGroupType() {
        stubGetFixture("/v3/did_group_types/d6530a8c-924c-469a-98c0-9525602e6192", "did_group_types/show.json");

        ApiResponse<DidGroupType> response = client.didGroupTypes().find("d6530a8c-924c-469a-98c0-9525602e6192");
        DidGroupType didGroupType = response.getData();

        assertThat(didGroupType.getName()).isEqualTo("Global");
    }
}
