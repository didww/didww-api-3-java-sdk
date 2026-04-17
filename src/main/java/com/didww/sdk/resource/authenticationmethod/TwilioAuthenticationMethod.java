package com.didww.sdk.resource.authenticationmethod;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwilioAuthenticationMethod extends AuthenticationMethod {

    @JsonProperty("twilio_account_sid")
    private String twilioAccountSid;

    @Override
    @JsonIgnore
    public String getType() {
        return "twilio";
    }
}
