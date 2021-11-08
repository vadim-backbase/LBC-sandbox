package com.backbase.accesscontrol.domain.idclass;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ServiceAgreementAssignablePermissionSetIdClass implements Serializable {

    private String serviceAgreementId;
    private Long assignablePermissionSetId;
    private Integer assignedPermissionUserType;
}
