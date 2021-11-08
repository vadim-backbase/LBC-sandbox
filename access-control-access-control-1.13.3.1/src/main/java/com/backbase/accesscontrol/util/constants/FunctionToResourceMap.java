package com.backbase.accesscontrol.util.constants;

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionToResourceMap {

    private static final Map<String, String> functionResourceMap = new HashMap<>();

    private static final String ASSIGN_PERMISSIONS = "Assign Permissions";
    private static final String SERVICE_AGREEMENT = "Service Agreement";
    private static final String MANAGE_DATA_GROUPS = "Manage Data Groups";
    private static final String MANAGE_FUNCTION_GROUPS = "Manage Function Groups";
    private static final String MANAGE_SERVICE_AGREEMENTS = "Manage Service Agreements";
    private static final String ENTITLEMENTS = "Entitlements";

    static {
        functionResourceMap.put(ASSIGN_PERMISSIONS, SERVICE_AGREEMENT);
        functionResourceMap.put(MANAGE_DATA_GROUPS, ENTITLEMENTS);
        functionResourceMap.put(MANAGE_FUNCTION_GROUPS, ENTITLEMENTS);
        functionResourceMap.put(MANAGE_SERVICE_AGREEMENTS, SERVICE_AGREEMENT);
    }

    public static String getResourceName(String functionName) {
        return functionResourceMap.get(functionName);
    }

}
