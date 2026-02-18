package com.didww.sdk;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DidwwClientTest {

    @Test
    void testBuilderWithProductionEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.PRODUCTION))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://api.didww.com/v3");
    }

    @Test
    void testBuilderWithSandboxEnvironment() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("https://sandbox-api.didww.com/v3");
    }

    @Test
    void testBuilderWithCustomBaseUrl() {
        DidwwClient client = DidwwClient.builder()
                .credentials(new DidwwCredentials("my-key", DidwwEnvironment.SANDBOX))
                .baseUrl("http://localhost:8080/v3")
                .build();
        assertThat(client.getBaseUrl()).isEqualTo("http://localhost:8080/v3");
    }

    @Test
    void testBuilderRequiresCredentials() {
        assertThatThrownBy(() -> DidwwClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }
}
