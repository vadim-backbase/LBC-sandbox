package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.business.persistence.useraccess.AssignUserContextPermissionsApprovalHandler;
import com.backbase.accesscontrol.business.persistence.useraccess.AssignUserContextPermissionsHandler;
import com.backbase.accesscontrol.domain.dto.PersistenceUserContextPermissionsApproval;
import com.backbase.accesscontrol.dto.parameterholder.UserIdServiceAgreementIdParameterHolder;
import com.backbase.accesscontrol.dto.parameterholder.UserPermissionsApprovalParameterHolder;
import com.backbase.accesscontrol.mappers.PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper;
import com.backbase.accesscontrol.mappers.PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_111;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_116;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_113;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionsService {

    private final AssignUserContextPermissionsHandler assignUserContextPermissionsHandler;
    private final AssignUserContextPermissionsApprovalHandler assignUserContextPermissionsApprovalHandler;
    private final PresentationFunctionDataGroupItemsListAssignUserContextPermissionsMapper userContextPermissionsMapper;
    private final PresentationFunctionDataGroupItemsPersistenceUserContextPermissionsApprovalMapper userContextPermissionsApprovalMapper;

    /**
     * Save permissions.
     *
     * @param presentationFunctionDataGroupItems - new state of permissions.
     * @param serviceAgreementId                 - service agreement id.
     * @param userId                             - user id.
     */
    @AuditEvent(eventAction = EventAction.UPDATE_PERMISSIONS, objectType = AuditObjectType.UPDATE_USER_PERMISSIONS)
    public void savePermissions(
        PresentationFunctionDataGroupItems presentationFunctionDataGroupItems,
        GetUser userByInternalId,
        String serviceAgreementId, String userId) {

        validateCombinations(presentationFunctionDataGroupItems.getItems());
        validateDataGroupDuplicates(presentationFunctionDataGroupItems.getItems());
        validateSelfApprovalPoliciesDuplicatesPerCombination(presentationFunctionDataGroupItems.getItems());
        filterNulls(presentationFunctionDataGroupItems);

        UserIdServiceAgreementIdParameterHolder holder = new UserIdServiceAgreementIdParameterHolder()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementId);

        assignUserContextPermissionsHandler
            .handleRequest(holder, userContextPermissionsMapper.map(presentationFunctionDataGroupItems, userByInternalId.getLegalEntityId()));

        log
            .info(
                "Updated permissions: Updating permissions {}, for user id {}, "
                    + "under service agreement id {}",
                presentationFunctionDataGroupItems, userId, serviceAgreementId);

    }

    /**
     * Save permission state to approval.
     *
     * @param presentationFunctionDataGroupItems - new state of permissions.
     * @param serviceAgreementId                 - service agreement id.
     * @param userId                             - user id.
     * @param legalEntityId                      - legal entity id of the user
     * @param approvalId                         - approval id.
     */
    @AuditEvent(eventAction = EventAction.REQUEST_PERMISSIONS_UPDATE,
        objectType = AuditObjectType.UPDATE_USER_PERMISSIONS_APPROVAL)
    public void savePermissionsToApproval(PresentationFunctionDataGroupItems presentationFunctionDataGroupItems,
        String serviceAgreementId, String userId, String legalEntityId, String approvalId) {

        validateCombinations(presentationFunctionDataGroupItems.getItems());
        validateDataGroupDuplicates(presentationFunctionDataGroupItems.getItems());
        validateSelfApprovalPoliciesDuplicatesPerCombination(presentationFunctionDataGroupItems.getItems());
        filterNulls(presentationFunctionDataGroupItems);

        PersistenceUserContextPermissionsApproval persistenceUserContextPermissionsApproval =
            userContextPermissionsApprovalMapper.map(presentationFunctionDataGroupItems);
        UserPermissionsApprovalParameterHolder holder = new UserPermissionsApprovalParameterHolder()
            .withApprovalId(approvalId)
            .withLegalEntityId(legalEntityId)
            .withServiceAgreementId(serviceAgreementId)
            .withUserId(userId);
        assignUserContextPermissionsApprovalHandler.handleRequest(holder, persistenceUserContextPermissionsApproval);

        log
            .info(
                "Updated permissions with approval on: Updating permissions {}, for user id {}, "
                    + "under service agreement id {}",
                presentationFunctionDataGroupItems, userId, serviceAgreementId);

    }

    private void filterNulls(PresentationFunctionDataGroupItems presentationFunctionDataGroupItems) {
        List<PresentationFunctionDataGroup> items = presentationFunctionDataGroupItems.getItems().stream()
                .filter(item -> item != null && item.getFunctionGroupId() != null)
                .collect(Collectors.toList());

        presentationFunctionDataGroupItems.setItems(items);
    }

    private void validateCombinations(List<PresentationFunctionDataGroup> items) {
        Map<String, Boolean> combinations = new HashMap<>();

        for (PresentationFunctionDataGroup fgDg : items) {
            boolean fgHasNoCombination = Optional.ofNullable(fgDg.getDataGroupIds())
                .orElse(Collections.emptyList()).isEmpty();
            if (combinations.containsKey(fgDg.getFunctionGroupId()) && !combinations.get(fgDg.getFunctionGroupId())
                .equals(fgHasNoCombination)) {
                throw getBadRequestException(ERR_AG_111.getErrorMessage(), ERR_AG_111.getErrorCode());
            }
            combinations.put(fgDg.getFunctionGroupId(), fgHasNoCombination);
        }
    }

    private void validateDataGroupDuplicates(List<PresentationFunctionDataGroup> items) {
        Map<String, Set<String>> combinations = new HashMap<>();

        for (PresentationFunctionDataGroup item : items) {
            if (item.getDataGroupIds() != null && !item.getDataGroupIds().isEmpty()) {
                Set<String> dataGroupIds = getDataGroupIds(item.getDataGroupIds());

                if (dataGroupIds.size() != item.getDataGroupIds().size()
                        || dataGroupIds.equals(combinations.get(item.getFunctionGroupId()))) {
                    throw getBadRequestException(ERR_AG_116.getErrorMessage(), ERR_AG_116.getErrorCode());
                }

                combinations.put(item.getFunctionGroupId(), dataGroupIds);
            }
        }
    }

    private Set<String> getDataGroupIds(List<PresentationGenericObjectId> dataGroups) {
        return dataGroups.stream()
                .map(PresentationGenericObjectId::getId)
                .collect(Collectors.toSet());
    }

    private void validateSelfApprovalPoliciesDuplicatesPerCombination(List<PresentationFunctionDataGroup> items) {
        for (PresentationFunctionDataGroup item : items) {
            if (item.getSelfApprovalPolicies() != null && !item.getSelfApprovalPolicies().isEmpty()) {
                Map<String, Long> businessFunctionsCounterByFunctionName = item.getSelfApprovalPolicies().stream()
                        .map(SelfApprovalPolicy::getBusinessFunctionName)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                Optional<String> repeatedBusinessFunctionPerCombination = businessFunctionsCounterByFunctionName
                        .entrySet().stream()
                        .filter(this::isSelfApprovalPolicyBusinessFunctionRepeated)
                        .findFirst()
                        .map(Map.Entry::getKey);

                if (repeatedBusinessFunctionPerCombination.isPresent()) {
                    String businessFunction = repeatedBusinessFunctionPerCombination.get();
                    log.warn("Business Function " + businessFunction
                            + " is duplicated for SelfApprovalPolicies per combination of functionGroup and dataGroups");
                    String errorMessage = String.format(ERR_ACC_113.getErrorMessage(), businessFunction);
                    throw getBadRequestException(errorMessage, ERR_ACC_113.getErrorCode());
                }
            }
        }
    }

    private boolean isSelfApprovalPolicyBusinessFunctionRepeated(Map.Entry<String, Long> businessFunctionCounterByName) {
        return businessFunctionCounterByName.getValue() > 1;
    }
}
