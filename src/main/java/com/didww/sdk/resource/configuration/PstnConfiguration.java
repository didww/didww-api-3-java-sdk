package com.didww.sdk.resource.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PstnConfiguration extends TrunkConfiguration {

    @JsonProperty("dst")
    private String dst;

    @Override
    @JsonIgnore
    public String getType() {
        return "pstn_configurations";
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }
}
