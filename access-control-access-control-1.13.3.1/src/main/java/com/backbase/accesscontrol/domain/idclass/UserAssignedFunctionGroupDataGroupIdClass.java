package com.backbase.accesscontrol.domain.idclass;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserAssignedFunctionGroupDataGroupIdClass implements Serializable {

    private String dataGroup;
    private Long userAssignedFunctionGroup;
}
