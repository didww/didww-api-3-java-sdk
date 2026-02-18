package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.resource.Balance;

import java.math.BigDecimal;

public class BalanceExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();

        Balance balance = client.balance().find().getData();
        System.out.println("Total Balance: " + toPlainString(balance.getTotalBalance()));
        System.out.println("Balance: " + toPlainString(balance.getBalance()));
        System.out.println("Credit: " + toPlainString(balance.getCredit()));
    }

    private static String toPlainString(Double value) {
        return value == null ? "null" : BigDecimal.valueOf(value).toPlainString();
    }
}
