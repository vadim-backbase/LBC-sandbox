package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAgreementAdminJpaRepository extends JpaRepository<ServiceAgreementAdmin, String> {

    boolean existsByParticipantServiceAgreement(ServiceAgreement serviceAgreement);
}
