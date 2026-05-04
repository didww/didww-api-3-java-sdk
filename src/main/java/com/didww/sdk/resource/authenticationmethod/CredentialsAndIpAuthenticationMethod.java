package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CredentialsAndIpAuthenticationMethod extends AuthenticationMethod {

    @JsonProperty("allowed_sip_ips")
    private List<String> allowedSipIps;

    @JsonProperty("tech_prefix")
    private String techPrefix;

    @JsonProperty(value = "username", access = JsonProperty.Access.WRITE_ONLY)
    private String username;

    @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Override
    @JsonIgnore
    public String getType() {
        return "credentials_and_ip";
    }

    /**
     * Override toString() so default logging / debugger inspection / unhandled
     * exception traces never leak server-generated credentials. The wire
     * payload is unaffected — Jackson serializes the real values (or strips
     * them via the WRITE_ONLY access modifier on incoming requests).
     */
    @Override
    public String toString() {
        return "CredentialsAndIpAuthenticationMethod("
                + "allowedSipIps=" + allowedSipIps
                + ", techPrefix=" + techPrefix
                + ", username=" + (username == null ? "null" : "[FILTERED]")
                + ", password=" + (password == null ? "null" : "[FILTERED]")
                + ")";
    }
}
