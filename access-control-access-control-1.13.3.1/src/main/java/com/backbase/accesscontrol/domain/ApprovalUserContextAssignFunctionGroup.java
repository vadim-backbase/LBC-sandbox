package com.backbase.accesscontrol.domain;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "approval_uc_assign_fg")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
public class ApprovalUserContextAssignFunctionGroup {

    @Id
    @GenericGenerator(
        name = "approvalGenerator",
        strategy = "enhanced-table",
        parameters = {
            @Parameter(
                name = "table_name",
                value = "sequence_table"
            ),
            @Parameter(
                name = "optimizer",
                value = "pooled-lo"
            ),
            @Parameter(
                name = "initial_value",
                value = "1"
            ),
            @Parameter(
                name = "increment_size",
                value = "1000"
            )
        })
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "approvalGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "function_group_id", updatable = false, nullable = false, length = 36)
    private String functionGroupId;

    @JoinColumn(name = "approval_user_context_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ApprovalUserContext approvalUserContext;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "approval_uc_assign_fg_dg", joinColumns = {@JoinColumn(name = "approval_uc_assign_fg_id")})
    @Column(name = "data_group_id", length = 36)
    private Set<String> dataGroups = new LinkedHashSet<>();
    
    @OneToMany(mappedBy = "approvalUserContextAssignFunctionGroup", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
        orphanRemoval = true)
    private Set<ApprovalSelfApprovalPolicy> approvalSelfApprovalPolicies = new HashSet<>();

    public void addPolicies(Set<ApprovalSelfApprovalPolicy> selfApprovalPolicies) {
        for (ApprovalSelfApprovalPolicy approvalSelfApprovalPolicy : selfApprovalPolicies) {
            this.approvalSelfApprovalPolicies.add(approvalSelfApprovalPolicy);
            approvalSelfApprovalPolicy.setApprovalUserContextAssignFunctionGroup(this);
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
        ApprovalUserContextAssignFunctionGroup that = (ApprovalUserContextAssignFunctionGroup) o;
        return Objects.equals(functionGroupId, that.functionGroupId)
            && Objects.equals(approvalUserContext, that.approvalUserContext)
            && Objects.equals(dataGroups, that.dataGroups);
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(functionGroupId, approvalUserContext, dataGroups);
    }
}
