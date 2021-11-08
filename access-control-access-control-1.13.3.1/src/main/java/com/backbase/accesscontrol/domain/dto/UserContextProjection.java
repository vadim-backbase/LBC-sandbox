package com.backbase.accesscontrol.domain.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class UserContextProjection {
    private String userId;
    private String serviceAgreementId;
}
