package com.didww.sdk.resource.smstrunkconfiguration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Inbound messages are forwarded as email. {@code sendTo}, {@code subject} and {@code message} are required
 * when the trunk is created.
 */
@Getter
@Setter
public class SmsToEmailConfiguration extends SmsTrunkConfiguration {

    @JsonProperty("send_to")
    private String sendTo;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("message")
    private String message;

    @Override
    @JsonIgnore
    public String getType() {
        return "sms_to_email";
    }

    @Override
    public String toString() {
        return "SmsToEmailConfiguration("
                + "sendTo=" + sendTo
                + ", subject=" + subject
                + ", message=" + message
                + ")";
    }
}
