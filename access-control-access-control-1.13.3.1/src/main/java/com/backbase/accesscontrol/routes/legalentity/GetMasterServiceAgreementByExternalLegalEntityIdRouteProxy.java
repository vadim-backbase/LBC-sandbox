package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface GetMasterServiceAgreementByExternalLegalEntityIdRouteProxy {

    InternalRequest<MasterServiceAgreementGetResponseBody> getMasterServiceAgreementByExternalLegalEntityId(
        @Body InternalRequest<Void>
            request, @Header("externalId") String externalId);
}
