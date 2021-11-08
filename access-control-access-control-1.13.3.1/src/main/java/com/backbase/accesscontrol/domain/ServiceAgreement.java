package com.backbase.accesscontrol.domain;

import static java.util.Objects.nonNull;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.WhereJoinTable;

@Entity
@Table(name = "service_agreement")
@NamedEntityGraphs(value = {
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_ADDITIONS, attributeNodes = {
        @NamedAttributeNode(value = "additions"),
        @NamedAttributeNode(value = "creatorLegalEntity")}
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_CREATOR, attributeNodes = {
        @NamedAttributeNode(value = "creatorLegalEntity")}
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_DATAGROUPS, attributeNodes = {
        @NamedAttributeNode(value = "dataGroups"),
        @NamedAttributeNode(value = "creatorLegalEntity")}
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_EXTENDED, attributeNodes = {
        @NamedAttributeNode(value = "participants", subgraph = "admins"),
        @NamedAttributeNode(value = "additions"),
        @NamedAttributeNode(value = "creatorLegalEntity")},
        subgraphs = @NamedSubgraph(name = "admins", attributeNodes = @NamedAttributeNode("admins"))
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADDITIONS, attributeNodes = {
        @NamedAttributeNode(value = "participants", subgraph = "admins"),
        @NamedAttributeNode(value = "additions"),
        @NamedAttributeNode(value = "creatorLegalEntity")},
        subgraphs = @NamedSubgraph(name = "admins", attributeNodes = @NamedAttributeNode("admins"))
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_ADMINS_AND_FUNCTION_GROUPS, attributeNodes = {
        @NamedAttributeNode(value = "functionGroups"),
        @NamedAttributeNode(value = "additions"),
        @NamedAttributeNode(value = "participants", subgraph = "admins")},
        subgraphs = @NamedSubgraph(name = "admins", attributeNodes = @NamedAttributeNode("admins"))
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PARTICIPANTS_AND_CREATOR, attributeNodes = {
        @NamedAttributeNode(value = "participants", subgraph = "legalEntity"),
        @NamedAttributeNode(value = "creatorLegalEntity")},
        subgraphs = @NamedSubgraph(name = "legalEntity", attributeNodes = @NamedAttributeNode("legalEntity"))
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_CREATOR_AND_FUNCTION_AND_DATA_GROUPS,
        attributeNodes = {
            @NamedAttributeNode(value = "functionGroups"),
            @NamedAttributeNode(value = "dataGroups"),
            @NamedAttributeNode(value = "creatorLegalEntity")
        }),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_EXTENDED_WITH_FUNCTION_GROUPS,
        attributeNodes = {
            @NamedAttributeNode(value = "functionGroups"),
            @NamedAttributeNode(value = "participants", subgraph = "admins"),
            @NamedAttributeNode(value = "creatorLegalEntity")},
        subgraphs = @NamedSubgraph(name = "admins", attributeNodes = @NamedAttributeNode("admins"))
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR,
        attributeNodes = {
            @NamedAttributeNode(value = "permissionSetsRegular", subgraph = "permission")},
        subgraphs = @NamedSubgraph(name = "permission", attributeNodes = @NamedAttributeNode("permissions"))

    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS_REGULAR_AND_CREATOR_LE_AND_FGS,
        attributeNodes = {
            @NamedAttributeNode(value = "permissionSetsRegular"),
            @NamedAttributeNode(value = "functionGroups"),
            @NamedAttributeNode(value = "creatorLegalEntity")}
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_FGS,
        attributeNodes = {
            @NamedAttributeNode(value = "functionGroups")}
    ),
    @NamedEntityGraph(name = GraphConstants.SERVICE_AGREEMENT_WITH_PERMISSION_SETS,
        attributeNodes = {
            @NamedAttributeNode(value = "permissionSetsRegular"),
            @NamedAttributeNode(value = "permissionSetsAdmin")})
})
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@With
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceAgreement {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @EqualsAndHashCode.Include
    @Column(name = "external_id", length = 64, unique = true)
    private String externalId;

    @EqualsAndHashCode.Include
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @EqualsAndHashCode.Include
    @Column(name = "description", nullable = false)
    private String description;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_legal_entity_id", nullable = false)
    private LegalEntity creatorLegalEntity;

    @EqualsAndHashCode.Include
    @Column(name = "is_master", nullable = false, updatable = false)
    private boolean isMaster;

    @OneToMany(mappedBy = "serviceAgreement", cascade = {CascadeType.PERSIST,
        CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY, orphanRemoval = true)
    @MapKeyColumn(name = "legal_entity_id")
    private Map<String, Participant> participants = new HashMap<>();


    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "property_key", length = 50)
    @Column(name = "property_value", length = 500)
    @CollectionTable(name = "add_prop_service_agreement",
        joinColumns = @JoinColumn(name = "add_prop_service_agreement_id"))
    private Map<String, String> additions = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private ServiceAgreementState state = ServiceAgreementState.ENABLED;

    @EqualsAndHashCode.Include
    @Column(name = "start_date")
    private Date startDate;

    @EqualsAndHashCode.Include
    @Column(name = "end_date")
    private Date endDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "state_changed_at")
    private Date stateChangedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceAgreement", cascade = {PERSIST, REMOVE},
        orphanRemoval = true)
    private Set<FunctionGroup> functionGroups = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "serviceAgreement", cascade = {PERSIST, REMOVE},
        orphanRemoval = true)
    private Set<DataGroup> dataGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "service_agreement_aps",
        joinColumns = @JoinColumn(name = "service_agreement_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "assignable_permission_set_id", referencedColumnName = "id"))
    @WhereJoinTable(clause = "type = " + DomainConstants.ADMIN_PERMISSIONS)
    @SQLInsert(sql = "insert into service_agreement_aps (service_agreement_id, assignable_permission_set_id, type) "
        + "values (?, ?, " + DomainConstants.ADMIN_PERMISSIONS + ")")
    @SQLDelete(sql = "delete from service_agreement_aps where service_agreement_id = ? "
        + "and assignable_permission_set_id = ? and type = " + DomainConstants.ADMIN_PERMISSIONS)
    private Set<AssignablePermissionSet> permissionSetsAdmin = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "service_agreement_aps",
        joinColumns = @JoinColumn(name = "service_agreement_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "assignable_permission_set_id", referencedColumnName = "id"))
    @WhereJoinTable(clause = "type = " + DomainConstants.REGULAR_PERMISSIONS)
    @SQLInsert(sql = "insert into service_agreement_aps (service_agreement_id, assignable_permission_set_id, type) "
        + "values (?, ?, " + DomainConstants.REGULAR_PERMISSIONS + ")")
    @SQLDelete(sql = "delete from service_agreement_aps where service_agreement_id = ? "
        + "and assignable_permission_set_id = ? and type = " + DomainConstants.REGULAR_PERMISSIONS)
    private Set<AssignablePermissionSet> permissionSetsRegular = new HashSet<>();


    /**
     * Sets description and trims white spaces.
     *
     * @param description - description of the service agreement.
     */
    public void setDescription(String description) {
        this.description = Optional.ofNullable(description)
            .map(String::trim)
            .orElse("");
    }

    /**
     * Add participants to service agreement.
     *
     * @param participants - List of {@Link Participant}.
     */
    public void addParticipant(List<Participant> participants) {
        for (Participant participant : participants) {
            Participant participantExisting = this.participants.get(participant.getLegalEntity().getId());
            if (Objects.nonNull(participantExisting)) {
                participantExisting.setShareUsers(participant.isShareUsers());
                participantExisting.setShareAccounts(participant.isShareAccounts());
                this.participants.put(participant.getLegalEntity().getId(), participantExisting);
            } else {
                addParticipant(participant);
            }
        }
    }

    /**
     * Remove participants to service agreement.
     *
     * @param participants - List of {@Link Participant}.
     */
    public void removeParticipant(List<Participant> participants) {
        for (Participant participant : participants) {
            removeParticipant(participant);
        }
    }

    /**
     * Add participant to service agreement.
     *
     * @param participant - {@link Participant}
     */
    public void addParticipant(Participant participant) {
        participant.setServiceAgreement(this);
        this.participants.put(participant.getLegalEntity().getId(), participant);
    }

    /**
     * Remove participant to service agreement.
     *
     * @param participant - {@link Participant}
     */
    public void removeParticipant(Participant participant) {
        participant.setServiceAgreement(this);
        this.participants.remove(participant.getLegalEntity().getId(), participant);
    }


    /**
     * Add participant to service agreement.
     *
     * @param participant   - {@link Participant}
     * @param legalEntityId - legal entity id
     * @param shareUsers    - is participant shares users.
     * @param shareAccounts - is participant shares accounts.
     */
    public void addParticipant(Participant participant, String legalEntityId, boolean shareUsers,
        boolean shareAccounts) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(legalEntityId);
        participant.setLegalEntity(legalEntity);
        participant.setServiceAgreement(this);
        participant.setShareAccounts(shareAccounts);
        participant.setShareUsers(shareUsers);
        addParticipant(participant);
    }

    /**
     * Get additions.
     *
     * @return additions map
     */
    public Map<String, String> getAdditions() {
        return this.additions;
    }

    /**
     * Additions setter.
     *
     * @param additions additions
     */
    public void setAdditions(Map<String, String> additions) {
        this.additions.clear();
        if (nonNull(additions)) {
            this.additions.putAll(additions);
        }
    }

    public void setAddition(String key, String value) {
        this.additions.put(key, value);
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

        ServiceAgreement that = (ServiceAgreement) o;

        return new EqualsBuilder()
            .append(id, that.id)
            .append(externalId, that.externalId)
            .append(name, that.name)
            .append(description, that.description)
            .append(isMaster, that.isMaster)
            .append(state, that.state)
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
            .append(externalId)
            .append(name)
            .append(description)
            .append(isMaster)
            .append(state)
            .append(startDate)
            .append(endDate)
            .toHashCode();
    }

}
