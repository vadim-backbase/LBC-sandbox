package com.backbase.accesscontrol.api.routeextension.datagroup;

import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import org.apache.camel.Consume;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Primary
@Profile("routes")
public class GetDataGroupHookService {

    @Consume(value = GetDataGroupRouteHook.TEST_HOOK_GET_DATA_GROUP)
    public InternalRequest<DataGroupByIdGetResponseBody> postHookHandler(
        InternalRequest<DataGroupByIdGetResponseBody> internalRequest) {
        internalRequest.getData().setDescription("Data Group has been in hook.");
        return internalRequest;
    }
}
