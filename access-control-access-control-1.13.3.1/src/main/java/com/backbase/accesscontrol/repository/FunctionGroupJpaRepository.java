package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.FUNCTION_GROUP_WITH_GROUPED_FUNCTION_PRIVILEGES;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FunctionGroupJpaRepository extends JpaRepository<FunctionGroup, String>,
    FunctionGroupJpaRepositoryCustom {

    @EntityGraph(value = FUNCTION_GROUP_WITH_GROUPED_FUNCTION_PRIVILEGES, type = EntityGraphType.FETCH)
    List<FunctionGroup> findByServiceAgreementAndType(ServiceAgreement serviceAgreement, FunctionGroupType type);

    @Query("select fg from FunctionGroup fg join fetch fg.permissions where fg.id = :id")
    Optional<FunctionGroup> findByIdWithPermissions(@Param("id") String id);

    Optional<FunctionGroup> findByIdAndType(String id, FunctionGroupType type);

    Optional<FunctionGroup> findByIdAndTypeNot(String id, FunctionGroupType type);

    Optional<FunctionGroup> findById(String id);

    List<FunctionGroup> findByIdIn(Collection<String> idList);

    List<FunctionGroup> findByServiceAgreementId(String serviceAgreementId);

    Optional<FunctionGroup> findByNameAndServiceAgreementId(String name, String serviceAgreementId);

    boolean existsByNameAndServiceAgreementIdAndIdNot(String name, String serviceAgreementId, String id);

    boolean existsByNameAndServiceAgreementId(String name, String serviceAgreementId);

    Optional<FunctionGroup> findByServiceAgreementExternalIdAndName(String externalServiceAgreementId,
        String functionGroupName);

    @Query("SELECT fg FROM FunctionGroup fg inner join ServiceAgreement sa on fg.serviceAgreementId = sa.id "
        + "and sa.isMaster = true "
        + "inner join LegalEntityAncestor lea on lea.ancestorId = sa.creatorLegalEntity.id "
        + "where lea.descendentId = ?1 and fg.type = ?3"
        + " and fg.assignablePermissionSetId IN ?2")
    List<FunctionGroup> findByCreatorLegalEntityIdAndAps(String creatorLe, Set<Long> apsIds,
        FunctionGroupType fgType);

}
