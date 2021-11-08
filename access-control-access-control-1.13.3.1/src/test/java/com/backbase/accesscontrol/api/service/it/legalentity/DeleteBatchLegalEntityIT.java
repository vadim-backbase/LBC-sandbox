package com.backbase.accesscontrol.api.service.it.legalentity;

import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsSuccessfulResponseItem;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.util.helpers.AccessTokenGenerator;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

public class DeleteBatchLegalEntityIT extends TestDbWireMock {

    private static final String LEGALENTITIES_BATCH_DELETE_PRESENTATION_URL = "/legalentities/batch/delete";

    @Autowired
    private AccessTokenGenerator accessTokenGenerator;

    private LegalEntity legalEntity2;

    @Before
    public void setUp() {

        LegalEntity legalEntity1 = LegalEntityUtil
            .createLegalEntity(null, "le1", "le1", rootLegalEntity, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity1);
        legalEntity2 = LegalEntityUtil
            .createLegalEntity(null, "le2", "le2", rootLegalEntity, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity2);
        LegalEntity legalEntityParent = LegalEntityUtil
            .createLegalEntity(null, "le4", "le4", rootLegalEntity, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityParent);
        LegalEntity legalEntityChild = LegalEntityUtil
            .createLegalEntity(null, "le5", "le5", legalEntityParent, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityChild);
        LegalEntity leInSa = LegalEntityUtil
            .createLegalEntity(null, "leInSa", "leInSa", null, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(leInSa);

        ServiceAgreement sa1 = ServiceAgreementUtil
            .createServiceAgreement("sa1", "sa1", "", leInSa, leInSa.getId(), leInSa.getId());
        sa1.setMaster(false);
        serviceAgreementJpaRepository.save(sa1);

        LegalEntity le3 = LegalEntityUtil
            .createLegalEntity(null, "le3", "le3", null, LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(le3);
        ServiceAgreement sa2 = ServiceAgreementUtil
            .createServiceAgreement("sa2", "sa2", "", le3, null, null);
        sa2.setMaster(false);
        serviceAgreementJpaRepository.save(sa2);
        createAssignDataGroupToUserInMSA();
        createPendingRequestForDataGroupInMSA();
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup("dg-name-customers", "CUSTOMERS", "dg-name-customers-desc", rootMsa);
        dataGroup.setDataItemIds(Collections.singleton(legalEntity1.getId()));
        dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    public void shouldDeleteBatchLegalEntity() throws Exception {

        BatchResponseItem successfulBatchResponseItem = new BatchResponseItem();
        successfulBatchResponseItem.setResourceId("le2");
        successfulBatchResponseItem.setStatus(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem failedInDataGroup = new BatchResponseItem();
        failedInDataGroup.setResourceId("le1");
        failedInDataGroup.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        failedInDataGroup.setErrors(Collections.singletonList("The Legal Entity cannot be deleted, "
            + "because it is contained in at least one data group of type CUSTOMERS."));
        BatchResponseItem failedNotFound = new BatchResponseItem();
        failedNotFound.setResourceId("invalid");
        failedNotFound.setStatus(StatusEnum.HTTP_STATUS_NOT_FOUND);
        failedNotFound.setErrors(Collections.singletonList("Legal Entity does not exist"));
        BatchResponseItem failedHasChildren = new BatchResponseItem();
        failedHasChildren.setResourceId("le4");
        failedHasChildren.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        failedHasChildren.setErrors(Collections.singletonList("Legal entity contains children, cannot be deleted"));
        BatchResponseItem failedInCsa = new BatchResponseItem();
        failedInCsa.setResourceId("leInSa");
        failedInCsa.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        failedInCsa.setErrors(Collections.singletonList("Legal entity is participant in CSA, cannot be deleted"));
        BatchResponseItem failedAsCreator = new BatchResponseItem();
        failedAsCreator.setResourceId("le3");
        failedAsCreator.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        failedAsCreator.setErrors(Collections.singletonList("Legal entity is creator of CSA, cannot be deleted"));
        BatchResponseItem failedUserWithPermissions = new BatchResponseItem();
        failedUserWithPermissions.setResourceId("lePermissionsMsa");
        failedUserWithPermissions.setStatus(StatusEnum.HTTP_STATUS_BAD_REQUEST);
        failedUserWithPermissions
            .setErrors(Collections.singletonList("There are users with assigned permissions in the Service Agreement, "
                + "so You cannot delete it."));

        String[] externalIds = new String[]{"le2", "le1", "invalid", "le4", "leInSa", "le3", "lePermissionsMsa"};

        PresentationBatchDeleteLegalEntities request = new PresentationBatchDeleteLegalEntities()
            .withAccessToken(accessTokenGenerator.generateValidToken())
            .withExternalIds(new LinkedHashSet<>(asList(externalIds)));

        String responseJson = executeRequest(LEGALENTITIES_BATCH_DELETE_PRESENTATION_URL, request, HttpMethod.POST);

        List<BatchResponseItem> response = readValue(
            responseJson,
            new TypeReference<List<BatchResponseItem>>() {
            });

        assertTrue(containsSuccessfulResponseItem(response, convertValue(successfulBatchResponseItem,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedInDataGroup,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedNotFound,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedHasChildren,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedInCsa,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedAsCreator,
            BatchResponseItem.class)));
        assertTrue(containsFailedResponseItem(response, convertValue(failedUserWithPermissions,
            BatchResponseItem.class)));

        verifyLegalEntityEvents(Sets.newHashSet(new LegalEntityEvent()
            .withAction(DELETE)
            .withId(legalEntity2.getId())));
    }

    private void createAssignDataGroupToUserInMSA() {

        LegalEntity lePermissionsMsa = LegalEntityUtil
            .createLegalEntity(null, "lePermissionsMsa", "lePermissionsMsa", null, LegalEntityType.CUSTOMER);
        ServiceAgreement sa3 = ServiceAgreementUtil
            .createServiceAgreement("sa3", "sa3", "", lePermissionsMsa, null, null);
        sa3.setMaster(true);

        legalEntityJpaRepository.save(lePermissionsMsa);
        sa3 = serviceAgreementJpaRepository.save(sa3);
        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "name", "description", new HashSet<>(), FunctionGroupType.DEFAULT,
                sa3);

        functionGroup = functionGroupJpaRepository.save(functionGroup);

        UserContext userContext = new UserContext(UUID.randomUUID().toString(), sa3.getId());
        userContext = userContextJpaRepository.save(userContext);

        UserAssignedFunctionGroup userAssignedFunctionGroup = new UserAssignedFunctionGroup(functionGroup,
            userContext);
        userAssignedFunctionGroupJpaRepository.save(userAssignedFunctionGroup);

    }

    private void createPendingRequestForDataGroupInMSA() {

        LegalEntity lePendingPermissionsMsa = LegalEntityUtil
            .createLegalEntity(null, "lePendingPermissionsMsa", "lePendingPermissionsMsa", null,
                LegalEntityType.CUSTOMER);
        legalEntityJpaRepository.save(lePendingPermissionsMsa);

        ServiceAgreement sa4 = ServiceAgreementUtil
            .createServiceAgreement("sa4", "sa4", "", lePendingPermissionsMsa, null, null);
        sa4.setMaster(true);
        serviceAgreementJpaRepository.save(sa4);

        ApprovalUserContext auc = new ApprovalUserContext()
            .withLegalEntityId(lePendingPermissionsMsa.getId())
            .withServiceAgreementId(sa4.getId())
            .withUserId("123");
        auc.setApprovalId("123e4567-e89b-12d3-a456-426655440000");

        approvalUserContextJpaRepository.save(auc);
    }
}
