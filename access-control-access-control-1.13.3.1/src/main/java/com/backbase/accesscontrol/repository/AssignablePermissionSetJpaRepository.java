package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.APS_PERMISSIONS_EXTENDED;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignablePermissionSetJpaRepository extends JpaRepository<AssignablePermissionSet, Long>,
    AssignablePermissionSetJpaCustomRepository {

    @EntityGraph(value = APS_PERMISSIONS_EXTENDED, type = EntityGraphType.FETCH)
    List<AssignablePermissionSet> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String apsName);

    Optional<AssignablePermissionSet> findByName(String name);

    @EntityGraph(value = APS_PERMISSIONS_EXTENDED, type = EntityGraphType.FETCH)
    List<AssignablePermissionSet> findAll();

    @EntityGraph(value = APS_PERMISSIONS_EXTENDED, type = EntityGraphType.FETCH)
    Set<AssignablePermissionSet> findAllByAssignedAsAdminToServiceAgreement(String serviceAgreementId);

    Set<AssignablePermissionSet> findAllByNameIn(Collection<String> names);

    Set<AssignablePermissionSet> findAllByIdIn(Collection<Long> ids);

    Optional<AssignablePermissionSet> findFirstByType(int type);
}
