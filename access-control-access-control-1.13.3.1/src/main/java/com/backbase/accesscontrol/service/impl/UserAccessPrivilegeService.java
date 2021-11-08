package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.client.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.PersistenceUserPermissionKey;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.dto.BusinessFunctionKey;
import com.backbase.accesscontrol.domain.dto.DataGroupPermissions;
import com.backbase.accesscontrol.domain.dto.DataGroupWithApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.dto.FunctionGroupDataGroupCombinations;
import com.backbase.accesscontrol.domain.dto.UserAssignedFunctionGroupDataGroupPermissions;
import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.dto.UserPrivilegesSummaryGetResponseBodyDto;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.PrivilegesEnum;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceDataItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccessPrivilegeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAccessPrivilegeService.class);
    public static final String ARRANGEMENTS = "ARRANGEMENTS";

    private final DataGroupJpaRepository dataGroupJpaRepository;
    private final DataGroupItemJpaRepository dataGroupItemJpaRepository;
    private final UserContextJpaRepository userContextJpaRepository;
    private final UserAssignedCombinationRepository userAssignedCombinationRepository;

    private final PersistenceLegalEntityService persistenceLegalEntityService;
    private final BusinessFunctionCache businessFunctionCache;
    private final MasterServiceAgreementFallbackProperties fallbackProperties;

    /**
     * Returns a list of privileges that are assigned to the user.
     *
     * @param userId             - id of the user
     * @param serviceAgreementId - id of the service agreement
     * @param resourceName       - name of the resource
     * @param functionName       - name of the function
     * @return list of {@link String}
     */
    @Transactional(readOnly = true)
    public List<String> getPrivileges(String userId, String serviceAgreementId, String resourceName,
        String functionName) {
        return userContextJpaRepository
            .findAllByUserIdAndServiceAgreementIdAndAfpIds(
                userId,
                serviceAgreementId,
                ServiceAgreementState.ENABLED,
                businessFunctionCache
                    .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, null)
            ).stream()
            .map(id -> businessFunctionCache.getApplicableFunctionPrivilegeById(id).getPrivilegeName())
            .distinct()
            .collect(Collectors.toList());

    }

    /**
     * Returns a list of arrangement privileges that are assigned to the user.
     *
     * @param userId             - id of the user
     * @param serviceAgreementId - id of the service agreement
     * @param functionName       - name of the function
     * @param resourceName       - name of the resource
     * @param privilege          - privilege
     * @param legalEntityId      - legal entity id
     * @param arrangementId      - arrangement id
     * @return List of {@link ArrangementPrivilegesGetResponseBody}
     */
    @Transactional(readOnly = true)
    public List<ArrangementPrivilegesDto> getArrangementPrivileges(String userId, String serviceAgreementId,
        String functionName, String resourceName, String privilege, String legalEntityId,
        String arrangementId) {

        if (Objects.isNull(serviceAgreementId)) {
            if (!fallbackProperties.isEnabled()) {
                throw getForbiddenException(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode());
            }
            serviceAgreementId = persistenceLegalEntityService.getMasterServiceAgreement(legalEntityId).getId();
        }

        List<PersistenceUserDataItemPermission> userDataItemsPrivileges = getUserDataItemsPrivileges(userId,
            serviceAgreementId, resourceName, functionName,
            privilege, ARRANGEMENTS, arrangementId);

        return convertToArrangementPrivileges(userDataItemsPrivileges);
    }

    /**
     * Returns a list of privileges summary by user id and service agreement.
     *
     * @param userId             - id of the user
     * @param serviceAgreementId - id of the service agreement
     * @return List of {@link UserPrivilegesSummaryGetResponseBodyDto}
     */
    @Transactional(readOnly = true)
    public List<UserPrivilegesSummaryGetResponseBodyDto> getPrivilegesSummary(String userId,
        String serviceAgreementId) {
        List<String> applicableFunctionPrivilegeIds
            = userContextJpaRepository.findAfpIdsByUserIdAndServiceAgreementId(
            userId,
            serviceAgreementId
        );

        return getUserPrivilegesSummaryGetResponseBody(applicableFunctionPrivilegeIds);
    }

    /**
     * Return permission data group combinations by user id, service agreement id and permission request parameters.
     *
     * @param userId             internal id of the user
     * @param serviceAgreementId id of the service agreement
     * @param permissionsRequest {@link PermissionsRequest}
     * @return List of {@link PermissionsDataGroup}
     */
    @Transactional(readOnly = true)
    public PermissionsDataGroup getPermissionsDataGroup(String userId, String serviceAgreementId,
        PermissionsRequest permissionsRequest) {

        LOGGER.info("Trying to get service agreement ids with data group ids and data item ids");
        List<String> functionNames = permissionsRequest.getFunctionNames();
        List<String> privileges = permissionsRequest.getPrivileges();
        String resourceName = permissionsRequest.getResourceName();

        List<String> validPrivilegeNames = Arrays.stream(PrivilegesEnum.values())
            .map(PrivilegesEnum::getPrivilegeName)
            .collect(toList());

        if (CollectionUtils.isNotEmpty(privileges) && !validPrivilegeNames.containsAll(privileges)) {
            LOGGER.warn("Some of the privilege names provided are not valid.");
            return new PermissionsDataGroup().permissionsData(emptyList()).dataGroupsData(emptyList());
        }

        Set<String> afpIds = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        if (CollectionUtils.isEmpty(afpIds)) {
            LOGGER.warn("There are no applicable function privileges for functions, resource name or privileges");
            return new PermissionsDataGroup().permissionsData(emptyList()).dataGroupsData(emptyList());
        }

        Set<UserAssignedFunctionGroupDataGroupPermissions> uaFgDgCombinationPermissions = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdAndAfpIdInAndDataGroupTypeIn(userId, serviceAgreementId, afpIds,
                permissionsRequest.getDataGroupTypes());

        return populateResponse(uaFgDgCombinationPermissions);
    }

    private PermissionsDataGroup populateResponse(
        Set<UserAssignedFunctionGroupDataGroupPermissions> uaFgDgPermissions) {

        List<PermissionDataGroup> permissionsData = uaFgDgPermissions.stream()
            .map(uaFgDgPermission -> new PermissionDataGroup()
                .permissions(getPermissionData(uaFgDgPermission.getApplicableFunctionPrivilegeIds()))
                .dataGroups(getDataGroupData(uaFgDgPermission.getFgDgCombinations())))
            .collect(toList());

        return new PermissionsDataGroup()
            .permissionsData(permissionsData)
            .dataGroupsData(getDataGroupDataItems(uaFgDgPermissions));
    }

    private List<DataGroupData> getDataGroupDataItems(
        Set<UserAssignedFunctionGroupDataGroupPermissions> uaFgDgPermissions) {
        Set<String> dgIds = uaFgDgPermissions.stream()
            .flatMap(p -> p.getFgDgCombinations().stream()
                .flatMap(combination -> combination.getDgTypeIds().values().stream()
                    .flatMap(Collection::stream)))
            .collect(toSet());

        List<DataGroup> dataGroups = dataGroupJpaRepository.findByIdIn(dgIds);

        return dataGroups.stream()
            .map(dg -> new DataGroupData()
                .dataGroupId(dg.getId())
                .dataItemIds(new ArrayList<>(dg.getDataItemIds())))
            .collect(Collectors.toList());
    }

    private List<PermissionData> getPermissionData(Set<String> applicableFunctionPrivilegeIds) {
        Map<String, Map<String, Set<String>>> afpMappings = businessFunctionCache
            .getApplicableFunctionPrivileges(applicableFunctionPrivilegeIds).stream()
            .collect(groupingBy(ApplicableFunctionPrivilege::getBusinessFunctionResourceName,
                groupingBy(ApplicableFunctionPrivilege::getBusinessFunctionName,
                    mapping(ApplicableFunctionPrivilege::getPrivilegeName, toSet()))));

        return afpMappings.entrySet().stream()
            .flatMap(resourceEntry -> resourceEntry.getValue().entrySet().stream()
                .map(functionEntry -> new PermissionData()
                    .resourceName(resourceEntry.getKey())
                    .functionName(functionEntry.getKey())
                    .privileges(new ArrayList<>(functionEntry.getValue()))))
            .collect(Collectors.toList());
    }

    private List<List<PermissionDataGroupData>> getDataGroupData(
        Set<FunctionGroupDataGroupCombinations> fgDgCombinations) {
        return fgDgCombinations.stream()
            .map(combination -> combination.getDgTypeIds().entrySet().stream()
                .map(entry -> new PermissionDataGroupData()
                    .dataGroupType(entry.getKey())
                    .dataGroupIds(new ArrayList<>(entry.getValue())))
                .collect(toList()))
            .collect(Collectors.toList());
    }

    /**
     * Searches for users' data items privileges.
     *
     * @param userId             - required
     * @param serviceAgreementId - required
     * @param resource           - optional
     * @param businessFunction   - optional
     * @param privilege          - optional
     * @param dataGroupType      - optional
     * @param dataItemId         - optional
     */
    public List<PersistenceUserDataItemPermission> getUserDataItemsPrivileges(String userId, String serviceAgreementId,
        String resource, String businessFunction, String privilege, String dataGroupType, String dataItemId) {

        Set<String> privileges = isNotEmpty(privilege) ? Collections.singleton(privilege) : null;

        Set<String> appFnPrivilegeIds = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(businessFunction, resource, privileges);

        List<DataGroupWithApplicableFunctionPrivilege> itemsPrivileges = userAssignedCombinationRepository
            .findAllUserDataItemsPrivileges(userId, serviceAgreementId, dataGroupType, appFnPrivilegeIds);

        Map<String, DataGroupPermissions> dataGroupsPermissions = createDataGroupPermissionsMap(itemsPrivileges);

        Set<String> dataGroupIds = dataGroupsPermissions.keySet();

        List<DataGroupItem> dataGroupItems = getDataGroupItems(dataItemId, dataGroupIds);

        Map<PersistenceDataItem, Map<BusinessFunctionKey, Set<String>>> dataItemPermissions =
            createDataItemPermissionsMap(dataGroupsPermissions, dataGroupItems);

        return generateDataItemPermissionsResponse(dataItemPermissions);
    }

    private List<PersistenceUserDataItemPermission> generateDataItemPermissionsResponse(
        Map<PersistenceDataItem, Map<BusinessFunctionKey, Set<String>>> dataItemPermissions) {
        return dataItemPermissions.entrySet()
            .stream()
            .map(item -> new PersistenceUserDataItemPermission()
                .withDataItem(item.getKey())
                .withPermissions(item.getValue().entrySet()
                    .stream()
                    .map(userPermission -> new PersistenceUserPermission()
                        .withBusinessFunction(userPermission.getKey().getBusinessFunctionName())
                        .withFunctionId(userPermission.getKey().getFunctionId())
                        .withFunctionCode(userPermission.getKey().getFunctionCode())
                        .withResource(userPermission.getKey().getResourceName())
                        .withPrivileges(new ArrayList<>(userPermission.getValue()))).collect(toList())))
            .collect(toList());
    }

    private Map<PersistenceDataItem, Map<BusinessFunctionKey, Set<String>>> createDataItemPermissionsMap(
        Map<String, DataGroupPermissions> dataGroupsPermissions, List<DataGroupItem> dataGroupItems) {
        return dataGroupItems
            .stream()
            .collect(Collectors
                .toMap(item -> new PersistenceDataItem()
                        .withId(item.getDataItemId())
                        .withDataType(dataGroupsPermissions.get(item.getDataGroupId()).getDataGroupType()),
                    item -> dataGroupsPermissions.get(item.getDataGroupId()).getPermissions(),
                    (businessFunctionKeySetMap1, businessFunctionKeySetMap2) -> {
                        businessFunctionKeySetMap2.forEach((key, value) -> {
                            if (businessFunctionKeySetMap1.containsKey(key)) {
                                businessFunctionKeySetMap1.get(key).addAll(value);
                            } else {
                                businessFunctionKeySetMap1.put(key, value);
                            }
                        });
                        return businessFunctionKeySetMap1;
                    }, HashMap::new));
    }

    private List<DataGroupItem> getDataGroupItems(String dataItemId, Set<String> dataGroupIds) {
        List<DataGroupItem> dataGroupItems;
        if (dataItemId != null) {
            dataGroupItems = dataGroupItemJpaRepository.findAllByDataItemIdAndDataGroupIdIn(dataItemId, dataGroupIds);
        } else {
            dataGroupItems = dataGroupItemJpaRepository.findAllByDataGroupIdIn(dataGroupIds);
        }
        return dataGroupItems;
    }

    private Map<String, DataGroupPermissions> createDataGroupPermissionsMap(
        List<DataGroupWithApplicableFunctionPrivilege> itemsPrivileges) {

        return itemsPrivileges
            .stream()
            .collect(Collectors.toMap(DataGroupWithApplicableFunctionPrivilege::getDataGroupId,
                item -> {
                    DataGroupPermissions permission = new DataGroupPermissions(item.getDataGroupType());
                    ApplicableFunctionPrivilege applicableFunctionPrivilege = businessFunctionCache
                        .getApplicableFunctionPrivilegeById(item.getApplicableFunctionPrivilegeId());

                    permission.getPermissions().put(
                        new BusinessFunctionKey(applicableFunctionPrivilege.getBusinessFunction().getResourceName(),
                            applicableFunctionPrivilege.getBusinessFunction().getFunctionName(),
                            applicableFunctionPrivilege.getBusinessFunction().getId(),
                            applicableFunctionPrivilege.getBusinessFunction().getFunctionCode()),
                        newHashSet(applicableFunctionPrivilege.getPrivilege().getName()));

                    return permission;
                },
                (dataGroupPermission1, dataGroupPermission2) -> {
                    BusinessFunctionKey key = dataGroupPermission2.getPermissions().keySet().stream().findFirst().get();
                    if (dataGroupPermission1.getPermissions().containsKey(key)) {
                        dataGroupPermission1.getPermissions().get(key)
                            .addAll(dataGroupPermission2.getPermissions().get(key));
                    } else {
                        dataGroupPermission1.getPermissions().put(key, dataGroupPermission2.getPermissions().get(key));
                    }
                    return dataGroupPermission1;
                },
                HashMap::new));
    }

    private List<UserPrivilegesSummaryGetResponseBodyDto> getUserPrivilegesSummaryGetResponseBody(
        List<String> applicableFunctionPrivilegeIds) {
        return applicableFunctionPrivilegeIds
            .stream()
            .collect(groupingBy(applicableFunctionPrivilegeId ->
                new PersistenceUserPermissionKey(
                    businessFunctionCache.getApplicableFunctionPrivilegeById(
                        applicableFunctionPrivilegeId)
                        .getBusinessFunctionResourceName(),
                    businessFunctionCache.getApplicableFunctionPrivilegeById(
                        applicableFunctionPrivilegeId).getBusinessFunctionName())))
            .entrySet().stream()
            .map(item -> {
                    UserPrivilegesSummaryGetResponseBodyDto userPrivilegesSummaryGetResponseBodyDto
                        = new UserPrivilegesSummaryGetResponseBodyDto();
                    userPrivilegesSummaryGetResponseBodyDto.setResource(item.getKey().getResource());
                    userPrivilegesSummaryGetResponseBodyDto.setFunction(item.getKey().getBusinessFunction());
                    userPrivilegesSummaryGetResponseBodyDto.setPrivileges(getPrivilegesMap(item.getValue()));
                    return userPrivilegesSummaryGetResponseBodyDto;
                }

            )
            .collect(toList());
    }

    private Map<String, Boolean> getPrivilegesMap(List<String> applicableFunctionPrivilegeIds) {
        return applicableFunctionPrivilegeIds.stream()
            .collect(toMap(
                applicableFunctionPrivilegeId -> businessFunctionCache
                    .getApplicableFunctionPrivilegeById(applicableFunctionPrivilegeId).getPrivilegeName(),
                item -> true, (oldValue, newValue) -> true
            ));
    }

    private List<ArrangementPrivilegesDto> convertToArrangementPrivileges(
        List<PersistenceUserDataItemPermission> userDataItemsPrivileges) {
        return userDataItemsPrivileges.stream()
            .map(dataItemPrivileges ->
                new ArrangementPrivilegesDto()
                    .withArrangementId(dataItemPrivileges.getDataItem().getId())
                    .withPrivileges(dataItemPrivileges.getPermissions().stream()
                        .flatMap(permission -> permission.getPrivileges().stream()
                            .map(privilege1 -> new Privilege().withPrivilege(privilege1))
                        )
                        .collect(Collectors.toList()))
            ).collect(Collectors.toList());
    }
}
