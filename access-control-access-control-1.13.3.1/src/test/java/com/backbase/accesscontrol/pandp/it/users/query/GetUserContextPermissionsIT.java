package com.backbase.accesscontrol.pandp.it.users.query;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_001;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.UserContextQueryController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupData;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionData;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionDataGroupData;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsDataGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PermissionsRequest;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link UserContextQueryController#getUserContextPermissions}
 */
public class GetUserContextPermissionsIT extends TestDbWireMock {

    private final String GET_USER_CONTEXT_PERMISSIONS_URL = "/accessgroups/usercontext/data-group-permissions";

    private final String userId = contextUserId;

    private DataGroup dataGroup01;
    private DataGroup dataGroup02;
    private DataGroup dataGroup03;
    private DataGroup dataGroup04;
    private DataGroup dataGroup05;

    private FunctionGroup functionGroup01;
    private FunctionGroup functionGroup02;
    private FunctionGroup functionGroup03;
    private FunctionGroup functionGroup04;
    private FunctionGroup functionGroup05;

    private ApplicableFunctionPrivilege apfBf1028View;
    private ApplicableFunctionPrivilege apfBf1028Create;
    private ApplicableFunctionPrivilege apfBf1002Create;
    private ApplicableFunctionPrivilege apfBf1002Edit;
    private ApplicableFunctionPrivilege apfBf1020Edit;
    private ApplicableFunctionPrivilege apfBf1020View;

