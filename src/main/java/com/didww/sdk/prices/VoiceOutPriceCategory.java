package com.didww.sdk.prices;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The category of a Voice OUT price, as the sheets of the price list group
 * them. A category this SDK does not know reads as {@code null}.
 */
public enum VoiceOutPriceCategory {
    @JsonProperty("International") INTERNATIONAL,
    @JsonProperty("Origin based") ORIGIN_BASED,
    @JsonProperty("Local") LOCAL
}
