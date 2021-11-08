package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.routes.useraccess.GetUserContextsRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ValidateServiceAgreementRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service for User Context Forwards the request on to relevant camel route using route proxies.
 */
@Service
public class UserContextServiceImpl implements UserContextService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserContextServiceImpl.class);

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_USER_CONTEXT)
    private GetUserContextsRouteProxy getUserContextsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_VALIDATE_SERVICE_AGREEMENT)
    private ValidateServiceAgreementRouteProxy validateServiceAgreementRouteProxy;

    @Autowired
    private InternalRequestContext internalRequestContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> getUserContextByUserId(String userId,
        String query, Integer from, String cursor, Integer size) {
        LOGGER.info(
            "Service call for get user context by user provider. "
                + "User External Id, query {}, from {}, size {}",
            query, from, size);
        return getUserContextsRouteProxy
            .getUserContextsByUserId(getVoidInternalRequest(internalRequestContext), userId, query, from, cursor,
                size).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validate(String externalUserId, String serviceAgreementId) {
        LOGGER.info("Service call to validate the serviceAgreementId {} for the user {}", serviceAgreementId,
            externalUserId);
        return validateServiceAgreementRouteProxy.validate(getVoidInternalRequest(internalRequestContext),
            externalUserId, serviceAgreementId).getData();
    }
}
