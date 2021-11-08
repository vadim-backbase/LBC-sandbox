package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;

import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.service.ObjectConverter;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityForUserGetResponseBody;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Get Legal Entity business consumer, the business process component of the Legal Entity presentation service,
 * communicating with the integration services.
 */
@Service
@AllArgsConstructor
public class GetLegalEntityForCurrentUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetLegalEntityForCurrentUser.class);

    private ObjectConverter objectConverter;
    private LegalEntityPAndPService legalEntityPAndPService;
    private UserContextUtil userContextUtil;

    /**
     * Method that listens on direct:getLegalEntityForCurrentUserRequestedInternal endpoint to forward the request to
     * the P&P service It returns a {@link LegalEntityForUserGetResponseBody} based on the current user.
     *
     * @param request Internal Request of {@link Void} type to be send by the client
     * @return Internal Request of List{@link LegalEntityForUserGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_LEGAL_ENTITY_FOR_CURRENT_USER)
    public InternalRequest<LegalEntityForUserGetResponseBody> getLegalEntityForCurrentUser(
        @Body InternalRequest<Void> request) {
        String legalEntityId = userContextUtil.getUserContextDetails().getLegalEntityId();

        LOGGER.info("Trying to fetch the Legal Entity with id {} for the current user", legalEntityId);

        return getInternalRequest(getLegalEntityForCurrentUser(legalEntityId), request.getInternalRequestContext());

    }

    private LegalEntityForUserGetResponseBody getLegalEntityForCurrentUser(
        String legalEntityId) {

        LegalEntityGetResponseBody legalEntityForUser = legalEntityPAndPService
            .getLegalEntityByIdAsResponseBody(legalEntityId);

        LegalEntityForUserGetResponseBody legalEntityGetResponseBodyInternalRequest = objectConverter
            .convertValue(legalEntityForUser, LegalEntityForUserGetResponseBody.class);

        return legalEntityGetResponseBodyInternalRequest;
    }
}
