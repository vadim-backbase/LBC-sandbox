package com.backbase.accesscontrol.dto;

import java.util.Map;
import lombok.Data;

@Data
public class UserPrivilegesSummaryGetResponseBodyDto {

    private String resource;
    private String function;
    private Map<String, Boolean> privileges;

}
