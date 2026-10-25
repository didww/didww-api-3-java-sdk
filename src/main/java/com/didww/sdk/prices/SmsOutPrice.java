package com.didww.sdk.prices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * A row of an SMS OUT (A2P, P2P or OTP) price lookup. The price is in USD per
 * SMS, VAT excluded, with the scale of the answer kept (compare it with
 * {@link BigDecimal#compareTo(BigDecimal)}); it is {@code null} when
 * {@code rejectSms} is set. The ID is derived from the row content and changes
 * with the price, so it does not track a destination over time.
 */
@Type("prices")
@Getter
@ToString
@EqualsAndHashCode
public final class SmsOutPrice {

    @Id
    private String id;

    @JsonProperty("source_prefix")
    private String sourcePrefix;

    @JsonProperty("destination_prefix")
    private String destinationPrefix;

    @JsonProperty("country")
    private String country;

    @JsonProperty("mcc")
    private Integer mcc;

    @JsonProperty("mcc_name")
    private String mccName;

    @JsonProperty("mnc")
    private Integer mnc;

    @JsonProperty("mnc_name")
    private String mncName;

    @JsonProperty("reject_sms")
    private Boolean rejectSms;

    @JsonProperty("price")
    private BigDecimal price;

    private SmsOutPrice() {
    }
}
