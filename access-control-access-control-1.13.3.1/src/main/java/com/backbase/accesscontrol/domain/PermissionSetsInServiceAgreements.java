package com.backbase.accesscontrol.domain;

import com.backbase.accesscontrol.domain.idclass.ServiceAgreementAssignablePermissionSetIdClass;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Table(name = "service_agreement_aps")
@NoArgsConstructor
@AllArgsConstructor
@Data
@IdClass(ServiceAgreementAssignablePermissionSetIdClass.class)
@EqualsAndHashCode
@With
public class PermissionSetsInServiceAgreements {


    @Id
    @Column(name = "service_agreement_id ")
    private String serviceAgreementId;

    @Id
    @Column(name = "assignable_permission_set_id")
    private Long assignablePermissionSetId;

    @Id
    @Column(name = "type")
    private Integer assignedPermissionUserType;
}
