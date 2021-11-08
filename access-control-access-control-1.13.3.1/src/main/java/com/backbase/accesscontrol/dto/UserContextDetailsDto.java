package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UserContextDetailsDto {

    private String internalUserId;
    private String legalEntityId;

}
