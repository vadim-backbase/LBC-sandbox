package com.backbase.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdentifierDto {

    private String idIdentifier;

    private NameIdentifier nameIdentifier;

    public IdentifierDto withIdIdentifier(String idIdentifier) {
        this.idIdentifier = idIdentifier;
        return this;
    }

}
