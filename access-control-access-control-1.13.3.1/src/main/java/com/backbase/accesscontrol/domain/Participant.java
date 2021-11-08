package com.backbase.accesscontrol.domain;

import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_ADMINS;
import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY_AND_SERVICE_AGREEMENT_CREATOR;
import static com.backbase.accesscontrol.domain.GraphConstants.PARTICIPANT_WITH_SERVICE_AGREEMENT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "participant",
    uniqueConstraints = @UniqueConstraint(name = "uk_le_for_sa",
        columnNames = {"legal_entity_id", "service_agreement_id"}))
@NamedEntityGraph(name = PARTICIPANT_WITH_SERVICE_AGREEMENT,
    attributeNodes = @NamedAttributeNode(value = "serviceAgreement", subgraph = "additions"),
    subgraphs = @NamedSubgraph(name = "additions", attributeNodes = @NamedAttributeNode("additions"))
)
@NamedEntityGraph(name = GraphConstants.PARTICIPANT_WITH_LEGAL_ENTITY,
    attributeNodes = @NamedAttributeNode(value = "legalEntity", subgraph = "additions")
)
@NamedEntityGraph(name = PARTICIPANT_WITH_LEGAL_ENTITY_AND_SERVICE_AGREEMENT_CREATOR,
    attributeNodes = {
        @NamedAttributeNode(value = "legalEntity"),
        @NamedAttributeNode(value = "serviceAgreement", subgraph = "creator")
    },
    subgraphs = @NamedSubgraph(name = "creator", attributeNodes = @NamedAttributeNode("creatorLegalEntity"))
)
@NamedEntityGraph(name = PARTICIPANT_WITH_ADMINS,
    attributeNodes = @NamedAttributeNode(value = "admins")
)
@NamedEntityGraph(
    name = GraphConstants.PARTICIPANT_WITH_SERVICE_AGREEMENT_LEGAL_ENTITY_USERS,
    attributeNodes = {
        @NamedAttributeNode("participantUsers"),
        @NamedAttributeNode("serviceAgreement"),
        @NamedAttributeNode("legalEntity")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@With
public class Participant {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "legal_entity_id", nullable = false, updatable = false)
    private LegalEntity legalEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_agreement_id", nullable = false, updatable = false)
    private ServiceAgreement serviceAgreement;

    @Column(name = "share_users", nullable = false)
    private boolean shareUsers;

    @Column(name = "share_accounts", nullable = false)
    private boolean shareAccounts;

    @OneToMany(fetch = FetchType.LAZY,
        mappedBy = "participant",
        cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE},
        orphanRemoval = true)
    private Set<ParticipantUser> participantUsers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = {CascadeType.PERSIST,
        CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "participant")
    @MapKey(name = "userId")
    private Map<String, ServiceAgreementAdmin> admins = new HashMap<>();

    /**
     * Add admins to the participant.
     *
     * @param userIds - list of user ids.
     */
    public void addAdmins(Set<String> userIds) {
        userIds.forEach(this::addAdmin);
    }

    /**
     * Add admin to the participant.
     *
     * @param userId - id of the user.
     */
    public void addAdmin(String userId) {
        ServiceAgreementAdmin admin = new ServiceAgreementAdmin();
        admin.setParticipant(this);
        admin.setUserId(userId);
        getAdmins().put(userId, admin);
    }

    /**
     * Add exposed users to participant.
     *
     * @param participantUsers - List of user ids.
     */
    public void addParticipantUsers(List<String> participantUsers) {
        participantUsers.forEach(this::addParticipantUser);
    }

    /**
     * Add exposed user to participant.
     *
     * @param userId - user id.
     */
    public void addParticipantUser(String userId) {
        ParticipantUser participantUser = new ParticipantUser();
        participantUser.setUserId(userId);
        participantUser.setParticipant(this);
        this.participantUsers.add(participantUser);
    }
}
