package com.backbase.accesscontrol.service.batch.datagroup;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;

import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Batch Data Group delete.
 */
@Service
public class DeleteDataGroupPersistence extends
    LeanGenericBatchProcessor<PresentationIdentifier, ResponseItemExtended, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteDataGroupPersistence.class);
    private static final String DUPLICATED_IDENTIFIERS_MESSAGE = "Multiple identifiers detected, single expected";
    private DataGroupService dataGroupService;

    @Autowired
    public DeleteDataGroupPersistence(Validator validator, EventBus eventBus,
        DataGroupService dataGroupService) {
        super(validator, eventBus);
        this.dataGroupService = dataGroupService;
    }

    @Override
    protected String performBatchProcess(PresentationIdentifier dataGroupIdentifier) {
        String dataGroupId = dataGroupService.retrieveDataGroupIdFromIdentifier(dataGroupIdentifier);
        LOGGER.info("Trying to delete data group by id {}", dataGroupId);

        dataGroupService.delete(dataGroupId);
        return dataGroupId;
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }

    @Override
    protected ResponseItemExtended getBatchResponseItem(PresentationIdentifier item,
        ItemStatusCode statusCode, List<String> errorMessages) {

        ResponseItemExtended batchResponseItemExtended = Optional.ofNullable(item.getIdIdentifier())
            .map(id -> {
                ResponseItemExtended responseItemExtended = new ResponseItemExtended();
                responseItemExtended.setResourceId(id);
                return responseItemExtended;
            })
            .orElseGet(() -> {
                ResponseItemExtended responseItemExtended = new ResponseItemExtended();
                responseItemExtended.setResourceId(item.getNameIdentifier().getName());
                responseItemExtended
                    .setExternalServiceAgreementId(item.getNameIdentifier().getExternalServiceAgreementId());
                return responseItemExtended;
            });

        batchResponseItemExtended.setStatus(statusCode);
        batchResponseItemExtended.setErrors(errorMessages);
        return batchResponseItemExtended;
    }

    @Override
    protected List<String> customValidateConstraintsForRequestBody(PresentationIdentifier requestItem) {
        if (requestItem != null && !hasSingleIdentifier(requestItem)) {
            return Lists.newArrayList(DUPLICATED_IDENTIFIERS_MESSAGE);
        }
        return super.customValidateConstraintsForRequestBody(requestItem);
    }

    @Override
    protected DataGroupEvent createEvent(PresentationIdentifier request, String internalId) {
        return new DataGroupEvent()
            .withId(internalId)
            .withAction(DELETE);
    }

    private boolean hasSingleIdentifier(PresentationIdentifier identifier) {
        return identifier.getIdIdentifier() == null ^ identifier.getNameIdentifier() == null;
    }
}
