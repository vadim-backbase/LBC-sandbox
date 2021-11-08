package com.backbase.accesscontrol.domain.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class DataGroupPermissions {

    @NonNull
    private String dataGroupType;
    private Map<BusinessFunctionKey, Set<String>> permissions = new HashMap<>();

}
