package com.backbase.accesscontrol.business.flows.legalentity;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.business.service.LegalEntitiesService;
import com.backbase.accesscontrol.dto.ExternalLegalEntitySearchParameters;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExternalLegalEntitySearchFlow extends AbstractFlow<ExternalLegalEntitySearchParameters,
    RecordsDto<LegalEntityExternalData>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalLegalEntitySearchFlow.class);

    private boolean validateOnPersistence;
    private LegalEntitiesService legalEntitiesService;
    private PersistenceLegalEntityService persistenceLegalEntityService;


    /**
     * Constructor documentation.
     *
     * @param validateOnPersistence         - defines whether to validate on persistence
     * @param legalEntitiesService          - service client
     * @param persistenceLegalEntityService - persistence service
     */
    public ExternalLegalEntitySearchFlow(
        @Value("${backbase.legalentity.externalSearch.validateOnPersistence:false}") boolean validateOnPersistence,
        LegalEntitiesService legalEntitiesService,
        PersistenceLegalEntityService persistenceLegalEntityService) {

        this.validateOnPersistence = validateOnPersistence;
        this.legalEntitiesService = legalEntitiesService;
        this.persistenceLegalEntityService = persistenceLegalEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final RecordsDto<LegalEntityExternalData> execute(ExternalLegalEntitySearchParameters searchParameters) {

        LOGGER.info("Get Legal entities external data with search {}.", searchParameters);

        ListElementsWrapper<LegalEntityExternalData> integrationResponse = legalEntitiesService
            .getLegalEntities(searchParameters.getField(), searchParameters.getTerm(), searchParameters.getFrom(),
                searchParameters.getCursor(), searchParameters.getSize());

        List<LegalEntityExternalData> presentationLegalEntities = integrationResponse.getRecords();

        if (validateOnPersistence && isNotEmpty(presentationLegalEntities)) {

            Set<String> externalIds = presentationLegalEntities.stream()
                .map(LegalEntityExternalData::getExternalId)
                .collect(Collectors.toSet());

            Map<String, String> externalToInternalIdMap =
                persistenceLegalEntityService
                    .findInternalByExternalIdsForLegalEntity(externalIds);

            presentationLegalEntities.forEach(le -> populateInternalIds(le, externalToInternalIdMap));
        }
        return new RecordsDto<>(integrationResponse.getTotalNumberOfRecords(), presentationLegalEntities);
    }

    private void populateInternalIds(LegalEntityExternalData legalEntity, Map<String, String> externalToInternalIdMap) {

        if (externalToInternalIdMap.containsKey(legalEntity.getExternalId())) {
            legalEntity.setId(externalToInternalIdMap.get(legalEntity.getExternalId()));
        }
    }
}
