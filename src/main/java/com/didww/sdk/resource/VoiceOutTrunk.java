package com.didww.sdk.resource;

import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.enums.VoiceOutTrunkStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Type("voice_out_trunks")

@Getter
public class VoiceOutTrunk extends BaseResource {

    @JsonProperty("name")
    private String name;

    @JsonProperty("allowed_sip_ips")
    private List<String> allowedSipIps;

    @JsonProperty("on_cli_mismatch_action")
    private OnCliMismatchAction onCliMismatchAction;

    @JsonProperty("allowed_rtp_ips")
    private List<String> allowedRtpIps;

    @JsonProperty("allow_any_did_as_cli")
    private Boolean allowAnyDidAsCli;

    @JsonProperty("status")
    private VoiceOutTrunkStatus status;

    @JsonProperty("capacity_limit")
    private Integer capacityLimit;

    @JsonProperty("threshold_amount")
    private String thresholdAmount;

    @JsonProperty("media_encryption_mode")
    private MediaEncryptionMode mediaEncryptionMode;

    @JsonProperty("default_dst_action")
    private DefaultDstAction defaultDstAction;

    @JsonProperty("dst_prefixes")
    private List<String> dstPrefixes;

    @JsonProperty("force_symmetric_rtp")
    private Boolean forceSymmetricRtp;

    @JsonProperty("rtp_ping")
    private Boolean rtpPing;

    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonProperty(value = "username", access = JsonProperty.Access.WRITE_ONLY)
    private String username;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(value = "threshold_reached", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean thresholdReached;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @Relationship("dids")
    private List<Did> dids;

    @Relationship("default_did")
    private Did defaultDid;

    public void setName(String name) {
        this.name = markDirty("name", name);
    }

    public void setAllowedSipIps(List<String> allowedSipIps) {
        this.allowedSipIps = markDirty("allowedSipIps", allowedSipIps);
    }

    public void setOnCliMismatchAction(OnCliMismatchAction onCliMismatchAction) {
        this.onCliMismatchAction = markDirty("onCliMismatchAction", onCliMismatchAction);
    }

    public void setAllowedRtpIps(List<String> allowedRtpIps) {
        this.allowedRtpIps = markDirty("allowedRtpIps", allowedRtpIps);
    }

    public void setAllowAnyDidAsCli(Boolean allowAnyDidAsCli) {
        this.allowAnyDidAsCli = markDirty("allowAnyDidAsCli", allowAnyDidAsCli);
    }

    public void setCapacityLimit(Integer capacityLimit) {
        this.capacityLimit = markDirty("capacityLimit", capacityLimit);
    }

    public void setThresholdAmount(String thresholdAmount) {
        this.thresholdAmount = markDirty("thresholdAmount", thresholdAmount);
    }

    public void setMediaEncryptionMode(MediaEncryptionMode mediaEncryptionMode) {
        this.mediaEncryptionMode = markDirty("mediaEncryptionMode", mediaEncryptionMode);
    }

    public void setDefaultDstAction(DefaultDstAction defaultDstAction) {
        this.defaultDstAction = markDirty("defaultDstAction", defaultDstAction);
    }

    public void setDstPrefixes(List<String> dstPrefixes) {
        this.dstPrefixes = markDirty("dstPrefixes", dstPrefixes);
    }

    public void setForceSymmetricRtp(Boolean forceSymmetricRtp) {
        this.forceSymmetricRtp = markDirty("forceSymmetricRtp", forceSymmetricRtp);
    }

    public void setRtpPing(Boolean rtpPing) {
        this.rtpPing = markDirty("rtpPing", rtpPing);
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = markDirty("callbackUrl", callbackUrl);
    }

    public void setDids(List<Did> dids) {
        this.dids = markDirty("dids", dids);
    }

    public void setDefaultDid(Did defaultDid) {
        this.defaultDid = markDirty("defaultDid", defaultDid);
    }

}
