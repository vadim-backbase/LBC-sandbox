package com.backbase.accesscontrol.dto;

import com.backbase.accesscontrol.dto.parameterholder.GenericParameterHolder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * convenience class to represent request-response pairs.
 */

@AllArgsConstructor
@Data
public class IdentifierPair implements GenericParameterHolder {

    String identifierType;
    String identifier;
}
