package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface ListFunctionGroupsRouteProxy {

    InternalRequest<List<FunctionGroupsGetResponseBody>> getFunctionGroups(
        @Body InternalRequest<Void> request,
        @Header("serviceAgreementId") String serviceAgreementId
    );
}
