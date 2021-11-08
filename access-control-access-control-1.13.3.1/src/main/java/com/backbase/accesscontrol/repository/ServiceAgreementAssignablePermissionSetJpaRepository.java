package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.PermissionSetsInServiceAgreements;
import com.backbase.accesscontrol.domain.idclass.ServiceAgreementAssignablePermissionSetIdClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceAgreementAssignablePermissionSetJpaRepository extends
    JpaRepository<PermissionSetsInServiceAgreements,
        ServiceAgreementAssignablePermissionSetIdClass> {

    boolean existsByAssignablePermissionSetId(Long permissionSet);
}
