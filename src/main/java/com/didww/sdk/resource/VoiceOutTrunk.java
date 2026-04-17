package com.didww.sdk.resource;

import com.didww.sdk.converter.AuthenticationMethodDeserializer;
import com.didww.sdk.converter.AuthenticationMethodSerializer;
import com.didww.sdk.resource.authenticationmethod.AuthenticationMethod;
import com.didww.sdk.resource.enums.DefaultDstAction;
import com.didww.sdk.resource.enums.MediaEncryptionMode;
import com.didww.sdk.resource.enums.OnCliMismatchAction;
import com.didww.sdk.resource.enums.VoiceOutTrunkStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JsonProperty(value = "threshold_reached", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean thresholdReached;

    @JsonProperty(value = "created_at", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty("external_reference_id")
    private String externalReferenceId;

    @JsonProperty("emergency_enable_all")
    private Boolean emergencyEnableAll;

    @JsonProperty("rtp_timeout")
    private Integer rtpTimeout;

    @JsonProperty("authentication_method")
    @JsonDeserialize(using = AuthenticationMethodDeserializer.class)
    @JsonSerialize(using = AuthenticationMethodSerializer.class)
    private AuthenticationMethod authenticationMethod;

    @Relationship("dids")
    private List<Did> dids;

    @Relationship("default_did")
    private Did defaultDid;

    @Relationship("emergency_dids")
    private List<Did> emergencyDids;

    public void setName(String name) {
        this.name = markDirty("name", name);
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

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = markDirty("authenticationMethod", authenticationMethod);
    }

    public void setDids(List<Did> dids) {
        this.dids = markDirty("dids", dids);
    }

    public void setDefaultDid(Did defaultDid) {
        this.defaultDid = markDirty("defaultDid", defaultDid);
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = markDirty("externalReferenceId", externalReferenceId);
    }

    public void setEmergencyEnableAll(Boolean emergencyEnableAll) {
        this.emergencyEnableAll = markDirty("emergencyEnableAll", emergencyEnableAll);
    }

    public void setRtpTimeout(Integer rtpTimeout) {
        this.rtpTimeout = markDirty("rtpTimeout", rtpTimeout);
    }

    public void setEmergencyDids(List<Did> emergencyDids) {
        this.emergencyDids = markDirty("emergencyDids", emergencyDids);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isActive() {
        return VoiceOutTrunkStatus.ACTIVE.equals(status);
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isBlocked() {
        return VoiceOutTrunkStatus.BLOCKED.equals(status);
    }
}
