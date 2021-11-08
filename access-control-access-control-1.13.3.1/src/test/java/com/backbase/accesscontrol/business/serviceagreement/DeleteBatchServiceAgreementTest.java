package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.DeleteBatchServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)

public class DeleteBatchServiceAgreementTest {

    @InjectMocks
    private DeleteBatchServiceAgreement deleteBatchServiceAgreement;
    @Mock
    private InternalRequestContext internalRequestContext;
    @Spy
    private BatchResponseItemMapper batchResponseItemMapper = Mappers.getMapper(BatchResponseItemMapper.class);
    @Mock
    private DeleteBatchServiceAgreementService deleteBatchServiceAgreementService;

    @Test
    public void shouldDeleteServiceAgreements() {
        ResponseItem successfulBatchResponseItem = new ResponseItem()
            .withResourceId("saName")
            .withStatus(ItemStatusCode.HTTP_STATUS_OK)
            .withErrors(new ArrayList<>());
        ResponseItem failedBatchResponseItem = new ResponseItem()
            .withResourceId("externalId")
            .withStatus(ItemStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList("error"));

        PresentationServiceAgreementIdentifier identifierName = new PresentationServiceAgreementIdentifier()
            .withNameIdentifier("saName");
        PresentationServiceAgreementIdentifier identifierExternalId = new PresentationServiceAgreementIdentifier()
            .withExternalIdIdentifier("externalId");

        List<PresentationServiceAgreementIdentifier> identifiers = asList(identifierName, identifierExternalId);
        InternalRequest<PresentationDeleteServiceAgreements> request = getInternalRequest(
            new PresentationDeleteServiceAgreements()
                .withAccessToken("123")
                .withServiceAgreementIdentifiers(identifiers)
        );

        when(deleteBatchServiceAgreementService.deleteBatchServiceAgreement
            (any(PresentationDeleteServiceAgreements.class)))
            .thenReturn(asList(successfulBatchResponseItem, failedBatchResponseItem));

        InternalRequest<List<BatchResponseItem>>
            response = deleteBatchServiceAgreement.deleteBatchServiceAgreement(request);

        verify(deleteBatchServiceAgreementService, times(1))
            .deleteBatchServiceAgreement(request.getData());
        assertEquals(2, response.getData().size());
    }
}