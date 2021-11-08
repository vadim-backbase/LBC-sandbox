package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationUserDataItemPermission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PrivilegesGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UserPermissionsSummaryGetResponseBody;
import java.util.List;

/**
 * Interface for interacting with users.
 */
public interface UsersService {

    /**
     * Get privileges.
     *
     * @param userId - user id
     * @param serviceAgreementId - service agreement id
     * @param functionName - business function name
     * @param resourceName - resource name
     * @return list of permissions
     */
    List<PrivilegesGetResponseBody> getPrivileges(String userId, String serviceAgreementId,
        String functionName, String resourceName);

    /**
     * Check user permission.
     *
     * @param userId - user id
     * @param serviceAgreementId - service agreement id
     * @param functionName - business function name
     * @param resourceName - resource name
     * @param privileges - privileges
     */
    void getUserPermissionCheck(String userId, String serviceAgreementId, String functionName, String resourceName,
        String privileges);

    /**
     * Get arrangements permissions.
     *
     * @param requestData {@link DataItemPermissionsSearchParametersHolder}
     * @return list of arrangements with permissions
     */
    List<ArrangementPrivilegesGetResponseBody> getArrangementPrivileges(
        DataItemPermissionsSearchParametersHolder requestData);

    /**
     * Get permissions for current user context.
     *
     * @return list of permissions
     */
    List<UserPermissionsSummaryGetResponseBody> getUserPermissionsSummary();

    /**
     * Check permission for arrangement.
     *
     * @param request {@link DataItemPermissionsSearchParametersHolder}
     * @param arrangementId arrangement id
     */
    void getArrangementPermissionCheck(DataItemPermissionsSearchParametersHolder request, String arrangementId);

    /**
     * Batch assign permissions to user context.
     *
     * @param userPermissions list of permissions
     * @return list of responses.
     */
    List<BatchResponseItemExtended> saveBulkUserPermissions(
        List<PresentationAssignUserPermissions> userPermissions);

    /**
     * Get data item permissions.
     *
     * @param request {@link DataItemPermissionsSearchParametersHolder}
     * @param dataGroupType data group type
     * @param dataItemId data item id
     * @return list of permissions.
     */
    List<PresentationUserDataItemPermission> getDataItemPrivileges(
        DataItemPermissionsSearchParametersHolder request, String dataGroupType, String dataItemId);
}
