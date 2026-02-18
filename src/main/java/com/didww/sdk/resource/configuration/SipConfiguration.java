package com.didww.sdk.resource.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SipConfiguration extends TrunkConfiguration {

    @JsonProperty("username")
    private String username;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("codec_ids")
    private List<Integer> codecIds;

    @JsonProperty("rx_dtmf_format_id")
    private Integer rxDtmfFormatId;

    @JsonProperty("tx_dtmf_format_id")
    private Integer txDtmfFormatId;

    @JsonProperty("resolve_ruri")
    private Boolean resolveRuri;

    @JsonProperty("auth_enabled")
    private Boolean authEnabled;

    @JsonProperty("auth_user")
    private String authUser;

    @JsonProperty("auth_password")
    private String authPassword;

    @JsonProperty("auth_from_user")
    private String authFromUser;

    @JsonProperty("auth_from_domain")
    private String authFromDomain;

    @JsonProperty("sst_enabled")
    private Boolean sstEnabled;

    @JsonProperty("sst_min_timer")
    private Integer sstMinTimer;

    @JsonProperty("sst_max_timer")
    private Integer sstMaxTimer;

    @JsonProperty("sst_accept_501")
    private Boolean sstAccept501;

    @JsonProperty("sst_session_expires")
    private Integer sstSessionExpires;

    @JsonProperty("sst_refresh_method_id")
    private Integer sstRefreshMethodId;

    @JsonProperty("sip_timer_b")
    private Integer sipTimerB;

    @JsonProperty("dns_srv_failover_timer")
    private Integer dnsSrvFailoverTimer;

    @JsonProperty("rtp_ping")
    private Boolean rtpPing;

    @JsonProperty("force_symmetric_rtp")
    private Boolean forceSymmetricRtp;

    @JsonProperty("rerouting_disconnect_code_ids")
    private List<Integer> reroutingDisconnectCodeIds;

    @JsonProperty("transport_protocol_id")
    private Integer transportProtocolId;

    @JsonProperty("max_transfers")
    private Integer maxTransfers;

    @JsonProperty("max_30x_redirects")
    private Integer max30xRedirects;

    @Override
    @JsonIgnore
    public String getType() {
        return "sip_configurations";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getRxDtmfFormatId() {
        return rxDtmfFormatId;
    }

    public void setRxDtmfFormatId(Integer rxDtmfFormatId) {
        this.rxDtmfFormatId = rxDtmfFormatId;
    }

    public Integer getTxDtmfFormatId() {
        return txDtmfFormatId;
    }

    public void setTxDtmfFormatId(Integer txDtmfFormatId) {
        this.txDtmfFormatId = txDtmfFormatId;
    }

    public Boolean getResolveRuri() {
        return resolveRuri;
    }

    public void setResolveRuri(Boolean resolveRuri) {
        this.resolveRuri = resolveRuri;
    }

    public Boolean getAuthEnabled() {
        return authEnabled;
    }

    public void setAuthEnabled(Boolean authEnabled) {
        this.authEnabled = authEnabled;
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

    public String getAuthFromUser() {
        return authFromUser;
    }

    public void setAuthFromUser(String authFromUser) {
        this.authFromUser = authFromUser;
    }

    public String getAuthFromDomain() {
        return authFromDomain;
    }

    public void setAuthFromDomain(String authFromDomain) {
        this.authFromDomain = authFromDomain;
    }

    public Boolean getSstEnabled() {
        return sstEnabled;
    }

    public void setSstEnabled(Boolean sstEnabled) {
        this.sstEnabled = sstEnabled;
    }

    public Integer getSstMinTimer() {
        return sstMinTimer;
    }

    public void setSstMinTimer(Integer sstMinTimer) {
        this.sstMinTimer = sstMinTimer;
    }

    public Integer getSstMaxTimer() {
        return sstMaxTimer;
    }

    public void setSstMaxTimer(Integer sstMaxTimer) {
        this.sstMaxTimer = sstMaxTimer;
    }

    public Boolean getSstAccept501() {
        return sstAccept501;
    }

    public void setSstAccept501(Boolean sstAccept501) {
        this.sstAccept501 = sstAccept501;
    }

    public Integer getSstSessionExpires() {
        return sstSessionExpires;
    }

    public void setSstSessionExpires(Integer sstSessionExpires) {
        this.sstSessionExpires = sstSessionExpires;
    }

    public Integer getSstRefreshMethodId() {
        return sstRefreshMethodId;
    }

    public void setSstRefreshMethodId(Integer sstRefreshMethodId) {
        this.sstRefreshMethodId = sstRefreshMethodId;
    }

    public Integer getSipTimerB() {
        return sipTimerB;
    }

    public void setSipTimerB(Integer sipTimerB) {
        this.sipTimerB = sipTimerB;
    }

    public Integer getDnsSrvFailoverTimer() {
        return dnsSrvFailoverTimer;
    }

    public void setDnsSrvFailoverTimer(Integer dnsSrvFailoverTimer) {
        this.dnsSrvFailoverTimer = dnsSrvFailoverTimer;
    }

    public Boolean getRtpPing() {
        return rtpPing;
    }

    public void setRtpPing(Boolean rtpPing) {
        this.rtpPing = rtpPing;
    }

    public Boolean getForceSymmetricRtp() {
        return forceSymmetricRtp;
    }

    public void setForceSymmetricRtp(Boolean forceSymmetricRtp) {
        this.forceSymmetricRtp = forceSymmetricRtp;
    }

    public List<Integer> getReroutingDisconnectCodeIds() {
        return reroutingDisconnectCodeIds;
    }

    public void setReroutingDisconnectCodeIds(List<Integer> reroutingDisconnectCodeIds) {
        this.reroutingDisconnectCodeIds = reroutingDisconnectCodeIds;
    }

    public Integer getTransportProtocolId() {
        return transportProtocolId;
    }

    public void setTransportProtocolId(Integer transportProtocolId) {
        this.transportProtocolId = transportProtocolId;
    }

    public Integer getMaxTransfers() {
        return maxTransfers;
    }

    public void setMaxTransfers(Integer maxTransfers) {
        this.maxTransfers = maxTransfers;
    }

    public Integer getMax30xRedirects() {
        return max30xRedirects;
    }

    public void setMax30xRedirects(Integer max30xRedirects) {
        this.max30xRedirects = max30xRedirects;
    }
}
