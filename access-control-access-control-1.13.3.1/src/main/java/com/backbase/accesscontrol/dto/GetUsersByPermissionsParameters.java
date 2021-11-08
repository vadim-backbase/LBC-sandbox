package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class GetUsersByPermissionsParameters {

    private String serviceAgreementId;
    private String functionName;
    private String privilege;
    private String dataGroupType;
    private String dataItemId;

}
