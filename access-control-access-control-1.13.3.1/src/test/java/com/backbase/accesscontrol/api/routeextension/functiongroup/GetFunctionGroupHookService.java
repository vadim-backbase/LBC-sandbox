package com.backbase.accesscontrol.api.routeextension.functiongroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import org.apache.camel.Consume;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("routes")
public class GetFunctionGroupHookService {

    public static final String FG_HOOK_DESCRIPTION = "FG has been in hook.";

    @Consume(value = GetFunctionGroupRouteHook.TEST_HOOK_GET_FUNCTION_GROUP)
    public InternalRequest<FunctionGroupByIdGetResponseBody> postHookHandler(
        InternalRequest<FunctionGroupByIdGetResponseBody> internalRequest) {
        internalRequest.getData().setDescription(FG_HOOK_DESCRIPTION);
        return internalRequest;
    }
}
