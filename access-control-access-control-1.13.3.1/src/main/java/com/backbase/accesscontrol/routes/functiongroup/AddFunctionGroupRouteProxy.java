package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsPostResponseBody;
import org.springframework.stereotype.Component;

@Component
public interface AddFunctionGroupRouteProxy {

    InternalRequest<FunctionGroupsPostResponseBody> addFunctionGroup(
        InternalRequest<FunctionGroupBase> internalRequest);
}
