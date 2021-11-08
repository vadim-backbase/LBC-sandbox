package com.backbase.accesscontrol.api.service.it.usercontext;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_112;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_114;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.UserContextQueryController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemIds;
import com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.google.common.collect.Sets;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link UserContextQueryController#getDataItemsPermissions(DataItemsPermissions)}
 */
public class UserContextQueryControllerTestIT extends TestDbWireMock {

    private static final String GET_DATA_ITEMS_PERMISSIONS = "/accessgroups/usercontext/data-items/permissions";

    @Before
    public void setUp() {
        ApplicableFunctionPrivilege apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");
        ApplicableFunctionPrivilege apfBf1003View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "view");
        ApplicableFunctionPrivilege apfBf1003Edit = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "edit");

        ApplicableFunctionPrivilege apfBf1005Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1005", "create");

        UserContext userContext = new UserContext(contextUserId, rootMsa.getId());
        userContext = userContextJpaRepository.save(userContext);

        GroupedFunctionPrivilege apfBf1002CreateAfp = getGroupedFunctionPrivilege(null, apfBf1002Create, null);
        GroupedFunctionPrivilege apfBf1003ViewAfp = getGroupedFunctionPrivilege(null, apfBf1003View, null);
        GroupedFunctionPrivilege apfBf1003EditAfp = getGroupedFunctionPrivilege(null, apfBf1003Edit, null);

        GroupedFunctionPrivilege apfBf1005CreateAfp = getGroupedFunctionPrivilege(null, apfBf1005Create, null);

        FunctionGroup functionGroup1 = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "fg1", "function-group-description",
                getGroupedFunctionPrivileges(
                    apfBf1002CreateAfp,
                    apfBf1003ViewAfp,
                    apfBf1003EditAfp
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        FunctionGroup functionGroup2 = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "fg2", "function-group2",
                getGroupedFunctionPrivileges(
                    apfBf1002CreateAfp,
                    apfBf1003ViewAfp,
                    apfBf1003EditAfp
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        FunctionGroup functionGroup3 = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "fg3", "function-group3",
                getGroupedFunctionPrivileges(
                    apfBf1005CreateAfp
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        FunctionGroup functionGroup4 = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "fg4", "function-group4",
                getGroupedFunctionPrivileges(
                    apfBf1002CreateAfp,
                    apfBf1003ViewAfp,
                    apfBf1003EditAfp
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        // create data group
        DataGroup dataGroup = DataGroupUtil.createDataGroup("DG1_T1", "ARRANGEMENTS", "type1", rootMsa);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003", "11111"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("DG2_T2", "PAYEES", "type2", rootMsa);
        dataGroup2.setDataItemIds(newHashSet("00004", "00005", "00006"));
        dataGroup2 = dataGroupJpaRepository.saveAndFlush(dataGroup2);
        DataGroup dataGroup3 = DataGroupUtil.createDataGroup("DG3_T1", "ARRANGEMENTS", "type1", rootMsa);
        dataGroup3.setDataItemIds(newHashSet("00007", "00008", "11111"));
        dataGroup3 = dataGroupJpaRepository.saveAndFlush(dataGroup3);
        DataGroup dataGroup4 = DataGroupUtil.createDataGroup("DG4_T2", "PAYEES", "type2", rootMsa);
        dataGroup4.setDataItemIds(newHashSet("00009", "00000"));
        dataGroup4 = dataGroupJpaRepository.saveAndFlush(dataGroup4);
        DataGroup dataGroup5 = DataGroupUtil.createDataGroup("DG5_T3", "CUSTOMERS", "type3", rootMsa);
        dataGroup5.setDataItemIds(newHashSet("00010", "00011"));
        dataGroup5 = dataGroupJpaRepository.saveAndFlush(dataGroup5);

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup1, userContext);
        uafg.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId(), dataGroup2.getId()), uafg),
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup3.getId()), uafg)));
        userAssignedFunctionGroupJpaRepository.save(uafg);

        UserAssignedFunctionGroup uafg2 = new UserAssignedFunctionGroup(functionGroup2, userContext);
        uafg2.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup4.getId()), uafg2)));
        userAssignedFunctionGroupJpaRepository.save(uafg2);

        UserAssignedFunctionGroup uafg3 = new UserAssignedFunctionGroup(functionGroup3, userContext);
        userAssignedFunctionGroupJpaRepository.save(uafg3);

        UserAssignedFunctionGroup uafg4 = new UserAssignedFunctionGroup(functionGroup4, userContext);
        uafg4.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup4.getId(), dataGroup5.getId()), uafg4)));
        userAssignedFunctionGroupJpaRepository.save(uafg4);
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1 and DG3_T1) and Item2 (part of DG2_T2) -> 204
     */
    @Test
    public void testCheckDataItemsPermissionsItemInDifferentDataGroupsAndCorrectCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("11111");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00005");
        dataItemIds2.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ResponseEntity<String> responseEntity = executeServiceRequestReturnResponseEntity(
            new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
            , HttpMethod.POST, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1) and Item2 (part of DG2_T2) and random not arrangements -> 204
     */
    @Test
    public void testCheckDataItemsPermissionsItemInCombinationAndRandomTypeNotArrangements() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00001");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00005");
        dataItemIds2.setItemType("PAYEES");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("00009");
        dataItemIds3.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));

        ResponseEntity<String> responseEntity = executeServiceRequestReturnResponseEntity(
            new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
            , HttpMethod.POST, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    /**
     * BF (from FG1) and Item3 (part of DG3_T1) -> 204
     */
    @Test
    public void testCheckDataItemsPermissionsSingleCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00007");
        dataItemIds1.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1));

        ResponseEntity<String> responseEntity = executeServiceRequestReturnResponseEntity(
            new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
            , HttpMethod.POST, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    /**
     * BF (from FG1) and Item3 (part of DG3_T1) and ItemX (type of TX) -> 204
     */
    @Test
    public void testCheckDataItemsPermissionsSingleCombinationAndRandomType() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00007");
        dataItemIds1.setItemType("ARRANGEMENTS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00009");
        dataItemIds2.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ResponseEntity<String> responseEntity = executeServiceRequestReturnResponseEntity(
            new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
            , HttpMethod.POST, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenOnlyFirstPartOfCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00001");
        dataItemIds1.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item2 (part of DG2_T2) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenOnlySecondPartOfCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00005");
        dataItemIds1.setItemType("PAYEES");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1) and ItemX (type T2, not part of DG2_T2) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenTypeInCombinationButNotItem() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00005");
        dataItemIds1.setItemType("PAYEES");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00007");
        dataItemIds2.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1) and ItemX (type of TX not equal to T2) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenItemInCombinationButNotType() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00005");
        dataItemIds1.setItemType("PAYEES");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00001");
        dataItemIds2.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1) and Item4 (part of DG4_T2) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenPartOfCombinationAndSingelCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00009");
        dataItemIds1.setItemType("PAYEES");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00001");
        dataItemIds2.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item1 (part of DG1_T1 and part of DG3_T1) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenDataItemInComplexAndSingelCombination() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("11111");
        dataItemIds1.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and ItemX (type TX) and ItemY (type of TY) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenRandomItemAndTypes() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("12345");
        dataItemIds1.setItemType("CUSTOMERS");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("54321");
        dataItemIds2.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG1) and Item9 (type T1 not part of any DG from the same type) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenItemNotPartOfAnyDataGroupFromSameType() throws Exception {
        DataItemsPermissions dataItemsPermissions =
            new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("55555");
        dataItemIds1.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }


    /**
     * BF (from FG1) and Item1_1 (part of DG1_T1) and Item2_1 (part of DG2_T2) and ItemX_1 (type TX, different than T1
     * and T2 and TX is ARRANGEMENTS) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsForbiddenValidComboAndNotOptionalType() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds1 = new DataItemIds();
        dataItemIds1.setItemId("00000");
        dataItemIds1.setItemType("PAYEES");
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00010");
        dataItemIds2.setItemType("CUSTOMERS");
        DataItemIds dataItemIds3 = new DataItemIds();
        dataItemIds3.setItemId("44242");
        dataItemIds3.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("SEPA CT");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds1, dataItemIds2, dataItemIds3));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG3) and ItemTY_2 (type TZ and TZ is ARRANGMENTS) -> 403
     */
    @Test
    public void testCheckDataItemsPermissionsEmptyFunctionGroupAndDataItemIsOptionalType() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("00001");
        dataItemIds2.setItemType("ARRANGEMENTS");
        dataItemsPermissions.setFunctionName("Contacts");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds2));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_112.getErrorMessage(), ERR_AG_112.getErrorCode()));
    }

    /**
     * BF (from FG3) and ItemTY_1 (type TY and TY is NOT ARRANGEMENTS) -> 204
     */
    @Test
    public void testCheckDataItemsPermissionsEmptyFunctionGroupAndDataItemIsNotOptionalType() throws Exception {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("44444");
        dataItemIds2.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("Contacts");
        dataItemsPermissions.setPrivilege("create");
        dataItemsPermissions.setDataItems(asList(dataItemIds2));

        ResponseEntity<String> responseEntity = executeServiceRequestReturnResponseEntity(
            new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
            , HttpMethod.POST, new HashMap<>());
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    /**
     * BF (from FG3) and ItemTY_1 (type TY and TY is NOT ARRANGEMENTS) but wrong privilege 403
     */
    @Test
    public void testCheckDataItemsPermissionsEmptyFunctionGroupAndDataItemIsNotOptionalTypeNoPrivilege() {
        DataItemsPermissions dataItemsPermissions = new DataItemsPermissions();
        DataItemIds dataItemIds2 = new DataItemIds();
        dataItemIds2.setItemId("44444");
        dataItemIds2.setItemType("CONTACTS");
        dataItemsPermissions.setFunctionName("Contacts");
        dataItemsPermissions.setPrivilege("execute");
        dataItemsPermissions.setDataItems(asList(dataItemIds2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeServiceRequestReturnResponseEntity(
                new UrlBuilder(GET_DATA_ITEMS_PERMISSIONS)
                    .build(), dataItemsPermissions, "USER", rootMsa.getId(), contextUserId, ""
                , HttpMethod.POST, new HashMap<>()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_114.getErrorMessage(), ERR_AG_114.getErrorCode()));
    }
}
