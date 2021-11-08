package com.backbase.accesscontrol.business.flows.legalentity;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.SegmentationLegalEntitiesSearchParameters;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.SegmentationLegalEntityMapper;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityBase.Type;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class SegmentationLegalEntitySearchFlowTest {

    @Mock
    private PersistenceLegalEntityService service;
    @Mock
    private SegmentationLegalEntityMapper mapper;
    @InjectMocks
    private SegmentationLegalEntitySearchFlow segmentationLegalEntitySearchFlow;

    @Test
    public void shouldExecuteFlow() {
        List<SegmentationGetResponseBodyQuery> segmentationLegalEntities = Arrays.asList(
            new SegmentationGetResponseBodyQuery()
                .withExternalId("LE_EXID1")
                .withId("LE_ID1")
                .withIsParent(true)
                .withParentId("parent_id1")
                .withType(Type.CUSTOMER),
            new SegmentationGetResponseBodyQuery()
                .withExternalId("LE_EXID2")
                .withId("LE_ID2")
                .withIsParent(false)
                .withParentId("parent_id2")
                .withType(Type.CUSTOMER));

        List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody> presentationResponse = Arrays
            .asList(
                new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody()
                    .withExternalId("LE_EXID1")
                    .withId("LE_ID1")
                    .withType(LegalEntityType.CUSTOMER)
                    .withIsParent(true)
                    .withParentId("parent_id1"),
                new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody()
                    .withExternalId("LE_EXID2")
                    .withId("LE_ID2")
                    .withType(LegalEntityType.CUSTOMER)
                    .withIsParent(false)
                    .withParentId("parent_id2"));

        SegmentationLegalEntitiesSearchParameters searchParameters = new SegmentationLegalEntitiesSearchParameters(
            "LE1", "ManageAccounts", "userid", "view", 0, "", 2);
        searchParameters.setServiceAgreementId("said");
        SearchAndPaginationParameters parameters = new SearchAndPaginationParameters(searchParameters.getFrom(),
            searchParameters.getSize(), searchParameters.getQuery(), searchParameters.getCursor());
        GetLegalEntitySegmentationHolder holder = new GetLegalEntitySegmentationHolder()
            .withBusinessFunction(searchParameters.getBusinessFunction())
            .withLegalEntityId(searchParameters.getLegalEntityId())
            .withServiceAgreementId(searchParameters.getServiceAgreementId())
            .withUserId(searchParameters.getUserId())
            .withPrivilege(searchParameters.getPrivilege())
            .withSearchAndPaginationParameters(parameters);

        Page<SegmentationGetResponseBodyQuery> legalEntitiesPage = new PageImpl<>(
            segmentationLegalEntities);

        when(service.getLegalEntitySegmentation(refEq(holder))).thenReturn(legalEntitiesPage);
        when(mapper.toPresentation(segmentationLegalEntities)).thenReturn(presentationResponse);

        RecordsDto<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody> res = segmentationLegalEntitySearchFlow
            .start(searchParameters);

        assertEquals(presentationResponse, res.getRecords());
        assertEquals(Long.valueOf(2), res.getTotalNumberOfRecords());
    }
}