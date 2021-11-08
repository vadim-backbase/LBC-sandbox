package com.backbase.accesscontrol.api.routeextension.functiongroup;

import com.backbase.accesscontrol.routes.functiongroup.GetFunctionGroupByIdRoute;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("routes")
public class GetFunctionGroupRouteHook extends GetFunctionGroupByIdRoute {

    public static final String TEST_HOOK_GET_FUNCTION_GROUP = "direct:testGetFG";

    @Override
    protected void configurePostHook(RouteDefinition rd) throws Exception {
        rd.to(TEST_HOOK_GET_FUNCTION_GROUP);
    }
}
