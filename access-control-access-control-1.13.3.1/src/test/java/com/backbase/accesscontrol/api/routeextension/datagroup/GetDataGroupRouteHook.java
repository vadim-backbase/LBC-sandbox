package com.backbase.accesscontrol.api.routeextension.datagroup;

import com.backbase.accesscontrol.routes.datagroup.GetDataGroupByIdRoute;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("routes")
public class GetDataGroupRouteHook extends GetDataGroupByIdRoute {

    public static final String TEST_HOOK_GET_DATA_GROUP = "direct:testGetDG";

    @Override
    protected void configurePostHook(RouteDefinition rd) throws Exception {
        rd.to(TEST_HOOK_GET_DATA_GROUP);
    }
}
