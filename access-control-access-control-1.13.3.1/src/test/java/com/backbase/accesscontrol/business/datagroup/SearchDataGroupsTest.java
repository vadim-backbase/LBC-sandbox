package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemExternalIdConverterService;
import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.mappers.SearchDataGroupsMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.repository.DataGroupJpaRepository;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.LegalEntityIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationItemIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationGetDataGroupsRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.PresentationServiceAgreementWithDataGroups;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.SharesEnum;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.google.common.collect.Lists;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(JUnitParamsRunner.class)
public class SearchDataGroupsTest {

    @Mock
    private DataItemExternalIdConverterService mockDataItemExternalIdConverterService;
    @Spy
    private SearchDataGroupsMapper searchDataGroupsMapper = Mappers.getMapper(SearchDataGroupsMapper.class);
    @Mock
    private DataGroupJpaRepository dataGroupJpaRepository;
    @Mock
    private ValidationConfig validationConfig;

    private SearchDataGroups testy;
    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockDataItemExternalIdConverterService.getType()).thenReturn("TEST");
        testy = new SearchDataGroups(Lists.newArrayList(mockDataItemExternalIdConverterService),
            searchDataGroupsMapper, validationConfig, dataGroupJpaRepository, persistenceServiceAgreementService);
    }

    @Test
    public void shouldGetInternalIdAndCallRepositoryWhenServiceAgreementAndDataItemIdentifierAreProvided() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withDataItemIdentifier(new PresentationItemIdentifier().withExternalIdIdentifier("Item id"))
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withExternalIdIdentifier("SA ID"));

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreement(new ServiceAgreement()
                .withName("saName")
                .withId("said1")
                .withDescription("saDesc")
                .withExternalId("saEx1"))
            .withDataItemType("TEST")
            .withName("dgName")
            .withDescription("dgDesc")
            .withId("1");

        when(persistenceServiceAgreementService
            .getServiceAgreementByExternalId(eq("SA ID")))
            .thenReturn(new ServiceAgreement()
                .withExternalId("SA ID")
                .withId("said1"));

        when(mockDataItemExternalIdConverterService.getInternalId(anyString(), anyString()))
            .thenReturn(singletonList("Item internal id"));
        when(dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem(argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true)))
            .thenReturn(Lists.newArrayList(dataGroup));
        List<PresentationServiceAgreementWithDataGroups> response = testy
            .searchDataGroups(new InternalRequest<>(request, null), "TEST").getData();

        verify(mockDataItemExternalIdConverterService, times(1))
            .getInternalId(eq(request.getDataItemIdentifier().getExternalIdIdentifier()), eq("said1"));

        verify(dataGroupJpaRepository)
            .findAllDataGroupsByServiceAgreementAndDataItem(eq("TEST"), isNull(), isNull(),
                eq(request.getServiceAgreementIdentifier().getExternalIdIdentifier()),
                eq("Item internal id"), eq(null), eq(SharesEnum.ACCOUNTS));

        assertEquals(dataGroup.getServiceAgreement().getId(), response.get(0).getServiceAgreement().getId());
        assertEquals(dataGroup.getServiceAgreement().getExternalId(),
            response.get(0).getServiceAgreement().getExternalId());
        assertEquals(dataGroup.getServiceAgreement().getName(), response.get(0).getServiceAgreement().getName());
        assertEquals(dataGroup.getId(), response.get(0).getDataGroups().get(0).getId());
        assertEquals(dataGroup.getName(), response.get(0).getDataGroups().get(0).getName());
        assertEquals(dataGroup.getDescription(), response.get(0).getDataGroups().get(0).getDescription());
    }

    @Test
    public void shouldCallRepositoryWhenServiceAgreementAndDataItemIdentifierAreProvidedWithoutConvertingDataItem() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withDataItemIdentifier(new PresentationItemIdentifier().withInternalIdIdentifier("Item id"))
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withIdIdentifier("SA ID"));

        DataGroup dataGroup = new DataGroup()
            .withServiceAgreement(new ServiceAgreement()
                .withName("saName")
                .withId("said1")
                .withDescription("saDesc")
                .withExternalId("saEx1"))
            .withDataItemType("TEST")
            .withName("dgName")
            .withDescription("dgDesc")
            .withId("1");

        when(dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem(argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true)))
            .thenReturn(Lists.newArrayList(dataGroup));
        List<PresentationServiceAgreementWithDataGroups> response = testy
            .searchDataGroups(new InternalRequest<>(request, null), "TEST").getData();

        verify(mockDataItemExternalIdConverterService, times(0))
            .getInternalId(anyString(), anyString());

        verify(dataGroupJpaRepository)
            .findAllDataGroupsByServiceAgreementAndDataItem(eq("TEST"),
                eq(request.getServiceAgreementIdentifier().getIdIdentifier()), isNull(), isNull(),
                eq(request.getDataItemIdentifier().getInternalIdIdentifier()), isNull(), eq(SharesEnum.ACCOUNTS));

        assertEquals(dataGroup.getServiceAgreement().getId(), response.get(0).getServiceAgreement().getId());
        assertEquals(dataGroup.getServiceAgreement().getExternalId(),
            response.get(0).getServiceAgreement().getExternalId());
        assertEquals(dataGroup.getServiceAgreement().getName(), response.get(0).getServiceAgreement().getName());
        assertEquals(dataGroup.getId(), response.get(0).getDataGroups().get(0).getId());
        assertEquals(dataGroup.getName(), response.get(0).getDataGroups().get(0).getName());
        assertEquals(dataGroup.getDescription(), response.get(0).getDataGroups().get(0).getDescription());
    }

    @Test
    public void shouldCallRepositoryWhenServiceAgreementNameIsProvidedWithoutConvertingDataItem() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                .withNameIdentifier("SA ID"));

        DataGroup dataGroup1 = new DataGroup()
            .withServiceAgreement(new ServiceAgreement()
                .withName("saName")
                .withId("said1")
                .withDescription("saDesc")
                .withExternalId("saEx1"))
            .withDataItemType("TEST")
            .withName("dgName1")
            .withDescription("dgDesc1")
            .withId("1");
        DataGroup dataGroup2 = new DataGroup()
            .withServiceAgreement(new ServiceAgreement()
                .withName("saName")
                .withId("said1")
                .withDescription("saDesc")
                .withExternalId("saEx1"))
            .withDataItemType("TEST")
            .withName("dgName2")
            .withDescription("dgDesc2")
            .withId("2");

        when(dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem(argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true)))
            .thenReturn(Lists.newArrayList(dataGroup1, dataGroup2));
        List<PresentationServiceAgreementWithDataGroups> response = testy
            .searchDataGroups(new InternalRequest<>(request, null), "TEST").getData();

        verify(mockDataItemExternalIdConverterService, times(0))
            .getInternalId(anyString(), anyString());

        verify(dataGroupJpaRepository)
            .findAllDataGroupsByServiceAgreementAndDataItem(eq("TEST"), isNull(),
                eq(request.getServiceAgreementIdentifier().getNameIdentifier()), isNull(), isNull(), isNull(),
                eq(SharesEnum.ACCOUNTS));

        assertEquals(dataGroup1.getServiceAgreement().getId(), response.get(0).getServiceAgreement().getId());
        assertEquals(dataGroup1.getServiceAgreement().getExternalId(),
            response.get(0).getServiceAgreement().getExternalId());
        assertEquals(dataGroup1.getServiceAgreement().getName(), response.get(0).getServiceAgreement().getName());
        assertEquals(dataGroup1.getId(), response.get(0).getDataGroups().get(0).getId());
        assertEquals(dataGroup1.getName(), response.get(0).getDataGroups().get(0).getName());
        assertEquals(dataGroup1.getDescription(), response.get(0).getDataGroups().get(0).getDescription());
        assertEquals(dataGroup2.getId(), response.get(0).getDataGroups().get(1).getId());
        assertEquals(dataGroup2.getName(), response.get(0).getDataGroups().get(1).getName());
        assertEquals(dataGroup2.getDescription(), response.get(0).getDataGroups().get(1).getDescription());
    }

    @Test
    public void shouldThrowBadRequestWhenServiceAgreementAndDataItemIdentifiersAreNull() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.searchDataGroups(new InternalRequest<>(new PresentationGetDataGroupsRequest(), null),
                "TEST"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenServiceAgreementIdentifierIsNullAndDataItemIdentifiersIsEmpty() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.searchDataGroups(new InternalRequest<>(new PresentationGetDataGroupsRequest()
                .withDataItemIdentifier(new PresentationItemIdentifier()), null), "TEST"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenServiceAgreementIdentifierIsEmptyAndDataItemIdentifiersIsNull() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.searchDataGroups(new InternalRequest<>(new PresentationGetDataGroupsRequest()
                .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()), null), "TEST"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldThrowBadRequestWhenServiceAgreementIdentifierIsInvalidAndDataItemIdentifiersIsEmpty() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> testy.searchDataGroups(new InternalRequest<>(new PresentationGetDataGroupsRequest()
                .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier())
                .withServiceAgreementIdentifier(new PresentationServiceAgreementIdentifier()
                    .withIdIdentifier("id").withNameIdentifier("name")), null), "TEST"));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldCallRepositoryWhenLegalEntityIdentifierIsNotNull() {

        doNothing().when(validationConfig).validateDataGroupType(eq("TEST"));
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
                .withLegalEntityIdentifier(new LegalEntityIdentifier()
                        .withExternalIdentifier("externalIdentifier"));

        DataGroup dataGroup1 = new DataGroup()
                .withServiceAgreement(new ServiceAgreement()
                        .withName("saName")
                        .withId("said1")
                        .withDescription("saDesc")
                        .withExternalId("saEx1"))
                .withDataItemType("TEST")
                .withName("dgName1")
                .withDescription("dgDesc1")
                .withId("1");

        when(dataGroupJpaRepository
                .findAllDataGroupsByServiceAgreementAndDataItem(argThat(value -> true),
                        argThat(value -> true),
                        argThat(value -> true),
                        argThat(value -> true),
                        argThat(value -> true),
                        argThat(value -> true),
                        argThat(value -> true)))
                .thenReturn(Lists.newArrayList(dataGroup1));
        List<PresentationServiceAgreementWithDataGroups> response = testy
                .searchDataGroups(new InternalRequest<>(request, null), "TEST").getData();

        verify(mockDataItemExternalIdConverterService, times(0))
                .getInternalId(anyString(), anyString());

        verify(dataGroupJpaRepository)
                .findAllDataGroupsByServiceAgreementAndDataItem(eq("TEST"), isNull(),
                        isNull(), isNull(), isNull(),
                        eq(request.getLegalEntityIdentifier().getExternalIdIdentifier()), eq(SharesEnum.ACCOUNTS));

        assertEquals(1, response.size());
    }

    @Test
    @Parameters({"ACCOUNTS", "USERS", "USERSANDACCOUNTS"})
    public void shouldCallRepositoryWhenLegalEntityIdentifierIsNotNull(SharesEnum sharesEnum) {

        doNothing().when(validationConfig).validateDataGroupType("TEST");
        PresentationGetDataGroupsRequest request = new PresentationGetDataGroupsRequest()
            .withLegalEntityIdentifier(new LegalEntityIdentifier()
                .withExternalIdentifier("externalIdentifier").withShares(sharesEnum));

        DataGroup dataGroup1 = new DataGroup()
            .withServiceAgreement(new ServiceAgreement()
                .withName("saName")
                .withId("said1")
                .withDescription("saDesc")
                .withExternalId("saEx1"))
            .withDataItemType("TEST")
            .withName("dgName1")
            .withDescription("dgDesc1")
            .withId("1");

        when(dataGroupJpaRepository
            .findAllDataGroupsByServiceAgreementAndDataItem(argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true),
                argThat(value -> true)))
            .thenReturn(Lists.newArrayList(dataGroup1));
        List<PresentationServiceAgreementWithDataGroups> response = testy
            .searchDataGroups(new InternalRequest<>(request, null), "TEST").getData();

        verify(mockDataItemExternalIdConverterService, times(0))
            .getInternalId(anyString(), anyString());

        verify(dataGroupJpaRepository)
            .findAllDataGroupsByServiceAgreementAndDataItem(eq("TEST"), isNull(),
                isNull(), isNull(), isNull(),
                eq(request.getLegalEntityIdentifier().getExternalIdIdentifier()), eq(sharesEnum));

        assertEquals(1, response.size());
    }
}