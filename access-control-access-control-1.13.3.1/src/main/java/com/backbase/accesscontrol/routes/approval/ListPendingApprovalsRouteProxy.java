package com.backbase.accesscontrol.routes.approval;

import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;

public interface ListPendingApprovalsRouteProxy {

    InternalRequest<ApprovalsListDto> listApprovals(
        @Body InternalRequest<ApprovalsParametersHolder> request);
}
