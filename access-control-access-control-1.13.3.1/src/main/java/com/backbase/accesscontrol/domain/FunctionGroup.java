package com.backbase.accesscontrol.domain;

import static java.util.Objects.nonNull;
import static javax.persistence.FetchType.LAZY;

import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.listener.FunctionGroupListener;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "function_group",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"service_agreement_id", "name"})
    })
@NamedEntityGraph(name = GraphConstants.FUNCTION_GROUP_WITH_SA,
    attributeNodes = {@NamedAttributeNode("serviceAgreement")})
@NamedEntityGraph(name = GraphConstants.FUNCTION_GROUP_WITH_GROUPED_FUNCTION_PRIVILEGES,
    attributeNodes = {@NamedAttributeNode(value = "permissions")})
@NamedEntityGraph(name = GraphConstants.FUNCTION_GROUP_WITH_SA_AND_LEGAL_ENTITY_AND_PERMISSION_SETS_REGULAR,
    attributeNodes = {@NamedAttributeNode(value = "serviceAgreement", subgraph = "SAlEntityGraph")},
    subgraphs = {@NamedSubgraph(name = "SAlEntityGraph",
        attributeNodes = {@NamedAttributeNode("creatorLegalEntity"),
            @NamedAttributeNode("permissionSetsRegular")})
    })
@EntityListeners(value = FunctionGroupListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class FunctionGroup {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false)
    private FunctionGroupType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_agreement_id", nullable = false)
    private ServiceAgreement serviceAgreement;

    @Column(name = "service_agreement_id", nullable = false, updatable = false, insertable = false)
    private String serviceAgreementId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "aps_id", updatable = false)
    private AssignablePermissionSet assignablePermissionSet;

    @Column(name = "aps_id", updatable = false, insertable = false)
    private Long assignablePermissionSetId;

    @ElementCollection(fetch = LAZY)
    @CollectionTable(name = "function_group_item", joinColumns = @JoinColumn(name = "function_group_id"))

    @AttributeOverride(name = "applicableFunctionPrivilegeId", column = @Column(name = "afp_id"))
    private Set<FunctionGroupItem> permissions = new LinkedHashSet<>();

    /**
     * Sets description and trims white spaces.
     *
     * @param description - description of the function group.
     */
    public void setDescription(String description) {
        this.description = Optional.ofNullable(description)
            .map(String::trim)
            .orElse("");
    }

    public void setPermissions(Set<GroupedFunctionPrivilege> groupedFunctionPrivilegeList) {
        this.permissions.clear();
        this.permissions.addAll(groupedFunctionPrivilegeList);
    }

    /**
     * Gets service agreement.
     *
     * @return {@link ServiceAgreement}
     */
    public ServiceAgreement getServiceAgreement() {
        return serviceAgreement;
    }

    /**
     * Sets service agreement.
     *
     * @param serviceAgreement service agreement to set
     */
    public void setServiceAgreement(ServiceAgreement serviceAgreement) {
        this.serviceAgreement = serviceAgreement;

        if (nonNull(serviceAgreement)) {
            this.serviceAgreementId = serviceAgreement.getId();
        }
    }

    public Set<FunctionGroupItem> getPermissions() {
        return this.permissions;
    }

    public FunctionGroup withPermissions(Set<GroupedFunctionPrivilege> permissions) {
        this.setPermissions(permissions);
        return this;
    }

    public FunctionGroup withId(String id) {
        this.setId(id);
        return this;
    }

    public FunctionGroup withDescription(String value) {
        this.setDescription(value);
        return this;
    }

    public FunctionGroup withName(String value) {
        this.setName(value);
        return this;
    }

    public FunctionGroup withType(FunctionGroupType type) {
        this.setType(type);
        return this;
    }

    public FunctionGroup withServiceAgreement(ServiceAgreement serviceAgreement) {
        this.setServiceAgreement(serviceAgreement);
        return this;
    }

    public FunctionGroup withAssignablePermissionSet(AssignablePermissionSet permissionSet) {

        this.setAssignablePermissionSet(permissionSet);
        return this;
    }

    public FunctionGroup withStartDate(Date date) {
        this.setStartDate(date);
        return this;
    }

    public FunctionGroup withEndDate(Date date) {
        this.setEndDate(date);
        return this;
    }

    public Stream<GroupedFunctionPrivilege> getPermissionsStream() {
        return this.permissions.stream()
            .map(item -> new GroupedFunctionPrivilege(this, item.getApplicableFunctionPrivilegeId()));
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

        FunctionGroup that = (FunctionGroup) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(name, that.name)
            .append(description, that.description)
            .append(serviceAgreementId, that.serviceAgreementId)
            .append(type, that.type)
            .append(startDate, that.startDate)
            .append(endDate, that.endDate)
            .isEquals();
    }

    /**
     * Hashcode for uniqueness.
     *
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .append(name)
            .append(description)
            .append(serviceAgreementId)
            .append(type)
            .append(startDate)
            .append(endDate)
            .toHashCode();
    }

    /**
     * To string class method.
     *
     * @return arguments description string
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .toString();
    }
}
