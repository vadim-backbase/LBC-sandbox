package com.backbase.accesscontrol.pandp.it.users.query;

import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.SERVICE_AGREEMENT_RESOURCE_NAME;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.UserQueryController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link UserQueryController#getArrangementPrivileges}
 */
public class GetArrangementPrivilegesIT extends TestConfig {

    private static String GET_ARRANGEMENT_PRIVILEGES_URL = "/service-api/v2/accesscontrol/accessgroups/users/privileges/arrangements";
    private static final String USER_ID = "userId";

    private ServiceAgreement serviceAgreement;


    @Before
    public void setUp() {

        LocalDateTime localDateTIme = LocalDateTime.now();
        Date startDate = Date.from(localDateTIme.minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDateTIme.plusDays(2).atZone(ZoneId.systemDefault()).toInstant());

        ApplicableFunctionPrivilege viewEntitlements = businessFunctionCache
            .getApplicableFunctionPrivilegeById(businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList(PRIVILEGE_VIEW))
                .stream().findFirst().get());
        ApplicableFunctionPrivilege createEntitlements = businessFunctionCache
            .getApplicableFunctionPrivilegeById(businessFunctionCache
                .getByFunctionNameOrResourceNameOrPrivilegesOptional(
                    SERVICE_AGREEMENT_FUNCTION_NAME, null, Lists.newArrayList(PRIVILEGE_CREATE))
                .stream().findFirst().get());

        LegalEntity legalEntity = legalEntityJpaRepository.save(createLegalEntity("le-ex-id", "le-name", null));

        serviceAgreement =
            createServiceAgreement("BB between self", "id.external", "desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement.setMaster(true);
        serviceAgreement.setStartDate(startDate);
        serviceAgreement.setEndDate(endDate);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        //save function group
        GroupedFunctionPrivilege viewEntitlementsWithLimit = getGroupedFunctionPrivilege(null, viewEntitlements, null);
        GroupedFunctionPrivilege createEntitlementsWitLimit = getGroupedFunctionPrivilege(null, createEntitlements,
            null);
        FunctionGroup savedFunctionGroup = functionGroupJpaRepository.save(
            getFunctionGroup(null, "function-group-name", "function-group-description",
                getGroupedFunctionPrivileges(
                    viewEntitlementsWithLimit,
                    createEntitlementsWitLimit
                ),
                FunctionGroupType.DEFAULT, serviceAgreement)
        );

        functionGroupJpaRepository.flush();

        // create data group
        DataGroup dataGroup = DataGroupUtil.createDataGroup("name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("01", "02"));
        dataGroupJpaRepository.save(dataGroup);

        DataGroup dataGroup2 = DataGroupUtil.createDataGroup("name2", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup2.setDataItemIds(Collections.singleton("03"));
        dataGroupJpaRepository.save(dataGroup2);
        dataGroupJpaRepository.flush();

        // create SA

        // assign FG to USER and SA
        UserContext userContext = userContextJpaRepository.save(new UserContext(USER_ID,
            serviceAgreement.getId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(savedFunctionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

        // assign DG TO FG For USER and SA
        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));
        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup2.getId()), userAssignedFunctionGroup));
    }

    @Test
    public void shouldGetArrangementPrivileges() throws Exception {
        String contentAsString = mockMvc.perform(get(GET_ARRANGEMENT_PRIVILEGES_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .param("userId", USER_ID)
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("functionName", SERVICE_AGREEMENT_FUNCTION_NAME)
            .param("resourceName", SERVICE_AGREEMENT_RESOURCE_NAME)
        )
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ArrangementPrivilegesGetResponseBody[] returnedListOfFunctions = objectMapper
            .readValue(contentAsString,
                ArrangementPrivilegesGetResponseBody[].class);
        Assert.assertEquals(3, returnedListOfFunctions.length);
        Assert.assertEquals("01", returnedListOfFunctions[0].getArrangementId());

        Assert.assertTrue(returnedListOfFunctions[0].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("view")));
        Assert.assertTrue(returnedListOfFunctions[0].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("create")));
        Assert.assertEquals("03", returnedListOfFunctions[1].getArrangementId());
        Assert.assertTrue(returnedListOfFunctions[1].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("view")));
        Assert.assertTrue(returnedListOfFunctions[1].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("create")));
        Assert.assertEquals("02", returnedListOfFunctions[2].getArrangementId());
        Assert.assertTrue(returnedListOfFunctions[2].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("view")));
        Assert.assertTrue(returnedListOfFunctions[2].getPrivileges().contains(
            new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                .withPrivilege("create")));
    }
}
