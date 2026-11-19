package com.didww.sdk.resource.smstrunkconfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class of the {@link com.didww.sdk.resource.SmsTrunk} configurations. Concrete types:
 * {@link SmsToEmailConfiguration} ({@code sms_to_email}), {@link HttpInConfiguration} ({@code http_in}),
 * {@link HttpOutConfiguration} ({@code http_out}), {@link SmppEsmeConfiguration} ({@code smpp_esme}),
 * {@link SmppSmscConfiguration} ({@code smpp_smsc}) and {@link GenericSmsTrunkConfiguration} for any other type.
 */
public abstract class SmsTrunkConfiguration {

    /**
     * Returns the wire-level configuration type (e.g. "http_in").
     *
     * @return the configuration type
     */
    @JsonIgnore
    public abstract String getType();
}
