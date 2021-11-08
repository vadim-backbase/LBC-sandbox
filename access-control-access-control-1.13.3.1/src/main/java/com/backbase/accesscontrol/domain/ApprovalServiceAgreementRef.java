package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "approval_service_agreement_ref")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ApprovalServiceAgreementRef extends AccessControlApproval {

    @Column(name = "service_agreement_id", length = 36)
    private String serviceAgreementId;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = false)
    @JoinColumn(name = "service_agreement_id", referencedColumnName = "id",
        updatable = false, insertable = false)
    private ServiceAgreement serviceAgreement;

    @Override
    public ApprovalAction getApprovalAction() {

        return ApprovalAction.DELETE;
    }

    @Override
    public ApprovalCategory getApprovalCategory() {
        return ApprovalCategory.MANAGE_SERVICE_AGREEMENT;
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
        if (!(o instanceof ApprovalServiceAgreementRef)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ApprovalServiceAgreementRef that = (ApprovalServiceAgreementRef) o;
        return Objects.equals(serviceAgreementId, that.serviceAgreementId);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceAgreementId);
    }
}
