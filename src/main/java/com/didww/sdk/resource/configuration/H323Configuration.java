package com.didww.sdk.resource.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class H323Configuration extends TrunkConfiguration {

    @JsonProperty("dst")
    private String dst;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("codec_ids")
    private List<Integer> codecIds;

    @Override
    @JsonIgnore
    public String getType() {
        return "h323_configurations";
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<Integer> getCodecIds() {
        return codecIds;
    }

    public void setCodecIds(List<Integer> codecIds) {
        this.codecIds = codecIds;
    }
}
