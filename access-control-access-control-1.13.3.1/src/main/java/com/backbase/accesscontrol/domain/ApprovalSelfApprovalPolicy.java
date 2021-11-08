package com.backbase.accesscontrol.domain;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_self_appr_policy",
    uniqueConstraints = {@UniqueConstraint(name = "uq_asap_01", columnNames = {"approval_uc_assign_fg_id", "afp_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalSelfApprovalPolicy {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_uc_assign_fg_id", nullable = false)
    private ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup;

    @ManyToOne
    @JoinColumns(value = {
        @JoinColumn(name = "function_group_id", referencedColumnName = "function_group_id"),
        @JoinColumn(name = "afp_id", referencedColumnName = "afp_id")
    })
    private FunctionGroupItemEntity functionGroupItem;

    @Column(name = "can_self_approve")
    private boolean canSelfApprove;

    @OneToMany(mappedBy = "approvalSelfApprovalPolicy", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
        orphanRemoval = true)
    private Set<ApprovalSelfApprovalPolicyBound> approvalSelfApprovalPolicyBounds = new HashSet<>();

    public void addBounds(Set<ApprovalSelfApprovalPolicyBound> selfApprovalPolicyBounds) {
        for (ApprovalSelfApprovalPolicyBound approvalPolicyBound : selfApprovalPolicyBounds) {
            this.approvalSelfApprovalPolicyBounds.add(approvalPolicyBound);
            approvalPolicyBound.setApprovalSelfApprovalPolicy(this);
        }
    }
}
