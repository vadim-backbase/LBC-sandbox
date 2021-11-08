package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;
import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_WITH_ITEMS;

import com.backbase.accesscontrol.domain.DataGroup;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataGroupJpaRepository extends JpaRepository<DataGroup, String>, DataGroupJpaCustomRepository {

    @EntityGraph(value = DATA_GROUP_EXTENDED, type = EntityGraph.EntityGraphType.FETCH)
    List<DataGroup> findDistinctByNameAndServiceAgreementId(String dataGroupName, String dataGroupServiceAgreementId);

    @EntityGraph(value = DATA_GROUP_EXTENDED, type = EntityGraph.EntityGraphType.FETCH)
    List<DataGroup> findByServiceAgreementId(String dataGroupServiceAgreementId);

    List<DataGroup> findDistinctByNameAndServiceAgreementIdAndIdNot(
        String dataGroupName,
        String dataGroupServiceAgreementId,
        String dataGroupId);

    @EntityGraph(value = DATA_GROUP_WITH_ITEMS, type = EntityGraph.EntityGraphType.FETCH)
    List<DataGroup> findByIdIn(Collection<String> dataGroupIds);

    Boolean existsByDataItemTypeAndDataItemIds(String type, String dataItemId);

    Boolean existsByServiceAgreementId(String serviceAgreementId);
}
