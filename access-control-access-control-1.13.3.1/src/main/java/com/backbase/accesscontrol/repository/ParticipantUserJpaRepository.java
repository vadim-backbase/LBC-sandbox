package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.ParticipantUser;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantUserJpaRepository extends JpaRepository<ParticipantUser, String> {

    boolean existsByUserIdAndParticipantServiceAgreement(String userId, ServiceAgreement serviceAgreement);
}
