package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import java.util.List;
import org.apache.camel.Body;

public interface UpdateUsersInServiceAgreementRouteProxy {

    InternalRequest<List<BatchResponseItemExtended>> updateUsersInServiceAgreement(
        @Body InternalRequest<PresentationServiceAgreementUsersUpdate> request);
}
