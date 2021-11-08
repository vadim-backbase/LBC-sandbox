package com.backbase.accesscontrol.api.client;

import static com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType.BANK;
import static com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType.CUSTOMER;
import static com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType.fromValue;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_012;
import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_LE_021;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_045;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.auth.ServiceAgreementIdProvider;
import com.backbase.accesscontrol.client.rest.spec.model.ExistingCustomServiceAgreement;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantCreateItem;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityAsParticipantItemId;
import com.backbase.accesscontrol.client.rest.spec.model.LegalEntityItemBase;
import com.backbase.accesscontrol.client.rest.spec.model.ParticipantInfo;
import com.backbase.accesscontrol.client.rest.spec.model.ParticipantOf;
import com.backbase.accesscontrol.client.rest.spec.model.SearchSubEntitiesParameters;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntitiesGetResponseBodyToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityAsParticipantCreateItemToLegalEntityAsParticipantPostRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityAsParticipantPostResponseBodyToLegalEntityAsParticipantItemIdMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityByExternalIdGetResponseBodyToLegalEntityItemBaseClientMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityByIdGetResponseBodyToLegalEntityItemIdMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityCreateItemToPresentationCreateLegalEntityItemPostRequestBodyMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityExternalDataToLegalEntityExternalDataItemMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.LegalEntityForUserGetResponseBodyToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.MasterServiceAgreementGetResponseBodyToGetServiceAgreementClientMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.SearchSubEntitiesParametersToSearchSubEntitiesParametersMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.SegmentationGetResponseBodyToLegalEntityItemMapper;
import com.backbase.accesscontrol.mappers.model.legalenitty.client.SubEntitiesPostResponseBodyToLegalEntityItemBaseClientMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.accesscontrol.service.facades.LegalEntityFlowService;
import com.backbase.accesscontrol.service.impl.UserAccessPermissionCheckService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostRequestBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntityAsParticipantPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.testcontainers.shaded.com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityControllerTest {

    private static final String LE_EXTERNAL_ID_KEY = "LE_EXTERNAL_ID";
    private static final String LE_EXTERNAL_ID_VALUE = "12345678";
    
    @Mock
    private LegalEntityFlowService legalEntityFlowService;
    @Mock
    private ParameterValidationService parameterValidationService;
    @Mock
    private ServiceAgreementIdProvider serviceAgreementProvider;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private AccessControlValidator accessControlValidator;
    @Mock
    private UserAccessPermissionCheckService permissionCheckService;

    @InjectMocks
    private LegalEntityController legalEntityController;

    @Spy
    private PayloadConverter payloadConverter = new PayloadConverter(asList(
        spy(Mappers.getMapper(LegalEntitiesGetResponseBodyToLegalEntityItemMapper.class)),
        spy(Mappers.getMapper(LegalEntityByExternalIdGetResponseBodyToLegalEntityItemBaseClientMapper.class)),
        spy(Mappers.getMapper(LegalEntityByIdGetResponseBodyToLegalEntityItemIdMapper.class)),
        spy(Mappers.getMapper(LegalEntityCreateItemToPresentationCreateLegalEntityItemPostRequestBodyMapper.class)),
        spy(Mappers.getMapper(LegalEntityAsParticipantCreateItemToLegalEntityAsParticipantPostRequestBodyMapper.class)),
        spy(Mappers.getMapper(LegalEntityAsParticipantPostResponseBodyToLegalEntityAsParticipantItemIdMapper.class)),
        spy(Mappers.getMapper(LegalEntityExternalDataToLegalEntityExternalDataItemMapper.class)),
        spy(Mappers.getMapper(LegalEntityForUserGetResponseBodyToLegalEntityItemMapper.class)),
        spy(Mappers.getMapper(MasterServiceAgreementGetResponseBodyToGetServiceAgreementClientMapper.class)),
        spy(Mappers.getMapper(SegmentationGetResponseBodyToLegalEntityItemMapper.class)),
        spy(Mappers.getMapper(SubEntitiesPostResponseBodyToLegalEntityItemBaseClientMapper.class)),
        spy(Mappers.getMapper(SearchSubEntitiesParametersToSearchSubEntitiesParametersMapper.class))

    ));

    @Test
    public void shouldPassWhenPostSubLegalEntitiesRequestIsInvoked() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = 10;
        String cursor = "cursor";
        String query = "BANK";
        List<String> excludeIds = asList("1", "2");
        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        SubEntitiesPostResponseBody legalEntity01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        legalEntity01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody legalEntity02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        legalEntity02.withAddition("address1", "address02-01");
        legalEntity02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody legalEntity03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        legalEntity03.withAddition("address1", "address03-01");
        legalEntity03.withAddition("phone", "88888888");

        LegalEntityItemBase le01 = new LegalEntityItemBase();
        le01.setExternalId("external-01");
        le01.setId("id-01");
        le01.setName("name-01");
        le01.setType(CUSTOMER);
        le01.setAdditions(Collections.singletonMap("phone", "123456"));

        LegalEntityItemBase le02 = new LegalEntityItemBase();
        le02.setExternalId("external-02");
        le02.setId("id-02");
        le02.setName("name-02");
        le02.setType(CUSTOMER);
        Map<String, String> additions2 = Maps.newHashMap();
        additions2.put("address1", "address02-01");
        additions2.put("address2", "address02-02");
        le02.setAdditions(additions2);

        LegalEntityItemBase le03 = new LegalEntityItemBase();
        le03.setExternalId("external-03");
        le03.setId("id-03");
        le03.setName("name-03");
        le03.setType(BANK);
        Map<String, String> additions3 = Maps.newHashMap();
        additions3.put("address1", "address03-01");
        additions3.put("phone", "88888888");
        le03.setAdditions(additions3);

        List<SubEntitiesPostResponseBody> subEntities = asList(legalEntity01, legalEntity02,
            legalEntity03);

        when(parameterValidationService.validateQueryParameter(eq(query))).thenReturn(query);
        when(legalEntityFlowService.getSubLegalEntities(any(GetLegalEntitiesRequestDto.class)))
            .thenReturn(new RecordsDto<>((long) subEntities.size(), subEntities));

        List<LegalEntityItemBase> response = legalEntityController
            .postSubEntities(searchSubEntitiesParameters).getBody();

        ArgumentCaptor<GetLegalEntitiesRequestDto> subLegalEntitiesRequestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);
        verify(legalEntityFlowService).getSubLegalEntities(subLegalEntitiesRequestCaptor.capture());

        GetLegalEntitiesRequestDto requestCaptorValue = subLegalEntitiesRequestCaptor.getValue();
        assertEquals(parentEntityId, requestCaptorValue.getParentEntityId());
        assertTrue(requestCaptorValue.getExcludeIds().containsAll(excludeIds));
        assertEquals(cursor, requestCaptorValue.getCursor());
        assertEquals(from, requestCaptorValue.getFrom());
        assertEquals(size, requestCaptorValue.getSize());
        assertEquals(query, requestCaptorValue.getQuery());

        assertThat(response, containsInAnyOrder(
            allOf(
                hasProperty("id", is(le01.getId())),
                hasProperty("externalId", is(le01.getExternalId())),
                hasProperty("name", is(le01.getName())),
                hasProperty("type", is(fromValue(le01.getType().name()))),
                hasProperty("additions", is(le01.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(le02.getId())),
                hasProperty("externalId", is(le02.getExternalId())),
                hasProperty("name", is(le02.getName())),
                hasProperty("type", is(fromValue(le02.getType().name()))),
                hasProperty("additions", is(le02.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(le03.getId())),
                hasProperty("externalId", is(le03.getExternalId())),
                hasProperty("name", is(le03.getName())),
                hasProperty("type", is(fromValue(le03.getType().name()))),
                hasProperty("additions", is(le03.getAdditions()))
            )
        ));
    }

    @Test
    public void shouldPassWhenPostSubLegalEntitiesRequestIsInvokedWithNullElementsInExcludeIds() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = 10;
        String cursor = "cursor";
        String query = null;
        List<String> excludeIds = asList(null, "1", "2", null);
        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        SubEntitiesPostResponseBody legalEntity01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        legalEntity01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody legalEntity02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        legalEntity02.withAddition("address1", "address02-01");
        legalEntity02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody legalEntity03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        legalEntity03.withAddition("address1", "address03-01");
        legalEntity03.withAddition("phone", "88888888");

        List<SubEntitiesPostResponseBody> subEntities = asList(legalEntity01, legalEntity02,
            legalEntity03);

        when(legalEntityFlowService.getSubLegalEntities(any(GetLegalEntitiesRequestDto.class)))
            .thenReturn(new RecordsDto<>((long) subEntities.size(), subEntities));

        legalEntityController
            .postSubEntities(searchSubEntitiesParameters).getBody();

        ArgumentCaptor<GetLegalEntitiesRequestDto> subLegalEntitiesRequestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);
        verify(legalEntityFlowService).getSubLegalEntities(subLegalEntitiesRequestCaptor.capture());

        GetLegalEntitiesRequestDto requestCaptorValue = subLegalEntitiesRequestCaptor.getValue();
        assertEquals(parentEntityId, requestCaptorValue.getParentEntityId());
        assertTrue(requestCaptorValue.getExcludeIds().containsAll(asList("1", "2")));
        assertFalse(requestCaptorValue.getExcludeIds().contains(null));
    }

    @Test
    public void shouldPassWhenPostSubLegalEntitiesRequestIsInvokedWithNullExcludeIds() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = 10;
        String cursor = "cursor";
        String query = null;

        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(null);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        SubEntitiesPostResponseBody legalEntity01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        legalEntity01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody legalEntity02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        legalEntity02.withAddition("address1", "address02-01");
        legalEntity02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody legalEntity03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        legalEntity03.withAddition("address1", "address03-01");
        legalEntity03.withAddition("phone", "88888888");

        List<SubEntitiesPostResponseBody> subEntities = asList(legalEntity01, legalEntity02,
            legalEntity03);

        when(legalEntityFlowService.getSubLegalEntities(any(GetLegalEntitiesRequestDto.class)))
            .thenReturn(new RecordsDto<>((long) subEntities.size(), subEntities));

        legalEntityController
            .postSubEntities(searchSubEntitiesParameters);

        ArgumentCaptor<GetLegalEntitiesRequestDto> subLegalEntitiesRequestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);
        verify(legalEntityFlowService).getSubLegalEntities(subLegalEntitiesRequestCaptor.capture());

        GetLegalEntitiesRequestDto requestCaptorValue = subLegalEntitiesRequestCaptor.getValue();
        assertEquals(parentEntityId, requestCaptorValue.getParentEntityId());
        assertThat(requestCaptorValue.getExcludeIds(), hasSize(0));
    }

    @Test
    public void shouldPassWhenPostSubLegalEntitiesRequestIsInvokedWithNullRequestBody() {
        Integer from = 0;
        Integer size = 10;

        SubEntitiesPostResponseBody legalEntity01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        legalEntity01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody legalEntity02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        legalEntity02.withAddition("address1", "address02-01");
        legalEntity02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody legalEntity03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        legalEntity03.withAddition("address1", "address03-01");
        legalEntity03.withAddition("phone", "88888888");

        List<SubEntitiesPostResponseBody> subEntities = asList(legalEntity01, legalEntity02, legalEntity03);

        when(legalEntityFlowService.getSubLegalEntities(any(GetLegalEntitiesRequestDto.class)))
            .thenReturn(new RecordsDto<>((long) subEntities.size(), subEntities));

        List<LegalEntityItemBase> response = legalEntityController
            .postSubEntities(null).getBody();

        ArgumentCaptor<GetLegalEntitiesRequestDto> subLegalEntitiesRequestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);
        verify(legalEntityFlowService).getSubLegalEntities(subLegalEntitiesRequestCaptor.capture());

        GetLegalEntitiesRequestDto requestCaptorValue = subLegalEntitiesRequestCaptor.getValue();
        assertNull(requestCaptorValue.getParentEntityId());
        assertEquals(0, requestCaptorValue.getExcludeIds().size());
        assertNull(requestCaptorValue.getCursor());
        assertEquals(from, requestCaptorValue.getFrom());
        assertEquals(size, requestCaptorValue.getSize());
        assertNull(requestCaptorValue.getQuery());

        assertThat(response, containsInAnyOrder(
            allOf(
                hasProperty("id", is(legalEntity01.getId())),
                hasProperty("externalId", is(legalEntity01.getExternalId())),
                hasProperty("name", is(legalEntity01.getName())),
                hasProperty("type", is(
                    fromValue(legalEntity01.getType().name()))),
                hasProperty("additions", is(legalEntity01.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity02.getId())),
                hasProperty("externalId", is(legalEntity02.getExternalId())),
                hasProperty("name", is(legalEntity02.getName())),
                hasProperty("type", is(
                    fromValue(legalEntity02.getType().name()))),
                hasProperty("additions", is(legalEntity02.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity03.getId())),
                hasProperty("externalId", is(legalEntity03.getExternalId())),
                hasProperty("name", is(legalEntity03.getName())),
                hasProperty("type", is(
                    fromValue(legalEntity03.getType().name()))),
                hasProperty("additions", is(legalEntity03.getAdditions()))
            )
        ));
    }

    @Test
    public void shouldPassWhenPostSubLegalEntitiesRequestIsInvokedWithFromAndSizeNotSet() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = 10;
        String cursor = "cursor";
        String query = "BANK";
        List<String> excludeIds = asList("1", "2");
        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        SubEntitiesPostResponseBody legalEntity01 = new SubEntitiesPostResponseBody().withExternalId("external-01")
            .withId("id-01")
            .withName("name-01").withType(LegalEntityType.CUSTOMER);
        legalEntity01.withAddition("phone", "123456");

        SubEntitiesPostResponseBody legalEntity02 = new SubEntitiesPostResponseBody().withExternalId("external-02")
            .withId("id-02")
            .withName("name-02").withType(LegalEntityType.CUSTOMER);
        legalEntity02.withAddition("address1", "address02-01");
        legalEntity02.withAddition("address2", "address02-02");

        SubEntitiesPostResponseBody legalEntity03 = new SubEntitiesPostResponseBody().withExternalId("external-03")
            .withId("id-03")
            .withName("name-03").withType(LegalEntityType.BANK);
        legalEntity03.withAddition("address1", "address03-01");
        legalEntity03.withAddition("phone", "88888888");

        List<SubEntitiesPostResponseBody> subEntities = asList(legalEntity01, legalEntity02,
            legalEntity03);

        when(parameterValidationService.validateQueryParameter(eq(query))).thenReturn(query);
        when(legalEntityFlowService.getSubLegalEntities(any(GetLegalEntitiesRequestDto.class)))
            .thenReturn(new RecordsDto<>((long) subEntities.size(), subEntities));

        List<LegalEntityItemBase> response = legalEntityController
            .postSubEntities(searchSubEntitiesParameters).getBody();

        ArgumentCaptor<GetLegalEntitiesRequestDto> subLegalEntitiesRequestCaptor = ArgumentCaptor
            .forClass(GetLegalEntitiesRequestDto.class);
        verify(legalEntityFlowService).getSubLegalEntities(subLegalEntitiesRequestCaptor.capture());

        GetLegalEntitiesRequestDto requestCaptorValue = subLegalEntitiesRequestCaptor.getValue();
        assertEquals(parentEntityId, requestCaptorValue.getParentEntityId());
        assertTrue(requestCaptorValue.getExcludeIds().containsAll(excludeIds));
        assertEquals(cursor, requestCaptorValue.getCursor());
        assertEquals(from, requestCaptorValue.getFrom());
        assertEquals(size, requestCaptorValue.getSize());
        assertEquals(query, requestCaptorValue.getQuery());

        assertThat(response, containsInAnyOrder(
            allOf(
                hasProperty("id", is(legalEntity01.getId())),
                hasProperty("externalId", is(legalEntity01.getExternalId())),
                hasProperty("name", is(legalEntity01.getName())),
                hasProperty("type", is(fromValue(legalEntity01.getType().name()))),
                hasProperty("additions", is(legalEntity01.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity02.getId())),
                hasProperty("externalId", is(legalEntity02.getExternalId())),
                hasProperty("name", is(legalEntity02.getName())),
                hasProperty("type", is(fromValue(legalEntity02.getType().name()))),
                hasProperty("additions", is(legalEntity02.getAdditions()))
            ),
            allOf(
                hasProperty("id", is(legalEntity03.getId())),
                hasProperty("externalId", is(legalEntity03.getExternalId())),
                hasProperty("name", is(legalEntity03.getName())),
                hasProperty("type", is(fromValue(legalEntity03.getType().name()))),
                hasProperty("additions", is(legalEntity03.getAdditions()))
            )
        ));
    }
    
    @Test
    public void shouldPassWhenPostLegalEntityAsParticipantFromMasterServiceAgreement() {
        String serviceAgreementId = "sa-id";
        Optional<ServiceAgreementItem> serviceAgreement = Optional.of(new ServiceAgreementItem()
                        .withIsMaster(true)
                        .withCreatorLegalEntity("creator-le-id"));
        
        LegalEntityAsParticipantPostResponseBody responseBody = new LegalEntityAsParticipantPostResponseBody()
                        .withLegalEntityId("le-id")
                        .withServiceAgreementId("sa-id");
        responseBody.setAddition(LE_EXTERNAL_ID_KEY, LE_EXTERNAL_ID_VALUE);

        
        when(accessControlValidator.userHasNoAccessToEntitlementResource(any(String.class),
                        any(AccessResourceType.class))).thenReturn(false);
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("internal-user-id", "user-le-id"));
        when(serviceAgreementProvider.getServiceAgreementById(eq(serviceAgreementId))).thenReturn(serviceAgreement);
        when(legalEntityFlowService.createLegalEntityAsParticipant(any(LegalEntityAsParticipantPostRequestBody.class)))
                        .thenReturn(responseBody);
        
        Map<String, String> additions = Maps.newHashMap();
        additions.put(LE_EXTERNAL_ID_KEY, LE_EXTERNAL_ID_VALUE);
        
        LegalEntityAsParticipantCreateItem request = new LegalEntityAsParticipantCreateItem()
                        .legalEntityName("le-name")
                        .legalEntityExternalId("le-id")
                        .legalEntityParentId("parent-id")
                        .additions(additions)
                        .legalEntityType(com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType.BANK)
                        .participantOf(new ParticipantOf()
                                        .existingCustomServiceAgreement(new ExistingCustomServiceAgreement()
                                                        .serviceAgreementId("sa-id")
                                                        .participantInfo(new ParticipantInfo()
                                                                        .shareAccounts(false)
                                                                        .shareUsers(true))));
        
        LegalEntityAsParticipantItemId response =
                        legalEntityController.postLegalEntitiesAsParticipant(request).getBody();
        assertEquals(responseBody.getLegalEntityId(), response.getLegalEntityId());
        assertEquals(responseBody.getServiceAgreementId(), response.getServiceAgreementId());
        assertEquals(responseBody.getAdditions(), response.getAdditions());
        
        ArgumentCaptor<LegalEntityAsParticipantPostRequestBody> requestCaptor = ArgumentCaptor.forClass(LegalEntityAsParticipantPostRequestBody.class);
        
        verify(legalEntityFlowService).createLegalEntityAsParticipant(requestCaptor.capture());
        LegalEntityAsParticipantPostRequestBody requestBody = requestCaptor.getValue();
        
        assertEquals(request.getLegalEntityName(), requestBody.getLegalEntityName());
        assertEquals(request.getLegalEntityExternalId(), requestBody.getLegalEntityExternalId());
        assertEquals(request.getLegalEntityParentId(), requestBody.getLegalEntityParentId());
        assertEquals(request.getLegalEntityType().toString(), requestBody.getLegalEntityType().toString());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getServiceAgreementId(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getServiceAgreementId());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers());
        assertEquals(request.getAdditions().size(), requestBody.getAdditions().size());
        assertEquals(request.getAdditions().get(LE_EXTERNAL_ID_KEY), requestBody.getAdditions().get(LE_EXTERNAL_ID_KEY));
    }
    
    @Test
    public void shouldPassWhenPostLegalEntityAsParticipantWithNoLegalEntityParentId() {
        String serviceAgreementId = "sa-id";
        Optional<ServiceAgreementItem> serviceAgreement = Optional.of(new ServiceAgreementItem()
                        .withIsMaster(true)
                        .withCreatorLegalEntity("creator-le-id"));
        
        LegalEntityAsParticipantPostResponseBody responseBody = new LegalEntityAsParticipantPostResponseBody()
                        .withLegalEntityId("le-id")
                        .withServiceAgreementId("sa-id");
        responseBody.setAddition(LE_EXTERNAL_ID_KEY, LE_EXTERNAL_ID_VALUE);

        
        when(accessControlValidator.userHasNoAccessToEntitlementResource(any(String.class),
                        any(AccessResourceType.class))).thenReturn(false);
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("internal-user-id", "user-le-id"));
        when(serviceAgreementProvider.getServiceAgreementById(eq(serviceAgreementId))).thenReturn(serviceAgreement);
        when(legalEntityFlowService.createLegalEntityAsParticipant(any(LegalEntityAsParticipantPostRequestBody.class)))
                        .thenReturn(responseBody);
        
        Map<String, String> additions = Maps.newHashMap();
        additions.put(LE_EXTERNAL_ID_KEY, LE_EXTERNAL_ID_VALUE);
        
        LegalEntityAsParticipantCreateItem request = new LegalEntityAsParticipantCreateItem()
                        .legalEntityName("le-name")
                        .legalEntityExternalId("le-id")
                        .additions(additions)
                        .legalEntityType(com.backbase.accesscontrol.client.rest.spec.model.LegalEntityType.BANK)
                        .participantOf(new ParticipantOf()
                                        .existingCustomServiceAgreement(new ExistingCustomServiceAgreement()
                                                        .serviceAgreementId("sa-id")
                                                        .participantInfo(new ParticipantInfo()
                                                                        .shareAccounts(false)
                                                                        .shareUsers(true))));
        
        LegalEntityAsParticipantItemId response =
                        legalEntityController.postLegalEntitiesAsParticipant(request).getBody();
        assertEquals(responseBody.getLegalEntityId(), response.getLegalEntityId());
        assertEquals(responseBody.getServiceAgreementId(), response.getServiceAgreementId());
        assertEquals(responseBody.getAdditions(), response.getAdditions());
        
        ArgumentCaptor<LegalEntityAsParticipantPostRequestBody> requestCaptor = ArgumentCaptor.forClass(LegalEntityAsParticipantPostRequestBody.class);
        
        verify(legalEntityFlowService).createLegalEntityAsParticipant(requestCaptor.capture());
        LegalEntityAsParticipantPostRequestBody requestBody = requestCaptor.getValue();
        
        assertEquals(request.getLegalEntityName(), requestBody.getLegalEntityName());
        assertEquals(request.getLegalEntityExternalId(), requestBody.getLegalEntityExternalId());
        assertEquals(serviceAgreement.get().getCreatorLegalEntity(), requestBody.getLegalEntityParentId());
        assertEquals(request.getLegalEntityType().toString(), requestBody.getLegalEntityType().toString());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getServiceAgreementId(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getServiceAgreementId());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareAccounts());
        assertEquals(request.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers(), requestBody.getParticipantOf().getExistingCustomServiceAgreement().getParticipantInfo().getShareUsers());
        assertEquals(request.getAdditions().size(), requestBody.getAdditions().size());
        assertEquals(request.getAdditions().get(LE_EXTERNAL_ID_KEY), requestBody.getAdditions().get(LE_EXTERNAL_ID_KEY));
    }
    
    @Test
    public void shouldThrowForbiddenErrorNotMasterServiceAgreement() {
        String serviceAgreementId = "sa-id";
        Optional<ServiceAgreementItem> serviceAgreement = Optional.of(new ServiceAgreementItem()
                        .withIsMaster(false));
        
        when(userContextUtil.getServiceAgreementId()).thenReturn(serviceAgreementId);
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("internal-user-id", "user-le-id"));
        when(serviceAgreementProvider.getServiceAgreementById(eq(serviceAgreementId)))
        .thenReturn(serviceAgreement);
        
        LegalEntityAsParticipantCreateItem requestBody = new LegalEntityAsParticipantCreateItem()
                        .participantOf(new ParticipantOf()
                                        .existingCustomServiceAgreement(new ExistingCustomServiceAgreement()));
        
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                        () -> legalEntityController.postLegalEntitiesAsParticipant(requestBody));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_LE_021.getErrorMessage(), ERR_LE_021.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestErrorInvalidSizeParameter() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = 10000;
        String cursor = "cursor";
        String query = "BANK";
        List<String> excludeIds = asList("1", "2");

        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> legalEntityController
            .postSubEntities(searchSubEntitiesParameters));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_045.getErrorMessage(), ERR_ACQ_045.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestErrorIfSizeIsNullAndFromIsNot() {
        String parentEntityId = "parentEntityId";
        Integer from = 0;
        Integer size = null;
        String cursor = "cursor";
        String query = "BANK";
        List<String> excludeIds = asList("1", "2");

        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> legalEntityController
            .postSubEntities(searchSubEntitiesParameters));

        assertThat(exception, new BadRequestErrorMatcher(ERR_LE_012.getErrorMessage(), ERR_LE_012.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestErrorIfFromIsNullAndSizeIsNot() {
        String parentEntityId = "parentEntityId";
        Integer from = null;
        Integer size = 24;
        String cursor = "cursor";
        String query = "BANK";
        List<String> excludeIds = asList("1", "2");

        SearchSubEntitiesParameters searchSubEntitiesParameters = new SearchSubEntitiesParameters();
        searchSubEntitiesParameters.setParentEntityId(parentEntityId);
        searchSubEntitiesParameters.setExcludeIds(excludeIds);
        searchSubEntitiesParameters.setFrom(from);
        searchSubEntitiesParameters.setSize(size);
        searchSubEntitiesParameters.setCursor(cursor);
        searchSubEntitiesParameters.setQuery(query);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> legalEntityController
            .postSubEntities(searchSubEntitiesParameters));

        assertThat(exception, new BadRequestErrorMatcher(ERR_LE_012.getErrorMessage(), ERR_LE_012.getErrorCode()));
    }
}
