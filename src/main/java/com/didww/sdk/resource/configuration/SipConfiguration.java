package com.didww.sdk.resource.configuration;

import com.didww.sdk.resource.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SipConfiguration extends TrunkConfiguration {

    @JsonProperty("username")
    private String username;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private Integer port;

    @JsonProperty("codec_ids")
    private List<Codec> codecIds;

    @JsonProperty("rx_dtmf_format_id")
    private RxDtmfFormat rxDtmfFormatId;

    @JsonProperty("tx_dtmf_format_id")
    private TxDtmfFormat txDtmfFormatId;

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
    private SstRefreshMethod sstRefreshMethodId;

    @JsonProperty("sip_timer_b")
    private Integer sipTimerB;

    @JsonProperty("dns_srv_failover_timer")
    private Integer dnsSrvFailoverTimer;

    @JsonProperty("rtp_ping")
    private Boolean rtpPing;

    @JsonProperty("force_symmetric_rtp")
    private Boolean forceSymmetricRtp;

    @JsonProperty("rerouting_disconnect_code_ids")
    private List<ReroutingDisconnectCode> reroutingDisconnectCodeIds;

    @JsonProperty("transport_protocol_id")
    private TransportProtocol transportProtocolId;

    @JsonProperty("media_encryption_mode")
    private MediaEncryptionMode mediaEncryptionMode;

    @JsonProperty("stir_shaken_mode")
    private StirShakenMode stirShakenMode;

    @JsonProperty("max_transfers")
    private Integer maxTransfers;

    @JsonProperty("max_30x_redirects")
    private Integer max30xRedirects;

    @JsonProperty("allowed_rtp_ips")
    private List<String> allowedRtpIps;

    @Override
    @JsonIgnore
    public String getType() {
        return "sip_configurations";
    }
}
