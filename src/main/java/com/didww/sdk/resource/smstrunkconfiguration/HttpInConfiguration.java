package com.didww.sdk.resource.smstrunkconfiguration;

import com.didww.sdk.resource.enums.SmsBodyType;
import com.didww.sdk.resource.enums.SmsHttpMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Inbound messages are delivered as an HTTP request. Query parameters, headers and body may use the
 * placeholders {@code {SMS_TEXT}}, {@code {SMS_TEXT_BASE64_ENCODED}}, {@code {SMS_SRC_ADDR}},
 * {@code {SMS_DST_ADDR}} and {@code {SMS_TIME}}. {@code url} and {@code httpMethod} are required when the
 * trunk is created; {@code bodyType} and {@code body} are required for post and put and refused for get.
 */
@Getter
@Setter
public class HttpInConfiguration extends SmsTrunkConfiguration {

    @JsonProperty("url")
    private String url;

    @JsonProperty("http_method")
    private SmsHttpMethod httpMethod;

    @JsonProperty("query_parameters")
    private Map<String, Object> queryParameters;

    /** Headers added to the request. They may carry credentials, so {@link #toString()} masks them. */
    @JsonProperty("headers")
    private Map<String, Object> headers;

    @JsonProperty("body_type")
    private SmsBodyType bodyType;

    /**
     * Request body template: a JSON string for {@link SmsBodyType#JSON}, a map for the form encodings and a
     * string for {@link SmsBodyType#RAW}. An object body is read back as a {@code Map}.
     */
    @JsonProperty("body")
    private Object body;

    @JsonProperty("follow_redirect")
    private Boolean followRedirect;

    @JsonProperty("validate_https_certificate")
    private Boolean validateHttpsCertificate;

    @Override
    @JsonIgnore
    public String getType() {
        return "http_in";
    }

    /**
     * Masks the headers, which may carry credentials. The wire payload is unaffected.
     */
    @Override
    public String toString() {
        return "HttpInConfiguration("
                + "url=" + url
                + ", httpMethod=" + httpMethod
                + ", headers=" + (headers == null ? "null" : "[FILTERED]")
                + ", bodyType=" + bodyType
                + ", followRedirect=" + followRedirect
                + ", validateHttpsCertificate=" + validateHttpsCertificate
                + ")";
    }
}
