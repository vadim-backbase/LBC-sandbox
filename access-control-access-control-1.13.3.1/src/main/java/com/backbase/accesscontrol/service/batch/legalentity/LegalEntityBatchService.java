package com.backbase.accesscontrol.service.batch.legalentity;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import java.util.List;
import javax.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class LegalEntityBatchService extends LeanGenericBatchProcessor<LegalEntityPut, ResponseItem, String> {

    private PersistenceLegalEntityService persistenceLegalEntityService;

    public LegalEntityBatchService(Validator validator, EventBus eventBus,
        PersistenceLegalEntityService persistenceLegalEntityService) {
        super(validator, eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    @Override
    public String performBatchProcess(LegalEntityPut legalEntity) {
        return persistenceLegalEntityService.updateLegalEntityFields(legalEntity.getExternalId(), legalEntity.getLegalEntity());
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }

    @Override
    protected ResponseItem getBatchResponseItem(LegalEntityPut item, ItemStatusCode statusCode,
        List<String> errorMessages) {
        return new ResponseItem(item.getExternalId(), statusCode, errorMessages);
    }

    @Override
    protected LegalEntityEvent createEvent(LegalEntityPut request, String internalId) {
        return new LegalEntityEvent()
            .withAction(Action.UPDATE)
            .withId(internalId);
    }
}