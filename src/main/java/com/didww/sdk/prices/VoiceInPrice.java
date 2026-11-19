package com.didww.sdk.prices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * A row of a Voice IN price lookup. Rates and fees are in USD, VAT excluded,
 * with the scale of the answer kept (compare them with
 * {@link BigDecimal#compareTo(BigDecimal)}); they are {@code null} when
 * {@code rejectCalls} is set. The ID names the answering row of the plan.
 */
@Type("prices")
@Getter
@ToString
@EqualsAndHashCode
public final class VoiceInPrice {

    @Id
    private String id;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("country")
    private String country;

    @JsonProperty("src_country_name")
    private String srcCountryName;

    @JsonProperty("source_type")
    private String sourceType;

    @JsonProperty("rate")
    private BigDecimal rate;

    @JsonProperty("connection_fee")
    private BigDecimal connectionFee;

    @JsonProperty("initial_interval")
    private Integer initialInterval;

    @JsonProperty("next_interval")
    private Integer nextInterval;

    @JsonProperty("reject_calls")
    private Boolean rejectCalls;

    private VoiceInPrice() {
    }
}
