package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.InternalRequestUtil.getVoidInternalRequest;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.routes.useraccess.AssignUserPermissionsBatchRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.CheckUserArrangementItemPermissionRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.GetArrangementPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.GetUserPrivilegesSummaryRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ListDataItemPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ListPrivilegesRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ValidatePermissionsRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import java.util.List;
import org.apache.camel.Produce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of service for User Forwards the request on to relevant camel route using route proxies.
 */
@Service
public class UsersServiceImpl implements UsersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersServiceImpl.class);

    @Autowired
    InternalRequestContext internalRequestContext;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_PRIVILEGES)
    private ListPrivilegesRouteProxy listPrivilegesRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_CHECK_PERMISSIONS)
    private ValidatePermissionsRouteProxy validatePermissionsRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_ARRANGEMENT_PRIVILEGES)
    private GetArrangementPrivilegesRouteProxy getArrangementPrivilegesRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_USER_PRIVILEGES_SUMMARY)
    private GetUserPrivilegesSummaryRouteProxy getUserPrivilegesSummaryRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_CHECK_PERMISSIONS_FOR_ARRANGEMENT_ID)
    private CheckUserArrangementItemPermissionRouteProxy checkUserArrangementItemPermissionRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_ASSIGN_USER_PERMISSIONS_BATCH)
    private AssignUserPermissionsBatchRouteProxy assignUserPermissionsBatchRouteProxy;

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_LIST_DATA_ITEM_PRIVILEGES)
    private ListDataItemPrivilegesRouteProxy listDataItemPrivilegesRouteProxy;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PrivilegesGetResponseBody> getPrivileges(String userId, String serviceAgreementId,
        String functionName, String resourceName) {
        LOGGER.info(
            "Trying to get privileges for user with id {}, service agreement ID {}, "
                + "function with name {} and resource with name {}",
            userId, serviceAgreementId, functionName, resourceName);
        return listPrivilegesRouteProxy
            .getPrivileges(getVoidInternalRequest(internalRequestContext), userId, serviceAgreementId,
                functionName, resourceName).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getUserPermissionCheck(String userId, String serviceAgreementId, String functionName,
        String resourceName, String privileges) {

        validatePermissionsRouteProxy
            .getUserPermissionCheck(getVoidInternalRequest(internalRequestContext), userId,
                serviceAgreementId, resourceName, functionName, privileges);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ArrangementPrivilegesGetResponseBody> getArrangementPrivileges(
        DataItemPermissionsSearchParametersHolder requestData) {

        LOGGER.info(
            "Getting all arrangements with their privileges for user with id {} and service agreement with id {}",
            requestData.getUserId(), requestData.getServiceAgreementId());

        return getArrangementPrivilegesRouteProxy
            .getArrangementPrivileges(getInternalRequest(requestData, internalRequestContext)).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserPermissionsSummaryGetResponseBody> getUserPermissionsSummary() {
        return getUserPrivilegesSummaryRouteProxy
            .getUserPrivilegesSummary(getVoidInternalRequest(internalRequestContext)).getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getArrangementPermissionCheck(DataItemPermissionsSearchParametersHolder request, String arrangementId) {

        LOGGER.info("Check if user {} has permission for arrangement id {} ", request.getUserId(), arrangementId);

        checkUserArrangementItemPermissionRouteProxy
            .getArrangementPermissionCheck(getInternalRequest(request, internalRequestContext), arrangementId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PresentationUserDataItemPermission> getDataItemPrivileges(
        DataItemPermissionsSearchParametersHolder holder,
        String dataGroupType,
        String dataItemId) {

        LOGGER.info(
            "Trying to get privileges for user with id {}, service agreement ID {}, "
                + "function with name {}, resource with name {},"
                + "privilege {}, data group type {} and data item id {}",
            holder.getUserId(), holder.getServiceAgreementId(), holder.getFunctionName(), holder.getResourceName(),
            holder.getPrivilege(), dataGroupType, dataItemId);

        return listDataItemPrivilegesRouteProxy
            .getDataItemPrivileges(getInternalRequest(holder, internalRequestContext), dataGroupType, dataItemId)
            .getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BatchResponseItemExtended> saveBulkUserPermissions(
        List<PresentationAssignUserPermissions> userPermissions) {
        return assignUserPermissionsBatchRouteProxy
            .assignUserPermissionsBatch(getInternalRequest(userPermissions, internalRequestContext)).getData();
    }
}
