package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY;
import static com.backbase.accesscontrol.matchers.ServiceAgreementMatcher.getServiceAgreementMatcher;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;


public class ParticipantJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private ParticipantJpaRepository participantJpaRepository;

    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;

    @PersistenceContext
    private EntityManager entityManager;

    private LegalEntity legalEntity;

    private PersistenceUtil persistenceUtil;


    @Before
    public void setUp() throws Exception {
        repositoryCleaner.clean();

        persistenceUtil = Persistence.getPersistenceUtil();
        legalEntity = createLegalEntity(null, "Backbase", "ex", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        flushAndClear();
    }

    @Test
    @Transactional
    public void testFindByServiceAgreementExternalIdAndLegalEntityExternalId() {
        String userId = "USER-001";

        ServiceAgreement serviceAgreement = createServiceAgreement("SA name", "id.external", "SA desc", legalEntity,
            legalEntity.getId(), null);
        Participant consumer1 = new Participant();
        serviceAgreement.addParticipant(consumer1, legalEntity.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClear();

        ServiceAgreement persistedServiceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId()).orElse(null);
        Participant participant = persistedServiceAgreement.getParticipants().get(this.legalEntity.getId());
        participant.addAdmin(userId);

        serviceAgreementJpaRepository.save(persistedServiceAgreement);
        flushAndClear();

        Optional<Participant> participants = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId("id.external", legalEntity.getExternalId());
        flushAndClear();
        assertThat(participants.get().getServiceAgreement(),
            getServiceAgreementMatcher(equalTo(serviceAgreement.getId()), equalTo(serviceAgreement.getName()),
                equalTo(serviceAgreement.getDescription()), notNullValue()));
        LegalEntity le = participants.get().getLegalEntity();
        assertEquals(legalEntity.getExternalId(), le.getExternalId());
        assertEquals(legalEntity.getId(), le.getId());
        assertEquals(legalEntity.getName(), le.getName());
    }

    @Test
    @Transactional
    public void testDeleteParticipant() {
        String userId = "USER-001";

        ServiceAgreement serviceAgreement = createServiceAgreement("SA name", "id.external", "SA desc", legalEntity,
            legalEntity.getId(), null);
        Participant consumer1 = new Participant();
        serviceAgreement.addParticipant(consumer1, legalEntity.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClear();

        ServiceAgreement persistedServiceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId()).orElse(null);
        Participant participant = persistedServiceAgreement.getParticipants().get(this.legalEntity.getId());
        participant.addAdmin(userId);

        serviceAgreementJpaRepository.save(persistedServiceAgreement);
        flushAndClear();

        participantJpaRepository.delete(participant);
        flushAndClear();

        Optional<Participant> deletedParticipant = participantJpaRepository
            .findByServiceAgreementExternalIdAndLegalEntityExternalId("id.external", legalEntity.getExternalId());

        assertFalse(deletedParticipant.isPresent());
    }

    @Test
    @Transactional
    public void shouldLoadServiceAgreementWithCreator() throws Exception {
        String userId = "USER-001";

        ServiceAgreement serviceAgreement = createServiceAgreement("SA name", "id.external", "SA desc", legalEntity,
            legalEntity.getId(), null);
        Participant consumer1 = new Participant();
        serviceAgreement.addParticipant(consumer1, legalEntity.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement);
        flushAndClear();

        ServiceAgreement persistedServiceAgreement = serviceAgreementJpaRepository
            .findById(serviceAgreement.getId()).orElse(null);
        Participant participant1 = persistedServiceAgreement.getParticipants().get(this.legalEntity.getId());
        participant1.addAdmin(userId);

        serviceAgreementJpaRepository.save(persistedServiceAgreement);
        flushAndClear();

        List<Participant> participants = participantJpaRepository
            .findByServiceAgreementExternalIdAndServiceAgreementIsMasterFalse(serviceAgreement.getExternalId());
        flushAndClear();

        assertEquals(1, participants.size());
        Participant participant = participants.get(0);
        assertTrue(persistenceUtil.isLoaded(participant, "serviceAgreement"));
        assertTrue(persistenceUtil.isLoaded(participant.getServiceAgreement(), "creatorLegalEntity"));
        assertFalse(persistenceUtil.isLoaded(participant.getServiceAgreement(), "additions"));
        assertThat(participant.getServiceAgreement(),
            getServiceAgreementMatcher(equalTo(serviceAgreement.getId()), equalTo(serviceAgreement.getName()),
                equalTo(serviceAgreement.getDescription()), equalTo(legalEntity)));
    }

    @Test
    @Transactional
    public void shouldFindParticipantsByServiceAgreementId() throws Exception {
        LegalEntity legalEntityCreator = createLegalEntity(null, "name1", "external1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityCreator);
        LegalEntity legalEntityProviderAndConsumer = createLegalEntity(null, "name2", "external", null,
            LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityProviderAndConsumer);
        LegalEntity legalEntityConsumer = createLegalEntity(null, "name3", "external3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityConsumer);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "SA desc", legalEntityCreator,
            legalEntityProviderAndConsumer.getId(), legalEntityProviderAndConsumer.getId());
        serviceAgreement.addParticipant(new Participant(), legalEntityConsumer.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement);

        List<Participant> byServiceAgreementId = participantJpaRepository
            .findByServiceAgreementId(serviceAgreement.getId(), PARTICIPANT_WITH_LEGAL_ENTITY);

        assertEquals(2, byServiceAgreementId.size());

        // Returns the legalEntityProviderAndConsumer two times, one time as provider, one time as consumer
        List<String> participantIds = byServiceAgreementId.stream()
            .map(Participant::getLegalEntity)
            .map(LegalEntity::getId)
            .collect(Collectors.toList());

        assertTrue(
            participantIds.containsAll(asList(legalEntityConsumer.getId(), legalEntityProviderAndConsumer.getId())));
    }

    @Test
    @Transactional
    public void shouldListParticipantsByExternalServiceAgreementIds() throws Exception {
        LegalEntity legalEntityCreator = createLegalEntity(null, "name1", "external1", null, LegalEntityType.BANK);
        LegalEntity legalEntityProviderAndConsumer = createLegalEntity(null, "name2", "external", null,
            LegalEntityType.BANK);
        LegalEntity legalEntityConsumer = createLegalEntity(null, "name3", "external3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntityCreator);
        legalEntityJpaRepository.save(legalEntityProviderAndConsumer);
        legalEntityJpaRepository.save(legalEntityConsumer);

        ServiceAgreement serviceAgreement = createServiceAgreement("name", "id.external", "SA desc", legalEntityCreator,
            legalEntityProviderAndConsumer.getId(), legalEntityProviderAndConsumer.getId());
        serviceAgreement.addParticipant(new Participant(), legalEntityConsumer.getId(), false, true);
        serviceAgreementJpaRepository.save(serviceAgreement);

        Set<String> ids = new HashSet<>();
        ids.add("id.external");
        List<Participant> participantsByExSaIds = participantJpaRepository
            .findAllParticipantsWithExternalServiceAgreementIdsIn(ids,
                "graph.Participant.withLegalEntityAndServiceAgreementCreator");

        assertEquals(2, participantsByExSaIds.size());

        List<String> participantIds = participantsByExSaIds.stream()
            .map(Participant::getLegalEntity)
            .map(LegalEntity::getId)
            .collect(Collectors.toList());

        assertTrue(
            participantIds.containsAll(asList(legalEntityConsumer.getId(), legalEntityProviderAndConsumer.getId())));

    }

    private PageRequest getPagebleObjWithoutSortingObject(int from, int size) {
        return PageRequest.of(from, size);
    }

    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}