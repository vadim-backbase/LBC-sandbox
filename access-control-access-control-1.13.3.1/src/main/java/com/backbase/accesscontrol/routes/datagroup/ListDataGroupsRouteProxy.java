package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface ListDataGroupsRouteProxy {

    InternalRequest<List<DataGroupsGetResponseBody>> getDataGroups(
        @Body InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId,
        @Header("type") String type,
        @Header("includeItems") boolean includeItems);
}

