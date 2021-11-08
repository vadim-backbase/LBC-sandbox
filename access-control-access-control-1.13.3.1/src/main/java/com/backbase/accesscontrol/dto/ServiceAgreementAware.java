package com.backbase.accesscontrol.dto;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.ToStringBuilder;

@Getter
@Setter
public class ServiceAgreementAware {

    private String serviceAgreementId;
    private InternalRequestContext internalRequestContext;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("serviceAgreementId", serviceAgreementId)
            .append("internalRequestContext", internalRequestContext)
            .toString();
    }
}
