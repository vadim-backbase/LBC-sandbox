package com.backbase.accesscontrol.api.service.it.useraccess;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_068;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.UserContextEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationGenericObjectId;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "backbase.approval.validation.enabled=false")
public class UpdateAssignUsersPermissionsApprovalOffIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/service-agreements/{id}/users/{userId}/permissions";
    private static final  String URL_USERS = "/service-api/v2/users/{id}";


    private final String userId = "us-id";

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
    private FunctionGroup functionGroup06;
    private FunctionGroup functionGroup07;

    @Before
    public void setup(){

        dataGroup01 = createDataGroup("dag01", "ARRANGEMENTS", "dag01", rootMsa);
        dataGroup01 = dataGroupJpaRepository.save(dataGroup01);

        dataGroup02 = createDataGroup("dag02", "ARRANGEMENTS", "dag02", rootMsa);
        dataGroup02 = dataGroupJpaRepository.save(dataGroup02);

        dataGroup03 = createDataGroup("dag03", "ARRANGEMENTS", "dag03", rootMsa);
        dataGroup03 = dataGroupJpaRepository.save(dataGroup03);

        dataGroup04 = createDataGroup("dag04", "ARRANGEMENTS", "dag04", rootMsa);
        dataGroup04 = dataGroupJpaRepository.save(dataGroup04);

        dataGroup05 = createDataGroup("dag05", "ARRANGEMENTS", "dag05", rootMsa);
        dataGroup05 = dataGroupJpaRepository.save(dataGroup05);

        functionGroup01 = createFunctionGroup("fg01", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup02 = createFunctionGroup("fg02", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup03 = createFunctionGroup("fg03", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup04 = createFunctionGroup("fg04", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup05 = createFunctionGroup("fg05", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);
        functionGroup06 = createFunctionGroup("fg06", "desc", rootMsa, new ArrayList<>(), FunctionGroupType.DEFAULT);

        functionGroup07 = functionGroupJpaRepository.save(
            new FunctionGroup()
                .withServiceAgreement(rootMsa)
                .withName("fg07")
                .withDescription("desc")
                .withType(FunctionGroupType.TEMPLATE)
                .withPermissions(emptySet())
                .withAssignablePermissionSet(apsDefaultRegular));

        UserContext userContext = new UserContext(userId, rootMsa.getId());

        UserAssignedFunctionGroup uaFg01 = new UserAssignedFunctionGroup(functionGroup01, userContext);
        uaFg01.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(newHashSet(dataGroup01.getId()), uaFg01)));

        UserAssignedFunctionGroup uaFg03 = new UserAssignedFunctionGroup(functionGroup03, userContext);
        uaFg03.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(newHashSet(dataGroup01.getId()), uaFg03)));

        UserAssignedFunctionGroup uaFg04 = new UserAssignedFunctionGroup(functionGroup04, userContext);
        uaFg04.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(newHashSet(dataGroup04.getId()), uaFg04),
                new UserAssignedFunctionGroupCombination(newHashSet(dataGroup05.getId()), uaFg04)));

        UserAssignedFunctionGroup uaFg05 = new UserAssignedFunctionGroup(functionGroup05, userContext);
        uaFg05.setUserAssignedFunctionGroupCombinations(
            newHashSet(new UserAssignedFunctionGroupCombination(newHashSet(dataGroup01.getId()), uaFg05)));

        userContext.setUserAssignedFunctionGroups(newHashSet(uaFg01, uaFg03, uaFg04, uaFg05));
        userContextJpaRepository.save(userContext);
    }

    @Test
    public void shouldInvokePersistenceEndpointOverService() throws IOException {

        PresentationFunctionDataGroup item11 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup01.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup01.getId())));

        PresentationFunctionDataGroup item123 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup01.getId())
            .withDataGroupIds(asList(new PresentationGenericObjectId().withId(dataGroup02.getId()), new PresentationGenericObjectId().withId(dataGroup03.getId())));

        PresentationFunctionDataGroup item24 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup02.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup04.getId())));

        PresentationFunctionDataGroup item25 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup02.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup05.getId())));

        PresentationFunctionDataGroup item32 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup03.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup02.getId())));

        PresentationFunctionDataGroup item44 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup04.getId())
            .withDataGroupIds(singletonList(new PresentationGenericObjectId().withId(dataGroup04.getId())));

        PresentationFunctionDataGroup item6 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup06.getId())
            .withDataGroupIds(emptyList());

        PresentationFunctionDataGroupItems putData = new PresentationFunctionDataGroupItems()
            .withItems(asList(item11,item123, item24, item25, item32, item44, item6));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setExternalId("username");
        user.setId(userId);
        user.setFullName("userFullName");
        user.legalEntityId(rootLegalEntity.getId());

        addStubGet(new UrlBuilder(URL_USERS).addPathParameter(userId)
            .addQueryParameter("skipHierarchyCheck","true").build(), user, 200);

        String jsonResponse = executeRequest(
            new UrlBuilder(URL).addPathParameter(rootMsa.getId()).addPathParameter(userId).build(), putData,
            HttpMethod.PUT);

        assertEquals("{\"additions\":{},\"approvalStatus\":null}", jsonResponse);

        verifyUserContextEvents(Sets.newHashSet(new UserContextEvent()
            .withServiceAgreementId(rootMsa.getId())
            .withUserId(userId)));

        UserContext userContext = userContextJpaRepository
            .findByUserIdAndServiceAgreementIdWithFunctionDataGroupIdsAndSelfApprovalPolicies(userId, rootMsa.getId()).get();

        assertThat(userContext.getUserAssignedFunctionGroups(), containsInAnyOrder(
            allOf(
                hasProperty("functionGroupId", equalTo(functionGroup01.getId())),
                hasProperty("userAssignedFunctionGroupCombinations", containsInAnyOrder(
                    allOf(hasProperty("dataGroupIds", containsInAnyOrder(dataGroup01.getId()))),
                    allOf(hasProperty("dataGroupIds",
                        containsInAnyOrder(dataGroup02.getId(), dataGroup03.getId())))))),
            allOf(
                hasProperty("functionGroupId", equalTo(functionGroup02.getId())),
                hasProperty("userAssignedFunctionGroupCombinations", containsInAnyOrder(
                    allOf(hasProperty("dataGroupIds", containsInAnyOrder(dataGroup04.getId()))),
                    allOf(hasProperty("dataGroupIds", containsInAnyOrder(dataGroup05.getId())))))),
            allOf(
                hasProperty("functionGroupId", equalTo(functionGroup03.getId())),
                hasProperty("userAssignedFunctionGroupCombinations", containsInAnyOrder(
                    allOf(hasProperty("dataGroupIds", containsInAnyOrder(dataGroup02.getId())))
                ))),
            allOf(
                hasProperty("functionGroupId", equalTo(functionGroup04.getId())),
                hasProperty("userAssignedFunctionGroupCombinations", containsInAnyOrder(
                    allOf(hasProperty("dataGroupIds", containsInAnyOrder(dataGroup04.getId())))))),
            allOf(
                hasProperty("functionGroupId", equalTo(functionGroup06.getId())),
                hasProperty("userAssignedFunctionGroupCombinations", hasSize(0)))
        ));

    }

    @Test
    public void shouldThrowBadRequestIfSaDoesNotContainApsOfJrtAps() {

        PresentationFunctionDataGroup item7 = new PresentationFunctionDataGroup()
            .withFunctionGroupId(functionGroup07.getId())
            .withDataGroupIds(emptyList());

        PresentationFunctionDataGroupItems putData = new PresentationFunctionDataGroupItems()
            .withItems(singletonList(item7));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setExternalId("username");
        user.setId(userId);
        user.setFullName("userFullName");
        user.legalEntityId(rootLegalEntity.getId());

        addStubGet(new UrlBuilder(URL_USERS).addPathParameter(userId)
            .addQueryParameter("skipHierarchyCheck","true").build(), user, 200);

        BadRequestException exception = assertThrows(BadRequestException.class, () ->  executeRequest(
            new UrlBuilder(URL).addPathParameter(rootMsa.getId()).addPathParameter(userId).build(), putData,
            HttpMethod.PUT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_068.getErrorMessage(), ERR_ACQ_068.getErrorCode()));
    }
}
