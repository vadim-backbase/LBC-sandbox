package com.backbase.accesscontrol.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "participant_user", uniqueConstraints =
    @UniqueConstraint(name = "uk_user_for_provider", columnNames = {"user_id", "participant_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@With
public class ParticipantUser {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false, updatable = false)
    private Participant participant;

    @Column(name = "user_id", nullable = false, updatable = false, length = 36)
    private String userId;

    /**
     * Custom constructor.
     *
     * @param participant - participant
     * @param userId      - user id
     */
    public ParticipantUser(Participant participant, String userId) {
        this.participant = participant;
        this.userId = userId;
    }
}
