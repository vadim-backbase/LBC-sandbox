package com.backbase.accesscontrol.repository;

import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.persistence.Tuple;
import javax.persistence.metamodel.SingularAttribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceAgreementJpaCustomRepository {

    Page<ServiceAgreement> findAllServiceAgreementsByParameters(String name, String creatorId,
        SearchAndPaginationParameters searchAndPaginationParameters, String entityGraph);

    Optional<ServiceAgreement> findById(String id, String entityGraph);


    Page<ServiceAgreement> findByCreatorIdInHierarchyAndParameters(String creatorId, UserParameters userParameters,
        SearchAndPaginationParameters searchAndPaginationParameters, String entityGraphName);

    Optional<ServiceAgreement> findByCreatorLegalEntityIdAndIsMaster(String legalEntityId, boolean isMaster,
        String entityGraph);

    Page<ServiceAgreement> findServiceAgreementsWhereUserHasPermissions(String userId, String query, Pageable pageable);

    boolean existContextForUserIdAndServiceAgreementId(String userId, String serviceAgreementId);

    Optional<ServiceAgreement> findByExternalId(String externalId, String entityGraphName);

    Boolean checkIfExistsUsersWithAssignedPermissionsInServiceAgreement(
        ServiceAgreement serviceAgreement);

    Boolean checkIsExistsUsersWithPendingPermissionsInServiceAgreement(String serviceAgreementId);

    Page<ServiceAgreement> getServiceAgreementByPermissionSetId(
        AssignablePermissionSet assignablePermissionSet,
        SearchAndPaginationParameters searchAndPaginationParameters);

    List<ServiceAgreement> getServiceAgreementsByIds(Collection<String> ids,
        SingularAttribute<ServiceAgreement, String> orderedBy, String entityGraph);

    /**
     * Return service agreement id with data group id, data item id and applicable function privilege by user id, data
     * group type and applicable function privilege ids.
     *
     * @param userId        user id
     * @param dataGroupType data group type
     * @param afpIds        applicable function privilege ids
     * @return {@link Tuple} of service agreement id, data group id, data item id, applicable function privilege id
     */
    List<Tuple> findByUserIdAndDataGroupTypeAndAfpIdsIn(String userId, String dataGroupType,
        Collection<String> afpIds);
}
