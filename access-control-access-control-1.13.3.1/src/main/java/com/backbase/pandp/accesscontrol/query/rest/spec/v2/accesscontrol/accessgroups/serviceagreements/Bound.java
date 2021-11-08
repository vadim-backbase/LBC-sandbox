package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import java.math.BigDecimal;

public class Bound {
    private BigDecimal amount;

    private String currencyCode;

    public Bound amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Bound currencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}