package com.backbase.accesscontrol.dto;

import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FunctionGroupDataGroups {

    private FunctionGroup functionGroup;
    private List<DataGroup> dataGroups;
}
