package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.accesscontrol.domain.enums.LegalEntityType.CUSTOMER;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_039;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mapstruct.ap.internal.util.Collections.asSet;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.CreateStatus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class PostServiceAgreementIngestIT extends TestDbWireMock {

    private static final String GET_USERS_BY_EXTERNAL_IDS_PANDP_URL = baseServiceUrl + "/users/externalids";
    private static final String INGEST_SERVICE_AGREEMENT_URL = "/accessgroups/serviceagreements/ingest/serviceagreements";

    @SuppressWarnings("squid:S2699")
    @Test
    public void shouldIngestCustomServiceAgreementWithCreatorLegalEntity() throws IOException {

        LegalEntity bank = rootLegalEntity;
        LegalEntity companyAUnderBank = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity("id", "companyAUnderBank", "companyAUnderBank", bank, CUSTOMER));
        LegalEntity companyBUnderA = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyBUnderA", "companyBUnderA", companyAUnderBank, CUSTOMER));
        LegalEntity companyCUnderB = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyCUnderB", "companyCUnderB", companyBUnderA, CUSTOMER));

        String serviceAgreementExternalId = "id.external";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(companyBUnderA.getId());
        user.setId(getUuid());
        user.setExternalId("EX-us1");
        user.setFullName("user1");

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setLegalEntityId(companyCUnderB.getId());
        user2.setId(getUuid());
        user2.setExternalId("EX-us2");
        user2.setFullName("user2");

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersList = asList(user, user2);

        ServiceAgreementIngestPostRequestBody requestBody = new ServiceAgreementIngestPostRequestBody()
            .withExternalId(serviceAgreementExternalId)
            .withDescription("desc")
            .withName("name")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(false)
            .withParticipantsToIngest(Sets.newHashSet(
                new ParticipantIngest()
                    .withExternalId(companyBUnderA.getExternalId())
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(Sets.newHashSet("EX-us1"))
                    .withUsers(Sets.newHashSet("EX-us1")),
                new ParticipantIngest()
                    .withExternalId(companyCUnderB.getExternalId())
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("EX-us2"))))
            .withRegularUserAps(new PresentationUserApsIdentifiers().withIdIdentifiers(asSet(new BigDecimal(1))))
            .withAdminUserAps(new PresentationUserApsIdentifiers().withNameIdentifiers(asSet("Admin user APS")))
            .withCreatorLegalEntity(companyAUnderBank.getId());

        addStubPost(GET_USERS_BY_EXTERNAL_IDS_PANDP_URL, usersList, 200);

        String content = executeRequest(INGEST_SERVICE_AGREEMENT_URL, requestBody, HttpMethod.POST);

        String serviceAgreementResponseId = readValue(content, ServiceAgreementIngestPostResponseBody.class).getId();

        Optional<ServiceAgreement> serviceAgreementByExternalId = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId);

        assertTrue(serviceAgreementByExternalId.isPresent());
        assertFalse(serviceAgreementByExternalId.get().isMaster());
        assertEquals(serviceAgreementByExternalId.get().getId(), serviceAgreementResponseId);
        assertEquals(requestBody.getCreatorLegalEntity(),
            serviceAgreementByExternalId.get().getCreatorLegalEntity().getId());
    }

    @Test
    public void shouldIngestCustomServiceAgreementWithoutCreatorLegalEntity() throws IOException {

        LegalEntity bank = rootLegalEntity;

        LegalEntity companyAUnderBank = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", bank, CUSTOMER));
        LegalEntity companyBUnderBank = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyBUnderBank", "companyBUnderBank", bank, CUSTOMER));

        String serviceAgreementExternalId = "id.external";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(companyAUnderBank.getId());
        user.setId(getUuid());
        user.setExternalId("EX-us1");
        user.setFullName("user1");

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setLegalEntityId(companyBUnderBank.getId());
        user2.setId(getUuid());
        user2.setExternalId("EX-us2");
        user2.setFullName("user2");

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersList = asList(user, user2);

        ServiceAgreementIngestPostRequestBody requestBody = new ServiceAgreementIngestPostRequestBody()
            .withExternalId(serviceAgreementExternalId)
            .withDescription("desc")
            .withName("name")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(false)
            .withParticipantsToIngest(Sets.newHashSet(
                new ParticipantIngest()
                    .withExternalId(companyAUnderBank.getExternalId())
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(Sets.newHashSet("EX-us1"))
                    .withUsers(Sets.newHashSet("EX-us1")),
                new ParticipantIngest()
                    .withExternalId(companyBUnderBank.getExternalId())
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("EX-us2"))))
            .withRegularUserAps(new PresentationUserApsIdentifiers().withIdIdentifiers(asSet(new BigDecimal(1))))
            .withAdminUserAps(new PresentationUserApsIdentifiers().withNameIdentifiers(asSet("Admin user APS")));

        addStubPost(GET_USERS_BY_EXTERNAL_IDS_PANDP_URL, usersList, 200);

        String content = executeRequest(INGEST_SERVICE_AGREEMENT_URL, requestBody, HttpMethod.POST);

        String serviceAgreementResponseId = readValue(content, ServiceAgreementIngestPostResponseBody.class)
            .getId();

        Optional<ServiceAgreement> serviceAgreementByExternalId = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId);

        assertTrue(serviceAgreementByExternalId.isPresent());
        assertFalse(serviceAgreementByExternalId.get().isMaster());
        assertEquals(serviceAgreementByExternalId.get().getId(), serviceAgreementResponseId);
        assertEquals(bank.getId(), serviceAgreementByExternalId.get().getCreatorLegalEntity().getId());
    }

    @Test
    public void shouldThrowBadRequestOnInvalidHierarchyByProvidingCreatorLegalEntityId() {

        LegalEntity bank = rootLegalEntity;

        LegalEntity companyAUnderBank = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", bank, CUSTOMER));
        LegalEntity companyBUnderCompanyA = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyBUnderCompanyA", "companyBUnderCompanyA", companyAUnderBank, CUSTOMER));

        LegalEntity companyFromOtherHierarchy = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity("creatorLegalEntityId", "companyFromOtherHierarchy", "companyFromOtherHierarchy", bank,
                CUSTOMER));

        String serviceAgreementExternalId = "id.external";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(companyAUnderBank.getId());
        user.setId(getUuid());
        user.setExternalId("EX-us1");
        user.setFullName("user1");

        com.backbase.dbs.user.api.client.v2.model.GetUser user2 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user2.setLegalEntityId(companyBUnderCompanyA.getId());
        user2.setId(getUuid());
        user2.setExternalId("EX-us2");
        user2.setFullName("user2");

        List<com.backbase.dbs.user.api.client.v2.model.GetUser> usersList = asList(user, user2);

        ServiceAgreementIngestPostRequestBody requestBody = new ServiceAgreementIngestPostRequestBody()
            .withExternalId(serviceAgreementExternalId)
            .withDescription("desc")
            .withName("name")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(false)
            .withParticipantsToIngest(Sets.newHashSet(
                new ParticipantIngest()
                    .withExternalId(companyAUnderBank.getExternalId())
                    .withSharingUsers(true)
                    .withSharingAccounts(false)
                    .withAdmins(Sets.newHashSet("EX-us1"))
                    .withUsers(Sets.newHashSet("EX-us1")),
                new ParticipantIngest()
                    .withExternalId(companyBUnderCompanyA.getExternalId())
                    .withSharingAccounts(true)
                    .withSharingUsers(false)
                    .withAdmins(Sets.newHashSet("EX-us2"))))
            .withRegularUserAps(new PresentationUserApsIdentifiers().withIdIdentifiers(asSet(new BigDecimal(1))))
            .withAdminUserAps(new PresentationUserApsIdentifiers().withNameIdentifiers(asSet("Admin user APS")))
            .withCreatorLegalEntity(companyFromOtherHierarchy.getId());

        addStubPost(GET_USERS_BY_EXTERNAL_IDS_PANDP_URL, usersList, 200);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> executeRequest(INGEST_SERVICE_AGREEMENT_URL, requestBody, HttpMethod.POST));

        assertThat(badRequestException,
            new BadRequestErrorMatcher(ERR_ACQ_039.getErrorMessage(), ERR_ACQ_039.getErrorCode()));
    }

    @Test
    public void shouldIngestMasterServiceAgreement() throws IOException {

        LegalEntity bank = rootLegalEntity;

        LegalEntity companyAUnderBank = legalEntityJpaRepository.saveAndFlush(
            createLegalEntity(null, "companyAUnderBank", "companyAUnderBank", bank, CUSTOMER));

        String serviceAgreementExternalId = "id.external";

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user.setLegalEntityId(companyAUnderBank.getId());
        user.setId(getUuid());
        user.setExternalId("EX-us1");
        user.setFullName("user1");

        ServiceAgreementIngestPostRequestBody requestBody = new ServiceAgreementIngestPostRequestBody()
            .withExternalId(serviceAgreementExternalId)
            .withDescription("desc")
            .withName("name")
            .withStatus(CreateStatus.ENABLED)
            .withIsMaster(true)
            .withParticipantsToIngest(Sets.newHashSet(new ParticipantIngest()
                .withExternalId(companyAUnderBank.getExternalId())
                .withSharingUsers(true)
                .withSharingAccounts(true)))
            .withRegularUserAps(new PresentationUserApsIdentifiers().withIdIdentifiers(asSet(new BigDecimal(1))))
            .withAdminUserAps(new PresentationUserApsIdentifiers().withNameIdentifiers(asSet("Admin user APS")));

        String content = executeRequest(INGEST_SERVICE_AGREEMENT_URL, requestBody, HttpMethod.POST);

        String serviceAgreementResponseId = readValue(content, ServiceAgreementIngestPostResponseBody.class).getId();

        Optional<ServiceAgreement> serviceAgreementByExternalId = serviceAgreementJpaRepository
            .findByExternalId(serviceAgreementExternalId);

        assertTrue(serviceAgreementByExternalId.isPresent());
        assertTrue(serviceAgreementByExternalId.get().isMaster());
        assertEquals(serviceAgreementByExternalId.get().getId(), serviceAgreementResponseId);
        assertEquals(companyAUnderBank.getId(), serviceAgreementByExternalId.get().getCreatorLegalEntity().getId());
    }
}
