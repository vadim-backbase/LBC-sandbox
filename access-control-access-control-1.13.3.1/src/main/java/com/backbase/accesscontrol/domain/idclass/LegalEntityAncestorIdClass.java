package com.backbase.accesscontrol.domain.idclass;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class LegalEntityAncestorIdClass implements Serializable {

    private String descendentId;
    private String ancestorId;
}
