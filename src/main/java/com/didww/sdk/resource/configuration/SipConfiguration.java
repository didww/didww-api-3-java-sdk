package com.didww.sdk.resource.configuration;

import com.didww.sdk.resource.enums.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SipConfiguration extends TrunkConfiguration {

    @JsonProperty("username")
    private String username;

    // host / port / enabledSipRegistration use manual setters that propagate
    // server-enforced field dependencies (API 2026-04-16). Lombok would
    // otherwise generate plain setters from the class-level @Setter, which
    // would skip the cascade. Jackson deserializes into the private fields
    // directly via reflection (no setter present), so server responses
    // bypass the cascade on the way in — exactly what we want, since
    // server data is already internally consistent.
    @Setter(AccessLevel.NONE)
    @JsonProperty("host")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String host;

    @Setter(AccessLevel.NONE)
    @JsonProperty("port")
    @JsonInclude(JsonInclude.Include.ALWAYS)
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

    @JsonProperty("diversion_relay_policy")
    private DiversionRelayPolicy diversionRelayPolicy;

    @JsonProperty("diversion_inject_mode")
    private DiversionInjectMode diversionInjectMode;

    @JsonProperty("network_protocol_priority")
    private NetworkProtocolPriority networkProtocolPriority;

    /**
     * Whether SIP registration is enabled. When {@code true} the server
     * generates {@code incoming_auth_username} / {@code incoming_auth_password}
     * and the trunk's {@code host} and {@code port} must be left blank. When
     * disabling sip registration on an existing trunk, the same PATCH must
     * also set {@code host} to a non-blank value and {@code use_did_in_ruri}
     * to {@code false}, or the server returns 422. (API 2026-04-16)
     */
    @Setter(AccessLevel.NONE)
    @JsonProperty("enabled_sip_registration")
    private Boolean enabledSipRegistration;

    @JsonProperty("use_did_in_ruri")
    private Boolean useDidInRuri;

    @JsonProperty("cnam_lookup")
    private Boolean cnamLookup;

    /**
     * Server-generated SIP authentication username, returned in responses
     * when {@code enabledSipRegistration} is {@code true}. Read-only; the API
     * rejects any write attempt with HTTP 400 "Param not allowed". (API 2026-04-16)
     */
    @JsonProperty(value = "incoming_auth_username", access = JsonProperty.Access.WRITE_ONLY)
    @Setter(AccessLevel.PRIVATE)
    private String incomingAuthUsername;

    /**
     * Server-generated SIP authentication password, returned in responses
     * when {@code enabledSipRegistration} is {@code true}. Read-only; the API
     * rejects any write attempt with HTTP 400 "Param not allowed". (API 2026-04-16)
     */
    @JsonProperty(value = "incoming_auth_password", access = JsonProperty.Access.WRITE_ONLY)
    @Setter(AccessLevel.PRIVATE)
    private String incomingAuthPassword;

    /**
     * Setting host to a non-null value cascades enabledSipRegistration and
     * useDidInRuri to false because the server requires those states
     * whenever host is present (API 2026-04-16).
     */
    public void setHost(String host) {
        if (host != null) {
            this.enabledSipRegistration = false;
            this.useDidInRuri = false;
        }
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * Setting enabledSipRegistration cascades dependent fields:
     * <ul>
     * <li>true  -> nullify host / port and emit them as {@code null} on the
     * wire (host/port must be blank when sip_registration is on). The wire
     * emission fires unconditionally so PATCH against an existing trunk that
     * already has host/port set server-side is told to clear them.</li>
     * <li>false -> force useDidInRuri = false (must be disabled when
     * sip_registration is disabled).</li>
     * </ul>
     */
    public void setEnabledSipRegistration(Boolean enabledSipRegistration) {
        if (Boolean.TRUE.equals(enabledSipRegistration)) {
            // Always emit host: null and port: null on the wire so a PATCH
            // against an existing trunk that already has them persisted on
            // the server side is told to clear them.
            this.host = null;
            this.port = null;
        } else if (Boolean.FALSE.equals(enabledSipRegistration)) {
            this.useDidInRuri = false;
        }
        this.enabledSipRegistration = enabledSipRegistration;
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "sip_configurations";
    }

    /**
     * Override toString() so default logging / debugger inspection / unhandled
     * exception traces never leak SIP credentials. The wire payload is
     * unaffected — Jackson serializes the real values (or strips read-only
     * ones via @JsonProperty WRITE_ONLY).
     */
    @Override
    public String toString() {
        return "SipConfiguration("
                + "username=" + username
                + ", host=" + host
                + ", port=" + port
                + ", authPassword=" + (authPassword == null ? "null" : "[FILTERED]")
                + ", enabledSipRegistration=" + enabledSipRegistration
                + ", useDidInRuri=" + useDidInRuri
                + ", incomingAuthUsername=" + (incomingAuthUsername == null ? "null" : "[FILTERED]")
                + ", incomingAuthPassword=" + (incomingAuthPassword == null ? "null" : "[FILTERED]")
                + ")";
    }
}
