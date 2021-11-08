package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.List;
import org.apache.camel.Body;

public interface DeleteDataGroupsByIdentifiersRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> deleteDataGroupsByIdentifiers(
        @Body InternalRequest<List<PresentationIdentifier>> internalRequest);
}
