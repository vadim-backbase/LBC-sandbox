package com.backbase.accesscontrol.repository.impl;

import com.backbase.accesscontrol.domain.ApprovalSelfApprovalPolicy_;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup_;
import com.backbase.accesscontrol.domain.ApprovalUserContext_;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepositoryCustom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.springframework.util.StringUtils;

public class ApprovalUserContextJpaRepositoryImpl implements ApprovalUserContextJpaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<ApprovalUserContext> findByApprovalIdWithFunctionAndDataGroupsAndSelfApprovalPolicies(String approvalId) {

        if (StringUtils.isEmpty(approvalId)) {
            return Optional.empty();
        }
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ApprovalUserContext> cq = criteriaBuilder.createQuery(ApprovalUserContext.class);
        Root<ApprovalUserContext> approvalUserAccessRoot = cq.from(ApprovalUserContext.class);

        Fetch<ApprovalUserContext, ApprovalUserContextAssignFunctionGroup> userAssignedFunctionGroupsFetch =
            approvalUserAccessRoot
                .fetch(ApprovalUserContext_.approvalUserContextAssignFunctionGroups, JoinType.LEFT);

        userAssignedFunctionGroupsFetch
                .fetch(ApprovalUserContextAssignFunctionGroup_.approvalSelfApprovalPolicies, JoinType.LEFT)
                .fetch(ApprovalSelfApprovalPolicy_.approvalSelfApprovalPolicyBounds, JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.equal(approvalUserAccessRoot.get("approvalId"), approvalId));

        cq.select(approvalUserAccessRoot).where(predicates.toArray(new Predicate[]{}));

        try {
            return Optional.of(entityManager.createQuery(cq)
                .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
