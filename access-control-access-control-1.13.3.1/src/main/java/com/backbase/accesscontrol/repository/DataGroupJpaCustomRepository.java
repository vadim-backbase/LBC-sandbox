package com.backbase.accesscontrol.repository;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_EXTENDED;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;

public interface DataGroupJpaCustomRepository {

    Optional<DataGroup> findById(String id, String entityGraphName);

    List<DataGroup> findAllDataGroupsWithIdsIn(Collection<String> ids, String queryGraph);

    @Transactional(readOnly = true)
    List<DataGroup> findAllDataGroupsWithExternalServiceAgreementIdsIn(Collection<String> ids, String queryGraph);

    @EntityGraph(value = DATA_GROUP_EXTENDED, type = EntityGraph.EntityGraphType.FETCH)
    List<DataGroup> findByServiceAgreementIdAndNameInOrByServiceAgreementIdAndIdIn(String serviceAgreement,
        List<String> dataGroupNames, List<String> ids);

    List<DataGroup> findAllDataGroupsByServiceAgreementAndDataItem(String type, String serviceAgreementId,
        String serviceAgreementName, String serviceAgreementExternalId, String dataItemId, String leExternalId,
        SharesEnum shares);

    Optional<DataGroup> findByServiceAgreementExternalIdAndName(String externalServiceAgreementId, String name,
        String entityGraphName);

    List<DataGroup> findByServiceAgreementId(String serviceAgreementId, String entityGraphName);

    List<DataGroup> findByServiceAgreementIdAndDataItemType(String serviceAgreementId, String dataItemType,
        String entityGraphName);
}
