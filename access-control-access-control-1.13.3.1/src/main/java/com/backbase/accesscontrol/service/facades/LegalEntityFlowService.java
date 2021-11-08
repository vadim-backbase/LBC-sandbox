package com.backbase.accesscontrol.service.facades;

import com.backbase.accesscontrol.business.flows.legalentity.CreateLegalEntityAsParticipantFlow;
import com.backbase.accesscontrol.business.flows.legalentity.CreateLegalEntityFlow;
import com.backbase.accesscontrol.business.flows.legalentity.ExternalLegalEntitySearchFlow;
import com.backbase.accesscontrol.business.flows.legalentity.GetSubLegalEntitlesFlow;
import com.backbase.accesscontrol.business.flows.legalentity.SegmentationLegalEntitySearchFlow;
import com.backbase.accesscontrol.dto.ExternalLegalEntitySearchParameters;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SegmentationLegalEntitiesSearchParameters;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityExternalData;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LegalEntityFlowService {

    private CreateLegalEntityFlow createLegalEntityFlow;
    private CreateLegalEntityAsParticipantFlow createLegalEntityAsParticipantFlow;
    private ExternalLegalEntitySearchFlow externalLegalEntitySearchFlow;
    private SegmentationLegalEntitySearchFlow segmentationLegalEntitySearchFlow;
    private GetSubLegalEntitlesFlow getSubLegalEntitlesFlow;

    /**
     * Calls create legal entity on presentation flow.
     *
     * @param requestBody - request body.
     * @return - structure with the id of the newly created legal entity.
     */
    public LegalEntitiesPostResponseBody createLegalEntityWithInternalParentIdFlow(
        PresentationCreateLegalEntityItemPostRequestBody requestBody) {

        return createLegalEntityFlow
            .start(requestBody);
    }
    
    /**
     * Calls create legal entity as participant on presentation flow.
     * 
     * @param requestBody - request body.
     * @return - structure with the ids of the newly created legal entity and service agreement if
     *         applicable.
     */
    public LegalEntityAsParticipantPostResponseBody createLegalEntityAsParticipant(
                    LegalEntityAsParticipantPostRequestBody requestBody) {
        return createLegalEntityAsParticipantFlow.start(requestBody);
    }

    /**
     * Get external legal entity data by search parameters.
     *
     * @param searchParameters - search parameters
     * @return RequestDto of type {@link LegalEntityExternalData}
     */
    public RecordsDto<LegalEntityExternalData> getExternalLegalEntityData(
        ExternalLegalEntitySearchParameters searchParameters) {

        return externalLegalEntitySearchFlow.start(searchParameters);
    }

    /**
     * Get segmented legal entity data by search parameters.
     *
     * @param searchParameters - search parameters
     * @return RequestDto of type {@link SegmentationGetResponseBody}
     */
    public RecordsDto<SegmentationGetResponseBody> getSegmentationLegalEntity(
        SegmentationLegalEntitiesSearchParameters searchParameters) {

        return segmentationLegalEntitySearchFlow.start(searchParameters);
    }


    /**
     * Get sub legal entities.
     *
     * @param legalEntitiesRequestDto object which contain legal entity ids to be excluded, parent legal entity id,
     *                                cursor, from, size, search query
     * @return responseBody {@link RecordsDto} of type {@link SubEntitiesPostResponseBody}
     */
    public RecordsDto<SubEntitiesPostResponseBody> getSubLegalEntities(
        GetLegalEntitiesRequestDto legalEntitiesRequestDto) {
        return getSubLegalEntitlesFlow.start(legalEntitiesRequestDto);
    }
}
