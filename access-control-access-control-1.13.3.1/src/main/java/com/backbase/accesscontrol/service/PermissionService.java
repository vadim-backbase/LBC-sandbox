package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.domain.dto.UserContextPermissions;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationPermissionsApprovalDetailsItem;
import java.util.Set;

public interface PermissionService {

    /**
     * Update user permission.
     *
     * @param assignUserPermissionsData {@link AssignUserPermissionsData}
     */
    String updateUserPermission(AssignUserPermissionsData assignUserPermissionsData);

    /**
     * Assign user context permissions.
     *
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @param userLegalEntityId  user legal entity id
     * @param permissionsState   set of {@link UserContextPermissions}
     */
    void assignUserContextPermissions(String serviceAgreementId, String userId,
        String userLegalEntityId, Set<UserContextPermissions> permissionsState);

    /**
     * Assign user context permissions approval.
     *
     * @param serviceAgreementId service agreement id
     * @param userId             user id
     * @param legalEntityId      legal entity id
     * @param approvalId         approval id
     * @param requestBodies      set of {@link UserContextPermissions}
     */
    void assignUserContextPermissionsApproval(String serviceAgreementId,
        String userId,
        String legalEntityId,
        String approvalId,
        Set<UserContextPermissions> requestBodies);

    /**
     * Get user permission approval details.
     *
     * @param approvalId approval id.
     * @return {@link PresentationPermissionsApprovalDetailsItem}
     */
    PresentationPermissionsApprovalDetailsItem getUserPermissionApprovalDetails(String approvalId);
}
