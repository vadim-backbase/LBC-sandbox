package com.backbase.accesscontrol.dto.parameterholder;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
@EqualsAndHashCode
public class ApprovalsParametersHolder implements Serializable {

    private String serviceAgreementId;
    private String userId;
    private String legalEntityId;
    private Integer from;
    private Integer size;
    private String cursor;
}
