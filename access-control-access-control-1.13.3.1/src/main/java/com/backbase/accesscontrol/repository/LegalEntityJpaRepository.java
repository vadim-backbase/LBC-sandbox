package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.GRAPH_LEGAL_ENTITY_WITH_ADDITIONS;

import com.backbase.accesscontrol.domain.EntityIds;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LegalEntityJpaRepository extends JpaRepository<LegalEntity, String>, LegalEntityJpaRepositoryCustom {

    @EntityGraph(value = "graph.LegalEntity.withChildrenAndParent", type = EntityGraph.EntityGraphType.LOAD)
    List<LegalEntity> findDistinctByParentId(String parentId);

    @EntityGraph(value = GRAPH_LEGAL_ENTITY_WITH_ADDITIONS, type = EntityGraph.EntityGraphType.LOAD)
    List<LegalEntity> findDistinctByParentIsNull();

    List<IdProjection> findByLegalEntityAncestorsIdAndIdIn(String legalEntityId, Collection<String> participants);

    @Query("SELECT count(ancestor_id) FROM LegalEntityAncestor where  ancestorId in ?1 and descendentId=?2")
    int countByLegalEntityAncestorsIdInAndId(Collection<String> participants, String legalEntityId);

    List<LegalEntity> findAllByParentIdAndType(String id, LegalEntityType legalEntityType);

    Optional<LegalEntity> findByExternalIdIgnoreCaseAndIdNot(String externalId, String id);

    boolean existsByExternalId(String externalId);

    Optional<LegalEntity> findByExternalId(String externalId);

    boolean existsByParentIsNull();

    List<IdProjection> findByLegalEntityAncestorsId(String id);

    @Override
    @EntityGraph(value = GRAPH_LEGAL_ENTITY_WITH_ADDITIONS, type = EntityGraph.EntityGraphType.LOAD)
    Optional<LegalEntity> findById(String id);

    List<LegalEntity> findByParentId(String parentId, Pageable limit);

    List<EntityIds> findByExternalIdIn(Collection<String> ids);

}
