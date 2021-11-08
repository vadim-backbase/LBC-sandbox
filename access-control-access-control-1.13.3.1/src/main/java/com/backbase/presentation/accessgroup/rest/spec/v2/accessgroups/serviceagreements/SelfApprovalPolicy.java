package com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class SelfApprovalPolicy {
    private String businessFunctionName;

    private Boolean canSelfApprove;

    private List<Bound> bounds = new ArrayList<>();

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(businessFunctionName).append(canSelfApprove).append(bounds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SelfApprovalPolicy)) {
            return false;
        }
        SelfApprovalPolicy rhs = ((SelfApprovalPolicy) other);
        return new EqualsBuilder().append(businessFunctionName, rhs.businessFunctionName)
                .append(canSelfApprove, rhs.canSelfApprove).append(bounds, rhs.bounds).isEquals();
    }

    public String getBusinessFunctionName() {
        return businessFunctionName;
    }

    public void setBusinessFunctionName(String businessFunctionName) {
        this.businessFunctionName = businessFunctionName;
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

    public SelfApprovalPolicy businessFunctionName(String businessFunctionName) {
        this.businessFunctionName = businessFunctionName;
        return this;
    }

    public SelfApprovalPolicy canSelfApprove(Boolean canSelfApprove) {
        this.canSelfApprove = canSelfApprove;
        return this;
    }

    public SelfApprovalPolicy bounds(List<Bound> bounds) {
        this.bounds = bounds;
        return this;
    }
}