package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface DeleteDataGroupRouteProxy {

    InternalRequest<DataGroupOperationResponse> deleteDataGroup(@Body InternalRequest<Void> request,
        @Header("id") String id);
}
