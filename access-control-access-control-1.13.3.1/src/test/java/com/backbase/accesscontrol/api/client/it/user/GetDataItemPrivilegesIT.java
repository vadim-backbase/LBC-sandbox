package com.backbase.accesscontrol.api.client.it.user;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UsersController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.PersistenceUserDataItemPermission;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.List;
import liquibase.pro.packaged.U;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Test for {@link UsersController#getDataItemPermissionsContext}
 */
public class GetDataItemPrivilegesIT extends TestDbWireMock {

    private static final String GET_DATA_ITEM_PERMISSIONS_PRESENTATION_URL = "/accessgroups/users/data-item-permissions";
    private static final String GET_DATA_ITEM_PERMISSIONS_SERVICE_URL = "/accesscontrol/accessgroups/users/{userId}/service-agreements/{serviceAgreementId}/data-item-permissions";
    private final String SEPA_CT = "SEPA CT";
    private final String PAYMENTS_SEPA = "payments.sepa";
    private final String PAYMENTS = "Payments";
    private final String CREATE = "create";
    private final String ARRANGEMENTS = "ARRANGEMENTS";
    private final String DATA_ITEM_TEST_ID = "00001";
    private final String FUNCTION_ID = "1002";

    @Before
    public void setUp() {
        ApplicableFunctionPrivilege apfBf1002Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1002", "create");
        ApplicableFunctionPrivilege apfBf1003View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "view");
        ApplicableFunctionPrivilege apfBf1003Edit = businessFunctionCache
            .getByFunctionIdAndPrivilege("1003", "edit");

        UserContext userContext = new UserContext(contextUserId, rootMsa.getId());
        userContext = userContextJpaRepository.save(userContext);

        GroupedFunctionPrivilege apfBf1002CreateAfp = getGroupedFunctionPrivilege(null, apfBf1002Create, null);
        GroupedFunctionPrivilege apfBf1003ViewAfp = getGroupedFunctionPrivilege(null, apfBf1003View, null);
        GroupedFunctionPrivilege apfBf1003EditAfp = getGroupedFunctionPrivilege(null, apfBf1003Edit, null);

        FunctionGroup functionGroup = functionGroupJpaRepository.saveAndFlush(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    apfBf1002CreateAfp,
                    apfBf1003ViewAfp,
                    apfBf1003EditAfp
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        // create data group
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", rootMsa);
        dataGroup.setDataItemIds(newHashSet("00001", "00002", "00003"));
        dataGroup = dataGroupJpaRepository.saveAndFlush(dataGroup);
        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc2", rootMsa);
        dataGroup2.setDataItemIds(newHashSet("00004", "00005", "00006"));
        dataGroup2 = dataGroupJpaRepository.saveAndFlush(dataGroup2);

        UserAssignedFunctionGroup uafg = new UserAssignedFunctionGroup(functionGroup, userContext);
        uafg.setUserAssignedFunctionGroupCombinations(newHashSet(
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), uafg),
            new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup2.getId()), uafg)));
        userAssignedFunctionGroupJpaRepository.save(uafg);
    }

    @Test
    public void testGetDataItemPermissionsWithStatusOk() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_DATA_ITEM_PERMISSIONS_PRESENTATION_URL)
                .addQueryParameter("functionName", SEPA_CT)
                .addQueryParameter("resourceName", PAYMENTS)
                .addQueryParameter("privilege", CREATE)
                .addQueryParameter("dataGroupType", ARRANGEMENTS)
                .addQueryParameter("dataItemId", DATA_ITEM_TEST_ID)
                .build(), HttpMethod.GET, "USER");

        List<PersistenceUserDataItemPermission> response = readValue(contentAsString,
            new TypeReference<List<PersistenceUserDataItemPermission>>() {
            });

        assertThat(response, containsInAnyOrder(allOf(
            hasProperty("dataItem", hasProperty("id", equalTo(DATA_ITEM_TEST_ID))),
            hasProperty("dataItem", hasProperty("dataType", equalTo(ARRANGEMENTS))),
            hasProperty("permissions", containsInAnyOrder(allOf(
                hasProperty("resource", equalTo(PAYMENTS)),
                hasProperty("businessFunction", equalTo(SEPA_CT)),
                hasProperty("privileges", containsInAnyOrder(CREATE.split(",")))
            )))
        )));
    }

    @Test
    public void testGetServiceDataItemPermissionsWithStatusOk() throws Exception {

        String contentAsString = executeServiceRequest(
            new UrlBuilder(GET_DATA_ITEM_PERMISSIONS_SERVICE_URL)
                .addPathParameter(contextUserId)
                .addPathParameter(rootMsa.getId())
                .addQueryParameter("functionName", SEPA_CT)
                .addQueryParameter("resourceName", PAYMENTS)
                .addQueryParameter("privilege", CREATE)
                .addQueryParameter("dataGroupType", ARRANGEMENTS)
                .addQueryParameter("dataItemId", DATA_ITEM_TEST_ID)
                .build(), HttpMethod.GET, "USER", rootMsa.getId(), HttpMethod.GET);

        List<PersistenceUserDataItemPermission> response = readValue(contentAsString,
            new TypeReference<List<PersistenceUserDataItemPermission>>() {
            });

        assertThat(response, containsInAnyOrder(allOf(
            hasProperty("dataItem", hasProperty("id", equalTo(DATA_ITEM_TEST_ID))),
            hasProperty("dataItem", hasProperty("dataType", equalTo(ARRANGEMENTS))),
            hasProperty("permissions", containsInAnyOrder(allOf(
                hasProperty("resource", equalTo(PAYMENTS)),
                hasProperty("businessFunction", equalTo(SEPA_CT)),
                hasProperty("functionId", equalTo(FUNCTION_ID)),
                hasProperty("functionCode",equalTo(PAYMENTS_SEPA)),
                hasProperty("privileges", containsInAnyOrder(CREATE.split(",")))
            )))
        )));
    }
}
