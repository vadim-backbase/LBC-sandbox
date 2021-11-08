package com.backbase.accesscontrol.business.usercontext;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class is the business process component of the access-group presentation service, communicating with the
 * UserContextPnpService.
 */
@Service
@AllArgsConstructor
public class GetUserContexts {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetUserContexts.class);

    private UserContextService userContextService;
    private UserManagementService userManagementService;
    private UserContextUtil userContextUtil;

    /**
     * Access to the PnP layer to get the list of user context by provider user id.
     *
     * @param request Internal Request
     * @param userId  User provider Id
     * @param query   Filter by service agreement name
     * @param from    Beginning of the page
     * @param cursor  Pagination cursor
     * @param size    Pagination size
     * @return List of user context and total number of elements.
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_GET_USER_CONTEXT)
    public InternalRequest<ListElementsWrapper<UserContextServiceAgreementsGetResponseBody>> getUserContextsByUserId(
        @Body InternalRequest<Void> request,
        @Header("userId") String userId,
        @Header("query") String query,
        @Header("from") Integer from,
        @Header("cursor") String cursor,
        @Header("size") Integer size) {
        LOGGER.info(
            "Service call for get user context by user provider. User internal user Id {}, "
                + "query {}, from {}, size {}",
            userId, query, from, size);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> userContexts =
            getListElementsWrapper(userId, query, from, size);
        return getInternalRequest(userContexts, request.getInternalRequestContext());
    }

    private ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> getListElementsWrapper(String userId,
        String query, Integer from, Integer size) {

        String internalUserId = getInternalUserId(userId);

        UserContextsGetResponseBody userContextsByUserId =
            userContextService.getUserContextsByUserId(internalUserId, query, from, size);
        List<UserContextServiceAgreementsGetResponseBody> records = userContextsByUserId
            .getElements()
            .stream()
            .map(this::getUserContextGetResponseBody)
            .collect(Collectors.toList());

        return new ListElementsWrapper<>(records, userContextsByUserId.getTotalElements());
    }

    private UserContextServiceAgreementsGetResponseBody getUserContextGetResponseBody(Element userContextGetResponse) {
        return new UserContextServiceAgreementsGetResponseBody()
            .withName(userContextGetResponse.getServiceAgreementName())
            .withId(userContextGetResponse.getServiceAgreementId())
            .withIsMaster(userContextGetResponse.getServiceAgreementMaster())
            .withExternalId(userContextGetResponse.getExternalId())
            .withDescription(userContextGetResponse.getDescription());
    }

    private String getInternalUserId(String userId) {
        if (nonNull(userId) && userId.equals(userContextUtil.getAuthenticatedUserName())) {
            return userContextUtil.getUserContextDetails().getInternalUserId();
        }
        return userManagementService.getUserByExternalId(userId).getId();
    }
}
