package com.backbase.accesscontrol.domain.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class FunctionGroupIngest extends PersistenceFunctionGroup {

    private String externalServiceAgreementId;

    private List<Permission> permissions = new ArrayList<>();

    public FunctionGroupIngest withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    public FunctionGroupIngest withPermissions(List<Permission> permissions) {
        this.permissions = permissions;
        return this;
    }

    @Override
    public FunctionGroupIngest withName(String name) {
        super.withName(name);
        return this;
    }

    @Override
    public FunctionGroupIngest withDescription(String description) {
        super.withDescription(description);
        return this;
    }

    @Override
    public FunctionGroupIngest withType(Type type) {
        super.withType(type);
        return this;
    }

    @Override
    public FunctionGroupIngest withValidFrom(Date validFrom) {
        super.withValidFrom(validFrom);
        return this;
    }

    @Override
    public FunctionGroupIngest withValidUntil(Date validUntil) {
        super.withValidUntil(validUntil);
        return this;
    }

    @Override
    public FunctionGroupIngest withApsId(BigDecimal apsId) {
        super.withApsId(apsId);
        return this;
    }

    @Override
    public FunctionGroupIngest withApsName(String apsName) {
        super.withApsName(apsName);
        return this;
    }
}
