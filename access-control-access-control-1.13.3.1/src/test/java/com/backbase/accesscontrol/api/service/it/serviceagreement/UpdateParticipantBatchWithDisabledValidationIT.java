package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.DisabledParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.business.serviceagreement.participant.validation.IngestParticipantUpdateRemoveDataValidationProcessor;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementServiceApiController#putPresentationIngestServiceAgreementParticipants}
 */
public class UpdateParticipantBatchWithDisabledValidationIT extends TestDbWireMock {

    private static final String BATCH_UPDATE_PARTICIPANTS_URL = "/accessgroups/serviceagreements/ingest/service-agreements/participants";

    @Autowired
    private ApplicationContext applicationContext;

    private ServiceAgreement serviceAgreement;
    private LegalEntity legalEntity;

    @Before
    public void setup() {
        legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, legalEntity.getId(),
                legalEntity.getId());
        serviceAgreement.setMaster(false);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        DataGroup dataGroup1 = DataGroupUtil.createDataGroup("dg-name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup1.setDataItemIds(Collections.singleton("001"));
        dataGroupJpaRepository.save(dataGroup1);
    }

    @Test
    public void shouldCreateOnlyDisabledParticipantDataValidator() throws Exception {
        IngestParticipantUpdateRemoveDataValidationProcessor bean = applicationContext
            .getBean(IngestParticipantUpdateRemoveDataValidationProcessor.class);
        assertEquals(DisabledParticipantUpdateRemoveDataValidationProcessor.class, bean.getClass());
    }

    @Test
    public void shouldProcessAllItems() throws IOException {
        PresentationParticipantPutBody removeNotValid = new PresentationParticipantPutBody()
            .withAction(PresentationAction.REMOVE)
            .withExternalParticipantId(legalEntity.getExternalId())
            .withExternalServiceAgreementId(serviceAgreement.getExternalId());

        PresentationParticipantsPut data = new PresentationParticipantsPut()
            .withParticipants(
                Lists.newArrayList(
                    removeNotValid
                )
            );

        String contentAsString = executeRequest(BATCH_UPDATE_PARTICIPANTS_URL, data, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseData = readValue(contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertThat(responseData, hasSize(1));
        assertThat(responseData,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(HTTP_STATUS_BAD_REQUEST)
                    .withResourceId(removeNotValid.getExternalParticipantId())
                    .withExternalServiceAgreementId(removeNotValid.getExternalServiceAgreementId())
                    .withAction(removeNotValid.getAction())
                    .withErrors(Lists.newArrayList(
                        "Unable to remove participant, please remove data groups from the service agreement"))
            )
        );
    }
}
