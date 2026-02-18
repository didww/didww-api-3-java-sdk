package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.util.List;

@Type("voice_in_trunk_groups")
public class VoiceInTrunkGroup implements HasId {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @Relationship("voice_in_trunks")
    private List<VoiceInTrunk> voiceInTrunks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<VoiceInTrunk> getVoiceInTrunks() {
        return voiceInTrunks;
    }

    public void setVoiceInTrunks(List<VoiceInTrunk> voiceInTrunks) {
        this.voiceInTrunks = voiceInTrunks;
    }
}
