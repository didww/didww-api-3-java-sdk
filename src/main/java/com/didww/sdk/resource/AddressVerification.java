package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("address_verifications")
public class AddressVerification extends BaseResource {

    @JsonProperty("service_description")
    private String serviceDescription;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("callback_method")
    private String callbackMethod;

    @JsonProperty(value = "status", access = JsonProperty.Access.WRITE_ONLY)
    private String status;

    @JsonProperty(value = "reject_reasons", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> rejectReasons;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "reference", access = JsonProperty.Access.WRITE_ONLY)
    private String reference;

    @Relationship("address")
    private Address address;

    @Relationship("dids")
    private List<Did> dids;

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackMethod() {
        return callbackMethod;
    }

    public void setCallbackMethod(String callbackMethod) {
        this.callbackMethod = callbackMethod;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getRejectReasons() {
        return rejectReasons;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public String getReference() {
        return reference;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Did> getDids() {
        return dids;
    }

    public void setDids(List<Did> dids) {
        this.dids = dids;
    }
}
