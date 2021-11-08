package com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements;

import java.util.ArrayList;
import java.util.List;

public class SelfApprovalPolicy {

    private String businessFunctionName;

    private Boolean canSelfApprove;

    private List<Bound> bounds = new ArrayList<>();

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