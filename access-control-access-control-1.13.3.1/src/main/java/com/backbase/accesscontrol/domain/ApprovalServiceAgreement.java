package com.backbase.accesscontrol.domain;

import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;

import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLInsert;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "approval_service_agreement")
@Getter
@Setter
@NoArgsConstructor
public class ApprovalServiceAgreement extends ApprovalServiceAgreementRef {

    @Column(name = "external_id", length = 64, unique = true)
    private String externalId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "creator_legal_entity_id", nullable = false)
    private String creatorLegalEntityId;

    @Column(name = "is_master", nullable = false, updatable = false)
    private boolean isMaster;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 16)
    private ServiceAgreementState state = ServiceAgreementState.ENABLED;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "assignable_permission_set_id")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "approval_service_agreement_aps",
        joinColumns = @JoinColumn(name = "id", referencedColumnName = "id"))
    @Where(clause = "type = " + DomainConstants.ADMIN_PERMISSIONS)
    @SQLInsert(sql = "insert into approval_service_agreement_aps (id, assignable_permission_set_id, type) "
        + "values (?, ?, " + DomainConstants.ADMIN_PERMISSIONS + ")")
    @SQLDelete(sql = "delete from approval_service_agreement_aps where id = ? "
        + "and assignable_permission_set_id = ? and type = " + DomainConstants.ADMIN_PERMISSIONS)
    private Set<Long> permissionSetsAdmin = new HashSet<>();

    @Column(name = "assignable_permission_set_id")
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "approval_service_agreement_aps",
        joinColumns = @JoinColumn(name = "id", referencedColumnName = "id"))
    @Where(clause = "type = " + DomainConstants.REGULAR_PERMISSIONS)
    @SQLInsert(sql = "insert into approval_service_agreement_aps (id, assignable_permission_set_id, type) "
        + "values (?, ?, " + DomainConstants.REGULAR_PERMISSIONS + ")")
    @SQLDelete(sql = "delete from approval_service_agreement_aps where id = ? "
        + "and assignable_permission_set_id = ? and type = " + DomainConstants.REGULAR_PERMISSIONS)
    private Set<Long> permissionSetsRegular = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "approvalServiceAgreement", cascade = {PERSIST, REMOVE},
        orphanRemoval = true)
    private Set<ApprovalServiceAgreementParticipant> participants = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "property_key", length = 50)
    @Column(name = "property_value", length = 500)
    @CollectionTable(name = "approval_add_prop_sa",
        joinColumns = @JoinColumn(name = "id"))
    private Map<String, String> additions = new HashMap<>();

    @Override
    public ApprovalAction getApprovalAction() {

        return Objects.nonNull(getServiceAgreementId()) ? ApprovalAction.EDIT : ApprovalAction.CREATE;
    }

    public void setParticipants(Set<ApprovalServiceAgreementParticipant> participants) {
        ApprovalServiceAgreement approvalServiceAgreement = this;
        this.participants = participants.stream()
            .map(participant -> {
                participant.setApprovalServiceAgreement(approvalServiceAgreement);
                return participant;
            })
            .collect(Collectors.toSet());
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
        ApprovalServiceAgreement that = (ApprovalServiceAgreement) o;
        return externalId.equals(that.externalId)
            && name.equals(that.name)
            && description.equals(that.description)
            && Objects.equals(creatorLegalEntityId, that.creatorLegalEntityId)
            && Objects.equals(startDate, that.startDate)
            && Objects.equals(endDate, that.endDate)
            && Objects.equals(state, that.state)
            && Objects.equals(isMaster, that.isMaster);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(super.hashCode(), externalId, name, description, creatorLegalEntityId, startDate, endDate,
                state, isMaster);
    }
}
