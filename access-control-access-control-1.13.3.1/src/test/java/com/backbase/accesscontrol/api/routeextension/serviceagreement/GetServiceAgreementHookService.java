package com.backbase.accesscontrol.api.routeextension.serviceagreement;

import static com.backbase.accesscontrol.api.routeextension.serviceagreement.GetServiceAgreementRouteHook.TEST_HOOK_GET_SERVICE_AGREEMENT;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementItemGetResponseBody;
import org.apache.camel.Consume;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("routes")
public class GetServiceAgreementHookService {

    @Consume(value = TEST_HOOK_GET_SERVICE_AGREEMENT)
    public InternalRequest<ServiceAgreementItemGetResponseBody> postHook(
        InternalRequest<ServiceAgreementItemGetResponseBody> request) {
        request.getData().setDescription("Service agreement was in post hook");
        return request;
    }


}
