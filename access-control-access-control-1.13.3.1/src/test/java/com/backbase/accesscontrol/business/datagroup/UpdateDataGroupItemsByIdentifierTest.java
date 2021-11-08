package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.dataitems.ArrangementItemService;
import com.backbase.accesscontrol.business.datagroup.dataitems.ContactItemService;
import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemExternalIdConverterService;
import com.backbase.accesscontrol.business.serviceagreement.GetServiceAgreementByExternalId;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.routes.datagroup.ValidateDataGroupRouteProxy;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.batch.datagroup.UpdateDataGroupItemsByIdentifierPersistence;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.InternalServerErrorException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationDataGroupItemPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementExternalIdGetResponseBody;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataGroupItemsByIdentifierTest {

    @Mock
    private ConstraintViolation<Object> constraintViolation;
    private UpdateDataGroupItemsByIdentifier updateDataGroupItemsByIdentifier;
    @Mock
    private DataGroupService dataGroupService;
    @Mock
    private GetServiceAgreementByExternalId getServiceAgreementByExternalId;
    @Mock
    private Validator validator;
    @Mock
    private ValidateDataGroupRouteProxy validateDataGroup;
    @Mock
    private ArrangementItemService arrangementItemService;
    @Mock
    private ContactItemService contactItemService;
    @Mock
    private UpdateDataGroupItemsByIdentifierPersistence updateDataGroupItemsByIdentifierPersistence;
    @Spy
    private BatchResponseItemExtendedMapper batchResponseItemExtendedMapper = Mappers
        .getMapper(BatchResponseItemExtendedMapper.class);

    private List<DataItemExternalIdConverterService> dataItemExternalIdConverterServices = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        when(arrangementItemService.getType()).thenReturn("ARRANGEMENTS");
        when(contactItemService.getType()).thenReturn("PAYEES");
        Map<String, String> returnedResponse = new HashMap<>();
        returnedResponse.put("itemId", "internalItemId");
        returnedResponse.put("externalId", "internalOfExternalId");

        dataItemExternalIdConverterServices.add(arrangementItemService);
        dataItemExternalIdConverterServices.add(contactItemService);
        updateDataGroupItemsByIdentifier = new UpdateDataGroupItemsByIdentifier(
            dataGroupService,
            validator,
            getServiceAgreementByExternalId,
            dataItemExternalIdConverterServices,
            updateDataGroupItemsByIdentifierPersistence,
            batchResponseItemExtendedMapper);
    }

    @Test
    public void updateDataGroupItemsByIdentifier() {

        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")));
        NameIdentifier nameIdentifier = new NameIdentifier()
            .withName("DataGroup")
            .withExternalServiceAgreementId("externalSaId");
        PresentationDataGroupItemPutRequestBody invalidDataGroupIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(
                new PresentationIdentifier().withNameIdentifier(nameIdentifier).withIdIdentifier("idIdentifier"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("internalId")
                .withExternalIdIdentifier("externalId")));
        PresentationDataGroupItemPutRequestBody internalIdDataGroupIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(
                new PresentationIdentifier().withNameIdentifier(nameIdentifier).withIdIdentifier("idIdentifier"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("internalId")));

        PresentationDataGroupItemPutRequestBody validItemDataGroupNameIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withNameIdentifier(nameIdentifier))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("itemId")));
        PresentationDataGroupItemPutRequestBody invalidDataGroupEmptyIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier())
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("itemId")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(
            validItemDataGroupIdIdentifier,
            invalidDataGroupIdentifier,
            validItemDataGroupNameIdentifier,
            invalidDataGroupEmptyIdentifier,
            internalIdDataGroupIdentifier);

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest =
            getInternalRequest(dataGroupItems);
        List<BatchResponseItemExtended> responseContainer = newArrayList();

        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        when(dataGroupService.getById(eq("dgId")))
            .thenReturn(new DataGroup()
                .withServiceAgreementId("saId"));

        when(arrangementItemService
            .mapExternalToInternalIds(eq(Sets.newHashSet("itemId")), anyString()))
            .thenReturn(new HashMap<String, List<String>>() {{
                put("itemId", singletonList("internalItemId"));
            }});

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId);
        assertEquals(2, validResponses.size());
        assertTrue(validResponses.containsValue(validItemDataGroupIdIdentifier));
        assertTrue(validResponses.containsValue(validItemDataGroupNameIdentifier));
        assertEquals(1, internalDataItemsIdsByTypeAndExternalId.size());
    }

    @Test
    public void updateDataGroupItemsByIdentifier1() {

        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier1 = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier()
                .withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")));
        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier2 = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("PAYEES")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")));
        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier3 = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId1"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId1")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(
            validItemDataGroupIdIdentifier1,
            validItemDataGroupIdIdentifier2,
            validItemDataGroupIdIdentifier3);

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest =
            getInternalRequest(dataGroupItems);
        List<BatchResponseItemExtended> responseContainer = newArrayList();

        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        when(arrangementItemService
            .mapExternalToInternalIds(eq(Sets.newHashSet("itemId")), eq("saId")))
            .thenReturn(new HashMap<String, List<String>>() {{
                put("itemId", singletonList("internalId"));
            }});
        when(arrangementItemService
            .mapExternalToInternalIds(eq(Sets.newHashSet("itemId1")), eq("saId1")))
            .thenReturn(new HashMap<String, List<String>>() {{
                put("itemId1", singletonList("internalId1"));
            }});

        when(contactItemService
            .mapExternalToInternalIds(eq(Sets.newHashSet("itemId")), eq("saId")))
            .thenReturn(new HashMap<String, List<String>>() {{
                put("itemId", singletonList("internalId"));
            }});

        when(dataGroupService.getById(eq("dgId"))).thenReturn(new DataGroup()
            .withServiceAgreementId("saId"));
        when(dataGroupService.getById(eq("dgId1"))).thenReturn(new DataGroup()
            .withServiceAgreementId("saId1"));

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId);
        assertEquals(3, validResponses.size());
    }

    @Test
    public void updateDataGroupItemsByIdentifierWithDataValidation() {
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "dataValidationEnabled", true);
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "validateDataGroup", validateDataGroup);
        HashSet<ConstraintViolation<Object>> value = new HashSet<>();
        value.add(constraintViolation);
        when(validator.validate(any()))
            .thenReturn(value);
        PresentationDataGroupItemPutRequestBody validItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")));
        NameIdentifier nameIdentifier = new NameIdentifier()
            .withName("DataGroup")
            .withExternalServiceAgreementId("externalSaId");
        PresentationDataGroupItemPutRequestBody invalidDataGroupIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.REMOVE)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(
                new PresentationIdentifier().withNameIdentifier(nameIdentifier).withIdIdentifier("idIdentifier"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("internalId")
                .withExternalIdIdentifier("externalId")));
        PresentationDataGroupItemPutRequestBody internalIdDataGroupIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(
                new PresentationIdentifier().withNameIdentifier(nameIdentifier))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("internalIdAdd")));
        PresentationDataGroupItemPutRequestBody invalidDataItemIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withNameIdentifier(
                new NameIdentifier().withName("invalid").withExternalServiceAgreementId("invalidSaId")))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("itemIdInvalid")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(
            validItemDataGroupIdIdentifier,
            invalidDataGroupIdentifier,
            internalIdDataGroupIdentifier,
            invalidDataItemIdentifier);
        ServiceAgreementExternalIdGetResponseBody serviceAgreementBody = new ServiceAgreementExternalIdGetResponseBody()
            .withExternalId("externalSaId");
        InternalRequest<ServiceAgreementExternalIdGetResponseBody> internalRequestServiceAgreementBody = getInternalRequest(
            serviceAgreementBody);
        when(dataGroupService.getById(eq("dgId"))).thenReturn(new DataGroup().withServiceAgreementId("said"));
        when(getServiceAgreementByExternalId.getServiceAgreementByExternalId(any(InternalRequest.class),
            eq(nameIdentifier.getExternalServiceAgreementId()))).thenReturn(internalRequestServiceAgreementBody);
        when(validator.validate(eq(validItemDataGroupIdIdentifier.getDataGroupIdentifier())))
            .thenReturn(new HashSet<>());
        when(validator.validate(eq(internalIdDataGroupIdentifier.getDataGroupIdentifier())))
            .thenReturn(new HashSet<>());

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest =
            getInternalRequest(dataGroupItems);

        List<BatchResponseItemExtended> responseContainer = newArrayList();
        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        when(arrangementItemService
            .mapExternalToInternalIds(eq(Sets.newHashSet("itemId")), anyString()))
            .thenReturn(new HashMap<String, List<String>>() {{
                put("itemId", singletonList("internalItemId"));
            }});

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId);

        assertThat(responseContainer, hasSize(4));
        assertThat(responseContainer,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(validItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier()),
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                    .withErrors(
                        Lists.newArrayList("Invalid Identifier. Either name or id identifier should be provided"))
                    .withAction(PresentationAction.REMOVE)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(invalidDataGroupIdentifier.getDataGroupIdentifier().getIdIdentifier()),
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK)
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(
                        internalIdDataGroupIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
                    .withExternalServiceAgreementId(
                        internalIdDataGroupIdentifier.getDataGroupIdentifier().getNameIdentifier()
                            .getExternalServiceAgreementId()),
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                    .withErrors(
                        Lists.newArrayList("Invalid Identifier. Either name or id identifier should be provided"))
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId("invalidSaId")
                    .withResourceId(invalidDataItemIdentifier.getDataGroupIdentifier().getNameIdentifier().getName())
            )
        );
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenThereAreValidationErrors() {
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "dataValidationEnabled", true);
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "validateDataGroup", validateDataGroup);
        HashSet<ConstraintViolation<Object>> value = new HashSet<>();
        value.add(constraintViolation);
        when(validator.validate(any()))
            .thenReturn(value);
        PresentationDataGroupItemPutRequestBody inValidItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("itemId")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(inValidItemDataGroupIdIdentifier);

        when(dataGroupService.getById(eq("dgId"))).thenReturn(new DataGroup().withServiceAgreementId("said"));

        when(validator.validate(eq(inValidItemDataGroupIdIdentifier.getDataGroupIdentifier())))
            .thenReturn(new HashSet<>());

        String errorMessage = "Invalid data item";

        doThrow(getBadRequestException(errorMessage, "code"))
            .when(validateDataGroup)
            .validate(any(InternalRequest.class));

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest = getInternalRequest(
            dataGroupItems);

        List<BatchResponseItemExtended> responseContainer = newArrayList();
        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId);
        assertThat(responseContainer, hasSize(1));
        assertThat(responseContainer,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                    .withErrors(newArrayList("Invalid data item"))
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(inValidItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())));
    }

    @Test
    public void shouldContainNotFoundExceptionWhenTDataGroupDoesNotExist() {

        PresentationDataGroupItemPutRequestBody inValidItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(inValidItemDataGroupIdIdentifier);

        when(dataGroupService.getById(eq("dgId")))
            .thenThrow(getNotFoundException(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest = getInternalRequest(
            dataGroupItems);

        List<BatchResponseItemExtended> responseContainer = newArrayList();
        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId);
        assertThat(responseContainer, hasSize(1));
        assertThat(responseContainer,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND)
                    .withErrors(newArrayList(ERR_ACQ_001.getErrorMessage()))
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(inValidItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())));
    }

    @Test
    public void shouldThrowBadRequestExceptionWithNotFoundMessageWhenThereAreValidationErrors() {
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "dataValidationEnabled", true);
        ReflectionTestUtils.setField(updateDataGroupItemsByIdentifier, "validateDataGroup", validateDataGroup);
        HashSet<ConstraintViolation<Object>> value = new HashSet<>();
        value.add(constraintViolation);
        when(validator.validate(any()))
            .thenReturn(value);
        PresentationDataGroupItemPutRequestBody inValidItemDataGroupIdIdentifier = new PresentationDataGroupItemPutRequestBody()
            .withAction(PresentationAction.ADD)
            .withType("ARRANGEMENTS")
            .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
            .withDataItems(newArrayList(new PresentationItemIdentifier().withInternalIdIdentifier("itemId")));

        List<PresentationDataGroupItemPutRequestBody> dataGroupItems = newArrayList(inValidItemDataGroupIdIdentifier);

        when(dataGroupService.getById(eq("dgId"))).thenReturn(new DataGroup().withServiceAgreementId("said"));

        when(validator.validate(eq(inValidItemDataGroupIdIdentifier.getDataGroupIdentifier())))
            .thenReturn(new HashSet<>());

        String errorMessage = "Invalid data item";

        doThrow(getNotFoundException(errorMessage, "code"))
            .when(validateDataGroup)
            .validate(any(InternalRequest.class));

        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest =
            getInternalRequest(dataGroupItems);

        List<BatchResponseItemExtended> responseContainer = newArrayList();
        Map<Integer, PresentationDataGroupItemPutRequestBody> validResponses = new HashMap<>();
        Map<String, Map<String, String>> internalDataItemsIdsByTypeAndExternalId = new HashMap<>();

        updateDataGroupItemsByIdentifier.validateDataGroupType(
            internalRequest,
            responseContainer,
            validResponses,
            internalDataItemsIdsByTypeAndExternalId
        );
        assertThat(responseContainer, hasSize(1));
        assertThat(responseContainer,
            contains(
                new BatchResponseItemExtended()
                    .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                    .withErrors(newArrayList("Not Found"))
                    .withAction(PresentationAction.ADD)
                    .withExternalServiceAgreementId(null)
                    .withResourceId(inValidItemDataGroupIdIdentifier.getDataGroupIdentifier().getIdIdentifier())));
    }

    @Test
    public void shouldThrowInternalServerErrorIfNoDataItemConverterExist() {
        InternalRequest<List<PresentationDataGroupItemPutRequestBody>> internalRequest =
            getInternalRequest(singletonList(
                new PresentationDataGroupItemPutRequestBody()
                    .withAction(PresentationAction.ADD)
                    .withType("NO_CONVERTER_FOR_ME")
                    .withDataGroupIdentifier(new PresentationIdentifier().withIdIdentifier("dgId"))
                    .withDataItems(newArrayList(new PresentationItemIdentifier().withExternalIdIdentifier("itemId")))
            ));

        when(dataGroupService.getById(eq("dgId")))
            .thenReturn(new DataGroup()
                .withId("dgId")
                .withServiceAgreementId("saId"));
        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
            () -> updateDataGroupItemsByIdentifier.validateDataGroupType(
                internalRequest,
                newArrayList(),
                new HashMap<>(),
                new HashMap<>()
            ));

        assertEquals("Data Item converter for NO_CONVERTER_FOR_ME does not exist", exception.getMessage());
    }
}