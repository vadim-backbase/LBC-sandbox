package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface AddDataGroupRouteProxy {

    InternalRequest<DataGroupOperationResponse> addDataGroup(InternalRequest<DataGroupBase> request);
}
