package com.backbase.accesscontrol.domain;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "self_appr_policy_bound",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_sapb_01", columnNames = {"self_appr_policy_id", "currency_code"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelfApprovalPolicyBound {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "self_appr_policy_id", referencedColumnName = "id", nullable = false)
    private SelfApprovalPolicy selfApprovalPolicy;

    @Column(name = "upper_bound", nullable = false, precision = 23, scale = 5)
    private BigDecimal upperBound;


    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    public SelfApprovalPolicyBound(SelfApprovalPolicyBound selfApprovalPolicyBound) {
        this.upperBound = selfApprovalPolicyBound.getUpperBound();
        this.currencyCode = selfApprovalPolicyBound.getCurrencyCode();
    }
}
