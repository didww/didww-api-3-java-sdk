package com.didww.sdk.resource;

import com.didww.sdk.converter.TrunkConfigurationDeserializer;
import com.didww.sdk.converter.TrunkConfigurationSerializer;
import com.didww.sdk.resource.configuration.TrunkConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;

@Type("voice_in_trunks")
public class VoiceInTrunk implements HasId {

    @Id
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("weight")
    private Integer weight;

    @JsonProperty("cli_format")
    private String cliFormat;

    @JsonProperty("cli_prefix")
    private String cliPrefix;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ringing_timeout")
    private Integer ringingTimeout;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("configuration")
    @JsonDeserialize(using = TrunkConfigurationDeserializer.class)
    @JsonSerialize(using = TrunkConfigurationSerializer.class)
    private TrunkConfiguration configuration;

    @Relationship("pop")
    private Pop pop;

    @Relationship("voice_in_trunk_group")
    private VoiceInTrunkGroup voiceInTrunkGroup;

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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public String getCliFormat() {
        return cliFormat;
    }

    public void setCliFormat(String cliFormat) {
        this.cliFormat = cliFormat;
    }

    public String getCliPrefix() {
        return cliPrefix;
    }

    public void setCliPrefix(String cliPrefix) {
        this.cliPrefix = cliPrefix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRingingTimeout() {
        return ringingTimeout;
    }

    public void setRingingTimeout(Integer ringingTimeout) {
        this.ringingTimeout = ringingTimeout;
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

    public TrunkConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(TrunkConfiguration configuration) {
        this.configuration = configuration;
    }

    public Pop getPop() {
        return pop;
    }

    public void setPop(Pop pop) {
        this.pop = pop;
    }

    public VoiceInTrunkGroup getVoiceInTrunkGroup() {
        return voiceInTrunkGroup;
    }

    public void setVoiceInTrunkGroup(VoiceInTrunkGroup voiceInTrunkGroup) {
        this.voiceInTrunkGroup = voiceInTrunkGroup;
    }
}
