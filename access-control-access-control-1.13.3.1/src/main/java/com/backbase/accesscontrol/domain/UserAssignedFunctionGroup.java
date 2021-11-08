package com.backbase.accesscontrol.domain;

import static javax.persistence.FetchType.LAZY;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

@Entity
@Table(name = "user_assigned_function_group",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_user_context_function_group",
        columnNames = {
            "user_context_id",
            "function_group_id"
        }
    )
)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserAssignedFunctionGroup {

    @Id
    @GenericGenerator(
        name = "userAssignedFunctionGroupGenerator",
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
                value = "SEQ_ID_UAFG"
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
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "userAssignedFunctionGroupGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "function_group_id", nullable = false, updatable = false)
    private FunctionGroup functionGroup;

    @Column(name = "function_group_id", nullable = false, updatable = false, insertable = false)
    @Include
    private String functionGroupId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_context_id", nullable = false, updatable = false)
    private UserContext userContext;

    @Column(name = "user_context_id", nullable = false, updatable = false, insertable = false)
    @Include
    private Long userContextId;

    @OneToMany(fetch = LAZY,
        mappedBy = "userAssignedFunctionGroup",
        cascade = {CascadeType.PERSIST,
            CascadeType.MERGE, CascadeType.REMOVE},
        orphanRemoval = true
    )
    private Set<UserAssignedFunctionGroupCombination> userAssignedFunctionGroupCombinations = new HashSet<>();

    public void addCombination(UserAssignedFunctionGroupCombination combination) {
        userAssignedFunctionGroupCombinations.add(combination);
        combination.setUserAssignedFunctionGroup(this);
    }

    /**
     * Constructor with function group and user context.
     *
     * @param functionGroup - {@link FunctionGroup}
     * @param userContext   - {@link UserContext}
     */
    public UserAssignedFunctionGroup(FunctionGroup functionGroup, UserContext userContext) {
        this.functionGroup = functionGroup;
        this.userContext = userContext;
        this.functionGroupId = functionGroup.getId();
        this.userContextId = userContext.getId();
    }

    /**
     * Wither method.
     *
     * @param id long
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withId(Long id) {
        this.id = id;
        return this;
    }

    /**
     * Wither method.
     *
     * @param functionGroup object
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withFunctionGroup(FunctionGroup functionGroup) {
        this.functionGroup = functionGroup;
        return this;
    }

    /**
     * Wither method.
     *
     * @param functionGroupId string
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withFunctionGroupId(String functionGroupId) {
        this.functionGroupId = functionGroupId;
        return this;
    }

    /**
     * Wither method.
     *
     * @param userContext object
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withUserContext(UserContext userContext) {
        this.userContext = userContext;
        return this;
    }

    /**
     * Wither method.
     *
     * @param userContextId long user context id
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withUserContextId(Long userContextId) {
        this.userContextId = userContextId;
        return this;
    }

    /**
     * Wither method.
     *
     * @param userAssignedFunctionGroupCombinations list
     * @return userAssignedFunctionGroup
     */
    public UserAssignedFunctionGroup withUserAssignedFunctionGroupCombinations(
        List<UserAssignedFunctionGroupCombination> userAssignedFunctionGroupCombinations) {
        this.userAssignedFunctionGroupCombinations = new HashSet<>(userAssignedFunctionGroupCombinations);
        return this;
    }
}
