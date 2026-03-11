package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Type;

import java.math.BigDecimal;

@Type("balances")
public class Balance extends BaseResource {

    public static Balance build(String id) {
        return BaseResource.build(Balance.class, id);
    }

    @JsonProperty("total_balance")
    private BigDecimal totalBalance;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("credit")
    private BigDecimal credit;

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getCredit() {
        return credit;
    }
}
