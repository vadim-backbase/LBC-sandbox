package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static java.util.Objects.isNull;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityAsParticipantHandler;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CreateLegalEntityAsParticipantFlow extends
                AbstractFlow<LegalEntityAsParticipantPostRequestBody, LegalEntityAsParticipantPostResponseBody> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntityAsParticipantFlow.class);
    
    private CreateLegalEntityAsParticipantHandler handler;
    private UserContextUtil userContextUtil;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected LegalEntityAsParticipantPostResponseBody execute(LegalEntityAsParticipantPostRequestBody request) {
        LOGGER.info("Trying to save legal entity as participant {}", request);
        
        String newExternalId = this.externalSave(request);
        request.setLegalEntityExternalId(newExternalId);
        this.validateExternalId(request);
        
        String legalEntityId = userContextUtil.getUserContextDetails().getLegalEntityId();
        
        return handler.handleRequest(new SingleParameterHolder<String>(legalEntityId), request);
    }
    
    /**
     * This method should be overwritten when is required the legal entity to be saved in external system.
     *
     * @param request - Legal Entity request data.
     * @return external id of the legal entity
     */
    protected String externalSave(LegalEntityAsParticipantPostRequestBody request) {
        return request.getLegalEntityExternalId();
    }

    private void validateExternalId(LegalEntityAsParticipantPostRequestBody responseFromBank) {
        if (isNull(responseFromBank.getLegalEntityExternalId())) {
            throw getBadRequestException(LegalEntityErrorCodes.ERR_LE_020.getErrorMessage(),
                LegalEntityErrorCodes.ERR_LE_020.getErrorCode());
        }
    }

}
