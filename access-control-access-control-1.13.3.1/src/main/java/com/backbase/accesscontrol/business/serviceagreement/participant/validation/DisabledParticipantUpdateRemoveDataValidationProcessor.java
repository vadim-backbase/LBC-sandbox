package com.backbase.accesscontrol.business.serviceagreement.participant.validation;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_086;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_101;

import com.backbase.accesscontrol.business.batch.DataItemValidatableItem;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.datagroup.ParticipantItemValidator;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(prefix = "backbase.data-group.validation", name = "enabled",
    havingValue = "false", matchIfMissing = true)
@Service
public class DisabledParticipantUpdateRemoveDataValidationProcessor implements
    IngestParticipantUpdateRemoveDataValidationProcessor {

    @Value("${backbase.approval.validation.enabled:false}")
    private boolean isApprovalOn;
    @Autowired
    private ParticipantItemValidator validator;
    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvalidParticipantItem> processValidateParticipants(
        List<DataItemValidatableItem> dataItemValidatableItems) {
        return dataItemValidatableItems
            .stream()
            .filter(
                validatableItem -> CollectionUtils.isNotEmpty(validatableItem.getPersistenceDataGroupExtendedItems()))
            .map(validatableItem1 -> new InvalidParticipantItem(validatableItem1,
                Lists.newArrayList(isApprovalOn ? ERR_AG_101.getErrorMessage() : ERR_AG_086.getErrorMessage())))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void canRemoveParticipantSharingAccounts(List<String> arrangementItems,
        List<String> legalEntityIdsToStay,String serviceAgreementId) {
        validator.canRemoveParticipantSharingAccounts(arrangementItems,legalEntityIdsToStay,serviceAgreementId);
    }

}
