package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
