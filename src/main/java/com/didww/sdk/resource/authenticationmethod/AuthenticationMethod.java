package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for VoiceOutTrunk polymorphic authentication methods.
 */
public abstract class AuthenticationMethod {

    /**
     * Returns the wire-level type string (e.g. "ip_only", "credentials_and_ip", "twilio").
     *
     * @return authentication method type identifier
     */
    @JsonIgnore
    public abstract String getType();
}
