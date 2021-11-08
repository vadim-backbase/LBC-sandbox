package com.backbase.accesscontrol.business.serviceagreement.participant.validation;

import static com.backbase.accesscontrol.util.InternalRequestUtil.getInternalRequest;
import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_BUSINESS_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.datagroup.ParticipantItemValidator;
import com.backbase.accesscontrol.routes.serviceagreement.RemoveParticipantSharingAccountRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import java.util.List;
import org.apache.camel.Produce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "backbase.data-group.validation", name = "enabled", havingValue = "true")
@Service
public class EnabledIngestParticipantUpdateRemoveDataValidationProcessor implements
    IngestParticipantUpdateRemoveDataValidationProcessor {

    @Autowired
    private InternalRequestContext internalRequestContext;
    @Autowired
    private ParticipantItemValidator validator;

    @Produce(value = DIRECT_BUSINESS_VALIDTE_PARTICIPANT_DATA_GROUPS_UPDATE)
    private RemoveParticipantSharingAccountRouteProxy removeParticipantSharingAccountRouteProxy;


    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvalidParticipantItem> processValidateParticipants(
        List<DataItemValidatableItem> dataItemValidatableItems) {
        return removeParticipantSharingAccountRouteProxy
            .getInvalidItemsSharingAccounts(getInternalRequest(dataItemValidatableItems, internalRequestContext));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void canRemoveParticipantSharingAccounts(List<String> arrangementItems,
        List<String> legalEntityIdsToStay, String serviceAgreementId) {
        validator.canRemoveParticipantSharingAccounts(arrangementItems, legalEntityIdsToStay);
    }


}
