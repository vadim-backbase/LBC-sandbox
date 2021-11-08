package com.backbase.accesscontrol.service.batch.datagroup;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static org.apache.commons.lang.StringUtils.isEmpty;

import com.backbase.accesscontrol.domain.dto.PresentationActionDto;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class UpdateDataGroupItemsByIdentifierPersistence extends
    LeanGenericBatchProcessor<PresentationDataGroupItemPutRequestBody, ResponseItemExtended, String> {

    private static final String DUPLICATED_IDENTIFIERS_MESSAGE =
        "Multiple data group identifiers detected, single expected";

    private DataGroupService dataGroupService;

    public UpdateDataGroupItemsByIdentifierPersistence(Validator validator, DataGroupService dataGroupService,
        EventBus eventBus) {
        super(validator, eventBus);
        this.dataGroupService = dataGroupService;
    }

    @Override
    protected String performBatchProcess(PresentationDataGroupItemPutRequestBody item) {
        return dataGroupService.updateDataGroupItemsByIdIdentifier(item);
    }

    @Override
    protected List<String> customValidateConstraintsForRequestBody(
        PresentationDataGroupItemPutRequestBody requestItem) {
        if (requestItem.getDataGroupIdentifier() != null && !hasSingleIdentifier(
            requestItem.getDataGroupIdentifier())) {
            return Lists.newArrayList(DUPLICATED_IDENTIFIERS_MESSAGE);
        }
        return super.customValidateConstraintsForRequestBody(requestItem);
    }

    @Override
    protected DataGroupEvent createEvent(PresentationDataGroupItemPutRequestBody request, String internalId) {
        return new DataGroupEvent()
            .withAction(UPDATE)
            .withId(internalId);
    }

    private boolean hasSingleIdentifier(PresentationIdentifier identifier) {
        return identifier.getIdIdentifier() == null ^ identifier.getNameIdentifier() == null;
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }

    @Override
    protected ResponseItemExtended getBatchResponseItem(PresentationDataGroupItemPutRequestBody item,
        ItemStatusCode statusCode, List<String> errorMessages) {
        String resourceId = null;
        String externalServiceAgreementId = null;
        PresentationIdentifier identifier = item.getDataGroupIdentifier();
        if (identifier != null) {
            if (!isEmpty(identifier.getIdIdentifier())) {
                resourceId = identifier.getIdIdentifier();
            } else {
                NameIdentifier nameIdentifier = identifier.getNameIdentifier();
                if (!ObjectUtils.isEmpty(nameIdentifier)) {
                    resourceId = nameIdentifier.getName();
                    externalServiceAgreementId = nameIdentifier.getExternalServiceAgreementId();
                }
            }
        }
        return new ResponseItemExtended(resourceId, externalServiceAgreementId, statusCode,
            item.getAction() != null ? PresentationActionDto.fromValue(item.getAction().toString()) : null,
            errorMessages);
    }
}
