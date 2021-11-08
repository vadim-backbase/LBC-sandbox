package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.dto.DataGroupOperationResponse;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface UpdateDataGroupByIdRouteProxy {

    InternalRequest<DataGroupOperationResponse> updateDataGroupById(
        @Body InternalRequest<DataGroupByIdPutRequestBody> request,
        @Header("id") String id);
}
