package com.backbase.accesscontrol.business.legalentity;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.mappers.BatchResponseItemMapper;
import com.backbase.accesscontrol.service.batch.legalentity.LegalEntityBatchService;
import com.backbase.buildingblocks.backend.internalrequest.DefaultInternalRequestContext;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.google.common.collect.Lists;
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
public class UpdateBatchLegalEntityTest {

    @InjectMocks
    private UpdateBatchLegalEntity updateBatchLegalEntity;

    @Mock
    private LegalEntityBatchService legalEntityBatchService;

    @Spy
    private BatchResponseItemMapper batchResponseItemMapper = Mappers.getMapper(BatchResponseItemMapper.class);

    @Test
    public void shouldUpdateLegalEntityByExternalId() {

        LegalEntityPut legalEntityPut1 = new LegalEntityPut()
            .withExternalId("LE-01")
            .withLegalEntity(new LegalEntity().withName("name")
                .withExternalId("LE-01")
                .withActivateSingleServiceAgreement(true)
                .withParentExternalId(null)
                .withType(LegalEntityType.CUSTOMER));
        LegalEntityPut legalEntityPut2 = new LegalEntityPut()
            .withExternalId("LE-02")
            .withLegalEntity(new LegalEntity()
                .withName("name")
                .withExternalId("LE-02")
                .withParentExternalId(null)
                .withActivateSingleServiceAgreement(false)
                .withType(LegalEntityType.CUSTOMER));

        ResponseItem successfulBatchResponseItem = new ResponseItem("id1", ItemStatusCode.HTTP_STATUS_OK,
            new ArrayList<>());
        ResponseItem failedBatchResponseItem = new ResponseItem("id2", ItemStatusCode.HTTP_STATUS_BAD_REQUEST,
            Collections.singletonList("error"));

        InternalRequest<List<LegalEntityPut>> request = getInternalRequest(asList(legalEntityPut1, legalEntityPut2),
            new DefaultInternalRequestContext());

        mockPutLegalEntityAccessControl(asList(successfulBatchResponseItem, failedBatchResponseItem));

        InternalRequest<List<BatchResponseItem>> response = updateBatchLegalEntity
            .updateBatchLegalEntity(request);

        verify(legalEntityBatchService).processBatchItems(eq(Lists.newArrayList(legalEntityPut1, legalEntityPut2)));

        assertEquals(asList(
            new BatchResponseItem()
                .withResourceId("id1")
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK),
            new BatchResponseItem()
                .withResourceId("id2")
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withErrors(Collections.singletonList("error"))), response.getData());
    }

    private void mockPutLegalEntityAccessControl(List<ResponseItem> data) {
        when(legalEntityBatchService.processBatchItems(anyList())).thenReturn(data);
    }
}