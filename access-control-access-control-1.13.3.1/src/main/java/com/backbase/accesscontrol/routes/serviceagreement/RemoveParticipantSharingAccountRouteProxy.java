package com.backbase.accesscontrol.routes.serviceagreement;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import java.util.List;
import org.apache.camel.Body;

public interface RemoveParticipantSharingAccountRouteProxy {

    List<InvalidParticipantItem> getInvalidItemsSharingAccounts(
        @Body InternalRequest<List<DataItemValidatableItem>> internalRequest);
}
