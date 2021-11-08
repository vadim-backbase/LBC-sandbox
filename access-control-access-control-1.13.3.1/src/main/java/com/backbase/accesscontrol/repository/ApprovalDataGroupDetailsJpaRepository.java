package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.APPROVAL_DATA_GROUP_WITH_ITEMS;

import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDataGroupDetailsJpaRepository extends JpaRepository<ApprovalDataGroupDetails, Long> {

    boolean existsByNameAndServiceAgreementId(String name, String serviceAgreementId);

    List<ApprovalDataGroupDetails> findAllByServiceAgreementIdIn(Collection<String> serviceAgreementIds);

    List<ApprovalDataGroupDetails> findByNameAndServiceAgreementId(String name,
        String serviceAgreementId);

    @EntityGraph(value = APPROVAL_DATA_GROUP_WITH_ITEMS, type = EntityGraph.EntityGraphType.FETCH)
    List<ApprovalDataGroupDetails> findAllByServiceAgreementIdAndType(String serviceAgreementId, String type);

    Boolean existsByServiceAgreementId(String serviceAgreementId);
}
