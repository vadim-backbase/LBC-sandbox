package com.backbase.accesscontrol.domain;

import static com.backbase.accesscontrol.domain.DomainConstants.ADMIN_PERMISSIONS;

import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Where;

@NamedEntityGraphs(value = {
    @NamedEntityGraph(name = GraphConstants.APS_PERMISSIONS_EXTENDED, attributeNodes = {
        @NamedAttributeNode(value = "permissions")})})
@Entity
@Table(name = "assignable_permission_set")
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AssignablePermissionSet {

    @EqualsAndHashCode.Include
    @Id
    @GenericGenerator(
        name = "assignablePermissionSetGenerator",
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
                value = "SEQ_ID_APS"
            ),
            @Parameter(
                name = "initial_value",
                value = "4"
            ),
            @Parameter(
                name = "increment_size",
                value = "1"
            )
        })

    @GeneratedValue(strategy = GenerationType.TABLE, generator = "assignablePermissionSetGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "name", nullable = false)
    private String name;

    @EqualsAndHashCode.Include
    @Column(name = "description", nullable = false)
    private String description;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "type", nullable = false)
    private int type = AssignablePermissionType.CUSTOM.getValue();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "service_agreement_aps",
        joinColumns = @JoinColumn(name = "assignable_permission_set_id"))
    @Column(name = "service_agreement_id")
    @Where(clause = "type = " + ADMIN_PERMISSIONS)
    private Set<String> assignedAsAdminToServiceAgreement = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "assignable_permission_set_item",
        joinColumns = @JoinColumn(name = "assignable_permission_set_id"))
    @Column(name = "function_privilege_id")
    private Set<String> permissions = new HashSet<>();

    /**
     * Get assignable permission type.
     *
     * @return {@link AssignablePermissionType}
     */
    public AssignablePermissionType getType() {
        return AssignablePermissionType.from(this.type);
    }

    /**
     * Set assignable permission type.
     *
     * @param assignablePermissionType -  assignable permission type
     */
    public void setType(AssignablePermissionType assignablePermissionType) {
        this.type = assignablePermissionType.getValue();
    }
}
