package com.backbase.accesscontrol.service.batch.legalentity;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.LegalEntityEvent;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LegalEntityBatchDeleteService extends LeanGenericBatchProcessor<String, ResponseItem, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalEntityBatchDeleteService.class);

    private PersistenceLegalEntityService persistenceLegalEntityService;

    private AccessToken accessToken;

    /**
     * Service that orchestrate deletion process of legal entities batch.
     *
     * @param validator                     - Validator
     * @param persistenceLegalEntityService - legal entity service
     * @param accessToken                   - Access token service
     */
    public LegalEntityBatchDeleteService(
        Validator validator, EventBus eventBus,
        PersistenceLegalEntityService persistenceLegalEntityService,
        AccessToken accessToken) {
        super(validator, eventBus);
        this.persistenceLegalEntityService = persistenceLegalEntityService;
        this.accessToken = accessToken;
    }

    /**
     * Delets batch of legal entities.
     *
     * @param batchLegalEntities - list of all legal entities defined by external ids
     * @return list with result status of deletion fo every legal entity
     */
    public List<ResponseItem> deleteBatchLegalEntities(PresentationBatchDeleteLegalEntities batchLegalEntities) {
        LOGGER.info("Deleting batch legal entities...");

        accessToken.validateAccessToken(batchLegalEntities.getAccessToken(), batchLegalEntities);

        List<String> listLegalEntities = new ArrayList<>(batchLegalEntities.getExternalIds());
        return processBatchItems(listLegalEntities);
    }

    @Override
    protected String performBatchProcess(String externalId) {
        return persistenceLegalEntityService.deleteLegalEntityByExternalId(externalId);
    }

    @Override
    protected ResponseItem getBatchResponseItem(String item, ItemStatusCode statusCode,
        List<String> errorMessages) {
        return new ResponseItem(item, statusCode, errorMessages);
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }

    @Override
    protected LegalEntityEvent createEvent(String request, String internalId) {
        return new LegalEntityEvent()
            .withAction(DELETE)
            .withId(internalId);
    }
}
