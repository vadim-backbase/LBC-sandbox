package com.backbase.accesscontrol.domain;

import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;

import java.util.HashSet;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "user_assigned_fg_combination")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserAssignedFunctionGroupCombination {

    @Id
    @GenericGenerator(
        name = "userAssignedFunctionGroupCombinationGenerator",
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
                name = "segment_value",
                value = "SEQ_USER_ASSIGNED_FG_COMBINATION"
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
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "userAssignedFunctionGroupCombinationGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = EAGER)
    @JoinColumn(name = "user_assigned_fg_id", updatable = false, nullable = false)
    private UserAssignedFunctionGroup userAssignedFunctionGroup;

    @ElementCollection(fetch = LAZY)
    @CollectionTable(name = "user_assigned_combination_dg",
        joinColumns = {@JoinColumn(name = "ua_fg_combination_id ")})
    @Column(name = "data_group_id")
    private Set<String> dataGroupIds = new HashSet<>();

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "user_assigned_combination_dg",
        joinColumns = {@JoinColumn(name = "ua_fg_combination_id ")},
        inverseJoinColumns = {@JoinColumn(name = "data_group_id")}
    )
    private Set<DataGroup> dataGroups = new HashSet<>();
    
    @OneToMany(mappedBy = "userAssignedFunctionGroupCombination", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
        orphanRemoval = true)
    private Set<SelfApprovalPolicy> selfApprovalPolicies = new HashSet<>();
    
    public void addPolicies(Set<SelfApprovalPolicy> policies) {
        for (SelfApprovalPolicy policy : policies) {
            this.selfApprovalPolicies.add(policy);
            policy.setUserAssignedFunctionGroupCombination(this);
        }
    }

    /**
     * Custom constructor.
     *
     * @param dataGroupsIds             data group ids
     * @param userAssignedFunctionGroup {@link UserAssignedFunctionGroup}
     */
    public UserAssignedFunctionGroupCombination(Set<String> dataGroupsIds,
        UserAssignedFunctionGroup userAssignedFunctionGroup) {
        this.dataGroupIds = dataGroupsIds;
        this.userAssignedFunctionGroup = userAssignedFunctionGroup;
    }

    public UserAssignedFunctionGroupCombination(Set<String> dataGroupsIds,
        UserAssignedFunctionGroup userAssignedFunctionGroup,
        Set<SelfApprovalPolicy> selfApprovalPolicies) {
        this.dataGroupIds = dataGroupsIds;
        this.userAssignedFunctionGroup = userAssignedFunctionGroup;
        this.selfApprovalPolicies = selfApprovalPolicies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAssignedFunctionGroupCombination that = (UserAssignedFunctionGroupCombination) o;

        return Objects.equals(id, that.id)
            || Objects.equals(userAssignedFunctionGroup, that.userAssignedFunctionGroup)
            && Objects.equals(dataGroupIds, that.dataGroupIds);
    }

    @Override
    public int hashCode() {
        if (Objects.nonNull(id)) {
            return Objects.hash(id);
        }
        return Objects.hash(userAssignedFunctionGroup, dataGroupIds);
    }
}
