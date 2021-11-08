package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_082;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.AssignUserPermissionsData;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.routes.useraccess.AssignUserPermissionsBatchRouteProxy;
import com.backbase.accesscontrol.service.batch.permission.UserPermissionBatchUpdate;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationAssignUserPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.PresentationFunctionGroupDataGroup;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.Consume;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AssignUserPermissions implements AssignUserPermissionsBatchRouteProxy {

    private UserManagementService userManagementService;
    private UserPermissionBatchUpdate userPermissionBatchUpdate;
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper;

    /**
     * Makes a request to the P&P service to assign user permissions.
     *
     * @param request internal request of list of {@link PresentationAssignUserPermissions}
     * @return InternalRequest Response from the P&P Service
     */
    @Override
    @Consume(value = EndpointConstants.DIRECT_DEFAULT_BUSINESS_ASSIGN_USER_PERMISSIONS)
    public InternalRequest<List<BatchResponseItemExtended>> assignUserPermissionsBatch(
        InternalRequest<List<PresentationAssignUserPermissions>> request) {

        return getInternalRequest(saveBulkUserPermissions(request.getData()), request.getInternalRequestContext());
    }

    /**
     * Batch method to update the new user permissions state on P and P.
     *
     * @param userPermissions List of the new users' permissions states
     * @return list of response for every request
     */
    public List<BatchResponseItemExtended> saveBulkUserPermissions(
        List<PresentationAssignUserPermissions> userPermissions) {

        List<BatchResponseItemExtended> responses = Lists.newArrayList();
        List<PresentationAssignUserPermissions> validRequests = Lists.newArrayList();
        Map<PresentationAssignUserPermissions, Integer> userPermissionsToIndexMap = new HashMap<>();
        Map<String, String> uppercaseToOriginalExternalUserIdMap = new HashMap<>();

        userPermissions
            .forEach(userPermission -> {
                String externalUserId = userPermission.getExternalUserId();
                if (nonNull(externalUserId)) {
                    uppercaseToOriginalExternalUserIdMap.put(externalUserId.toUpperCase(),
                        userPermission.getExternalUserId());
                }
                var userPermissionTemp = copyUserPermissionWithUpperCase(userPermission);
                userPermissionsToIndexMap.put(userPermissionTemp, userPermissions.indexOf(userPermission));
                if (userPermission.getFunctionGroupDataGroups() == null
                    || validateFunctionGroupDataGroups(userPermission.getExternalServiceAgreementId(),
                    userPermission.getFunctionGroupDataGroups())) {
                    validRequests.add(userPermissionTemp);
                    responses.add(null);
                } else {
                    responses.add(new BatchResponseItemExtended()
                        .withExternalServiceAgreementId(userPermission.getExternalServiceAgreementId())
                        .withResourceId(userPermission.getExternalUserId())
                        .withErrors(Lists.newArrayList(AccessGroupErrorCodes.ERR_AG_081.getErrorMessage()))
                        .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST));
                }
            });

        ListMultimap<String, PresentationAssignUserPermissions> userPermissionsByUserId = ArrayListMultimap.create();

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> responseWithUsers = new ArrayList<>();
        Set<String> externalIds = new HashSet<>();

        if (!validRequests.isEmpty()) {

            externalIds = validRequests.stream()
                .filter(e -> nonNull(e.getExternalUserId()) && !e.getExternalUserId().equalsIgnoreCase("null"))
                .map(user -> {
                    userPermissionsByUserId.put(user.getExternalUserId(), user);
                    return user.getExternalUserId();
                })
                .collect(toSet());

            if (!externalIds.isEmpty()) {
                responseWithUsers = userManagementService.getUsersByExternalIds(Lists.newArrayList(externalIds));
            } else {
                return getErrorBatchResultUserNotFound(responses, validRequests, uppercaseToOriginalExternalUserIdMap);
            }
        }

        List<PresentationAssignUserPermissions> cleanValidRequests = cleanValidUsersAndCreateResponseForNoUserRequests(
            responses, validRequests, userPermissionsToIndexMap, userPermissionsByUserId, externalIds,
            responseWithUsers, uppercaseToOriginalExternalUserIdMap);

        Map<String, GetUser> externalIdToUserMap = new HashMap<>();
        Map<String, String> idToExternalIdMap = new HashMap<>();

        createUserIdsIndexes(responseWithUsers, externalIdToUserMap, idToExternalIdMap,
            uppercaseToOriginalExternalUserIdMap);

        List<AssignUserPermissionsData> requestBody = cleanValidRequests.stream()
            .map(userPermission -> new AssignUserPermissionsData(userPermission, externalIdToUserMap))
            .collect(Collectors.toList());

        List<BatchResponseItemExtended> persistenceResponses = new ArrayList<>();

        if (!requestBody.isEmpty()) {
            persistenceResponses = batchResponseItemExtendedMapper
                .mapList(userPermissionBatchUpdate.processBatchItems(requestBody));
        }

        return mergeBulkResponses(idToExternalIdMap, responses, persistenceResponses);
    }

    private PresentationAssignUserPermissions copyUserPermissionWithUpperCase(
        PresentationAssignUserPermissions userPermission) {
        PresentationAssignUserPermissions presentationAssignUserPermissions = new PresentationAssignUserPermissions();
        presentationAssignUserPermissions.setExternalUserId(
            nonNull(userPermission.getExternalUserId()) ? userPermission.getExternalUserId().toUpperCase() : null);
        presentationAssignUserPermissions.setExternalServiceAgreementId(userPermission.getExternalServiceAgreementId());
        presentationAssignUserPermissions.setFunctionGroupDataGroups(userPermission.getFunctionGroupDataGroups());
        presentationAssignUserPermissions.setAdditions(userPermission.getAdditions());
        return presentationAssignUserPermissions;
    }

    private void createUserIdsIndexes(List<com.backbase.dbs.user.api.client.v2.model.GetUser> responseWithUsers,
        Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> externalIdToUserMap,
        Map<String, String> idToExternalIdMap,
        Map<String, String> uppercaseToOriginalExternalUserIdMap) {
        responseWithUsers.forEach(user -> {
            externalIdToUserMap.put(user.getExternalId().toUpperCase(), user);
            idToExternalIdMap
                .put(user.getId(), uppercaseToOriginalExternalUserIdMap.get(user.getExternalId().toUpperCase()));
        });
    }


    private List<PresentationAssignUserPermissions> cleanValidUsersAndCreateResponseForNoUserRequests(
        List<BatchResponseItemExtended> responses,
        List<PresentationAssignUserPermissions> validRequests,
        Map<PresentationAssignUserPermissions, Integer> userPermissionsToIndexMap,
        ListMultimap<String, PresentationAssignUserPermissions> userPermissionsByUserId,
        Set<String> externalIds,
        List<com.backbase.dbs.user.api.client.v2.model.GetUser> responseWithUsers,
        Map<String, String> uppercaseToOriginalExternalUserIdMap) {

        Set<String> foundUsersByExternalId = responseWithUsers.stream()
            .map(item->item.getExternalId().toUpperCase())
            .collect(toSet());

        Set<String> invalidExternalUserId = Sets.difference(externalIds, foundUsersByExternalId);

        invalidExternalUserId.forEach(externalId -> {
            List<PresentationAssignUserPermissions> invalidUerPermissions = userPermissionsByUserId.get(externalId);
            invalidUerPermissions.forEach(userPermission -> {
                int index = userPermissionsToIndexMap.get(userPermission);
                responses.set(index, new BatchResponseItemExtended()
                    .withResourceId(uppercaseToOriginalExternalUserIdMap.get(userPermission.getExternalUserId()))
                    .withExternalServiceAgreementId(userPermission.getExternalServiceAgreementId())
                    .withErrors(Lists.newArrayList(ERR_AG_082.getErrorMessage()))
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST));
            });
        });

        return validRequests.stream()
            .filter(userPermission -> !invalidExternalUserId.contains(userPermission.getExternalUserId()))
            .collect(Collectors.toList());
    }

    private List<BatchResponseItemExtended> mergeBulkResponses(Map<String, String> idToExternalId,
        List<BatchResponseItemExtended> allResponses, List<BatchResponseItemExtended> persistenceResponses) {
        int index = allResponses.indexOf(null);
        while (index >= 0) {
            BatchResponseItemExtended response = persistenceResponses.remove(0);
            response.setResourceId(idToExternalId.get(response.getResourceId()));
            allResponses.set(index, response);
            index = allResponses.indexOf(null);
        }
        return allResponses;
    }

    private boolean validateFunctionGroupDataGroups(String externalServiceAgreementId,
        List<PresentationFunctionGroupDataGroup> functionGroupDataGroups) {
        return functionGroupDataGroups != null
            && !functionGroupDataGroups.contains(null)
            && functionGroupDataGroups.stream()
            .noneMatch(
                functionDataGroup -> checkForInvalidFunctionDataGroup(externalServiceAgreementId, functionDataGroup));
    }

    private boolean checkForInvalidFunctionDataGroup(String externalServiceAgreementId,
        PresentationFunctionGroupDataGroup functionGroupDataGroup) {
        return functionGroupDataGroup.getFunctionGroupIdentifier() == null
            || (functionGroupDataGroup.getFunctionGroupIdentifier().getIdIdentifier() == null
            && functionGroupDataGroup.getFunctionGroupIdentifier().getNameIdentifier() == null)
            || (functionGroupDataGroup.getFunctionGroupIdentifier().getIdIdentifier() != null
            && functionGroupDataGroup.getFunctionGroupIdentifier().getNameIdentifier() != null)
            || (functionGroupDataGroup.getDataGroupIdentifiers() != null && functionGroupDataGroup
            .getDataGroupIdentifiers().contains(null))
            || functionGroupDataGroup.getDataGroupIdentifiers().stream()
            .anyMatch(dataGroupIdentifier -> checkForInvalidDataGroup(externalServiceAgreementId, dataGroupIdentifier));
    }

    private boolean checkForInvalidDataGroup(String externalServiceAgreementId,
        PresentationIdentifier dataGroupIdentifier) {
        return (dataGroupIdentifier.getIdIdentifier() == null && dataGroupIdentifier.getNameIdentifier() == null)
            || (dataGroupIdentifier.getIdIdentifier() != null && dataGroupIdentifier.getNameIdentifier() != null)
            || (dataGroupIdentifier.getNameIdentifier() != null && !externalServiceAgreementId
            .equals(dataGroupIdentifier.getNameIdentifier().getExternalServiceAgreementId()));
    }

    private List<BatchResponseItemExtended> getErrorBatchResultUserNotFound(
        List<BatchResponseItemExtended> responses,
        List<PresentationAssignUserPermissions> userPermissions,
        Map<String, String> uppercaseToOriginalExternalUserIdMap) {
        int index = responses.indexOf(null);
        while (index >= 0) {
            BatchResponseItemExtended response = new BatchResponseItemExtended()
                .withResourceId(uppercaseToOriginalExternalUserIdMap.get(userPermissions.get(0).getExternalUserId()))
                .withExternalServiceAgreementId(userPermissions.get(0).getExternalServiceAgreementId())
                .withErrors(Lists.newArrayList(ERR_AG_082.getErrorMessage()))
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);
            responses.set(index, response);
            userPermissions.remove(0);
            index = responses.indexOf(null);
        }
        return responses;

    }
}
