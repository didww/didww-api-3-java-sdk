package com.didww.sdk;

import com.didww.sdk.converter.DidwwResourceConverter;
import com.didww.sdk.exception.DidwwClientException;
import com.didww.sdk.http.ApiKeyInterceptor;
import com.didww.sdk.http.JsonApiMediaType;
import com.didww.sdk.repository.ReadOnlyRepository;
import com.didww.sdk.repository.Repository;
import com.didww.sdk.repository.SingletonRepository;
import com.didww.sdk.resource.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jasminb.jsonapi.ResourceConverter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

public class DidwwClient {
    private final DidwwCredentials credentials;
    private final OkHttpClient httpClient;
    private final ResourceConverter converter;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    private DidwwClient(Builder builder) {
        this.credentials = Objects.requireNonNull(builder.credentials, "credentials must not be null");
        this.baseUrl = builder.baseUrl != null ? builder.baseUrl : credentials.getBaseUrl();
        this.objectMapper = DidwwResourceConverter.createObjectMapper();
        this.converter = DidwwResourceConverter.create(objectMapper);

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .addInterceptor(new ApiKeyInterceptor(credentials.getApiKey()));

        if (builder.connectTimeout != null) {
            httpBuilder.connectTimeout(builder.connectTimeout);
        }
        if (builder.readTimeout != null) {
            httpBuilder.readTimeout(builder.readTimeout);
        }

        this.httpClient = httpBuilder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    // Read-only repositories
    public ReadOnlyRepository<Country> countries() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "countries", Country.class, objectMapper);
    }

    public ReadOnlyRepository<Region> regions() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "regions", Region.class, objectMapper);
    }

    public ReadOnlyRepository<City> cities() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "cities", City.class, objectMapper);
    }

    public ReadOnlyRepository<Pop> pops() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "pops", Pop.class, objectMapper);
    }

    public ReadOnlyRepository<DidGroupType> didGroupTypes() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "did_group_types", DidGroupType.class, objectMapper);
    }

    public ReadOnlyRepository<DidGroup> didGroups() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "did_groups", DidGroup.class, objectMapper);
    }

    public ReadOnlyRepository<AvailableDid> availableDids() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "available_dids", AvailableDid.class, objectMapper);
    }

    public ReadOnlyRepository<Area> areas() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "areas", Area.class, objectMapper);
    }

    public ReadOnlyRepository<NanpaPrefix> nanpaPrefixes() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "nanpa_prefixes", NanpaPrefix.class, objectMapper);
    }

    public ReadOnlyRepository<ProofType> proofTypes() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "proof_types", ProofType.class, objectMapper);
    }

    public ReadOnlyRepository<PublicKey> publicKeys() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "public_keys", PublicKey.class, objectMapper);
    }

    public ReadOnlyRepository<Requirement> requirements() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "requirements", Requirement.class, objectMapper);
    }

    public ReadOnlyRepository<SupportingDocumentTemplate> supportingDocumentTemplates() {
        return new ReadOnlyRepository<>(httpClient, converter, baseUrl, "supporting_document_templates", SupportingDocumentTemplate.class, objectMapper);
    }

    // Singleton repository
    public SingletonRepository<Balance> balance() {
        return new SingletonRepository<>(httpClient, converter, baseUrl, "balance", Balance.class, objectMapper);
    }

    // CRUD repositories
    public Repository<Did> dids() {
        return new Repository<>(httpClient, converter, baseUrl, "dids", Did.class, objectMapper);
    }

    public Repository<VoiceInTrunk> voiceInTrunks() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_in_trunks", VoiceInTrunk.class, objectMapper);
    }

    public Repository<VoiceInTrunkGroup> voiceInTrunkGroups() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_in_trunk_groups", VoiceInTrunkGroup.class, objectMapper);
    }

    public Repository<VoiceOutTrunk> voiceOutTrunks() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_out_trunks", VoiceOutTrunk.class, objectMapper);
    }

    public Repository<VoiceOutTrunkRegenerateCredential> voiceOutTrunkRegenerateCredentials() {
        return new Repository<>(httpClient, converter, baseUrl, "voice_out_trunk_regenerate_credentials", VoiceOutTrunkRegenerateCredential.class, objectMapper);
    }

    public Repository<DidReservation> didReservations() {
        return new Repository<>(httpClient, converter, baseUrl, "did_reservations", DidReservation.class, objectMapper);
    }

    public Repository<CapacityPool> capacityPools() {
        return new Repository<>(httpClient, converter, baseUrl, "capacity_pools", CapacityPool.class, objectMapper);
    }

    public Repository<SharedCapacityGroup> sharedCapacityGroups() {
        return new Repository<>(httpClient, converter, baseUrl, "shared_capacity_groups", SharedCapacityGroup.class, objectMapper);
    }

    public Repository<Order> orders() {
        return new Repository<>(httpClient, converter, baseUrl, "orders", Order.class, objectMapper);
    }

    public Repository<Export> exports() {
        return new Repository<>(httpClient, converter, baseUrl, "exports", Export.class, objectMapper);
    }

    public Repository<Address> addresses() {
        return new Repository<>(httpClient, converter, baseUrl, "addresses", Address.class, objectMapper);
    }

    public Repository<AddressVerification> addressVerifications() {
        return new Repository<>(httpClient, converter, baseUrl, "address_verifications", AddressVerification.class, objectMapper);
    }

    public Repository<Identity> identities() {
        return new Repository<>(httpClient, converter, baseUrl, "identities", Identity.class, objectMapper);
    }

    public Repository<EncryptedFile> encryptedFiles() {
        return new Repository<>(httpClient, converter, baseUrl, "encrypted_files", EncryptedFile.class, objectMapper);
    }

    public Repository<PermanentSupportingDocument> permanentSupportingDocuments() {
        return new Repository<>(httpClient, converter, baseUrl, "permanent_supporting_documents", PermanentSupportingDocument.class, objectMapper);
    }

    public Repository<Proof> proofs() {
        return new Repository<>(httpClient, converter, baseUrl, "proofs", Proof.class, objectMapper);
    }

    public Repository<RequirementValidation> requirementValidations() {
        return new Repository<>(httpClient, converter, baseUrl, "requirement_validations", RequirementValidation.class, objectMapper);
    }

    /**
     * Downloads an export file to the given path.
     */
    public void downloadExport(String url, Path destination) {
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new DidwwClientException("Failed to download export: HTTP " + response.code());
            }
            if (response.body() == null) {
                throw new DidwwClientException("Empty response body for export download");
            }
            try (OutputStream out = Files.newOutputStream(destination)) {
                out.write(response.body().bytes());
            }
        } catch (DidwwClientException e) {
            throw e;
        } catch (IOException e) {
            throw new DidwwClientException("Failed to download export", e);
        }
    }

    public DidwwCredentials getCredentials() {
        return credentials;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public static class Builder {
        private DidwwCredentials credentials;
        private Duration connectTimeout;
        private Duration readTimeout;
        private String baseUrl;

        public Builder credentials(DidwwCredentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Overrides the base URL from the credentials environment. Useful for testing.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public DidwwClient build() {
            return new DidwwClient(this);
        }
    }
}
