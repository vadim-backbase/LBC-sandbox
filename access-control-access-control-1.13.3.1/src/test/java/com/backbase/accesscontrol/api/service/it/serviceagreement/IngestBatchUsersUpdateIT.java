package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.accesscontrol.matchers.BatchResponseItemExtendedMatcher.getMatchers;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class IngestBatchUsersUpdateIT extends TestDbWireMock {

    private String url = "/accessgroups/serviceagreements/ingest/service-agreements/users";
    private String userUrl = baseServiceUrl + "/users/bulk/externalids";

    private String validInternalUserId = getUuid();
    private ServiceAgreement csa;

    @Test
    public void shouldFirstAddAndThanRemoveUser() throws Exception {

        csa = new ServiceAgreement()
            .withName("csa")
            .withExternalId("extCSA")
            .withDescription("description csa")
            .withCreatorLegalEntity(rootLegalEntity)
            .withMaster(false);
        csa.addParticipant(new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(rootLegalEntity));
        csa = serviceAgreementJpaRepository.saveAndFlush(csa);
        testAddBatchUsersToServiceAgreements();
        testRemoveBatchUsersToServiceAgreements();
    }

    private void testAddBatchUsersToServiceAgreements() throws Exception {

        String invalidExternalUserId = "U-02";
        String validExternalUserId = "U-01";
        String legalEntityId = rootLegalEntity.getId();
        String externalServiceAgreementId = csa.getExternalId();

        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(asList(new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(invalidExternalUserId)));

        BatchResponseItemExtended validItem = new BatchResponseItemExtended()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(validExternalUserId)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);

        BatchResponseItemExtended invalidItem = new BatchResponseItemExtended()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(invalidExternalUserId)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);

        List<BatchResponseItemExtended> pandpAllResponces = asList(
            validItem, invalidItem);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(legalEntityId);
        user.setId(validInternalUserId);
        user.setExternalId(validExternalUserId);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse = singletonList(
            user);

        addStubPost(userUrl, usersResponse, 200);

        String contentAsString = executeRequest(url, data, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<>() {
            });

        assertThat(responseData,
            containsInAnyOrder(
                getMatchers(pandpAllResponces)
            )
        );
        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(csa.getId())));
    }

    private void testRemoveBatchUsersToServiceAgreements() throws Exception {

        String invalidExternalUserId = "U-02";
        String validExternalUserId = "U-01";
        String legalEntityId = rootLegalEntity.getId();
        String externalServiceAgreementId = csa.getExternalId();

        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.REMOVE)
            .withUsers(asList(new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(validExternalUserId),
                new PresentationServiceAgreementUserPair()
                    .withExternalServiceAgreementId(externalServiceAgreementId)
                    .withExternalUserId(invalidExternalUserId)));

        BatchResponseItemExtended validItem = new BatchResponseItemExtended()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(validExternalUserId)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);

        BatchResponseItemExtended invalidItem = new BatchResponseItemExtended()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(invalidExternalUserId)
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);

        List<BatchResponseItemExtended> pandpAllResponces = asList(
            validItem, invalidItem);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(legalEntityId);
        user.setId(validInternalUserId);
        user.setExternalId(validExternalUserId);

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersResponse = singletonList(
            user);

        addStubPost(userUrl, usersResponse, 200);

        String contentAsString = executeRequest(url, data, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<>() {
            });

        assertThat(responseData,
            containsInAnyOrder(
                getMatchers(pandpAllResponces)
            )
        );

        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(csa.getId())));
    }
}
