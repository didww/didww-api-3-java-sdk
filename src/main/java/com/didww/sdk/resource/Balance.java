package com.didww.sdk.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;

@Type("balances")
public class Balance {

    @Id
    private String id;

    @JsonProperty("total_balance")
    private Double totalBalance;

    @JsonProperty("balance")
    private Double balance;

    @JsonProperty("credit")
    private Double credit;

    public String getId() {
        return id;
    }

    public Double getTotalBalance() {
        return totalBalance;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getCredit() {
        return credit;
    }
}
