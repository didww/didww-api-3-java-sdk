package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;

@Type("voice_out_trunks")
public class VoiceOutTrunk extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("allowed_sip_ips")
    private List<String> allowedSipIps;

    @JsonProperty("on_cli_mismatch_action")
    private String onCliMismatchAction;

    @JsonProperty("allowed_rtp_ips")
    private List<String> allowedRtpIps;

    @JsonProperty("allow_any_did_as_cli")
    private Boolean allowAnyDidAsCli;

    @JsonProperty("status")
    private String status;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty("threshold_amount")
    private String thresholdAmount;

    @JsonProperty("media_encryption_mode")
    private String mediaEncryptionMode;

    @JsonProperty("default_dst_action")
    private String defaultDstAction;

    @JsonProperty("dst_prefixes")
    private List<String> dstPrefixes;

    @JsonProperty("force_symmetric_rtp")
    private Boolean forceSymmetricRtp;

    @JsonProperty("rtp_ping")
    private Boolean rtpPing;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("threshold_reached")
    private Boolean thresholdReached;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @Relationship("dids")
    private List<Did> dids;

    @Relationship("default_did")
    private Did defaultDid;

    @Relationship("voice_in_trunk_group")
    private VoiceInTrunkGroup voiceInTrunkGroup;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAllowedSipIps() {
        return allowedSipIps;
    }

    public void setAllowedSipIps(List<String> allowedSipIps) {
        this.allowedSipIps = allowedSipIps;
    }

    public String getOnCliMismatchAction() {
        return onCliMismatchAction;
    }

    public void setOnCliMismatchAction(String onCliMismatchAction) {
        this.onCliMismatchAction = onCliMismatchAction;
    }

    public List<String> getAllowedRtpIps() {
        return allowedRtpIps;
    }

    public void setAllowedRtpIps(List<String> allowedRtpIps) {
        this.allowedRtpIps = allowedRtpIps;
    }

    public Boolean getAllowAnyDidAsCli() {
        return allowAnyDidAsCli;
    }

    public void setAllowAnyDidAsCli(Boolean allowAnyDidAsCli) {
        this.allowAnyDidAsCli = allowAnyDidAsCli;
    }

    public String getStatus() {
        return status;
    }

    public Integer getCapacityLimit() {
        return capacityLimit;
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = capacityLimit;
    }

    public String getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(String thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public String getMediaEncryptionMode() {
        return mediaEncryptionMode;
    }

    public void setMediaEncryptionMode(String mediaEncryptionMode) {
        this.mediaEncryptionMode = mediaEncryptionMode;
    }

    public String getDefaultDstAction() {
        return defaultDstAction;
    }

    public void setDefaultDstAction(String defaultDstAction) {
        this.defaultDstAction = defaultDstAction;
    }

    public List<String> getDstPrefixes() {
        return dstPrefixes;
    }

    public void setDstPrefixes(List<String> dstPrefixes) {
        this.dstPrefixes = dstPrefixes;
    }

    public Boolean getForceSymmetricRtp() {
        return forceSymmetricRtp;
    }

    public void setForceSymmetricRtp(Boolean forceSymmetricRtp) {
        this.forceSymmetricRtp = forceSymmetricRtp;
    }

    public Boolean getRtpPing() {
        return rtpPing;
    }

    public void setRtpPing(Boolean rtpPing) {
        this.rtpPing = rtpPing;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getThresholdReached() {
        return thresholdReached;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Did> getDids() {
        return dids;
    }

    public void setDids(List<Did> dids) {
        this.dids = dids;
    }

    public Did getDefaultDid() {
        return defaultDid;
    }

    public void setDefaultDid(Did defaultDid) {
        this.defaultDid = defaultDid;
    }

    public VoiceInTrunkGroup getVoiceInTrunkGroup() {
        return voiceInTrunkGroup;
    }

    public void setVoiceInTrunkGroup(VoiceInTrunkGroup voiceInTrunkGroup) {
        this.voiceInTrunkGroup = voiceInTrunkGroup;
    }
}
