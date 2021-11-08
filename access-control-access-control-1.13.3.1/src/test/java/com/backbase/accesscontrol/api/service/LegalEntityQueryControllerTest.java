package com.backbase.accesscontrol.api.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.transformer.LegalEntityTransformer;
import com.backbase.accesscontrol.business.service.LegalEntityPAndPService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.dto.parameterholder.GetLegalEntitySegmentationHolder;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.LegalEntityGetResponseBodyToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.RootLegalEntityGetResponseBodyToLegalEntityItemBaseMapper;
import com.backbase.accesscontrol.mappers.model.query.service.SegmentationGetResponseBodyQueryToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemMapper;
import com.backbase.accesscontrol.mappers.model.query.service.ServiceAgreementItemToServiceAgreementItemQueryMapper;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.LegalEntityGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.RootLegalEntityGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityQueryControllerTest {

    @InjectMocks
    private LegalEntityQueryController legalEntityQueryController;
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Mock
    private LegalEntityTransformer legalEntityTransformer;
    @Mock
    private LegalEntityPAndPService legalEntityPAndPService;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(RootLegalEntityGetResponseBodyToLegalEntityItemBaseMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemQueryMapper.class)),
            spy(Mappers.getMapper(ServiceAgreementItemToServiceAgreementItemMapper.class)),
            spy(Mappers.getMapper(SegmentationGetResponseBodyQueryToLegalEntityItemMapper.class)),
            spy(Mappers.getMapper(LegalEntityGetResponseBodyToLegalEntityItemMapper.class))
        ));

    private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    @Test
    public void shouldGetLegalEntityMasterServiceAgreement() {
        ServiceAgreementItem masterServiceAgreement = new ServiceAgreementItem()
            .withName("name");

        when(legalEntityPAndPService.getMasterServiceAgreement(anyString()))
            .thenReturn(masterServiceAgreement);

        String legalEntityId = "id";
        legalEntityQueryController
            .getMasterServiceAgreement(legalEntityId);
        verify(legalEntityPAndPService)
            .getMasterServiceAgreement(eq(legalEntityId));
    }

    @Test
    public void shouldGetLegalEntitiesSegmentation() {

        String externalId = "externalId";
        Page<SegmentationGetResponseBodyQuery> legalEntity = new PageImpl<>(singletonList(new SegmentationGetResponseBodyQuery()
            .withExternalId(externalId)), Pageable.unpaged(), 1);
        when(persistenceLegalEntityService.getLegalEntitySegmentation(any(GetLegalEntitySegmentationHolder.class)))
            .thenReturn(legalEntity);
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.addHeader(TOTAL_COUNT_HEADER, 1);
        ResponseEntity<List<LegalEntityItem>> responseEntity = legalEntityQueryController.getSegmentation(
            externalId,
            "entitlements",
            "saId",
            "userId",
            "leId",
            "view",
            0,
            null,
            10);

        List<LegalEntityItem> legalEntityItems = responseEntity.getBody();
        HttpHeaders headers = responseEntity.getHeaders();
        assertEquals(1, headers.get(TOTAL_COUNT_HEADER).size());
        assertEquals(legalEntity.get().findFirst().get().getExternalId(), legalEntityItems.get(0).getExternalId());
        assertEquals(legalEntity.get().findFirst().get().getIsParent(), legalEntityItems.get(0).getIsParent());
    }

    @Test
    public void shouldGetRootLegalEntity() {
        RootLegalEntityGetResponseBody rootLegalEntity = new RootLegalEntityGetResponseBody();
        LegalEntity legalEntity = new LegalEntity();
        when(persistenceLegalEntityService.getRootLegalEntity())
            .thenReturn(legalEntity);
        when(legalEntityTransformer.transformLegalEntity(any(), eq(legalEntity)))
            .thenReturn(rootLegalEntity);
        LegalEntityItemBase returnedLegalEntity =
            legalEntityQueryController.getRootLegalEntity().getBody();

        assertEquals(rootLegalEntity.getExternalId(), returnedLegalEntity.getExternalId());
        assertEquals(rootLegalEntity.getId(), returnedLegalEntity.getId());
        assertEquals(rootLegalEntity.getAdditions(), returnedLegalEntity.getAdditions());
    }

    @Test
    public void shouldGetLegalEntityById() {
        LegalEntityGetResponseBody legalEntity = new LegalEntityGetResponseBody();

        when(legalEntityPAndPService.getLegalEntityByIdAsResponseBody(anyString()))
            .thenReturn(legalEntity);

        LegalEntityItem returnedLegalEntity =
            legalEntityQueryController
                .getLegalEntity("id").getBody();

        assertEquals(legalEntity.getIsParent(), returnedLegalEntity.getIsParent());
        assertEquals(legalEntity.getParentId(), returnedLegalEntity.getParentId());
    }

}
