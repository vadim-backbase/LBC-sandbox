package com.backbase.accesscontrol.pandp.it.controllers;

import com.backbase.accesscontrol.api.service.DataGroupQueryController;
import com.backbase.accesscontrol.domain.*;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.service.rest.spec.model.AccessResourceType;
import com.backbase.accesscontrol.service.rest.spec.model.ContextLegalEntities;
import com.backbase.accesscontrol.service.rest.spec.model.UserAccessEntitlementsResource;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createParticipant;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test for {@link DataGroupQueryController#getDataGroups}
 */
public class UserQueryControllerIT extends TestConfig {

    private static final String url = "/service-api/v2/accesscontrol/accessgroups/users/%s/service-agreements/%s/permissions";

    private static final String USER_ACCESS_TO_ENTITLEMENTS_RESOURCE_URL = "/service-api/v2/accesscontrol/accessgroups/users/access/resources";

    @Test
    public void shouldReturnPersistenceApprovalPermissions() throws Exception {

        String userId = "userId";
        LegalEntity legalEntity = legalEntityJpaRepository.save(
            LegalEntityUtil.createLegalEntity("ex-id", "le-name", null));

        ServiceAgreement serviceAgreementSave = serviceAgreementJpaRepository.save(new ServiceAgreement()
            .withName("SA-Name")
            .withDescription("description")
            .withCreatorLegalEntity(legalEntity));

        FunctionGroup functionGroup = new FunctionGroup()
            .withDescription("Desc")
            .withName("Name")
            .withServiceAgreement(serviceAgreementSave)
            .withType(FunctionGroupType.DEFAULT);
        functionGroupJpaRepository.save(functionGroup);

        DataGroup dataGroup = new DataGroup()
            .withDescription("desc0")
            .withName("name")
            .withServiceAgreement(serviceAgreementSave)
            .withDataItemType("CONTACTS");
        dataGroupJpaRepository.save(dataGroup);

        UserContext userContextSave = userContextJpaRepository.save(new UserContext()
            .withUserId(userId)
            .withServiceAgreementId(serviceAgreementSave.getId()));

        UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination = new UserAssignedFunctionGroupCombination();
        userAssignedFunctionGroupCombination.setDataGroups(Sets.newHashSet(dataGroup));
        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup,
            userContextSave)
            .withUserAssignedFunctionGroupCombinations(Collections.singletonList(userAssignedFunctionGroupCombination));

        userAssignedFunctionGroupCombination.setUserAssignedFunctionGroup(userAssignedFunctionGroup);

        Set<UserAssignedFunctionGroup> oldState = new HashSet<>();
        oldState.add(userAssignedFunctionGroup);

        userContextSave.setUserAssignedFunctionGroups(oldState);
        userContextJpaRepository.saveAndFlush(userContextSave);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup =
            new ApprovalUserContextAssignFunctionGroup()
                .withFunctionGroupId(functionGroup.getId())
                .withDataGroups(Sets.newHashSet((dataGroup.getId())));

        ApprovalUserContext appUserContext = new ApprovalUserContext()
            .withUserId(userContextSave.getUserId())
            .withLegalEntityId(legalEntity.getId())
            .withServiceAgreementId(serviceAgreementSave.getId());
        appUserContext.setApprovalId("012345678901234567890123456789012345");

        approvalUserContextAssignFunctionGroup.setApprovalUserContext(appUserContext);
        appUserContext.getApprovalUserContextAssignFunctionGroups().add(approvalUserContextAssignFunctionGroup);
        approvalUserContextJpaRepository.save(appUserContext);

        String contentAsString = mockMvc
            .perform(get(String.format(String.format(url, userId, serviceAgreementSave.getId())))
                .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        PersistenceApprovalPermissions response = objectMapper
            .readValue(contentAsString, PersistenceApprovalPermissions.class);

        Assert.assertEquals(functionGroup.getId(),
            response.getItems().get(0).getFunctionGroupId());
        Assert.assertEquals(dataGroup.getId(),
            response.getItems().get(0).getDataGroupIds().get(0));
        Assert.assertEquals("012345678901234567890123456789012345",
            response.getApprovalId());
    }

    @Test
    public void shouldReturnLegalEntityIdsWhenLegalEntitySharesAccountsAndCheckAccessToAccountResource() throws Exception {
        LegalEntity creatorLegalEntity = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id", "le-name", null));
        LegalEntity legalEntitySharesUsers = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id-users", "le-user-name", null));
        LegalEntity legalEntitySharesAccounts = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id-accounts", "le-account-name", null));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
                .withName("SA-Name")
                .withDescription("description")
                .withCreatorLegalEntity(creatorLegalEntity);

        Participant participantSharingUsers = createParticipant(true, false, legalEntitySharesUsers);
        Participant participantSharingAccounts = createParticipant(false, true, legalEntitySharesAccounts);

        serviceAgreement.addParticipant(participantSharingUsers);
        serviceAgreement.addParticipant(participantSharingAccounts);

        ServiceAgreement savedServiceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        UserAccessEntitlementsResource entitlementsResource = new UserAccessEntitlementsResource()
                .contextServiceAgreementId(savedServiceAgreement.getId())
                .userLegalEntityId(legalEntitySharesAccounts.getId())
                .legalEntityIds(Lists.newArrayList(legalEntitySharesAccounts.getId()))
                .accessResourceType(AccessResourceType.ACCOUNT);

        String contentAsString = mockMvc
                .perform(post(USER_ACCESS_TO_ENTITLEMENTS_RESOURCE_URL)
                        .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entitlementsResource)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ContextLegalEntities response = objectMapper.readValue(contentAsString, ContextLegalEntities.class);

        assertEquals(1, response.getLegalEntities().size());
        assertEquals(legalEntitySharesAccounts.getId(), response.getLegalEntities().get(0));
    }

    @Test
    public void shouldReturnLegalEntityIdsWhenLegalEntitySharesAccountsAndCheckAccessToUsersResource() throws Exception {
        LegalEntity creatorLegalEntity = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id", "le-name", null));
        LegalEntity legalEntitySharesUsers = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id-users", "le-user-name", null));
        LegalEntity legalEntitySharesAccounts = legalEntityJpaRepository.save(
                LegalEntityUtil.createLegalEntity("ex-id-accounts", "le-account-name", null));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
                .withName("SA-Name")
                .withDescription("description")
                .withCreatorLegalEntity(creatorLegalEntity);

        Participant participantSharesUsers = createParticipant(true, false, legalEntitySharesUsers);
        Participant participantSharesAccounts = createParticipant(false, true, legalEntitySharesAccounts);

        serviceAgreement.addParticipant(participantSharesUsers);
        serviceAgreement.addParticipant(participantSharesAccounts);

        ServiceAgreement savedServiceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        UserAccessEntitlementsResource entitlementsResource = new UserAccessEntitlementsResource()
                .contextServiceAgreementId(savedServiceAgreement.getId())
                .userLegalEntityId(legalEntitySharesAccounts.getId())
                .legalEntityIds(Lists.newArrayList(legalEntitySharesUsers.getId()))
                .accessResourceType(AccessResourceType.USER);

        String contentAsString = mockMvc
                .perform(post(USER_ACCESS_TO_ENTITLEMENTS_RESOURCE_URL)
                        .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entitlementsResource)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        ContextLegalEntities response = objectMapper.readValue(contentAsString, ContextLegalEntities.class);

        assertEquals(1, response.getLegalEntities().size());
        assertEquals(legalEntitySharesUsers.getId(), response.getLegalEntities().get(0));
    }
}
