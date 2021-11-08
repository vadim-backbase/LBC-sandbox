package com.backbase.accesscontrol.api.client.it.user;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.FUNCTION_MANAGE_USERS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.UsersController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.UsersByPermissionsResponseBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;


/**
 * Test for {@link UsersController#getUsersByPermissions(String,
 * String, String, String, String, HttpServletRequest, HttpServletResponse)}
 */
public class GetUsersByPermissionsIT extends TestDbWireMock {

    private static String url = "/accessgroups/users/by-permissions";

    private String USER_ID1 = UUID.randomUUID().toString();
    private String USER_ID2 = UUID.randomUUID().toString();

    private DataGroup dataGroup1;
    private DataGroup dataGroup2;
    private ServiceAgreement serviceAgreement;
    private ApplicableFunctionPrivilege apfBf1007View;
    private ApplicableFunctionPrivilege apfBf1007Create;

    @Before
    public void setUp() {

        // create SA
        serviceAgreement =
            createServiceAgreement("BB between self", "id.external", "desc",
                rootLegalEntity, rootLegalEntity.getId(), rootLegalEntity.getId());
        serviceAgreement = serviceAgreementJpaRepository.saveAndFlush(serviceAgreement);

        apfBf1007View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "view");
        apfBf1007Create = businessFunctionCache
            .getByFunctionIdAndPrivilege("1007", "create");

        //save function group
        GroupedFunctionPrivilege groupedFunctionPrivilegeView =
            getGroupedFunctionPrivilege(null, apfBf1007View, null);
        GroupedFunctionPrivilege groupedFunctionPrivilegeCreate =
            getGroupedFunctionPrivilege(null, apfBf1007Create,
                null);
        FunctionGroup savedFunctionGroup1 = functionGroupJpaRepository.save(
            getFunctionGroup(null, "fgName1", "fgDescription123",
                getGroupedFunctionPrivileges(
                    groupedFunctionPrivilegeView,
                    groupedFunctionPrivilegeCreate
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        FunctionGroup savedFunctionGroup2 = functionGroupJpaRepository.save(
            getFunctionGroup(null, "fgName2", "fgDescription123",
                getGroupedFunctionPrivileges(
                    groupedFunctionPrivilegeView,
                    groupedFunctionPrivilegeCreate
                ),
                FunctionGroupType.DEFAULT, rootMsa)
        );

        // create data group
        dataGroup1 = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement,
            Lists.newArrayList("00001", "00002", "00003"));
        dataGroup1 = dataGroupJpaRepository.saveAndFlush(dataGroup1);

        dataGroup2 = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", rootMsa,
            Lists.newArrayList("00001", "00002", "00004"));
        dataGroup2 = dataGroupJpaRepository.saveAndFlush(dataGroup2);

        assignPermissions(savedFunctionGroup1, dataGroup1);
        assignPermissions(savedFunctionGroup2, dataGroup2);
    }

    private void assignPermissions(FunctionGroup functionGroup, DataGroup dataGroup) {
        UserContext userContext1 = userContextJpaRepository.save(new UserContext(USER_ID1,

            functionGroup.getServiceAgreementId()));
        UserContext userContext2 = userContextJpaRepository.save(new UserContext(USER_ID2, functionGroup.getServiceAgreementId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup1 = new UserAssignedFunctionGroup(functionGroup,
            userContext1);
        UserAssignedFunctionGroup userAssignedFunctionGroup2 = new UserAssignedFunctionGroup(functionGroup,
            userContext2);
        userAssignedFunctionGroup1 = userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup1);
        userAssignedFunctionGroup2 = userAssignedFunctionGroupJpaRepository.saveAndFlush(userAssignedFunctionGroup2);

        userAssignedCombinationRepository
            .saveAndFlush(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup1));
        userAssignedCombinationRepository
            .saveAndFlush(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup2));
    }

    @Test
    public void shouldGetUsersByPermissions() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(url)
                .addQueryParameter("serviceAgreementId", serviceAgreement.getId())
                .addQueryParameter("functionName", apfBf1007Create.getBusinessFunctionName())
                .addQueryParameter("privilege", "view")
                .addQueryParameter("dataGroupType", dataGroup1.getDataItemType())
                .addQueryParameter("dataItemId", "00001")
                .build(),
            HttpMethod.GET, "user", FUNCTION_MANAGE_USERS, PRIVILEGE_VIEW);

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = readValue(
            contentAsString,
            UsersByPermissionsResponseBody.class
        );
        assertThat(usersByPermissionsResponseBody.getUserIds().size(), is(2));
        assertThat(usersByPermissionsResponseBody.getUserIds(), containsInAnyOrder(USER_ID1, USER_ID2));
    }

    @Test
    public void shouldGetUsersByPermissionsWhenServiceAgreementIdParamIsNull() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(url)
                .addQueryParameter("functionName", apfBf1007Create.getBusinessFunctionName())
                .addQueryParameter("privilege", "view")
                .addQueryParameter("dataGroupType", dataGroup1.getDataItemType())
                .addQueryParameter("dataItemId", "00001")
                .build(),
            HttpMethod.GET, "user", FUNCTION_MANAGE_USERS, PRIVILEGE_VIEW);

        UsersByPermissionsResponseBody usersByPermissionsResponseBody = readValue(
            contentAsString,
            UsersByPermissionsResponseBody.class
        );
        assertThat(usersByPermissionsResponseBody.getUserIds().size(), is(2));
        assertThat(usersByPermissionsResponseBody.getUserIds(), containsInAnyOrder(USER_ID1, USER_ID2));
    }

}
