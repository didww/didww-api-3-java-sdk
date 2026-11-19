package com.didww.sdk.resource.smstrunkconfiguration;

import com.didww.sdk.internal.Redact;
import com.didww.sdk.resource.enums.SmsServerLocation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * DIDWW binds to the customer's SMSC as an SMPP client. {@code host}, {@code systemId}, {@code password} and
 * {@code serverLocation} are required when the trunk is created; {@code txPort} defaults to 2775 and
 * {@code rxPort} to 2776.
 */
@Getter
@Setter
public class SmppSmscConfiguration extends SmsTrunkConfiguration {

    @JsonProperty("host")
    private String host;

    @JsonProperty("tx_port")
    private Integer txPort;

    @JsonProperty("rx_port")
    private Integer rxPort;

    @JsonProperty("transceiver_mode")
    private Boolean transceiverMode;

    /** Bind timeout, seconds. */
    @JsonProperty("connection_timeout")
    private Integer connectionTimeout;

    @JsonProperty("use_ssl")
    private Boolean useSsl;

    /** SMPP system_id DIDWW binds with, up to 15 characters. */
    @JsonProperty("system_id")
    private String systemId;

    /** SMPP system_type, up to 12 characters. */
    @JsonProperty("system_type")
    private String systemType;

    /** SMPP bind password, up to 8 characters. */
    @JsonProperty("password")
    private String password;

    @JsonProperty("server_location")
    private SmsServerLocation serverLocation;

    @Override
    @JsonIgnore
    public String getType() {
        return "smpp_smsc";
    }

    /**
     * Masks the bind password. The wire payload is unaffected.
     */
    @Override
    public String toString() {
        return "SmppSmscConfiguration("
                + "host=" + host
                + ", txPort=" + txPort
                + ", rxPort=" + rxPort
                + ", transceiverMode=" + transceiverMode
                + ", connectionTimeout=" + connectionTimeout
                + ", useSsl=" + useSsl
                + ", systemId=" + systemId
                + ", systemType=" + systemType
                + ", password=" + Redact.mask(password)
                + ", serverLocation=" + serverLocation
                + ")";
    }
}
