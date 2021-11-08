package com.backbase.accesscontrol.domain.dto;

import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ResponseItemExtended extends ResponseItem {

    private String externalServiceAgreementId;
    private PresentationActionDto action;

    public ResponseItemExtended(String resourceId, String externalServiceAgreementId, ItemStatusCode status,
        PresentationActionDto action, List<String> errors) {
        super(resourceId, status, errors);
        this.externalServiceAgreementId = externalServiceAgreementId;
        this.action = action;
    }

    public ResponseItemExtended withExternalServiceAgreementId(String externalServiceAgreementId) {
        this.externalServiceAgreementId = externalServiceAgreementId;
        return this;
    }

    public ResponseItemExtended withAction(PresentationActionDto action) {
        this.action = action;
        return this;
    }
}
