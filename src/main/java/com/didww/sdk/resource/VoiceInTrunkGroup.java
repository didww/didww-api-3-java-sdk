package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("voice_in_trunk_groups")

@Getter
public class VoiceInTrunkGroup extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @Relationship("voice_in_trunks")
    private List<VoiceInTrunk> voiceInTrunks;

    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = markDirty("capacityLimit", capacityLimit);
    }

    public void setVoiceInTrunks(List<VoiceInTrunk> voiceInTrunks) {
        this.voiceInTrunks = markDirty("voiceInTrunks", voiceInTrunks);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }
}
