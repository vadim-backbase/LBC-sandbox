package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;

import com.backbase.accesscontrol.business.persistence.legalentity.UpdateLegalEntityHandler;
import com.backbase.accesscontrol.dto.parameterholder.SingleParameterHolder;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Updates Legal Entity by it's external ID, communicate with persistence access control service.
 */
@Service
@AllArgsConstructor
public class UpdateLegalEntityByExternalId {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLegalEntityByExternalId.class);

    private UpdateLegalEntityHandler updateLegalEntityHandler;

    /**
     * Method that listens on {@link EndpointConstants#DIRECT_DEFAULT_UPDATE_LEGAL_ENTITY_BY_EXTERNAL_ID} endpoint.
     *
     * @param request - internal request containing the {@link LegalEntityByExternalIdPutRequestBody}
     * @return void {@link InternalRequest}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_UPDATE_LEGAL_ENTITY_BY_EXTERNAL_ID)
    public InternalRequest<Void> updateLegalEntityByExternalId(
        @Body InternalRequest<LegalEntityByExternalIdPutRequestBody> request,
        @Header("externalId") String externalId) {

        LOGGER.info("Trying to update the type: {} of the Legal Entity with external id {}",
            request.getData().getType(), externalId);

        sendRequestToUpdateLegalEntity(request.getData(), externalId);

        return new InternalRequest<>();
    }

    /**
     * Request to update legal entity.
     *
     * @param request    of type {@link LegalEntityByExternalIdPutRequestBody}
     * @param externalId - external legal entity id.
     */
    protected void sendRequestToUpdateLegalEntity(
        LegalEntityByExternalIdPutRequestBody request, String externalId) {
        LOGGER.info("Trying to update legal entity with external id {}", externalId);

        try {
            updateLegalEntityHandler.handleRequest(new SingleParameterHolder<>(externalId), request);
        } catch (NotFoundException e) {
            throw getNotFoundException(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getKey());
        } catch (BadRequestException e) {
            throw getBadRequestException(e.getErrors().get(0).getMessage(), e.getErrors().get(0).getKey());
        }
    }
}
