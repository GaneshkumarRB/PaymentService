package com.bnk.paymentservice.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AccountDetailsEmbeddable {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 34)
    private String accountNumber;

    protected AccountDetailsEmbeddable() {
    }

    public AccountDetailsEmbeddable(String name, String accountNumber) {
        this.name = name;
        this.accountNumber = accountNumber;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
