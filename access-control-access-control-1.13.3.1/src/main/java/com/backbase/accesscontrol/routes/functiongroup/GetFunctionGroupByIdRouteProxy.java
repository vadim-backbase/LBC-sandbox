package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetFunctionGroupByIdRouteProxy {

    InternalRequest<FunctionGroupByIdGetResponseBody> getFunctionGroupById(@Body InternalRequest<Void> request,
        @Header("id") String id);
}
