package com.backbase.accesscontrol.util.helpers;


import com.backbase.accesscontrol.domain.BusinessFunction;

public class BusinessFunctionUtil {

    public static BusinessFunction getBusinessFunction(String id, String functionName, String functionCode,
        String resource, String resourceCode) {
        BusinessFunction businessFunction = new BusinessFunction();
        businessFunction.setId(id);
        businessFunction.setFunctionName(functionName);
        businessFunction.setFunctionCode(functionCode);
        businessFunction.setResourceName(resource);
        businessFunction.setResourceCode(resourceCode);
        return businessFunction;
    }

}
