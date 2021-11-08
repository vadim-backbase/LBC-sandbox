package com.backbase.accesscontrol.domain.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class FunctionGroupBase extends PersistenceFunctionGroup {

    private String serviceAgreementId;

    private List<Permission> permissions = new ArrayList<>();

    public FunctionGroupBase withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    public FunctionGroupBase withPermissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public FunctionGroupBase withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupBase withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupBase withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public FunctionGroupBase withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public FunctionGroupBase withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public FunctionGroupBase withApsId(BigDecimal apsId) {
        super.withApsId(apsId);
        return this;
    }

    @Override
    public FunctionGroupBase withApsName(String apsName) {
        super.withApsName(apsName);
        return this;
    }
}
