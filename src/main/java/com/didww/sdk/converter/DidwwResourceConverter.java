package com.didww.sdk.converter;

import com.didww.sdk.resource.*;
import com.didww.sdk.resource.configuration.*;
import com.didww.sdk.resource.orderitem.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.jasminb.jsonapi.ResourceConverter;

public class DidwwResourceConverter {

    private static final Class<?>[] ALL_RESOURCE_CLASSES = {
            Country.class,
            Region.class,
            City.class,
            Pop.class,
            DidGroupType.class,
            DidGroup.class,
            StockKeepingUnit.class,
            QtyBasedPricing.class,
            AvailableDid.class,
            DidReservation.class,
            Did.class,
            VoiceInTrunkGroup.class,
            VoiceInTrunk.class,
            CapacityPool.class,
            SharedCapacityGroup.class,
            Balance.class,
            Export.class,
            Order.class,
            Address.class,
            AddressVerification.class,
            Area.class,
            EncryptedFile.class,
            Identity.class,
            NanpaPrefix.class,
            PermanentSupportingDocument.class,
            Proof.class,
            ProofType.class,
            PublicKey.class,
            Requirement.class,
            RequirementValidation.class,
            SupportingDocumentTemplate.class,
            VoiceOutTrunk.class,
            VoiceOutTrunkRegenerateCredential.class
    };

    public static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    public static ResourceConverter create(ObjectMapper objectMapper) {
        ResourceConverter converter = new ResourceConverter(objectMapper, ALL_RESOURCE_CLASSES);
        converter.enableDeserializationOption(com.github.jasminb.jsonapi.DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS);
        return converter;
    }
}
