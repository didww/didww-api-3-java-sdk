package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Getter;

import java.math.BigDecimal;

@Type("balances")

@Getter
public class Balance extends BaseResource {

    @JsonProperty("total_balance")
    private BigDecimal totalBalance;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("credit")
    private BigDecimal credit;
}
