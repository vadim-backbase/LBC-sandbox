package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_092;

import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.ArrangementPrivilegesMapper;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Business consumer for retrieving a arrangement privileges . This class is the business process component of the
 * access-group presentation service, communicating with the persistence service.
 */
@Service
@AllArgsConstructor
public class GetOrCheckArrangementPrivileges {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOrCheckArrangementPrivileges.class);

    private UserAccessPrivilegeService userAccessPrivilegeService;
    private ArrangementPrivilegesMapper arrangementPrivilegesMapper;

    /**
     * Method that listens on the direct:getArrangementPrivilegesRequestedInternal endpoint and forward the request to
     * the persistence service.
     *
     * @param internalRequest internal request of {@link DataItemPermissionsSearchParametersHolder}
     * @return Internal Request of List{@link ArrangementPrivilegesGetResponseBody}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_LIST_ARRANGEMENT_PRIVILEGES)
    public InternalRequest<List<ArrangementPrivilegesGetResponseBody>> getArrangementPrivileges(
        @Body InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest) {

        DataItemPermissionsSearchParametersHolder holder = internalRequest.getData();

        LOGGER.info(
            "Trying to fetch arrangement privileges for user with userId {}, serviceAgreementId {}, "
                + "function {}, resource {}, privilege {} and legalEntityId {}",
            holder.getUserId(), holder.getServiceAgreementId(), holder.getFunctionName(), holder.getResourceName(),
            holder.getPrivilege(), holder.getLegalEntityId());

        List<ArrangementPrivilegesGetResponseBody> privilegesGetResponseBodies = getArrangementPrivilegesList(holder,
            null);

        LOGGER.info(
            "Retrieved {} privileges for userId = {}, serviceAgreementId = {}, "
                + "functionName={}, resourceName={} and privilegeName = {}",
            privilegesGetResponseBodies.size(), holder.getUserId(), holder.getServiceAgreementId(),
            holder.getFunctionName(), holder.getResourceName(), holder.getPrivilege());

        return getInternalRequest(privilegesGetResponseBodies, internalRequest.getInternalRequestContext());
    }

    /**
     * Makes a request to the P&P service to check user arrangement item permissions.
     *
     * @param internalRequest internal request of {@link DataItemPermissionsSearchParametersHolder}
     * @param arrangementId arrangement id
     * @return void of {@link InternalRequest}
     */
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_CHECK_PERMISSIONS_FOR_ARRANGEMENT_ID)
    public InternalRequest<Void> checkPermissions(
        @Body InternalRequest<DataItemPermissionsSearchParametersHolder> internalRequest,
        @Header("id") String arrangementId) {

        DataItemPermissionsSearchParametersHolder holder = internalRequest.getData();
        LOGGER.info(
            "Checking Permission for User {} under Service Agreement {}, "
                + "with Function Name {} , Resource Name {}, Privilege {} and arrangementId {}",
            holder.getUserId(), holder.getServiceAgreementId(), holder.getFunctionName(), holder.getResourceName(),
            holder.getPrivilege(), arrangementId);

        List<ArrangementPrivilegesGetResponseBody> results = getArrangementPrivilegesList(holder, arrangementId);

        if (results.isEmpty()) {
            LOGGER.warn("User does not have any privileges for arrangement with id {}", arrangementId);
            throw getForbiddenException(ERR_AG_092.getErrorMessage(), ERR_AG_092.getErrorCode());
        }

        return getVoidInternalRequest(internalRequest.getInternalRequestContext());
    }

    private List<ArrangementPrivilegesGetResponseBody> getArrangementPrivilegesList(
        DataItemPermissionsSearchParametersHolder holder, String arrangementId) {

        List<ArrangementPrivilegesDto> arrangementPrivileges = userAccessPrivilegeService
            .getArrangementPrivileges(holder.getUserId(), holder.getServiceAgreementId(), holder.getFunctionName(),
                holder.getResourceName(), holder.getPrivilege(), holder.getLegalEntityId(), arrangementId
            );

        return arrangementPrivilegesMapper.toListArrangementPrivilegesGetResponseBodyPresentation(arrangementPrivileges);
    }
}