    @Before
    public void setup() {
        apfBf1028View = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "view");
        apfBf1028Create = businessFunctionCache.getByFunctionIdAndPrivilege("1028", "create");
        apfBf1002Create = businessFunctionCache.getByFunctionIdAndPrivilege("1002", "create");
        apfBf1002Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1002", "edit");
        apfBf1020Edit = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "edit");
        apfBf1020View = businessFunctionCache.getByFunctionIdAndPrivilege("1020", "view");

        dataGroup01 = createDataGroup("dag01", "ARRANGEMENTS", "dag01", rootMsa);
        dataGroup01.setDataItemIds(newHashSet("001", "002", "003"));
        dataGroup01 = dataGroupJpaRepository.save(dataGroup01);

        dataGroup02 = createDataGroup("dag02", "ARRANGEMENTS", "dag02", rootMsa);
        dataGroup02.setDataItemIds(newHashSet("004", "005", "006"));
        dataGroup02 = dataGroupJpaRepository.save(dataGroup02);

        dataGroup03 = createDataGroup("dag03", "PAYEES", "dag03", rootMsa);
        dataGroup03.setDataItemIds(newHashSet("007", "008", "009"));
        dataGroup03 = dataGroupJpaRepository.save(dataGroup03);

        dataGroup04 = createDataGroup("dag04", "ARRANGEMENTS", "dag04", rootMsa);
        dataGroup04.setDataItemIds(newHashSet("010", "011", "012"));
        dataGroup04 = dataGroupJpaRepository.save(dataGroup04);

        dataGroup05 = createDataGroup("dag05", "ARRANGEMENTS", "dag05", rootMsa);
        dataGroup05.setDataItemIds(newHashSet("013", "014", "015"));
        dataGroup05 = dataGroupJpaRepository.save(dataGroup05);

        functionGroup01 = createFunctionGroup("fg01", "desc", rootMsa,
            asList(apfBf1028View.getId(), apfBf1028Create.getId(), apfBf1002Create.getId(), apfBf1002Edit.getId()),
            FunctionGroupType.DEFAULT);
        functionGroup02 = createFunctionGroup("fg02", "desc", rootMsa,
            asList(apfBf1002Create.getId(), apfBf1002Edit.getId()), FunctionGroupType.DEFAULT);
        functionGroup03 = createFunctionGroup("fg03", "desc", rootMsa,
            asList(apfBf1020View.getId(), apfBf1020Edit.getId()), FunctionGroupType.DEFAULT);
        functionGroup04 = createFunctionGroup("fg04", "desc", rootMsa,
            asList(apfBf1020View.getId(), apfBf1020Edit.getId()), FunctionGroupType.DEFAULT);
        functionGroup05 = createFunctionGroup("fg05", "desc", rootMsa,
            asList(apfBf1028View.getId(), apfBf1028Create.getId()), FunctionGroupType.DEFAULT);

        UserContext userContext = new UserContext(userId, rootMsa.getId());

        UserAssignedFunctionGroup uaFg01 = new UserAssignedFunctionGroup(functionGroup01, userContext);
        uaFg01.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), uaFg01),
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup02.getId(), dataGroup03.getId()), uaFg01)
        ));

        UserAssignedFunctionGroup uaFg02 = new UserAssignedFunctionGroup(functionGroup02, userContext);
        uaFg02.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup04.getId()), uaFg02),
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup05.getId()), uaFg02)
        ));

        UserAssignedFunctionGroup uaFg03 = new UserAssignedFunctionGroup(functionGroup03, userContext);
        uaFg03.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup02.getId()), uaFg03)));

        UserAssignedFunctionGroup uaFg04 = new UserAssignedFunctionGroup(functionGroup04, userContext);
        uaFg04.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup04.getId()), uaFg04)));

        UserAssignedFunctionGroup uaFg05 = new UserAssignedFunctionGroup(functionGroup05, userContext);
        uaFg05.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup01.getId()), uaFg05)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg01, uaFg03, uaFg04, uaFg05));
        userContextJpaRepository.save(userContext);
    }

    @Test
    public void shouldGetUserContextPermissions() throws Exception {

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"));

        String response = executeServiceRequest(new UrlBuilder(GET_USER_CONTEXT_PERMISSIONS_URL).build(),
            permissionsRequest, "USER", rootMsa.getId(), userId, rootLegalEntity.getId(), HttpMethod.POST,
            new HashMap<>());

        PermissionsDataGroup permissionsDataGroupResponse = readValue(response, PermissionsDataGroup.class);

        assertThat(permissionsDataGroupResponse, is(
            allOf(
                hasProperty("permissionsData", containsInAnyOrder(
                    allOf(
                        hasProperty("permissions", containsInAnyOrder(
                            permissionDataMatcher(apfBf1002Edit, apfBf1002Create),
                            permissionDataMatcher(apfBf1028View, apfBf1028Create))),
                        hasProperty("dataGroups", containsInAnyOrder(
                            containsInAnyOrder(
                                permissionDataGroupDataMatcher(dataGroup03),
                                permissionDataGroupDataMatcher(dataGroup02)),
                            containsInAnyOrder(
                                permissionDataGroupDataMatcher(dataGroup01))))),
                    allOf(
                        hasProperty("permissions", containsInAnyOrder(
                            permissionDataMatcher(apfBf1028View, apfBf1028Create))),
                        hasProperty("dataGroups", containsInAnyOrder(
                            containsInAnyOrder(
                                permissionDataGroupDataMatcher(dataGroup01))))))),
                hasProperty("dataGroupsData", containsInAnyOrder(
                    dataGroupsDataMatcher(dataGroup01),
                    dataGroupsDataMatcher(dataGroup02),
                    dataGroupsDataMatcher(dataGroup03))))));
    }

    @Test
    public void shouldThrowBadRequestWhenInvalidDataGroupTypePassedInRequestBody() {

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(asList("ARRANGEMENTS", "PAYEES", "INVALID"))
            .functionNames(asList("SEPA CT", "Manage Service Agreements"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(new UrlBuilder(GET_USER_CONTEXT_PERMISSIONS_URL).build(),
                permissionsRequest, "USER", rootMsa.getId(), userId, rootLegalEntity.getId(), HttpMethod.POST,
                new HashMap<>()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_001.getErrorMessage(), ERR_AG_001.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenEmptyListOfDataGroupTypePassedInRequestBody() {

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(Collections.emptyList())
            .functionNames(asList("SEPA CT", "Manage Service Agreements"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(new UrlBuilder(GET_USER_CONTEXT_PERMISSIONS_URL).build(),
                permissionsRequest, "USER", rootMsa.getId(), userId, rootLegalEntity.getId(), HttpMethod.POST,
                new HashMap<>()));

        assertThat(exception,
            new BadRequestErrorMatcher("size must be between 1 and 2147483647", "api.Size.dataGroupTypes"));
    }

    @Test
    public void shouldThrowBadRequestWhenNullDataGroupTypePassedInRequestBody() {

        PermissionsRequest permissionsRequest = new PermissionsRequest()
            .dataGroupTypes(null)
            .functionNames(asList("SEPA CT", "Manage Service Agreements"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequest(new UrlBuilder(GET_USER_CONTEXT_PERMISSIONS_URL).build(),
                permissionsRequest, "USER", rootMsa.getId(), userId, rootLegalEntity.getId(), HttpMethod.POST,
                new HashMap<>()));

        assertThat(exception, new BadRequestErrorMatcher("must not be null", "api.NotNull.dataGroupTypes"));
    }

    private Matcher<PermissionDataGroupData> permissionDataGroupDataMatcher(DataGroup dataGroup) {
        return allOf(
            hasProperty("dataGroupType", equalTo(dataGroup.getDataItemType())),
            hasProperty("dataGroupIds", containsInAnyOrder(dataGroup.getId()))
        );
    }

    private Matcher<PermissionData> permissionDataMatcher(ApplicableFunctionPrivilege apfBf01,
        ApplicableFunctionPrivilege apfBf02) {
        return allOf(
            hasProperty("resourceName", equalTo(apfBf01.getBusinessFunctionResourceName())),
            hasProperty("functionName", equalTo(apfBf01.getBusinessFunctionName())),
            hasProperty("privileges", containsInAnyOrder(apfBf01.getPrivilegeName(), apfBf02.getPrivilegeName()))
        );
    }

    private Matcher<DataGroupData> dataGroupsDataMatcher(DataGroup dataGroup) {
        return allOf(
            hasProperty("dataGroupId", equalTo(dataGroup.getId())),
            hasProperty("dataItemIds", containsInAnyOrder(dataGroup.getDataItemIds().toArray())));
    }
}
