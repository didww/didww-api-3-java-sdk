package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;

@Type("dids")
public class Did extends BaseResource {

    @JsonProperty(value = "number", access = JsonProperty.Access.WRITE_ONLY)
    private String number;

    @JsonProperty(value = "blocked", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean blocked;

    @JsonProperty(value = "awaiting_registration", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean awaitingRegistration;

    @JsonProperty("terminated")
    private Boolean terminated;

    @JsonProperty("pending_removal")
    private Boolean pendingRemoval;

    @JsonProperty("description")
    private String description;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty(value = "channels_included_count", access = JsonProperty.Access.WRITE_ONLY)
    private Integer channelsIncludedCount;

    @JsonProperty("dedicated_channels_count")
    private Integer dedicatedChannelsCount;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(value = "expires_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime expiresAt;

    @JsonProperty("billing_cycles_count")
    private Integer billingCyclesCount;

    @JsonProperty(value = "channels_included_count", access = JsonProperty.Access.WRITE_ONLY)
    private Integer channelsIncludedCount;

    @Relationship("order")
    private Order order;

    @Relationship("did_group")
    private DidGroup didGroup;

    @Relationship("voice_in_trunk")
    private VoiceInTrunk voiceInTrunk;

    @Relationship("voice_in_trunk_group")
    private VoiceInTrunkGroup voiceInTrunkGroup;

    @Relationship("capacity_pool")
    private CapacityPool capacityPool;

    @Relationship("shared_capacity_group")
    private SharedCapacityGroup sharedCapacityGroup;

    @Relationship("address_verification")
    private AddressVerification addressVerification;

    public String getNumber() {
        return number;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public Boolean getAwaitingRegistration() {
        return awaitingRegistration;
    }

    public Boolean getTerminated() {
        return terminated;
    }

    public void setTerminated(Boolean terminated) {
        this.terminated = terminated;
    }

    public Boolean getPendingRemoval() {
        return pendingRemoval;
    }

    public void setPendingRemoval(Boolean pendingRemoval) {
        this.pendingRemoval = pendingRemoval;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacityLimit() {
        return capacityLimit;
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public Integer getChannelsIncludedCount() {
        return channelsIncludedCount;
    }

    public Integer getDedicatedChannelsCount() {
        return dedicatedChannelsCount;
    }

    public void setDedicatedChannelsCount(Integer dedicatedChannelsCount) {
        this.dedicatedChannelsCount = dedicatedChannelsCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public Integer getBillingCyclesCount() {
        return billingCyclesCount;
    }

    public void setBillingCyclesCount(Integer billingCyclesCount) {
        this.billingCyclesCount = billingCyclesCount;
    }

    public Integer getChannelsIncludedCount() {
        return channelsIncludedCount;
    }

    public Order getOrder() {
        return order;
    }

    public DidGroup getDidGroup() {
        return didGroup;
    }

    public VoiceInTrunk getVoiceInTrunk() {
        return voiceInTrunk;
    }

    public void setVoiceInTrunk(VoiceInTrunk voiceInTrunk) {
        this.voiceInTrunk = voiceInTrunk;
    }

    public VoiceInTrunkGroup getVoiceInTrunkGroup() {
        return voiceInTrunkGroup;
    }

    public void setVoiceInTrunkGroup(VoiceInTrunkGroup voiceInTrunkGroup) {
        this.voiceInTrunkGroup = voiceInTrunkGroup;
    }

    public CapacityPool getCapacityPool() {
        return capacityPool;
    }

    public void setCapacityPool(CapacityPool capacityPool) {
        this.capacityPool = capacityPool;
    }

    public SharedCapacityGroup getSharedCapacityGroup() {
        return sharedCapacityGroup;
    }

    public void setSharedCapacityGroup(SharedCapacityGroup sharedCapacityGroup) {
        this.sharedCapacityGroup = sharedCapacityGroup;
    }

    public AddressVerification getAddressVerification() {
        return addressVerification;
    }
}
