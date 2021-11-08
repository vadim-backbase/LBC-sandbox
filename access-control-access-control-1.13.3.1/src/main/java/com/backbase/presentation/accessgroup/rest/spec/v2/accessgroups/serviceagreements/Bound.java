package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Bound {

    private BigDecimal amount;

    private String currencyCode;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(amount).append(currencyCode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Bound)) {
            return false;
        }
        Bound rhs = ((Bound) other);
        return new EqualsBuilder().append(amount, rhs.amount).append(currencyCode, rhs.currencyCode).isEquals();
    }

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