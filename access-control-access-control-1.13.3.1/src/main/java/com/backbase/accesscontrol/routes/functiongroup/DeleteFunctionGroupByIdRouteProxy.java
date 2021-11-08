package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface DeleteFunctionGroupByIdRouteProxy {

    InternalRequest<Void> deleteFunctionGroup(
        @Body InternalRequest<Void> request,
        @Header("id") String id);
}
