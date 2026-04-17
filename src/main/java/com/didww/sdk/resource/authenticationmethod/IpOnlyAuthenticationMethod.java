package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the {@code ip_only} authentication method for Voice Out Trunks.
 *
 * <p><strong>This is a read-only authentication method.</strong>
 * It can only be configured manually by DIDWW staff upon request.
 * It cannot be set via the API on create or update operations.</p>
 *
 * <p>Trunks that already have {@code ip_only} authentication can still be read
 * and their non-authentication attributes (name, default_dst_action, etc.)
 * can be updated normally via the API.</p>
 */
@Getter
@Setter
public class IpOnlyAuthenticationMethod extends AuthenticationMethod {

    @JsonProperty("allowed_sip_ips")
    private List<String> allowedSipIps;

    @JsonProperty("tech_prefix")
    private String techPrefix;

    @Override
    @JsonIgnore
    public String getType() {
        return "ip_only";
    }
}
