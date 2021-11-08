package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import java.util.List;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface SearchDataGroupsRouteProxy {

    InternalRequest<List<PresentationServiceAgreementWithDataGroups>> searchDataGroups(
        @Body InternalRequest<PresentationGetDataGroupsRequest> request, @Header("type") String type);
}
