package com.didww.sdk.resource.configuration;

import com.didww.sdk.resource.enums.Codec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Iax2Configuration extends TrunkConfiguration {

    @JsonProperty("dst")
    private String dst;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("codec_ids")
    private List<Codec> codecIds;

    @JsonProperty("auth_user")
    private String authUser;

    @JsonProperty("auth_password")
    private String authPassword;

    @Override
    @JsonIgnore
    public String getType() {
        return "iax2_configurations";
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

    public List<Codec> getCodecIds() {
        return codecIds;
    }

    public void setCodecIds(List<Codec> codecIds) {
        this.codecIds = codecIds;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }
}
