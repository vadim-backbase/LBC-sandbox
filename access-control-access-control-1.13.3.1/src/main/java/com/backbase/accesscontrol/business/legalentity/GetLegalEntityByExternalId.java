package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.mappers.RetriveLegalEntityMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


/**
 * Get Legal Entity by the external ID, communicates with Access Control P&P Service.
 */
@Service
public class GetLegalEntityByExternalId {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLegalEntityByExternalId.class);

    private AccessControlValidator accessControlValidator;
    private PersistenceLegalEntityService persistenceLegalEntityService;
    private RetriveLegalEntityMapper retriveLegalEntityMapper;

    /**
     * Constructor for {@link GetLegalEntityByExternalId} class.
     *
     * @param persistenceLegalEntityService pandp service
     * @param accessControlValidator validator
     * @param retriveLegalEntityMapper mapper
     */
    public GetLegalEntityByExternalId(
        PersistenceLegalEntityService persistenceLegalEntityService,
        AccessControlValidator accessControlValidator,
        RetriveLegalEntityMapper retriveLegalEntityMapper) {
        this.accessControlValidator = accessControlValidator;
        this.persistenceLegalEntityService = persistenceLegalEntityService;
        this.retriveLegalEntityMapper = retriveLegalEntityMapper;
    }

    /**
     * Method that listens on direct:getLegalEntityByExternalIdRequestedInternal endpoint to forward the request to the
     * P&P service It returns a {@link LegalEntityByExternalIdGetResponseBody} by the given external Id.
     *
     * @param request - void internal request
     * @param externalId - legal entity external id.
     * @return BusinessProcessResult internal request of type {@link LegalEntityByExternalIdGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_BY_EXTERNAL_ID)
    public InternalRequest<LegalEntityByExternalIdGetResponseBody> getLegalEntityByExternalId(
        @Body InternalRequest<Void> request,
        @Header("externalId") String externalId) {
        LOGGER.info("Trying to fetch the Legal Entity with externalID {}", externalId);
        return getInternalRequest(fetchLegalEntityFromPandP(externalId), request.getInternalRequestContext());
    }

    private LegalEntityByExternalIdGetResponseBody fetchLegalEntityFromPandP(String externalId) {

        LegalEntity responseFromPandP = persistenceLegalEntityService.getLegalEntityByExternalId(externalId, true);

        if (accessControlValidator
            .userHasNoAccessToEntitlementResource(responseFromPandP.getId(), AccessResourceType.USER_AND_ACCOUNT)) {
            throw getForbiddenException(LegalEntityErrorCodes.ERR_LE_003.getErrorMessage(),
                LegalEntityErrorCodes.ERR_LE_003.getErrorCode());
        }

        return retriveLegalEntityMapper
            .toLegalEntityByExternalIdGetResponseBody(responseFromPandP);
    }
}
