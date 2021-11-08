package com.backbase.accesscontrol.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "user_context",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_service_agreement", columnNames = {"user_id",
        "service_agreement_id"})
)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserContext {

    @Id
    @GenericGenerator(
        name = "userContextGenerator",
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
                value = "SEQ_ID_USER_CONTEXT"
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
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "userContextGenerator")
    @Column(name = "id", updatable = false, nullable = false)
    @Include
    private Long id;

    @Column(name = "user_id", nullable = false, length = 36)
    @Include
    private String userId;

    @Column(name = "service_agreement_id", nullable = false, length = 36)
    @Include
    private String serviceAgreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_agreement_id", insertable = false, updatable = false)
    private ServiceAgreement serviceAgreement;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userContext", cascade = {CascadeType.PERSIST,
        CascadeType.MERGE},
        orphanRemoval = true)
    private Set<UserAssignedFunctionGroup> userAssignedFunctionGroups = new HashSet<>();

    /**
     * Custom constructor.
     *
     * @param userId             - user id
     * @param serviceAgreementId service agreement id.
     */
    public UserContext(String userId, String serviceAgreementId) {
        this.userId = userId;
        this.serviceAgreementId = serviceAgreementId;

        this.serviceAgreement = new ServiceAgreement();
        this.serviceAgreement.setId(serviceAgreementId);
    }

    /**
     * Custom constructor.
     *
     * @param id                         user context id
     * @param userId                     user id
     * @param serviceAgreementId         service agreement id
     * @param userAssignedFunctionGroups user assigned function groups
     */
    public UserContext(Long id, String userId, String serviceAgreementId,
        Set<UserAssignedFunctionGroup> userAssignedFunctionGroups) {
        this.id = id;
        this.userId = userId;
        this.serviceAgreementId = serviceAgreementId;

        this.serviceAgreement = new ServiceAgreement();
        this.serviceAgreement.setId(serviceAgreementId);

        this.userAssignedFunctionGroups.addAll(userAssignedFunctionGroups);
    }

    /**
     * Wither method.
     *
     * @param userAssignedFunctionGroups set
     * @return UserContext
     */
    public UserContext withUserAssignedFunctionGroups(
        Set<UserAssignedFunctionGroup> userAssignedFunctionGroups) {
        this.userAssignedFunctionGroups = userAssignedFunctionGroups;
        return this;
    }

    /**
     * Wither method.
     *
     * @param serviceAgreementId string
     * @return UserContext
     */
    public UserContext withServiceAgreementId(String serviceAgreementId) {
        this.serviceAgreementId = serviceAgreementId;
        return this;
    }

    /**
     * Wither method.
     *
     * @param userId string
     * @return UserContext
     */
    public UserContext withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Wither method.
     *
     * @param id string
     * @return UserContext
     */
    public UserContext withId(Long id) {
        this.id = id;
        return this;
    }
}
