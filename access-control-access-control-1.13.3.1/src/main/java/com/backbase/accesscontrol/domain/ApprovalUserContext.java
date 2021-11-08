package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Entity
@Table(name = "approval_user_context")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
public class ApprovalUserContext extends AccessControlApproval {

    @Column(name = "user_id", updatable = false, nullable = false, length = 36)
    private String userId;

    @Column(name = "service_agreement_id", updatable = false, nullable = false, length = 36)
    private String serviceAgreementId;

    @Column(name = "legal_entity_id", updatable = false, nullable = false, length = 36)
    private String legalEntityId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "approvalUserContext", cascade = CascadeType.ALL,
        orphanRemoval = true)
    private Set<ApprovalUserContextAssignFunctionGroup> approvalUserContextAssignFunctionGroups =
        new LinkedHashSet<>();

    public void addApprovalUserContextAssignFunctionGroups(
        Set<ApprovalUserContextAssignFunctionGroup> assignFunctionGroups) {
        for (ApprovalUserContextAssignFunctionGroup assignFunctionGroup : assignFunctionGroups) {
            this.approvalUserContextAssignFunctionGroups.add(assignFunctionGroup);
            assignFunctionGroup.setApprovalUserContext(this);
        }
    }

    /**
     * Proper equals method.
     *
     * @param o - object for comparision
     * @return true/false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ApprovalUserContext that = (ApprovalUserContext) o;
        return Objects.equals(userId, that.userId)
            && Objects.equals(serviceAgreementId, that.serviceAgreementId)
            && Objects.equals(legalEntityId, that.legalEntityId);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userId, serviceAgreementId, legalEntityId);
    }

    /**
     * Get approval action.
     *
     * @return {@link ApprovalAction}
     */
    @Override
    public ApprovalAction getApprovalAction() {
        return ApprovalAction.EDIT;
    }

    /**
     * Get approval category.
     *
     * @return {@link ApprovalCategory}
     */
    @Override
    public ApprovalCategory getApprovalCategory() {
        return ApprovalCategory.ASSIGN_PERMISSIONS;
    }

    /**
     * Custom constructor.
     *
     * @param approvalId - approval id parameter
     * @return {@link ApprovalUserContext}
     */
    @Override
    public ApprovalUserContext withApprovalId(String approvalId) {
        return (ApprovalUserContext) super.withApprovalId(approvalId);
    }
}
