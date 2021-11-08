package com.backbase.accesscontrol.api.service.it.on;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.HTTP_STATUS_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.EnabledIngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

/**
 * Test for {@link ServiceAgreementServiceApiController#putPresentationIngestServiceAgreementParticipants}
 */
@TestPropertySource(properties = {
    "backbase.data-group.validation.enabled=true"}
)
public class PutPresentationIngestServiceAgreementParticipantsWithValidationIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/serviceagreements/ingest/service-agreements/participants";

    @Autowired
    private ApplicationContext applicationContext;

    private LegalEntity legalEntityA;
    private LegalEntity legalEntityB;
    private ServiceAgreement serviceAgreement;

    @Before
    public void setUp() {

        legalEntityA = LegalEntityUtil
            .createLegalEntity(null, "le-name-2", "ex-id-2", rootLegalEntity, LegalEntityType.BANK);
        legalEntityA = legalEntityJpaRepository.save(legalEntityA);

        legalEntityB = LegalEntityUtil
            .createLegalEntity(null, "le-name-3", "ex-id-3", legalEntityA, LegalEntityType.BANK);
        legalEntityB = legalEntityJpaRepository.save(legalEntityB);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name.sa", "exid.sa", "desc.sa", legalEntityA, legalEntityA.getId(),
                legalEntityA.getId());
        serviceAgreement.setMaster(false);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);
    }

    @Test
    public void shouldCreateOnlyEnabledParticipantDataValidator() {
        IngestParticipantUpdateRemoveDataValidationProcessor bean = applicationContext
            .getBean(IngestParticipantUpdateRemoveDataValidationProcessor.class);
        assertEquals(EnabledIngestParticipantUpdateRemoveDataValidationProcessor.class, bean.getClass());
    }

    @Test
    public void shouldProcessAllItems() throws Exception {
        PresentationParticipantPutBody bodyAdd = new PresentationParticipantPutBody()
            .withAction(PresentationAction.ADD)
            .withExternalParticipantId(legalEntityB.getExternalId())
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withSharingAccounts(true)
            .withSharingUsers(true);
        PresentationParticipantPutBody bodyRemove = new PresentationParticipantPutBody()
            .withAction(PresentationAction.REMOVE)
            .withExternalParticipantId(legalEntityA.getExternalId())
            .withExternalServiceAgreementId(serviceAgreement.getExternalId())
            .withSharingAccounts(true)
            .withSharingUsers(true);
        PresentationParticipantPutBody invalidBody = new PresentationParticipantPutBody()
            .withAction(PresentationAction.REMOVE)
            .withExternalParticipantId("pId")
            .withExternalServiceAgreementId("saId");

        PresentationParticipantsPut data = new PresentationParticipantsPut()
            .withParticipants(
                Lists.newArrayList(
                    bodyAdd,
                    bodyRemove,
                    invalidBody
                )
            );

        String responseAsString = executeServiceRequest(
            URL,
            data, "Username", rootMsa.getId(), HttpMethod.PUT);

        List<BatchResponseItemExtended> responseData =
            readValue(
                responseAsString,
                new TypeReference<List<BatchResponseItemExtended>>() {
                });

        assertThat(responseData, hasSize(3));
        assertThat(responseData,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(HTTP_STATUS_OK)
                    .withResourceId(bodyAdd.getExternalParticipantId())
                    .withExternalServiceAgreementId(bodyAdd.getExternalServiceAgreementId())
                    .withAction(bodyAdd.getAction()),
                new BatchResponseItemExtended()
                    .withStatus(HTTP_STATUS_OK)
                    .withResourceId(bodyRemove.getExternalParticipantId())
                    .withExternalServiceAgreementId(bodyRemove.getExternalServiceAgreementId())
                    .withAction(bodyRemove.getAction()),
                new BatchResponseItemExtended()
                    .withStatus(HTTP_STATUS_BAD_REQUEST)
                    .withResourceId(invalidBody.getExternalParticipantId())
                    .withExternalServiceAgreementId(invalidBody.getExternalServiceAgreementId())
                    .withAction(invalidBody.getAction())
                    .withErrors(Lists
                        .newArrayList("Invalid participant"))

            )
        );

        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(serviceAgreement.getId())));
    }
}
