package com.backbase.accesscontrol.domain.idclass;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DataGroupItemIdClass implements Serializable {

    private String dataItemId;

    private String dataGroupId;

}
