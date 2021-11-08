package com.backbase.accesscontrol.api.routeextension.serviceagreement;

import com.backbase.accesscontrol.routes.serviceagreement.GetServiceAgreementRoute;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("routes")
public class GetServiceAgreementRouteHook extends GetServiceAgreementRoute {

    public static final String TEST_HOOK_GET_SERVICE_AGREEMENT = "direct:testHookGetServiceAgreement";

    @Override
    protected void configurePostHook(RouteDefinition rd) throws Exception {
        rd.to(TEST_HOOK_GET_SERVICE_AGREEMENT);
    }
}
