package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.persistence.legalentity.AddLegalEntityHandler;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Creates Legal Entity, do validation before creation, communicate with PandP access control service.
 */
@Service
@AllArgsConstructor
public class CreateLegalEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLegalEntities.class);

    private AddLegalEntityHandler addLegalEntityHandler;

    /**
     * Method that listens on {@link EndpointConstants#DIRECT_BUSINESS_CREATE_LEGAL_ENTITY} endpoint It returns a {@link
     * LegalEntitiesPostResponseBody}.
     *
     * @param request type internal request of {@link LegalEntitiesPostRequestBody}
     * @return internal request of {@link LegalEntitiesPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_CREATE_LEGAL_ENTITY)
    public InternalRequest<LegalEntitiesPostResponseBody> createLegalEntity(
        @Body InternalRequest<LegalEntitiesPostRequestBody> request) {

        LOGGER.info("Trying to save Legal Entity with external id {}", request.getData().getExternalId());

        return getInternalRequest(addLegalEntityHandler.handleRequest(new EmptyParameterHolder(), request.getData()),
            request.getInternalRequestContext());
    }
}
