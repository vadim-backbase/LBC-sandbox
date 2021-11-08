package com.backbase.accesscontrol.business.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.PersistenceExtendedParticipant;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AgreementsPersistenceServiceTest {

    @InjectMocks
    private AgreementsPersistenceService agreementsPersistenceService;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;


    @Test
    public void shouldRetrieveServiceAgreementParticipantsByServiceAgreementIds() {
        Set<String> serviceAgreementExternalIds = Sets.newHashSet("1", "2");

        PersistenceExtendedParticipant participantSA1 = new PersistenceExtendedParticipant();
        participantSA1.setExternalServiceAgreementId("1");
        participantSA1.setExternalId("e1");
        PersistenceExtendedParticipant participantSA2 = new PersistenceExtendedParticipant();
        participantSA2.setExternalId("e2");

        List<PersistenceExtendedParticipant> response = Lists.newArrayList(
            participantSA1,
            participantSA1,
            participantSA2);

        when(persistenceServiceAgreementService.
            listParticipantsByExternalServiceAgreementIds(serviceAgreementExternalIds)).thenReturn(response);

        Map<String, Set<PersistenceExtendedParticipant>> participantsPerExternalId = agreementsPersistenceService
            .getParticipantsPerExternalId(serviceAgreementExternalIds);
        assertThat(participantsPerExternalId.keySet(), is(Sets.newHashSet("e1", "e2")));
        assertThat(participantsPerExternalId, hasEntry("e1", Sets.newHashSet(participantSA1)));
        assertThat(participantsPerExternalId, hasEntry("e2", Sets.newHashSet(participantSA2)));
    }


    @Test
    public void shouldRetrieveEmptyMap() {
        Set<String> serviceAgreementExternalIds = new HashSet<>();
        Map<String, Set<PersistenceExtendedParticipant>> participantsPerServiceAgreement = agreementsPersistenceService
            .getParticipantsPerExternalId(serviceAgreementExternalIds);
        assertThat(participantsPerServiceAgreement.keySet(), hasSize(0));

        verify(persistenceServiceAgreementService, never())
            .listParticipantsByExternalServiceAgreementIds(any());
    }

    @Test
    public void shouldRetrieveServiceAgreementParticipantIdsByServiceAgreementExternalId() {
        String serviceAgreementExternalId = "1";

        PersistenceExtendedParticipant participantSA1 = new PersistenceExtendedParticipant();
        participantSA1.setExternalServiceAgreementId(serviceAgreementExternalId);
        participantSA1.setId("p-id1");
        participantSA1.setSharingAccounts(true);
        participantSA1.setExternalId("e1");
        PersistenceExtendedParticipant participantSA2 = new PersistenceExtendedParticipant();
        participantSA2.setExternalServiceAgreementId(serviceAgreementExternalId);
        participantSA2.setId("p-id2");
        participantSA2.setSharingAccounts(true);
        participantSA2.setExternalId("e2");

        List<PersistenceExtendedParticipant> response = Lists.newArrayList(
            participantSA1,
            participantSA1,
            participantSA2);

        when(persistenceServiceAgreementService.
            listParticipantsByExternalServiceAgreementIds(Sets.newHashSet(serviceAgreementExternalId)))
            .thenReturn(response);

        Set<String> participantsPerExternalId = agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(serviceAgreementExternalId);
        assertThat(participantsPerExternalId, is(Sets.newHashSet("p-id1", "p-id2")));
    }

    @Test
    public void shouldRetrieveEmptySet() {
        String serviceAgreementExternalIds = null;
        Set<String> participantsPerServiceAgreement = agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(serviceAgreementExternalIds);
        assertThat(participantsPerServiceAgreement, hasSize(0));

        verify(persistenceServiceAgreementService, never())
            .listParticipantsByExternalServiceAgreementIds(any());
    }

    @Test
    public void shouldReturnEmptySetOfParticipantIdsWhenServiceAgreementParticipantIdInvalid() {
        String serviceAgreementExternalId = "invalidId";

        List<PersistenceExtendedParticipant> response = Collections.emptyList();

        when(persistenceServiceAgreementService.
            listParticipantsByExternalServiceAgreementIds(Sets.newHashSet(serviceAgreementExternalId)))
            .thenReturn(response);

        Set<String> sharingAccountsParticipantIds = agreementsPersistenceService
            .getSharingAccountsParticipantIdsForServiceAgreement(serviceAgreementExternalId);

        assertThat(sharingAccountsParticipantIds, hasSize(0));
    }


}