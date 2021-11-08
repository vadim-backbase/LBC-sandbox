package com.backbase.accesscontrol.api.routeextension.legalentity;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import org.apache.camel.Consume;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("routes")
public class GetLegalEntityByIdHookService {

    @Consume(value = GetLegalEntityByIdRouteHook.DIRECT_BUSINESS_GET_LEGAL_ENTITY)
    public InternalRequest<LegalEntityByIdGetResponseBody> getLegalEntity() {
        InternalRequest<LegalEntityByIdGetResponseBody> listInternalRequest = new InternalRequest<>();
        listInternalRequest.setData(new LegalEntityByIdGetResponseBody().withId("idHook"));
        return listInternalRequest;
    }

}
