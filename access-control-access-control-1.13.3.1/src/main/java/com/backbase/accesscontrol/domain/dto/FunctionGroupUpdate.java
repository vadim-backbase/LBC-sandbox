package com.backbase.accesscontrol.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionGroupUpdate {

    private String functionGroupId;
    private FunctionGroupBase functionGroupBase;

}
