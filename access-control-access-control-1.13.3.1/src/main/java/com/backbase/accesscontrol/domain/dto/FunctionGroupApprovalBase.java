package com.backbase.accesscontrol.domain.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class FunctionGroupApprovalBase extends FunctionGroupBase {

    private String approvalId;
    private String approvalTypeId;

    public FunctionGroupApprovalBase withApprovalId(String approvalId) {
        this.approvalId = approvalId;
        return this;
    }

    public FunctionGroupApprovalBase withApprovalTypeId(String approvalTypeId) {
        this.approvalTypeId = approvalTypeId;
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withServiceAgreementId(String serviceAgreementId) {
        super.withServiceAgreementId(serviceAgreementId);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withPermissions(List<Permission> permissions) {
        super.withPermissions(permissions);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withApsId(BigDecimal apsId) {
        super.withApsId(apsId);
        return this;
    }

    @Override
    public FunctionGroupApprovalBase withApsName(String apsName) {
        super.withApsName(apsName);
        return this;
    }
}
