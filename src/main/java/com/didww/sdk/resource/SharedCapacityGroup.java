package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("shared_capacity_groups")

@Getter
public class SharedCapacityGroup extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("shared_channels_count")
    private Integer sharedChannelsCount;

    @JsonProperty("metered_channels_count")
    private Integer meteredChannelsCount;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @Relationship("capacity_pool")
    private CapacityPool capacityPool;

    @Relationship("dids")
    private List<Did> dids;

    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setSharedChannelsCount(Integer sharedChannelsCount) {
        this.sharedChannelsCount = markDirty("sharedChannelsCount", sharedChannelsCount);
    }

    public void setMeteredChannelsCount(Integer meteredChannelsCount) {
        this.meteredChannelsCount = markDirty("meteredChannelsCount", meteredChannelsCount);
    }

    public void setCapacityPool(CapacityPool capacityPool) {
        this.capacityPool = markDirty("capacityPool", capacityPool);
    }

    public void setDids(List<Did> dids) {
        this.dids = markDirty("dids", dids);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }
}
