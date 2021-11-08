package com.backbase.accesscontrol.repository.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.DataGroup_;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.domain.FunctionGroupItem_;
import com.backbase.accesscontrol.domain.FunctionGroup_;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.ServiceAgreement_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination_;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup_;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.UserContext_;
import com.backbase.accesscontrol.domain.dto.DataGroupWithApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.repository.UserAssignedCombinationRepositoryCustom;
import com.backbase.accesscontrol.service.TimeZoneConverterService;
import com.backbase.accesscontrol.util.DateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * UserAssignedFunctionGroupDataGroupRepositoryCustom implementation.
 */
public class UserAssignedCombinationRepositoryImpl implements
    UserAssignedCombinationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TimeZoneConverterService timeZoneConverterService;

    /**
     * Find all user data items privileges.
     *
     * @param userId                         user id
     * @param serviceAgreementId             service agreement id
     * @param dataItemType                   data group type
     * @param applicableFunctionPrivilegeIds set of ids
     * @return list of {@link DataGroupWithApplicableFunctionPrivilege}
     */
    @Override
    public List<DataGroupWithApplicableFunctionPrivilege> findAllUserDataItemsPrivileges(String userId,
        String serviceAgreementId, String dataItemType, Set<String> applicableFunctionPrivilegeIds) {

        if (isEmpty(applicableFunctionPrivilegeIds)) {
            return Collections.emptyList();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DataGroupWithApplicableFunctionPrivilege> query = criteriaBuilder
            .createQuery(DataGroupWithApplicableFunctionPrivilege.class);
        Root<UserAssignedFunctionGroup> root = query.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> joinUserAssignedCombinations =
            root.join(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations);
        Join<UserAssignedFunctionGroupCombination, DataGroup> joinDataGroup = joinUserAssignedCombinations
            .join(
                UserAssignedFunctionGroupCombination_.DATA_GROUPS);

        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP);
        Join<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup.join(FunctionGroup_.PERMISSIONS);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        predicates
            .add(criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        predicates
            .add(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(applicableFunctionPrivilegeIds));

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinServiceAgreement.get("startDate"), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinServiceAgreement.get("endDate"), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinFunctionGroup.get("startDate"), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get("endDate"), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        if (dataItemType != null) {
            predicates.add(
                criteriaBuilder.equal(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE), dataItemType));
        }

        query.multiselect(joinDataGroup.get(DataGroup_.ID), joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE),
            joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID))
            .where(predicates.toArray(new Predicate[]{}));

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataGroup> findByUserIdAndServiceAgreementIdAndAfpIdsInAndDataType(
        String userId, String serviceAgreementId, Set<String> applicableFunctionPrivilegeIds, String dataItemType) {

        if (isEmpty(applicableFunctionPrivilegeIds)) {
            return Collections.emptyList();
        }

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<DataGroup> cq = criteriaBuilder.createQuery(DataGroup.class);

        Root<UserAssignedFunctionGroup> root = cq.from(UserAssignedFunctionGroup.class);
        Join<UserAssignedFunctionGroup, UserAssignedFunctionGroupCombination> joinUserAssignedCombination =
            root.join(UserAssignedFunctionGroup_.userAssignedFunctionGroupCombinations);
        Join<UserAssignedFunctionGroupCombination, DataGroup> joinDataGroup = joinUserAssignedCombination
            .join(
                UserAssignedFunctionGroupCombination_.DATA_GROUPS);
        Join<UserAssignedFunctionGroup, UserContext> joinUserContext = root
            .join(UserAssignedFunctionGroup_.USER_CONTEXT, JoinType.INNER);
        Join<UserAssignedFunctionGroup, FunctionGroup> joinFunctionGroup = root
            .join(UserAssignedFunctionGroup_.FUNCTION_GROUP);
        Join<FunctionGroup, FunctionGroupItem> joinItem = joinFunctionGroup.join(FunctionGroup_.PERMISSIONS);
        Join<UserContext, ServiceAgreement> joinServiceAgreement = joinUserContext
            .join(UserContext_.SERVICE_AGREEMENT, JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates
            .add(joinItem.get(FunctionGroupItem_.APPLICABLE_FUNCTION_PRIVILEGE_ID).in(applicableFunctionPrivilegeIds));
        predicates.add(
            criteriaBuilder.equal(joinDataGroup.get(DataGroup_.DATA_ITEM_TYPE), dataItemType));
        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.and(
                    criteriaBuilder.and(criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder
                            .coalesce(joinServiceAgreement.get(ServiceAgreement_.START_DATE), DateUtil.MIN_DATE),
                        criteriaBuilder
                            .coalesce(joinServiceAgreement.get(ServiceAgreement_.END_DATE), DateUtil.MAX_DATE)
                    ), criteriaBuilder.between(
                        criteriaBuilder.literal(timeZoneConverterService.getCurrentTime()),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.START_DATE), DateUtil.MIN_DATE),
                        criteriaBuilder.coalesce(joinFunctionGroup.get(FunctionGroup_.END_DATE), DateUtil.MAX_DATE)
                    )),
                    criteriaBuilder
                        .equal(joinServiceAgreement.get(ServiceAgreement_.STATE), ServiceAgreementState.ENABLED)
                ),
                criteriaBuilder.equal(joinFunctionGroup.get(FunctionGroup_.type), FunctionGroupType.SYSTEM)
            ));

        if (nonNull(userId)) {
            predicates.add(criteriaBuilder.equal(joinUserContext.get(UserContext_.USER_ID), userId));
        }

        if (nonNull(serviceAgreementId)) {
            predicates
                .add(criteriaBuilder.equal(joinUserContext.get(UserContext_.SERVICE_AGREEMENT_ID), serviceAgreementId));
        }

        cq.where(predicates.toArray(new Predicate[]{}))
            .distinct(true)
            .select(joinDataGroup);
        return entityManager.createQuery(cq).getResultList();
    }

}
