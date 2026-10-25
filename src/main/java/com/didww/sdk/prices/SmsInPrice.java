package com.didww.sdk.prices;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * A row of an SMS IN price lookup. The price is in USD per SMS, VAT excluded,
 * with the scale of the answer kept (compare it with
 * {@link BigDecimal#compareTo(BigDecimal)}). The ID is derived from the row
 * content and changes with the price, so it does not track a destination over
 * time.
 */
@Type("prices")
@Getter
@ToString
@EqualsAndHashCode
public final class SmsInPrice {

    @Id
    private String id;

    @JsonProperty("source_prefix")
    private String sourcePrefix;

    @JsonProperty("source_number_length_min")
    private Integer sourceNumberLengthMin;

    @JsonProperty("source_number_length_max")
    private Integer sourceNumberLengthMax;

    @JsonProperty("destination_prefix")
    private String destinationPrefix;

    @JsonProperty("destination_number_length_min")
    private Integer destinationNumberLengthMin;

    @JsonProperty("destination_number_length_max")
    private Integer destinationNumberLengthMax;

    @JsonProperty("country")
    private String country;

    @JsonProperty("price")
    private BigDecimal price;

    private SmsInPrice() {
    }
}
