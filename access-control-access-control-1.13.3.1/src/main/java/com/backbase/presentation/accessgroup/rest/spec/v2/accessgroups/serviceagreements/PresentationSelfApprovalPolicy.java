package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class PresentationSelfApprovalPolicy {
    private String businessFunctionCode;

    private Boolean canSelfApprove;

    private List<Bound> bounds = new ArrayList<>();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(businessFunctionCode).append(canSelfApprove).append(bounds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PresentationSelfApprovalPolicy)) {
            return false;
        }
        PresentationSelfApprovalPolicy rhs = ((PresentationSelfApprovalPolicy) other);
        return new EqualsBuilder().append(businessFunctionCode, rhs.businessFunctionCode)
                .append(canSelfApprove, rhs.canSelfApprove).append(bounds, rhs.bounds).isEquals();
    }

    public String getBusinessFunctionCode() {
        return businessFunctionCode;
    }

    public void setBusinessFunctionCode(String businessFunctionCode) {
        this.businessFunctionCode = businessFunctionCode;
    }

    public Boolean getCanSelfApprove() {
        return canSelfApprove;
    }

    public void setCanSelfApprove(Boolean canSelfApprove) {
        this.canSelfApprove = canSelfApprove;
    }

    public List<Bound> getBounds() {
        return bounds;
    }

    public void setBounds(List<Bound> bounds) {
        this.bounds = bounds;
    }

    public PresentationSelfApprovalPolicy businessFunctionCode(String businessFunctionCode) {
        this.businessFunctionCode = businessFunctionCode;
        return this;
    }

    public PresentationSelfApprovalPolicy canSelfApprove(Boolean canSelfApprove) {
        this.canSelfApprove = canSelfApprove;
        return this;
    }

    public PresentationSelfApprovalPolicy bounds(List<Bound> bounds) {
        this.bounds = bounds;
        return this;
    }
}