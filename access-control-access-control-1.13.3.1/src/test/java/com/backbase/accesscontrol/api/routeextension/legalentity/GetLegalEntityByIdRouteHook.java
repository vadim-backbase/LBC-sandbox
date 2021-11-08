package com.backbase.accesscontrol.api.routeextension.legalentity;

import com.backbase.accesscontrol.routes.legalentity.GetLegalEntityByIdRoute;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Primary
@Component
@Profile("routes")
public class GetLegalEntityByIdRouteHook extends GetLegalEntityByIdRoute {
    protected final static String DIRECT_BUSINESS_GET_LEGAL_ENTITY = "direct:getLegalEntityTest";

    @Override
    protected void configurePostHook(RouteDefinition routeDefinition) {
        routeDefinition.to(DIRECT_BUSINESS_GET_LEGAL_ENTITY);
    }
}
