package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.persistence.legalentity.CreateLegalEntityHandler;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
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
public class AddLegalEntities {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddLegalEntities.class);

    private CreateLegalEntityHandler crateLegalEntityHandler;

    /**
     * Method that listens on {@link EndpointConstants#DIRECT_BUSINESS_ADD_LEGAL_ENTITY} endpoint It returns a {@link
     * CreateLegalEntitiesPostResponseBody}.
     *
     * @param request body type internal request of {@link CreateLegalEntitiesPostRequestBody}
     * @return internal request of {@link CreateLegalEntitiesPostResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_ADD_LEGAL_ENTITY)
    public InternalRequest<CreateLegalEntitiesPostResponseBody> createLegalEntity(
        @Body InternalRequest<CreateLegalEntitiesPostRequestBody> request) {

        LOGGER.info("Trying to save Legal Entity with external id {}", request.getData().getExternalId());
        return getInternalRequest(
            crateLegalEntityHandler.handleRequest(new EmptyParameterHolder(), request.getData()),
            request.getInternalRequestContext());
    }

}
