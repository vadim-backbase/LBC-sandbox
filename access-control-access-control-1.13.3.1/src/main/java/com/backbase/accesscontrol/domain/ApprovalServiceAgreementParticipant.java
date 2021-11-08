package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.idclass.ApprovalServiceAgreementParticipantIdClass;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approval_sa_participant")
@IdClass(ApprovalServiceAgreementParticipantIdClass.class)
@Getter
@Setter
@NoArgsConstructor
public class ApprovalServiceAgreementParticipant {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "id", nullable = false)
    private ApprovalServiceAgreement approvalServiceAgreement;

    @Id
    @Column(name = "legal_entity_id", nullable = false, length = 36)
    private String legalEntityId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "legal_entity_id", nullable = false, insertable=false, updatable=false)
    private LegalEntity legalEntity;

    @Column(name = "share_users", nullable = false)
    private boolean shareUsers;

    @Column(name = "share_accounts", nullable = false)
    private boolean shareAccounts;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "approval_sa_admins",
        joinColumns = {@JoinColumn(name = "id"), @JoinColumn(name = "legal_entity_id")})
    @Column(name = "user_id")
    private Set<String> admins = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApprovalServiceAgreementParticipant that = (ApprovalServiceAgreementParticipant) o;
        return shareUsers == that.shareUsers &&
            shareAccounts == that.shareAccounts &&
            legalEntityId.equals(that.legalEntityId) &&
            admins.equals(that.admins);
    }

    @Override
    public int hashCode() {
        return Objects.hash(legalEntityId, shareUsers, shareAccounts, admins);
    }
}
