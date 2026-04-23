package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;

@Type("dids")

@Getter
public class Did extends BaseResource {

    @JsonProperty(value = "number", access = JsonProperty.Access.WRITE_ONLY)
    private String number;

    @JsonProperty(value = "blocked", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean blocked;

    @JsonProperty(value = "awaiting_registration", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean awaitingRegistration;

    @JsonProperty("terminated")
    private Boolean terminated;

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

    @JsonProperty(value = "emergency_enabled", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean emergencyEnabled;

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

    @Relationship("emergency_calling_service")
    private EmergencyCallingService emergencyCallingService;

    @Relationship("emergency_verification")
    private EmergencyVerification emergencyVerification;

    @Relationship("identity")
    private Identity identity;

    public void setTerminated(Boolean terminated) {
        this.terminated = markDirty("terminated", terminated);
    }

    public void setDescription(String description) {
        this.description = markDirty("description", description);
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = markDirty("capacityLimit", capacityLimit);
    }

    public void setDedicatedChannelsCount(Integer dedicatedChannelsCount) {
        this.dedicatedChannelsCount = markDirty("dedicatedChannelsCount", dedicatedChannelsCount);
    }

    public void setBillingCyclesCount(Integer billingCyclesCount) {
        this.billingCyclesCount = markDirty("billingCyclesCount", billingCyclesCount);
    }

    public void setVoiceInTrunk(VoiceInTrunk voiceInTrunk) {
        this.voiceInTrunk = markDirty("voiceInTrunk", voiceInTrunk);
        this.voiceInTrunkGroup = markDirty("voiceInTrunkGroup", null);
    }

    public void setVoiceInTrunkGroup(VoiceInTrunkGroup voiceInTrunkGroup) {
        this.voiceInTrunkGroup = markDirty("voiceInTrunkGroup", voiceInTrunkGroup);
        this.voiceInTrunk = markDirty("voiceInTrunk", null);
    }

    public void setCapacityPool(CapacityPool capacityPool) {
        this.capacityPool = markDirty("capacityPool", capacityPool);
    }

    public void setSharedCapacityGroup(SharedCapacityGroup sharedCapacityGroup) {
        this.sharedCapacityGroup = markDirty("sharedCapacityGroup", sharedCapacityGroup);
    }

    public void setEmergencyCallingService(EmergencyCallingService emergencyCallingService) {
        this.emergencyCallingService = markDirty("emergencyCallingService", emergencyCallingService);
    }
}
