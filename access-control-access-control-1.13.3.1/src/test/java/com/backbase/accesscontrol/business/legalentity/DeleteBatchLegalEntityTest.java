package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_BAD_REQUEST;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_OK;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.legalentity.LegalEntityBatchDeleteService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationBatchDeleteLegalEntities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteBatchLegalEntityTest {

    @InjectMocks
    private DeleteBatchLegalEntity deleteBatchLegalEntity;
    @Mock
    private LegalEntityBatchDeleteService legalEntityBatchDeleteService;
    @Spy
    private BatchResponseItemMapper batchResponseItemMapper = Mappers.getMapper(BatchResponseItemMapper.class);

    @Test
    public void shouldDeleteLegalEntityByExternalId() {

        String[] externalIds = new String[]{"LE-01", "LE-02"};

        ResponseItem successfulBatchResponseItem = new ResponseItem("id1", HTTP_STATUS_OK, new ArrayList<>());
        ResponseItem failedBatchResponseItem = new ResponseItem("id2", HTTP_STATUS_BAD_REQUEST,
            Collections.singletonList("error"));

        InternalRequest<PresentationBatchDeleteLegalEntities> request = getInternalRequest(
            new PresentationBatchDeleteLegalEntities()
                .withAccessToken("123")
                .withExternalIds(new LinkedHashSet<>(asList(externalIds)))
        );

        mockPutLegalEntityAccessControl(asList(successfulBatchResponseItem, failedBatchResponseItem));

        InternalRequest<List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem>> response = deleteBatchLegalEntity
            .deleteBatchLegalEntity(request);

        verify(legalEntityBatchDeleteService).deleteBatchLegalEntities(eq(new PresentationBatchDeleteLegalEntities()
            .withAccessToken("123").withExternalIds(new LinkedHashSet<>(asList(externalIds)))));

        assertEquals(asList(
            new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem()
                .withResourceId("id1")
                .withStatus(
                    com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode.HTTP_STATUS_OK),
            new BatchResponseItem()
                .withResourceId("id2")
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withErrors(Collections.singletonList("error"))), response.getData());
    }

    private void mockPutLegalEntityAccessControl(List<ResponseItem> responseItems) {

        doReturn(responseItems).when(legalEntityBatchDeleteService)
            .deleteBatchLegalEntities(any(PresentationBatchDeleteLegalEntities.class));
    }
}