package com.backbase.accesscontrol.service;

import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;

public interface IngestFunctionGroupTransformService {

    /**
     * Transform and creates function group.
     *
     * @param requestData the function group that needs to be created
     * @return the id of the created function group
     */
    String addFunctionGroup(FunctionGroupIngest requestData);
}
