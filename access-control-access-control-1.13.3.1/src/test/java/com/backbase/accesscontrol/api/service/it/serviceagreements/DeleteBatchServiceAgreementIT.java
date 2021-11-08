package com.backbase.accesscontrol.api.service.it.serviceagreements;

import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsSuccessfulResponseItem;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementsServiceApiController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem;
import com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem.StatusEnum;
import com.backbase.accesscontrol.util.helpers.AccessTokenGenerator;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementsServiceApiController#postBatchdelete}
 */
public class DeleteBatchServiceAgreementIT extends TestDbWireMock {

    private static String url = "/accessgroups/service-agreements/batch/delete";

    private LegalEntity legalEntityA;
    private LegalEntity legalEntityA1;
    private LegalEntity legalEntityB;
    private ServiceAgreement masterServiceAgreementOfA;
    private ServiceAgreement masterServiceAgreementOfA1;
    private ServiceAgreement masterServiceAgreementOfB;
    private ServiceAgreement customBetweenAandB;

    @Autowired
    private AccessTokenGenerator accessTokenGenerator;

    @Before
    public void setUp() {

        legalEntityA = LegalEntityUtil
            .createLegalEntity(null, "le-name-a", "ex-id-a", rootLegalEntity, LegalEntityType.BANK);
        legalEntityA = legalEntityJpaRepository.save(legalEntityA);
        masterServiceAgreementOfA = ServiceAgreementUtil
            .createServiceAgreement("master-of-a", "master-of-a", "desc.sa",
                legalEntityA, legalEntityA.getId(), legalEntityA.getId());
        masterServiceAgreementOfA.setMaster(true);
        masterServiceAgreementOfA = serviceAgreementJpaRepository.save(masterServiceAgreementOfA);

        legalEntityA1 = LegalEntityUtil
            .createLegalEntity(null, "le-name-a1", "ex-id-a1", legalEntityA, LegalEntityType.BANK);
        legalEntityA1 = legalEntityJpaRepository.save(legalEntityA1);
        masterServiceAgreementOfA1 = ServiceAgreementUtil
            .createServiceAgreement("master-of-a1", "master-of-a1", "desc.sa",
                legalEntityA1, legalEntityA1.getId(), legalEntityA1.getId());
        masterServiceAgreementOfA1.setMaster(true);
        masterServiceAgreementOfA1 = serviceAgreementJpaRepository.save(masterServiceAgreementOfA1);

        legalEntityB = LegalEntityUtil
            .createLegalEntity(null, "le-name-b", "ex-id-b", rootLegalEntity, LegalEntityType.BANK);
        legalEntityB = legalEntityJpaRepository.save(legalEntityB);
        masterServiceAgreementOfB = ServiceAgreementUtil
            .createServiceAgreement("master-of-b", "master-of-b", "desc.sa",
                legalEntityB, legalEntityB.getId(), legalEntityB.getId());
        masterServiceAgreementOfB.setMaster(true);
        masterServiceAgreementOfB = serviceAgreementJpaRepository.save(masterServiceAgreementOfB);

        customBetweenAandB = ServiceAgreementUtil
            .createServiceAgreement("custom-between-a-and-b", "custom-between-a-and-b", "desc.sa",
                rootLegalEntity, legalEntityA.getId(), legalEntityB.getId());
        customBetweenAandB.setMaster(false);
        customBetweenAandB.setStartDate(new Date(0));
        customBetweenAandB.setEndDate(new Date(100));
        customBetweenAandB = serviceAgreementJpaRepository.save(customBetweenAandB);
    }

    @Test
    public void testDeleteBatchServiceAgreement() throws Exception {

        PresentationServiceAgreementIdentifier identifier = new PresentationServiceAgreementIdentifier()
            .withIdIdentifier(masterServiceAgreementOfA1.getId());
        PresentationServiceAgreementIdentifier identifier2 = new PresentationServiceAgreementIdentifier()
            .withNameIdentifier("invalidIdentifier");
        PresentationDeleteServiceAgreements requestData = new PresentationDeleteServiceAgreements()
            .withAccessToken(accessTokenGenerator.generateValidToken())
            .withServiceAgreementIdentifiers(asList(identifier, identifier2));

        String responseString = executeServiceRequest(
            new UrlBuilder(url)
                .build()
            , requestData, "user", rootMsa.getId(), HttpMethod.POST);
        List<BatchResponseItem> response = readValue(
            responseString,
            new TypeReference<List<BatchResponseItem>>() {
            });

        BatchResponseItem successfulBatchResponseItem = new BatchResponseItem();
        successfulBatchResponseItem.setResourceId(masterServiceAgreementOfA1.getId());
        successfulBatchResponseItem.setStatus(StatusEnum.HTTP_STATUS_OK);
        BatchResponseItem failedNotFoundBatchResponseItem = new BatchResponseItem();
        failedNotFoundBatchResponseItem.setResourceId("invalidIdentifier");
        failedNotFoundBatchResponseItem.setStatus(StatusEnum.HTTP_STATUS_NOT_FOUND);
        failedNotFoundBatchResponseItem.setErrors(Collections.singletonList("Service agreement does not exist"));

        assertTrue(containsSuccessfulResponseItem(response, successfulBatchResponseItem));
        assertTrue(containsFailedResponseItem(response, failedNotFoundBatchResponseItem));

        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(DELETE)
            .withId(masterServiceAgreementOfA1.getId())));
    }
}
