package com.backbase.accesscontrol.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.Participant;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.util.helpers.RepositoryCleaner;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class ServiceAgreementAdminJpaRepositoryIT extends TestRepositoryContext {

    @Autowired
    private ServiceAgreementAdminJpaRepository testy;
    @Autowired
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;
    @Autowired
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @Autowired
    private RepositoryCleaner repositoryCleaner;


    @Before
    public void init(){
        repositoryCleaner.clean();
    }

    @Test
    @Transactional
    public void shouldExistAdminInServiceAgreement() {
        LegalEntity legalEntity = legalEntityJpaRepository.save(new LegalEntity()
            .withName("le-name")
            .withExternalId("le-ext-id")
            .withType(LegalEntityType.CUSTOMER));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("sa-name")
            .withDescription("sa-description")
            .withCreatorLegalEntity(legalEntity)
            .withExternalId("sa-ext-id")
            .withMaster(true);
        Participant participant = new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(legalEntity);
        participant.addAdmin("user-id");
        serviceAgreement.addParticipant(participant);

        serviceAgreementJpaRepository.save(serviceAgreement);

        assertTrue(testy.existsByParticipantServiceAgreement(serviceAgreement));
    }

    @Test
    @Transactional
    public void shouldNotExistAdminInServiceAgreement() {
        LegalEntity legalEntity = legalEntityJpaRepository.save(new LegalEntity()
            .withName("le-name")
            .withExternalId("le-ext-id")
            .withType(LegalEntityType.CUSTOMER));

        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withName("sa-name")
            .withDescription("sa-description")
            .withCreatorLegalEntity(legalEntity)
            .withExternalId("sa-ext-id")
            .withMaster(true);
        Participant participant = new Participant()
            .withShareUsers(true)
            .withShareAccounts(true)
            .withLegalEntity(legalEntity);
        serviceAgreement.addParticipant(participant);

        serviceAgreementJpaRepository.save(serviceAgreement);

        assertFalse(testy.existsByParticipantServiceAgreement(serviceAgreement));
    }

}
