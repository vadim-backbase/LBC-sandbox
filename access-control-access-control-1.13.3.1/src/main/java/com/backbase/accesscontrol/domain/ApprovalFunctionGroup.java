package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "approval_function_group")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ApprovalFunctionGroup extends ApprovalFunctionGroupRef {

    @Column(name = "service_agreement_id", nullable = false, length = 36)
    private String serviceAgreementId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "approval_type_id", length = 36)
    private String approvalTypeId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "approval_function_group_item",
        joinColumns = {@JoinColumn(name = "id")})
    @Column(name = "afp_id", nullable = false, length = 36)
    private Set<String> privileges = new HashSet<>();

    @Override
    public ApprovalAction getApprovalAction() {

        return Objects.nonNull(getFunctionGroupId()) ? ApprovalAction.EDIT : ApprovalAction.CREATE;
    }

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
        ApprovalFunctionGroup that = (ApprovalFunctionGroup) o;
        return serviceAgreementId.equals(that.serviceAgreementId)
            && name.equals(that.name)
            && description.equals(that.description)
            && Objects.equals(approvalTypeId, that.approvalTypeId)
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(privileges, that.privileges);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(super.hashCode(), serviceAgreementId, name, description, approvalTypeId, startDate, endDate,
                privileges);
    }
}
