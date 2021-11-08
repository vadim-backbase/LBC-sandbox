package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.List;
import org.apache.camel.Body;

/**
 * Method specification that will be used to automatically inject a Camel Producer.
 */
public interface DeleteFunctionGroupRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> deleteFunctionGroup(
        @Body InternalRequest<List<PresentationIdentifier>>
            internalRequest);
}
