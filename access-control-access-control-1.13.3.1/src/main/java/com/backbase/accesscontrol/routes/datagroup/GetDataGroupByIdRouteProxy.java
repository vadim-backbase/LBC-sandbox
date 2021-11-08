package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface GetDataGroupByIdRouteProxy {

    InternalRequest<DataGroupByIdGetResponseBody> getDataGroupById(@Body InternalRequest<Void> request,
        @Header("id") String dataGroupId);
}
