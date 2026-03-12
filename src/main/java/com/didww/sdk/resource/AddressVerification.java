package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.AddressVerificationStatus;
import com.didww.sdk.resource.enums.CallbackMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Type("address_verifications")

@Getter
public class AddressVerification extends BaseResource {

    @JsonProperty("service_description")
    private String serviceDescription;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private CallbackMethod callbackMethod;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private AddressVerificationStatus status;

    @JsonProperty(value = "reject_reasons", access = JsonProperty.Access.WRITE_ONLY)
    private String rejectReasons;

    @JsonProperty(value = "reference", access = JsonProperty.Access.WRITE_ONLY)
    private String reference;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("address")
    private Address address;

    @Relationship("dids")
    private List<Did> dids;

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = markDirty("serviceDescription", serviceDescription);
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = markDirty("callbackUrl", callbackUrl);
    }

    public void setCallbackMethod(CallbackMethod callbackMethod) {
        this.callbackMethod = markDirty("callbackMethod", callbackMethod);
    }

    public List<String> getRejectReasons() {
        if (rejectReasons == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(rejectReasons.split("; "));
    }

    public void setAddress(Address address) {
        this.address = markDirty("address", address);
    }

    public void setDids(List<Did> dids) {
        this.dids = markDirty("dids", dids);
    }
}
