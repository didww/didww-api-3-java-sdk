package com.didww.sdk.prices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A row of a Voice OUT price lookup. Rates and fees are in USD, VAT excluded,
 * with the scale of the answer kept (compare them with
 * {@link BigDecimal#compareTo(BigDecimal)}); they are {@code null} when
 * {@code rejectCalls} is set. The {@code next*} fields and
 * {@code effectiveAt} announce a scheduled change, named by {@code comment}.
 * The ID stays the same when a re-import changes only the price.
 */
@Type("prices")
@Getter
@ToString
@EqualsAndHashCode
public final class VoiceOutPrice {

    @Id
    private String id;

    @JsonProperty("category")
    private VoiceOutPriceCategory category;

    @JsonProperty("country")
    private String country;

    @JsonProperty("network")
    private String network;

    @JsonProperty("prefix")
    private String prefix;

    @JsonProperty("rate")
    private BigDecimal rate;

    @JsonProperty("connection_fee")
    private BigDecimal connectionFee;

    @JsonProperty("initial_increment")
    private Integer initialIncrement;

    @JsonProperty("recurring_increment")
    private Integer recurringIncrement;

    @JsonProperty("reject_calls")
    private Boolean rejectCalls;

    @JsonProperty("next_rate")
    private BigDecimal nextRate;

    @JsonProperty("next_connection_fee")
    private BigDecimal nextConnectionFee;

    @JsonProperty("next_initial_increment")
    private Integer nextInitialIncrement;

    @JsonProperty("next_recurring_increment")
    private Integer nextRecurringIncrement;

    @JsonProperty("effective_at")
    private OffsetDateTime effectiveAt;

    @JsonProperty("comment")
    private String comment;

    private VoiceOutPrice() {
    }
}
