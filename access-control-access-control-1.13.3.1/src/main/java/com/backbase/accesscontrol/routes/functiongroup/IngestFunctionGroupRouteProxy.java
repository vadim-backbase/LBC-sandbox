package com.backbase.accesscontrol.routes.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationIngestFunctionGroupPostResponseBody;
import org.apache.camel.Body;

public interface IngestFunctionGroupRouteProxy {

    InternalRequest<PresentationIngestFunctionGroupPostResponseBody> ingestFunctionGroup(
        @Body InternalRequest<PresentationFunctionGroup> internalRequest);
}
