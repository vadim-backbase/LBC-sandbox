package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Get Legal Entity business consumer, the business process component of the Legal Entity presentation service,
 * communicating with the integration services.
 */
@Service
public class GetLegalEntityById {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLegalEntityById.class);

    private AccessControlValidator accessControlValidator;

    private LegalEntityPAndPService legalEntityPAndPService;
    private ObjectConverter objectConverter;

    /**
     * Constructor for {@link GetLegalEntityById} class.
     *
     * @param legalEntityPAndPService legal entity pandp service
     * @param accessControlValidator validator
     * @param objectConverter object converter
     */
    public GetLegalEntityById(LegalEntityPAndPService legalEntityPAndPService,
        AccessControlValidator accessControlValidator,
        ObjectConverter objectConverter) {
        this.legalEntityPAndPService = legalEntityPAndPService;
        this.accessControlValidator = accessControlValidator;
        this.objectConverter = objectConverter;
    }

    /**
     * Method that listens on direct:getLegalEntityByIdRequestedInternal endpoint to forward the request to the P&P
     * service It returns a {@link LegalEntityByIdGetResponseBody} based on the legalEngtityParentId.
     *
     * @param request Internal Request of {@link Void} type to be send by the client
     * @param legalEntityId Legal Entity's id
     * @return Internal Request of List {@link LegalEntityByIdGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_BY_ID)
    public InternalRequest<LegalEntityByIdGetResponseBody> getLegalEntityById(@Body InternalRequest<Void> request,
        @Header("legalEntityId") String legalEntityId) {

        LOGGER.info("Trying to fetch the Legal Entity with legalEntityId {}", legalEntityId);

        return getInternalRequest(getLegalEntityByIdResult(legalEntityId), request.getInternalRequestContext());
    }

    /**
     * Get Business Process Result.
     *
     * @param legalEntityId - legal entity internal id
     * @return BusinessProcessResult of type {@link LegalEntityByIdGetResponseBody}
     */
    protected LegalEntityByIdGetResponseBody getLegalEntityByIdResult(String legalEntityId) {

        LOGGER.info("Trying to fetch legal entity by legal entity id {}", legalEntityId);

        if (accessControlValidator.userHasNoAccessToEntitlementResource(legalEntityId,
            AccessResourceType.USER_AND_ACCOUNT)) {
            throw getForbiddenException(LegalEntityErrorCodes.ERR_LE_003.getErrorMessage(),
                LegalEntityErrorCodes.ERR_LE_003.getErrorCode());
        }

        LegalEntityGetResponseBody legalEntityPandpResponse = legalEntityPAndPService
            .getLegalEntityByIdAsResponseBody(legalEntityId);

        LegalEntityByIdGetResponseBody legalEntityByIdGetResponseBody = objectConverter
            .convertValue(legalEntityPandpResponse, LegalEntityByIdGetResponseBody.class);

        return legalEntityByIdGetResponseBody;
    }
}
