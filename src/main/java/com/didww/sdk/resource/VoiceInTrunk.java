package com.didww.sdk.resource;

import com.didww.sdk.converter.TrunkConfigurationDeserializer;
import com.didww.sdk.converter.TrunkConfigurationSerializer;
import com.didww.sdk.resource.configuration.TrunkConfiguration;
import com.didww.sdk.resource.enums.CliFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;

@Type("voice_in_trunks")

@Getter
public class VoiceInTrunk extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("weight")
    private Integer weight;

    @JsonProperty("cli_format")
    private CliFormat cliFormat;

    @JsonProperty("cli_prefix")
    private String cliPrefix;

    @JsonProperty("description")
    private String description;

    @JsonProperty("ringing_timeout")
    private Integer ringingTimeout;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("configuration")
    @JsonDeserialize(using = TrunkConfigurationDeserializer.class)
    @JsonSerialize(using = TrunkConfigurationSerializer.class)
    private TrunkConfiguration configuration;

    @Relationship("pop")
    private Pop pop;

    @Relationship("voice_in_trunk_group")
    private VoiceInTrunkGroup voiceInTrunkGroup;

    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setPriority(Integer priority) {
        this.priority = markDirty("priority", priority);
    }

    public void setWeight(Integer weight) {
        this.weight = markDirty("weight", weight);
    }

    public void setCliFormat(CliFormat cliFormat) {
        this.cliFormat = markDirty("cliFormat", cliFormat);
    }

    public void setCliPrefix(String cliPrefix) {
        this.cliPrefix = markDirty("cliPrefix", cliPrefix);
    }

    public void setDescription(String description) {
        this.description = markDirty("description", description);
    }

    public void setRingingTimeout(Integer ringingTimeout) {
        this.ringingTimeout = markDirty("ringingTimeout", ringingTimeout);
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = markDirty("capacityLimit", capacityLimit);
    }

    public void setConfiguration(TrunkConfiguration configuration) {
        this.configuration = markDirty("configuration", configuration);
    }

    public void setPop(Pop pop) {
        this.pop = markDirty("pop", pop);
    }

    public void setVoiceInTrunkGroup(VoiceInTrunkGroup voiceInTrunkGroup) {
        this.voiceInTrunkGroup = markDirty("voiceInTrunkGroup", voiceInTrunkGroup);
    }
}
