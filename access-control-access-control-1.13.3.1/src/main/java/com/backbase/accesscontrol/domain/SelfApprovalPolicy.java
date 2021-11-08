package com.backbase.accesscontrol.domain;

import java.util.stream.Collectors;
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
@Table(name = "self_appr_policy",
    uniqueConstraints = {@UniqueConstraint(name = "uq_sap_01", columnNames = {"ua_fg_combination_id", "afp_id"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelfApprovalPolicy {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ua_fg_combination_id", referencedColumnName = "id", nullable = false)
    private UserAssignedFunctionGroupCombination userAssignedFunctionGroupCombination;

    @ManyToOne
    @JoinColumns(value = {
        @JoinColumn(name = "function_group_id", referencedColumnName = "function_group_id"),
        @JoinColumn(name = "afp_id", referencedColumnName = "afp_id")
    })
    private FunctionGroupItemEntity functionGroupItem;

    @Column(name = "can_self_approve")
    private boolean canSelfApprove;

    @OneToMany(mappedBy = "selfApprovalPolicy", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SelfApprovalPolicyBound> approvalPolicyBounds = new HashSet<>();

    public SelfApprovalPolicy(SelfApprovalPolicy selfApprovalPolicy) {
        this.functionGroupItem = selfApprovalPolicy.getFunctionGroupItem();
        this.canSelfApprove = selfApprovalPolicy.isCanSelfApprove();
        Set<SelfApprovalPolicyBound> bounds = selfApprovalPolicy.getApprovalPolicyBounds().stream()
            .map(SelfApprovalPolicyBound::new)
            .collect(Collectors.toSet());
        addBounds(bounds);
    }

    public void addBounds(Set<SelfApprovalPolicyBound> bounds) {
        for (SelfApprovalPolicyBound bound : bounds) {
            this.approvalPolicyBounds.add(bound);
            bound.setSelfApprovalPolicy(this);
        }
    }
}
