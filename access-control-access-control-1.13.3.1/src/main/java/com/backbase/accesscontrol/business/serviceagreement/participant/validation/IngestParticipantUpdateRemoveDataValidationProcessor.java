package com.backbase.accesscontrol.business.serviceagreement.participant.validation;

import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import java.util.List;

/**
 * This processor is used on REMOVE action when calling.
 * <p>
 * {@link ServiceAgreementServiceApiController #putPresentationIngestServiceAgreementParticipants}
 */
public interface IngestParticipantUpdateRemoveDataValidationProcessor {

    /**
     * Validate participants.
     *
     * @param dataItemValidatableItems list of {@link DataItemValidatableItem}
     * @return list of {@link InvalidParticipantItem}
     */
    List<InvalidParticipantItem> processValidateParticipants(List<DataItemValidatableItem> dataItemValidatableItems);

    /**
     * Validate sharing accounts participants.
     *
     * @param arrangementItems     - all the arrangement items
     * @param legalEntityIdsToStay - legal entity ids that stay
     */
    void canRemoveParticipantSharingAccounts(List<String> arrangementItems, List<String> legalEntityIdsToStay,
        String serviceAgreementId);


}
