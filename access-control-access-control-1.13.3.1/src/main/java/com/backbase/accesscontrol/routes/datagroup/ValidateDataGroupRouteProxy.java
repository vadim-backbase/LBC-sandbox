package com.backbase.accesscontrol.routes.datagroup;

import com.backbase.accesscontrol.dto.DataItemsValidatable;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import org.apache.camel.Body;

public interface ValidateDataGroupRouteProxy {

    void validate(@Body InternalRequest<DataItemsValidatable> body);
}
