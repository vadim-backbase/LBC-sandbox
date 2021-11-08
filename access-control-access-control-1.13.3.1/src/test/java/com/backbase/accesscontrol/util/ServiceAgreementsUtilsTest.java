package com.backbase.accesscontrol.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.ServiceAgreementTransformerPersistence;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.Status;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementsUtilsTest {

    @Mock
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;
    @InjectMocks
    private ServiceAgreementsUtils serviceAgreementsUtils;

    @Test
    public void transformToPersistenceServiceAgreements() {

        LegalEntity le = new LegalEntity();
        le.setId("leId");
        le.setName("leName");
        ServiceAgreement sa = new ServiceAgreement()
            .withId("id")
            .withName("name")
            .withExternalId("exId")
            .withCreatorLegalEntity(le)
            .withDescription("desc")
            .withState(ServiceAgreementState.ENABLED)
            .withParticipants(new HashMap<>())
            .withAdditions(new HashMap<>())
            .withStartDate(new Date(0))
            .withEndDate(new Date(1));
        List<ServiceAgreement> request = Collections.singletonList(sa);
        PersistenceServiceAgreement response = new PersistenceServiceAgreement()
            .withId(sa.getId())
            .withCreatorLegalEntity(sa.getCreatorLegalEntity().getId())
            .withExternalId(sa.getExternalId())
            .withDescription(sa.getDescription())
            .withStatus(Status.ENABLED)
            .withNumberOfParticipants(BigDecimal.ZERO)
            .withValidFrom(new Date(0))
            .withValidUntil(new Date(1));
        when(serviceAgreementTransformerPersistence
            .transformServiceAgreement(any(), eq(sa))).thenReturn(response);
        List<PersistenceServiceAgreement> persistenceServiceAgreements = serviceAgreementsUtils
            .transformToPersistenceServiceAgreements(request);
        assertEquals(persistenceServiceAgreements.get(0), response);
    }
}