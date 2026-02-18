package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.DidwwCredentials;
import com.didww.sdk.DidwwEnvironment;

final class ExampleClientFactory {

    private ExampleClientFactory() {
    }

    static DidwwClient fromEnv() {
        String apiKey = System.getenv("DIDWW_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("DIDWW_API_KEY environment variable is required");
        }

        return DidwwClient.builder()
                .credentials(new DidwwCredentials(apiKey, DidwwEnvironment.SANDBOX))
                .build();
    }
}
