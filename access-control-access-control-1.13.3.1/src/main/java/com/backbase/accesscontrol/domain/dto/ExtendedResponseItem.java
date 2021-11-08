package com.backbase.accesscontrol.domain.dto;

import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import java.util.List;
import java.util.Objects;
import lombok.Getter;

@Getter
public class ExtendedResponseItem extends ResponseItem {

    private String externalServiceAgreementId;

    public ExtendedResponseItem(String resourceId, ItemStatusCode status, List<String> errors,
        String externalServiceAgreementId) {
        super(resourceId, status, errors);
        this.externalServiceAgreementId = externalServiceAgreementId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExtendedResponseItem)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExtendedResponseItem that = (ExtendedResponseItem) o;
        return externalServiceAgreementId.equals(that.externalServiceAgreementId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalServiceAgreementId);
    }
}
