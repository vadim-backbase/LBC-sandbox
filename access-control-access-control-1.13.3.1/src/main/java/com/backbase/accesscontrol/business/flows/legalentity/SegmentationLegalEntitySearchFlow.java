package com.backbase.accesscontrol.business.flows.legalentity;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.SegmentationLegalEntitiesSearchParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.SegmentationLegalEntityMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SegmentationLegalEntitySearchFlow extends AbstractFlow<SegmentationLegalEntitiesSearchParameters,
    RecordsDto<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationLegalEntitySearchFlow.class);

    private PersistenceLegalEntityService service;
    private SegmentationLegalEntityMapper mapper;

    /**
     * {@inheritDoc}
     */
    @Override
    protected RecordsDto<com.backbase.presentation.legalentity.rest.spec.v2.legalentities
        .SegmentationGetResponseBody> execute(SegmentationLegalEntitiesSearchParameters searchParameters) {

        LOGGER.info("Get Legal entities where user has access with search {}.", searchParameters);

        SearchAndPaginationParameters parameters = new SearchAndPaginationParameters(searchParameters.getFrom(),
            searchParameters.getSize(), searchParameters.getQuery(), searchParameters.getCursor());
        GetLegalEntitySegmentationHolder holder = new GetLegalEntitySegmentationHolder()
            .withBusinessFunction(searchParameters.getBusinessFunction())
            .withLegalEntityId(searchParameters.getLegalEntityId())
            .withServiceAgreementId(searchParameters.getServiceAgreementId())
            .withUserId(searchParameters.getUserId())
            .withPrivilege(searchParameters.getPrivilege())
            .withSearchAndPaginationParameters(parameters);

        Page<SegmentationGetResponseBodyQuery> legalEntitiesPage = service
            .getLegalEntitySegmentation(holder);

        List<SegmentationGetResponseBodyQuery> response = legalEntitiesPage.get().collect(Collectors.toList());

        List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities
            .SegmentationGetResponseBody> presentationResponseBody = mapper.toPresentation(response);
        return new RecordsDto<>(legalEntitiesPage.getTotalElements(), presentationResponseBody);
    }
}
