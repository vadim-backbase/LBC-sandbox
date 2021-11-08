package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsFailedResponseItem;
import static com.backbase.accesscontrol.matchers.BatchResponseItemMatcher.containsSuccessfulResponseItem;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.CreateLegalEntitiesPostResponseBodyToLegalEntityItemIdMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntitiesBatchDeleteToPresentationBatchDeleteLegalEntitiesMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntitiesPostResponseBodyToLegalEntityItemIdMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityByExternalIdGetResponseBodyToLegalEntityItemBaseMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityByIdGetResponseBodyToLegalEntityItemBaseMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityByIdGetResponseBodyToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityCreateItemToCreateLegalEntitiesPostRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityCreateItemToLegalEntitiesPostRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityPutToLegalEntityPutMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.LegalEntityUpdateItemToLegalEntityByExternalIdPutRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.MasterServiceAgreementGetResponseBodyToGetServiceAgreementMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.service.ResponseItemMapper;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.LegalEntityFlowService;
import com.backbase.accesscontrol.service.facades.LegalEntityService;
import com.backbase.accesscontrol.service.rest.spec.model.GetServiceAgreement;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityCreateItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItem;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityItemId;
import com.backbase.accesscontrol.service.rest.spec.model.LegalEntityUpdateItem;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.BatchResponseStatusCode;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.CreateLegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntity;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByExternalIdPutRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityByIdGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityPut;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.MasterServiceAgreementGetResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityServiceApiControllerTest {

    @Mock
    private ParameterValidationService parameterValidationService;

    @Mock
    private LegalEntityService legalEntityService;

    @Mock
    private LegalEntityFlowService legalEntityFlowService;

    @InjectMocks
    private LegalEntityServiceApiController legalEntityServiceApiController;

    @Spy
    private PayloadConverter payloadConverter = new PayloadConverter(asList(
        spy(Mappers.getMapper(LegalEntitiesBatchDeleteToPresentationBatchDeleteLegalEntitiesMapper.class)),
        spy(Mappers.getMapper(LegalEntityByExternalIdGetResponseBodyToLegalEntityItemBaseMapper.class)),
        spy(Mappers.getMapper(LegalEntityByIdGetResponseBodyToLegalEntityItemBaseMapper.class)),
        spy(Mappers.getMapper(LegalEntityByIdGetResponseBodyToLegalEntityItemMapper.class)),
        spy(Mappers.getMapper(LegalEntityCreateItemToCreateLegalEntitiesPostRequestBodyMapper.class)),
        spy(Mappers.getMapper(LegalEntityCreateItemToLegalEntitiesPostRequestBodyMapper.class)),
        spy(Mappers.getMapper(LegalEntityUpdateItemToLegalEntityByExternalIdPutRequestBodyMapper.class)),
        spy(Mappers.getMapper(MasterServiceAgreementGetResponseBodyToGetServiceAgreementMapper.class)),
        spy(Mappers.getMapper(ResponseItemMapper.class)),
        spy(Mappers.getMapper(LegalEntitiesPostResponseBodyToLegalEntityItemIdMapper.class)),
        spy(Mappers.getMapper(CreateLegalEntitiesPostResponseBodyToLegalEntityItemIdMapper.class)),
        spy(Mappers.getMapper(LegalEntityPutToLegalEntityPutMapper.class))

    ));

    @Test
    public void shouldReturnSubEntities() {

        // Given
        RecordsDto<SubEntitiesPostResponseBody> recordsDto = new RecordsDto<>(0L, Collections.emptyList());
        String parentEntityId = "hello";
        String cursor = "";
        Integer from = 0;
        Integer size = 10;

        when(legalEntityFlowService.getSubLegalEntities(any())).thenReturn(recordsDto);

        // When
        legalEntityServiceApiController
            .getSubEntities(parentEntityId, cursor, from, size, null);

        ArgumentCaptor<GetLegalEntitiesRequestDto> requestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);

        // Then
        verify(legalEntityFlowService).getSubLegalEntities(requestCaptor.capture());
        GetLegalEntitiesRequestDto captorValue = requestCaptor.getValue();

        assertThat(captorValue.getParentEntityId(), is(parentEntityId));
        assertThat(captorValue.getCursor(), is(cursor));
        assertThat(captorValue.getFrom(), is(from));
        assertThat(captorValue.getSize(), is(size));
        assertThat(captorValue.getExcludeIds(), hasSize(0));
        assertNull(captorValue.getQuery());
    }

    @Test
    public void shouldReturnLegalEntityById() {
        when(legalEntityService.getLegalEntityById(eq("001")))
            .thenReturn(new LegalEntityByIdGetResponseBody().withId("001"));

        LegalEntityItem legalEntityById = legalEntityServiceApiController
            .getLegalEntityById("001").getBody();

        verify(legalEntityService, times(1)).getLegalEntityById(eq("001"));
        assertEquals("001", legalEntityById.getId());
    }

    @Test
    public void shouldThrowInternalServerExceptionWhenGetSubEntitiesIsInvoked() {
        when(legalEntityFlowService
            .getSubLegalEntities(any()))
            .thenThrow(new InternalServerErrorException().withMessage("Cannot Fetch Data."));

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
            () -> legalEntityServiceApiController
                .getSubEntities(null, null, null, null, null));

        assertEquals("Cannot Fetch Data.", exception.getMessage());
    }

    @Test
    public void shouldReturnLegalEntityIdAfterCreation() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setExternalId("externalId");
        legalEntitiesPostRequestBody.setName("legalEntity");
        legalEntitiesPostRequestBody.setActivateSingleServiceAgreement(true);
        legalEntitiesPostRequestBody.setParentExternalId("parentext");

        LegalEntitiesPostRequestBody requestBody = new LegalEntitiesPostRequestBody()
            .withExternalId("externalId")
            .withActivateSingleServiceAgreement(true)
            .withName("legalEntity")
            .withParentExternalId("parentext");

        LegalEntityItemId mockLegalEntitiesPostResponseBody = new LegalEntityItemId().additions(new HashMap<>());
        mockLegalEntitiesPostResponseBody.setId("id");
        mockLegalEntitiesPostResponseBody.setAdditions(new HashMap<>());

        LegalEntitiesPostResponseBody responseBody = new LegalEntitiesPostResponseBody()
            .withId("id");
        when(legalEntityService.createLegalEntity(any(LegalEntitiesPostRequestBody.class)))
            .thenReturn(responseBody);

        LegalEntityItemId legalEntitiesPostResponseBody = legalEntityServiceApiController
            .postLegalEntities(legalEntitiesPostRequestBody).getBody();

        verify(legalEntityService, times(1)).createLegalEntity(eq(requestBody));
        assertEquals(mockLegalEntitiesPostResponseBody, legalEntitiesPostResponseBody);
    }

    @Test
    public void shouldReturnLegalEntityIdAfterCreationWithoutMaster() {
        LegalEntityCreateItem legalEntitiesPostRequestBody = new LegalEntityCreateItem();
        legalEntitiesPostRequestBody.setName("legalEntity");
        legalEntitiesPostRequestBody.setActivateSingleServiceAgreement(false);
        legalEntitiesPostRequestBody.setParentExternalId("parentext");

        CreateLegalEntitiesPostRequestBody requestBody = new CreateLegalEntitiesPostRequestBody()
            .withName("legalEntity")
            .withActivateSingleServiceAgreement(false)
            .withParentExternalId("parentext");
        CreateLegalEntitiesPostResponseBody createLegalEntitiesPostResponseBody = new CreateLegalEntitiesPostResponseBody()
            .withId("id");

        LegalEntityItemId responseId = new LegalEntityItemId().additions(new HashMap<>());
        responseId.setId("id");
        responseId.setAdditions(new HashMap<>());

        when(legalEntityService.addLegalEntity(any(CreateLegalEntitiesPostRequestBody.class)))
            .thenReturn(createLegalEntitiesPostResponseBody);

        LegalEntityItemId legalEntitiesPostResponseBody = legalEntityServiceApiController
            .postCreateLegalEntities(legalEntitiesPostRequestBody).getBody();

        verify(legalEntityService, times(1)).addLegalEntity(eq(requestBody));
        assertEquals(responseId, legalEntitiesPostResponseBody);
    }

    @Test
    public void shouldReturnLegalEntityByExternalId() {
        String externalId = "123";
        LegalEntityByExternalIdGetResponseBody legalEntityByExternalIdGetResponseBody = new LegalEntityByExternalIdGetResponseBody()
            .withId("id")
            .withExternalId(externalId)
            .withName("legalEntity")
            .withType(LegalEntityType.CUSTOMER);

        LegalEntityItemBase response = new LegalEntityItemBase();
        response.setId("id");
        response.setExternalId(externalId);
        response.setName("legalEntity");
        response.setType(com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.CUSTOMER);
        response.setAdditions(emptyMap());

        when(legalEntityService.getLegalEntityByExternalId(eq(externalId)))
            .thenReturn(legalEntityByExternalIdGetResponseBody);

        LegalEntityItemBase legalEntityByExternalId = legalEntityServiceApiController
            .getLegalEntityByExternalId(externalId).getBody();

        verify(legalEntityService, times(1)).getLegalEntityByExternalId(eq(externalId));
        assertEquals(response, legalEntityByExternalId);
    }

    @Test
    public void shouldUpdateLegalEntityByExternalId() {
        String externalId = "exId";
        LegalEntityByExternalIdPutRequestBody legalEntityByExternalIdPutRequestBody = new LegalEntityByExternalIdPutRequestBody()
            .withType(LegalEntityType.BANK);

        LegalEntityUpdateItem request = new LegalEntityUpdateItem();
        request.setType(com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.BANK);
        doNothing().when(legalEntityService)
            .updateLegalEntityByExternalId(eq(legalEntityByExternalIdPutRequestBody), eq(externalId));

        legalEntityServiceApiController
            .putLegalEntityByExternalId(externalId, request).getBody();

        verify(legalEntityService, times(1))
            .updateLegalEntityByExternalId(eq(legalEntityByExternalIdPutRequestBody), eq(externalId));
    }

    @Test
    public void shouldUpdateBatchLegalEntity() {
        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut legalEntityPut1 = new com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut();
        legalEntityPut1.setExternalId("LE-01");
        LegalEntityCreateItem legalEntityCreateItem1 = new LegalEntityCreateItem();
        legalEntityCreateItem1.setName("name");
        legalEntityCreateItem1.setExternalId("LE-01");
        legalEntityCreateItem1.setParentExternalId(null);
        legalEntityCreateItem1.setType(com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.CUSTOMER);
        legalEntityPut1.setLegalEntity(legalEntityCreateItem1);

        com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut legalEntityPut2 = new com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut();
        legalEntityPut2.setExternalId("LE-02");
        LegalEntityCreateItem legalEntityCreateItem2 = new LegalEntityCreateItem();
        legalEntityCreateItem2.setName("name");
        legalEntityCreateItem2.setExternalId("LE-02");
        legalEntityCreateItem2.setParentExternalId(null);
        legalEntityCreateItem2.setType(com.backbase.accesscontrol.service.rest.spec.model.LegalEntityType.CUSTOMER);
        legalEntityPut2.setLegalEntity(legalEntityCreateItem2);

        BatchResponseItem successfulBatchResponseItem = new BatchResponseItem()
            .withResourceId("LE-01")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK);
        BatchResponseItem failedBatchResponseItem = new BatchResponseItem()
            .withResourceId("LE-02")
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
            .withErrors(Collections.singletonList("error"));

        List<com.backbase.accesscontrol.service.rest.spec.model.LegalEntityPut> legalEntities = asList(legalEntityPut1,
            legalEntityPut2);

        LegalEntityPut le1 = new LegalEntityPut()
            .withExternalId("LE-01")
            .withLegalEntity(new LegalEntity().withName("name")
                .withExternalId("LE-01")
                .withParentExternalId(null)
                .withType(LegalEntityType.CUSTOMER));
        LegalEntityPut le2 = new LegalEntityPut()
            .withExternalId("LE-02")
            .withLegalEntity(new LegalEntity()
                .withName("name")
                .withExternalId("LE-02")
                .withParentExternalId(null)
                .withType(LegalEntityType.CUSTOMER));

        List<LegalEntityPut> les = asList(le1, le2);

        when(legalEntityService.updateBatchLegalEntities(any()))
            .thenReturn(asList(successfulBatchResponseItem, failedBatchResponseItem));

        List<com.backbase.accesscontrol.service.rest.spec.model.BatchResponseItem> batchResponseItems = legalEntityServiceApiController
            .putLegalEntities(legalEntities).getBody();

        assertTrue(containsSuccessfulResponseItem(batchResponseItems, successfulBatchResponseItem));
        assertTrue(containsFailedResponseItem(batchResponseItems, failedBatchResponseItem));
    }


    @Test
    public void shouldGetMasterServiceAgreementByExternalLegalEntityId() {
        MasterServiceAgreementGetResponseBody data = new MasterServiceAgreementGetResponseBody().withId("id");
        GetServiceAgreement responseBody = new GetServiceAgreement();
        responseBody.setId("id");
        responseBody.setIsMaster(false);
        responseBody.setAdditions(emptyMap());

        when(legalEntityService.getMasterServiceAgreementByExternalId(eq("001"))).thenReturn(data);

        GetServiceAgreement masterServiceAgreement = legalEntityServiceApiController
            .getMasterServiceAgreementByExternalLegalEntity("001").getBody();

        assertEquals(responseBody, masterServiceAgreement);
    }
}
