package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("voice_in_trunk_groups")
public class VoiceInTrunkGroup extends BaseResource {

    public static VoiceInTrunkGroup build(String id) {
        return BaseResource.build(VoiceInTrunkGroup.class, id);
    }

    @JsonProperty("name")
    private String name;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("voice_in_trunks")
    private List<VoiceInTrunk> voiceInTrunks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacityLimit() {
        return capacityLimit;
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<VoiceInTrunk> getVoiceInTrunks() {
        return voiceInTrunks;
    }

    public void setVoiceInTrunks(List<VoiceInTrunk> voiceInTrunks) {
        this.voiceInTrunks = voiceInTrunks;
    }
}
