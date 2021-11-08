package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_079;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.client.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.client.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroupItem;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.dto.DataGroupWithApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.dto.FunctionGroupDataGroupCombinations;
import com.backbase.accesscontrol.domain.dto.UserAssignedFunctionGroupDataGroupPermissions;
import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.dto.UserPrivilegesSummaryGetResponseBodyDto;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.repository.DataGroupItemJpaRepository;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepository;
import com.backbase.accesscontrol.repository.UserContextJpaRepository;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.accesscontrol.util.helpers.BusinessFunctionUtil;
import com.backbase.accesscontrol.util.helpers.PrivilegeUtil;
import com.backbase.accesscontrol.util.properties.MasterServiceAgreementFallbackProperties;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAccessPrivilegeServiceTest {

    @Mock
    private UserAssignedCombinationRepository userAssignedCombinationRepository;

    @Mock
    private DataGroupJpaRepository dataGroupJpaRepository;

    @Mock
    private DataGroupItemJpaRepository dataGroupItemJpaRepository;

    @Mock
    private UserContextJpaRepository userContextJpaRepository;

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @Mock
    private BusinessFunctionCache businessFunctionCache;

    @Mock
    MasterServiceAgreementFallbackProperties fallbackProperties;

    @InjectMocks
    private UserAccessPrivilegeService userAccessPrivilegeService;

    @Test
    void shouldGetPrivileges() {
        String serviceAgreementId = "serviceAgreementId";
        String privilegeName = "privilegeName";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String userId = "userId";

        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(serviceAgreementId);

        Set<String> appFnPrivilegesIds = Sets.newLinkedHashSet("appFnPId");

        when(businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(eq(functionName), eq(resourceName), isNull()))
            .thenReturn(appFnPrivilegesIds);
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setBusinessFunctionName(functionName);
        applicableFunctionPrivilege.setBusinessFunctionResourceName(resourceName);
        applicableFunctionPrivilege.setPrivilegeName(privilegeName);
        when(businessFunctionCache.getApplicableFunctionPrivilegeById("appFnPId"))
            .thenReturn(applicableFunctionPrivilege);

        when(userContextJpaRepository
            .findAllByUserIdAndServiceAgreementIdAndAfpIds(
                userId,
                serviceAgreementId,
                ServiceAgreementState.ENABLED,
                appFnPrivilegesIds
            ))
            .thenReturn(new ArrayList<>(appFnPrivilegesIds));

        List<String> privileges = userAccessPrivilegeService
            .getPrivileges(userId, serviceAgreementId, resourceName, functionName);

        assertEquals(1, privileges.size());
        assertEquals(privilegeName, privileges.get(0));

    }

    @Test
    void shouldGetPrivilegesSummary() {
        String userId = "id.user";
        String serviceAgreementId = "id.sa";

        Privilege privilege1 = PrivilegeUtil.getPrivilege("1", "edit", "edit");
        Privilege privilege2 = PrivilegeUtil.getPrivilege("2", "create", "create");
        Privilege privilege3 = PrivilegeUtil.getPrivilege("3", "view", "view");

        BusinessFunction businessFunction1 = BusinessFunctionUtil
            .getBusinessFunction("101", "ACH Debit", "ach.debit", "Payments", "payments");
        BusinessFunction businessFunction2 = BusinessFunctionUtil
            .getBusinessFunction("102", "Audit", "audit", "Audit", "audit");
        BusinessFunction businessFunction3 = BusinessFunctionUtil
            .getBusinessFunction("103", "Manage Devices", "manage.devices", "Device", "device");

        ApplicableFunctionPrivilege applicableFunctionPrivilege1 = getApplicableFunctionPrivilege("1",
            businessFunction1, privilege1, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege2 = getApplicableFunctionPrivilege("2",
            businessFunction1, privilege2, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege3 = getApplicableFunctionPrivilege("3",
            businessFunction1, privilege3, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege4 = getApplicableFunctionPrivilege("4",
            businessFunction2, privilege1, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege5 = getApplicableFunctionPrivilege("5",
            businessFunction2, privilege3, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege6 = getApplicableFunctionPrivilege("6",
            businessFunction3, privilege1, false);
        ApplicableFunctionPrivilege applicableFunctionPrivilege7 = getApplicableFunctionPrivilege("7",
            businessFunction3, privilege3, false);

        mockGetApplicableFunctionPrivilegeById("1", applicableFunctionPrivilege1);
        mockGetApplicableFunctionPrivilegeById("2", applicableFunctionPrivilege2);
        mockGetApplicableFunctionPrivilegeById("3", applicableFunctionPrivilege3);
        mockGetApplicableFunctionPrivilegeById("4", applicableFunctionPrivilege4);
        mockGetApplicableFunctionPrivilegeById("5", applicableFunctionPrivilege5);
        mockGetApplicableFunctionPrivilegeById("6", applicableFunctionPrivilege6);
        mockGetApplicableFunctionPrivilegeById("7", applicableFunctionPrivilege7);

        List<String> applicableFunctionPrivilegeIds = Arrays.asList("1", "2", "3", "4", "5", "6", "7");

        when(userContextJpaRepository.findAfpIdsByUserIdAndServiceAgreementId(
            userId,
            serviceAgreementId
        ))
            .thenReturn(applicableFunctionPrivilegeIds);

        List<UserPrivilegesSummaryGetResponseBodyDto> privilegesSummary =
            userAccessPrivilegeService.getPrivilegesSummary(userId, serviceAgreementId);

        assertEquals(3, privilegesSummary.size());
        assertEquals(3, privilegesSummary.stream().filter(
            privilegesSummaryGetResponseBody -> (privilegesSummaryGetResponseBody.getFunction().equals("ACH Debit")
                && privilegesSummaryGetResponseBody.getResource().equals("Payments"))).collect(Collectors.toList())
            .get(0)
            .getPrivileges().size());
        assertEquals(2, privilegesSummary.stream().filter(
            privilegesSummaryGetResponseBody -> (privilegesSummaryGetResponseBody.getFunction().equals("Audit")
                && privilegesSummaryGetResponseBody.getResource().equals("Audit"))).collect(Collectors.toList())
            .get(0)
            .getPrivileges().size());
        assertEquals(2, privilegesSummary.stream().filter(
            privilegesSummaryGetResponseBody -> (privilegesSummaryGetResponseBody.getFunction().equals("Manage Devices")
                && privilegesSummaryGetResponseBody.getResource().equals("Device"))).collect(Collectors.toList())
            .get(0).getPrivileges().size());
    }

    @Test
    void shouldGetPrivilegesSummaryEmptyListRetrieved() {
        String userId = "id.user";
        String serviceAgreementId = "id.sa";

        when(userContextJpaRepository.findAfpIdsByUserIdAndServiceAgreementId(
            userId,
            serviceAgreementId
        ))
            .thenReturn(emptyList());

        List<UserPrivilegesSummaryGetResponseBodyDto> privilegesSummary =
            userAccessPrivilegeService.getPrivilegesSummary(userId, serviceAgreementId);

        assertEquals(0, privilegesSummary.size());
    }

    @Test
    void shouldGetUserDataItemPrivileges() {
        String serviceAgreementId = "serviceAgreementId";
        String privilegeName = "privilegeName";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String userId = "userId";
        String dgType = "CONTACTS";
        String itemId = "item.dg.id";
        String dgId = "dg.id";
        String afpId = "afpId";

        DataGroupItem dataGroupItem = new DataGroupItem();
        dataGroupItem.setDataItemId(itemId);
        dataGroupItem.setDataGroupId(dgId);
        DataGroup dataGroup = new DataGroup();
        dataGroup.setDataItemIds(asSet(itemId));
        dataGroup.setDataItemType(dgType);
        dataGroup.setId(dgId);

        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setFunctionName(functionName);
        businessFunction.setResourceName(resourceName);

        Privilege privilege = new Privilege();
        privilege.setName(privilegeName);

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId(afpId);
        applicableFunctionPrivilege.setBusinessFunction(businessFunction);
        applicableFunctionPrivilege.setPrivilege(privilege);

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(afpId))
            .thenReturn(applicableFunctionPrivilege);
        DataGroupWithApplicableFunctionPrivilege dataGroupWithApplicableFunctionPrivilege = new DataGroupWithApplicableFunctionPrivilege(
            dgId, dgType, afpId);

        mockFindAllFlatUserDataItemsPrivileges(serviceAgreementId, userId,
            dgType, dataGroupWithApplicableFunctionPrivilege);

        mockGetDataGroupItems(itemId, asSet(dgId), singletonList(dataGroupItem));

        List<PersistenceUserDataItemPermission> userDataItemPermission = userAccessPrivilegeService
            .getUserDataItemsPrivileges(userId,
                serviceAgreementId,
                resourceName,
                functionName,
                privilegeName,
                dgType,
                itemId
            );

        verify(userAssignedCombinationRepository)
            .findAllUserDataItemsPrivileges(eq(userId), eq(serviceAgreementId), eq(dgType), anySet());

        assertEquals(1, userDataItemPermission.size());
        assertEquals(dgType, userDataItemPermission.get(0).getDataItem().getDataType());
        assertEquals(itemId, userDataItemPermission.get(0).getDataItem().getId());
        assertEquals(1, userDataItemPermission.get(0).getPermissions().size());
        assertEquals(functionName, userDataItemPermission.get(0).getPermissions().get(0).getBusinessFunction());
        assertEquals(resourceName, userDataItemPermission.get(0).getPermissions().get(0).getResource());
        assertEquals(privilegeName, userDataItemPermission.get(0).getPermissions().get(0).getPrivileges().get(0));
    }

    @Test
    void shouldGetUserDataItemPrivilegesWhenEmptyListIsReturned() {
        String serviceAgreementId = "serviceAgreementId";
        String privilegeName = "privilegeName";
        String functionName = "functionName";
        String resourceName = "resourceName";
        String userId = "userId";
        String dgType = "CONTACTS";
        String itemId = "item.dg.id";

        mockFindAllFlatUserDataItemsPrivileges(serviceAgreementId, userId,
            dgType);

        List<PersistenceUserDataItemPermission> userDataItemPermission = userAccessPrivilegeService
            .getUserDataItemsPrivileges(userId,
                serviceAgreementId,
                resourceName,
                functionName,
                privilegeName,
                dgType,
                itemId
            );

        verify(userAssignedCombinationRepository)
            .findAllUserDataItemsPrivileges(eq(userId), eq(serviceAgreementId), eq(dgType), anySet());
        assertTrue(userDataItemPermission.isEmpty());
    }

    @Test
    void shouldGetArrangementPrivilegesWhenServiceAgreementIdIsProvided() {
        String arrangementId = "ARR-01";
        String resourceName = "Entitlements";
        String functionName = "Manage Entitlements";
        String serviceAgreementId = "SA-01";
        String userId = "U-01";
        String dataType = "ARRANGEMENTS";
        String privilegeName = "view";
        String dgId = "dg.id";
        String afpId = "afpId";

        DataGroupItem dataGroupItem = new DataGroupItem();
        dataGroupItem.setDataItemId(arrangementId);
        dataGroupItem.setDataGroupId(dgId);
        DataGroup dataGroup = new DataGroup();
        dataGroup.setDataItemIds(asSet(arrangementId));
        dataGroup.setDataItemType(dataType);
        dataGroup.setId(dgId);

        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setFunctionName(functionName);
        businessFunction.setResourceName(resourceName);

        Privilege privilege = new Privilege();
        privilege.setName(privilegeName);

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId(afpId);
        applicableFunctionPrivilege.setBusinessFunction(businessFunction);
        applicableFunctionPrivilege.setPrivilege(privilege);

        DataGroupWithApplicableFunctionPrivilege dataGroupWithApplicableFunctionPrivilege = new DataGroupWithApplicableFunctionPrivilege(
            dgId, dataType, afpId);

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(afpId))
            .thenReturn(applicableFunctionPrivilege);

        mockFindAllFlatUserDataItemsPrivileges(serviceAgreementId, userId,
            dataType, dataGroupWithApplicableFunctionPrivilege
        );

        mockGetDataGroupItems(arrangementId, asSet(dgId), singletonList(dataGroupItem));

        List<ArrangementPrivilegesDto> arrangementPrivileges = userAccessPrivilegeService
            .getArrangementPrivileges(userId, serviceAgreementId, functionName,
                resourceName, privilegeName, null, arrangementId);

        assertThat(arrangementPrivileges, hasItems(
            getArrangementPrivilegeMatcher(is(arrangementId), hasItems(
                getPrivilegeMatcher(is(privilegeName))
            ))
        ));
    }

    @Test
    void shouldGetArrangementPrivilegesWhenServiceAgreementIdAndDataItemIdIsNotProvided() {
        String arrangementId = "ARR-01";
        String resourceName = "Entitlements";
        String functionName = "Manage Entitlements";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String dataType = "ARRANGEMENTS";
        String privilegeName = "view";
        String serviceAgreementId = "SA-01";
        String dgId = "dg.id";
        String afpId = "afpId";

        DataGroupItem dataGroupItem = new DataGroupItem();
        dataGroupItem.setDataItemId(arrangementId);
        dataGroupItem.setDataGroupId(dgId);
        DataGroup dataGroup = new DataGroup();
        dataGroup.setDataItemIds(asSet(arrangementId));
        dataGroup.setDataItemType(dataType);
        dataGroup.setId(dgId);

        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setFunctionName(functionName);
        businessFunction.setResourceName(resourceName);

        Privilege privilege = new Privilege();
        privilege.setName(privilegeName);

        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId(afpId);
        applicableFunctionPrivilege.setBusinessFunction(businessFunction);
        applicableFunctionPrivilege.setPrivilege(privilege);

        DataGroupWithApplicableFunctionPrivilege dataGroupWithApplicableFunctionPrivilege = new DataGroupWithApplicableFunctionPrivilege(
            dgId, dataType, afpId);

        mockFindAllFlatUserDataItemsPrivileges(serviceAgreementId, userId,
            dataType, dataGroupWithApplicableFunctionPrivilege
        );

        when(businessFunctionCache.getApplicableFunctionPrivilegeById(afpId))
            .thenReturn(applicableFunctionPrivilege);

        mockGetDataGroupItemsWithoutItemId(asSet(dgId), singletonList(dataGroupItem));

        when(persistenceLegalEntityService.getMasterServiceAgreement(legalEntityId))
            .thenReturn(new ServiceAgreement()
                .withId(serviceAgreementId));

        when(fallbackProperties.isEnabled()).thenReturn(true);

        List<ArrangementPrivilegesDto> arrangementPrivileges = userAccessPrivilegeService
            .getArrangementPrivileges(userId, null, functionName,
                resourceName, privilegeName, legalEntityId, null);

        assertThat(arrangementPrivileges, hasItems(
            getArrangementPrivilegeMatcher(is(arrangementId), hasItems(
                getPrivilegeMatcher(is(privilegeName))
            ))
        ));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenServiceAgreementIdAndDataItemIdIsNotProvidedAndFallbackDisabled() {
        String resourceName = "Entitlements";
        String functionName = "Manage Entitlements";
        String legalEntityId = "LE-01";
        String userId = "U-01";
        String privilegeName = "view";

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> userAccessPrivilegeService
                .getArrangementPrivileges(userId, null, functionName,
                    resourceName, privilegeName, legalEntityId, null));

        assertThat(forbiddenException,
            new ForbiddenErrorMatcher(ERR_ACQ_079.getErrorMessage(), ERR_ACQ_079.getErrorCode()));
    }


    @Test
    void shouldReturnEmptyResponseWhenInvalidPrivilegeSentInTheRequest() {
        String userId = "userId";
        String serviceAgreementId = "saId";
        PermissionsDataGroup response = userAccessPrivilegeService.getPermissionsDataGroup(userId, serviceAgreementId,
            new PermissionsRequest().privileges(Collections.singletonList("invalid")));

        assertEquals(new PermissionsDataGroup().permissionsData(emptyList()).dataGroupsData(emptyList()), response);
    }

    @Test
    void shouldReturnEmptyResponseWhenEmptyAfpIdsReturned() {
        String userId = "userId";
        String serviceAgreementId = "saId";
        List<String> functionNames = Collections.singletonList("SEPA CT");
        String resourceName = "Payments";
        List<String> privileges = asList("view", "edit");

        when(businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges))
            .thenReturn(emptySet());

        PermissionsDataGroup response = userAccessPrivilegeService.getPermissionsDataGroup(userId, serviceAgreementId,
            new PermissionsRequest().functionNames(functionNames).resourceName(resourceName).privileges(privileges));

        assertEquals(new PermissionsDataGroup().permissionsData(emptyList()).dataGroupsData(emptyList()), response);
    }

    @Test
    void shouldReturnEmptyResponseWhenEmptyRecordsReturnedFromRepository() {
        String userId = "userId";
        String serviceAgreementId = "saId";
        List<String> dataGroupTypes = asList("ARRANGEMENTS", "PAYEES");
        List<String> functionNames = Collections.singletonList("SEPA CT");
        String resourceName = "Payments";
        List<String> privileges = asList("view", "edit");
        Set<String> afpIds = newHashSet("afpId01", "afpId02");

        when(businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges))
            .thenReturn(afpIds);

        PermissionsRequest permissionRequest = new PermissionsRequest()
            .dataGroupTypes(dataGroupTypes)
            .functionNames(functionNames)
            .resourceName(resourceName)
            .privileges(privileges);

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdAndAfpIdInAndDataGroupTypeIn(userId, serviceAgreementId,
                afpIds, dataGroupTypes))
            .thenReturn(emptySet());

        PermissionsDataGroup response = userAccessPrivilegeService.getPermissionsDataGroup(userId, serviceAgreementId,
            permissionRequest);

        assertEquals(new PermissionsDataGroup().permissionsData(emptyList()).dataGroupsData(emptyList()), response);
    }

    @Test
    void shouldReturnPermissionWithDataGroups() {
        String userId = "userId";
        String serviceAgreementId = "saId";
        List<String> dataGroupTypes = asList("ARRANGEMENTS", "PAYEES");
        List<String> functionNames = asList("SEPA CT", "Manage Service Agreements");
        String resourceName = "Payments";
        List<String> privileges = asList("view", "edit");
        Set<String> afpIds = newHashSet("afpId01", "afpId02");

        when(businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges))
            .thenReturn(afpIds);

        PermissionsRequest permissionRequest = new PermissionsRequest()
            .dataGroupTypes(dataGroupTypes)
            .functionNames(functionNames)
            .resourceName(resourceName)
            .privileges(privileges);

        PermissionsDataGroup mockResponse = new PermissionsDataGroup()
            .permissionsData(singletonList(new PermissionDataGroup()
                .permissions(asList(new PermissionData()
                        .resourceName("Payments")
                        .functionName("SEPA CT")
                        .privileges(asList("view", "edit")),
                    new PermissionData()
                        .resourceName("Payments")
                        .functionName("Manage Service Agreements")
                        .privileges(asList("view", "edit"))))
                .dataGroups(asList(
                    singletonList(new PermissionDataGroupData()
                        .dataGroupType("ARRANGEMENTS")
                        .dataGroupIds(singletonList("dgId01"))),
                    asList(new PermissionDataGroupData()
                            .dataGroupType("PAYEES")
                            .dataGroupIds(singletonList("dgId03")),
                        new PermissionDataGroupData()
                            .dataGroupType("ARRANGEMENTS")
                            .dataGroupIds(singletonList("dgId02"))
                    )))))
            .dataGroupsData(asList(
                new DataGroupData()
                    .dataGroupId("dgId01")
                    .dataItemIds(asList("item02", "item01")),
                new DataGroupData()
                    .dataGroupId("dgId02")
                    .dataItemIds(asList("item04", "item03")),
                new DataGroupData()
                    .dataGroupId("dgId03")
                    .dataItemIds(asList("item06", "item05"))));

        Set<UserAssignedFunctionGroupDataGroupPermissions> records = newHashSet(
            new UserAssignedFunctionGroupDataGroupPermissions(1L, afpIds,
                newHashSet(
                    new FunctionGroupDataGroupCombinations(1L, new HashMap<>() {{
                        put("ARRANGEMENTS", newHashSet("dgId01"));
                    }}),
                    new FunctionGroupDataGroupCombinations(2L, new HashMap<>() {{
                        put("ARRANGEMENTS", newHashSet("dgId02"));
                        put("PAYEES", newHashSet("dgId03"));
                    }})
                )));

        ApplicableFunctionPrivilege afp01 = new ApplicableFunctionPrivilege();
        afp01.setId("01");
        afp01.setBusinessFunctionResourceName("Payments");
        afp01.setBusinessFunctionName("SEPA CT");
        afp01.setPrivilegeName("view");

        ApplicableFunctionPrivilege afp02 = new ApplicableFunctionPrivilege();
        afp02.setId("02");
        afp02.setBusinessFunctionResourceName("Payments");
        afp02.setBusinessFunctionName("SEPA CT");
        afp02.setPrivilegeName("edit");

        ApplicableFunctionPrivilege afp03 = new ApplicableFunctionPrivilege();
        afp03.setId("03");
        afp03.setBusinessFunctionResourceName("Payments");
        afp03.setBusinessFunctionName("Manage Service Agreements");
        afp03.setPrivilegeName("view");

        ApplicableFunctionPrivilege afp04 = new ApplicableFunctionPrivilege();
        afp04.setId("04");
        afp04.setBusinessFunctionResourceName("Payments");
        afp04.setBusinessFunctionName("Manage Service Agreements");
        afp04.setPrivilegeName("edit");

        when(businessFunctionCache.getApplicableFunctionPrivileges(afpIds))
            .thenReturn(newHashSet(afp01, afp02, afp03, afp04));

        when(dataGroupJpaRepository.findByIdIn(newHashSet("dgId01", "dgId02", "dgId03")))
            .thenReturn(asList(
                new DataGroup().withId("dgId01").withDataItemIds(newHashSet("item01", "item02")),
                new DataGroup().withId("dgId02").withDataItemIds(newHashSet("item03", "item04")),
                new DataGroup().withId("dgId03").withDataItemIds(newHashSet("item05", "item06"))
            ));

        when(userContextJpaRepository
            .findByUserIdAndServiceAgreementIdAndAfpIdInAndDataGroupTypeIn(userId, serviceAgreementId,
                afpIds,
                dataGroupTypes)).thenReturn(records);

        PermissionsDataGroup response = userAccessPrivilegeService.getPermissionsDataGroup(userId, serviceAgreementId,
            permissionRequest);

        assertEquals(mockResponse, response);
    }

    private Matcher<ArrangementPrivilegesDto> getArrangementPrivilegeMatcher(
        Matcher<?> arrangementIdMatcher,
        Matcher<?> privilegesMatcher) {
        return allOf(
            hasProperty("arrangementId", arrangementIdMatcher),
            hasProperty("privileges", privilegesMatcher)
        );
    }

    private Matcher<Privilege> getPrivilegeMatcher(Matcher<?> privilegeMatcher) {
        return allOf(
            hasProperty("privilege", privilegeMatcher)
        );
    }


    private void mockFindAllFlatUserDataItemsPrivileges(
        String serviceAgreementId, String userId, String dgType,
        DataGroupWithApplicableFunctionPrivilege... persistenceDataGroupWithApplicableFunctionPrivilege) {
        when(userAssignedCombinationRepository.findAllUserDataItemsPrivileges(
            eq(userId),
            eq(serviceAgreementId),
            eq(dgType),
            anySet()
        ))
            .thenReturn(Lists.newArrayList(
                persistenceDataGroupWithApplicableFunctionPrivilege
            ));
    }

    private void mockGetDataGroupItems(String itemId, Set<String> dataGroupIds,
        List<DataGroupItem> dataGroupItems) {
        when(dataGroupItemJpaRepository.findAllByDataItemIdAndDataGroupIdIn(itemId, dataGroupIds))
            .thenReturn(dataGroupItems);
    }

    private void mockGetDataGroupItemsWithoutItemId(Set<String> dataGroupIds,
        List<DataGroupItem> dataGroupItems) {
        when(dataGroupItemJpaRepository.findAllByDataGroupIdIn(dataGroupIds))
            .thenReturn(dataGroupItems);
    }

    private void mockGetApplicableFunctionPrivilegeById(String applicableFunctionPrivilegeId,
        ApplicableFunctionPrivilege applicableFunctionPrivilege) {
        when(businessFunctionCache.getApplicableFunctionPrivilegeById(applicableFunctionPrivilegeId))
            .thenReturn(applicableFunctionPrivilege);
    }

    private static ApplicableFunctionPrivilege getApplicableFunctionPrivilege(String id,
        BusinessFunction businessFunction, Privilege privilege, boolean supportLimits) {
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId(id);
        applicableFunctionPrivilege.setBusinessFunction(businessFunction);
        applicableFunctionPrivilege.setPrivilege(privilege);
        applicableFunctionPrivilege.setPrivilegeName(privilege != null ? privilege.getName() : null);
        applicableFunctionPrivilege.setBusinessFunctionName(businessFunction.getFunctionName());
        applicableFunctionPrivilege.setBusinessFunctionResourceName(businessFunction.getResourceName());
        applicableFunctionPrivilege.setSupportsLimit(supportLimits);
        return applicableFunctionPrivilege;
    }

}
