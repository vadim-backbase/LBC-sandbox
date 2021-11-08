package com.backbase.accesscontrol.routes.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import org.apache.camel.Body;
import org.apache.camel.Header;

public interface UpdateLegalEntityByExternalIdRouteProxy {

    InternalRequest<Void> updateLegalEntityByExternalId(
        @Body InternalRequest<LegalEntityByExternalIdPutRequestBody> request,
        @Header("externalId") String externalId);
}
