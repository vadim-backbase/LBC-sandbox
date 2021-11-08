package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityWithInternalParentIdHandler;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateLegalEntityFlow extends
    AbstractFlow<PresentationCreateLegalEntityItemPostRequestBody, LegalEntitiesPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntityFlow.class);

    private CreateLegalEntityWithInternalParentIdHandler createLegalEntityWithInternalParentIdHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntitiesPostResponseBody execute(PresentationCreateLegalEntityItemPostRequestBody request) {
        LOGGER.info("Trying to save Legal Entity {}", request);

        String newExternalId = this.externalSave(request);
        request.setExternalId(newExternalId);
        this.validateExternalId(request);
        return createLegalEntityWithInternalParentIdHandler.handleRequest(new EmptyParameterHolder(), request);
    }

    /**
     * This method should be overwritten when is required the legal entity to be saved in external system.
     *
     * @param request - Legal Entity request data.
     * @return external id of the legal entity
     */
    protected String externalSave(PresentationCreateLegalEntityItemPostRequestBody request) {

        return request.getExternalId();
    }

    private void validateExternalId(
        PresentationCreateLegalEntityItemPostRequestBody responseFromBank) {
        if (isNull(responseFromBank.getExternalId())) {
            throw getBadRequestException(LegalEntityErrorCodes.ERR_LE_020.getErrorMessage(),
                LegalEntityErrorCodes.ERR_LE_020.getErrorCode());
        }
    }
}
