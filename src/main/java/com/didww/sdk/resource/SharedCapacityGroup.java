package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("shared_capacity_groups")
public class SharedCapacityGroup extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("shared_channels_count")
    private Integer sharedChannelsCount;

    @JsonProperty("metered_channels_count")
    private Integer meteredChannelsCount;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Relationship("capacity_pool")
    private CapacityPool capacityPool;

    @Relationship("dids")
    private List<Did> dids;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSharedChannelsCount() {
        return sharedChannelsCount;
    }

    public void setSharedChannelsCount(Integer sharedChannelsCount) {
        this.sharedChannelsCount = sharedChannelsCount;
    }

    public Integer getMeteredChannelsCount() {
        return meteredChannelsCount;
    }

    public void setMeteredChannelsCount(Integer meteredChannelsCount) {
        this.meteredChannelsCount = meteredChannelsCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public CapacityPool getCapacityPool() {
        return capacityPool;
    }

    public void setCapacityPool(CapacityPool capacityPool) {
        this.capacityPool = capacityPool;
    }

    public List<Did> getDids() {
        return dids;
    }
}
