package com.backbase.accesscontrol.business.flows.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.LegalEntityErrorCodes.ERR_AG_013;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.auth.AccessControlValidator;
import com.backbase.accesscontrol.auth.AccessResourceType;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.mappers.SubEntitiesPersistenceToRecordsDtoMapper;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SubEntitiesPostResponseBody;
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
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class GetSubLegalEntitlesFlowTest {

    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private UserManagementService userManagementService;
    @Mock
    private AccessControlValidator accessControlValidator;
    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;
    @Spy
    private SubEntitiesPersistenceToRecordsDtoMapper subEntitiesPersistenceToRecordsDtoMapper = Mappers
        .getMapper(SubEntitiesPersistenceToRecordsDtoMapper.class);

    @InjectMocks
    private GetSubLegalEntitlesFlow getSubLegalEntitlesFlow;


    @Test
    public void shouldReturnListSubLegalEntitiesWhenInvokedWithAuthenticatedUser() {
        String legalEntityId = "1";
        String userID = "admin";
        String parentEntityId = null;
        String cursor = "cursor";
        Integer from = 100;
        Integer size = 10;
        String searchQuery = "searchQuery";
        List<String> excludeIds = asList("1", "2");

        LegalEntity legalEntity = new LegalEntity()
            .withName("LE")
            .withType(LegalEntityType.BANK)
            .withExternalId("externalId")
            .withId("0001");
        legalEntity.setAddition("key", "value");
        Page<LegalEntity> data = new PageImpl<>(singletonList(legalEntity));

        com.backbase.dbs.user.api.client.v2.model.GetUser userData =
            new com.backbase.dbs.user.api.client.v2.model.GetUser();
        userData.setLegalEntityId(legalEntityId);

        doReturn(userID).when(userContextUtil).getAuthenticatedUserName();

        doReturn(userData)
            .when(userManagementService).getUserByExternalId(eq(userID));

        when(
            accessControlValidator.userHasNoAccessToEntitlementResource(eq(legalEntityId), eq(AccessResourceType.NONE)))
            .thenReturn(false);

        doReturn(data)
            .when(persistenceLegalEntityService).getSubEntities(
            anyString(),
            any(SearchAndPaginationParameters.class),
            anyCollection());

        GetLegalEntitiesRequestDto requestDto = new GetLegalEntitiesRequestDto(parentEntityId, newHashSet(excludeIds),
            cursor, from, size, searchQuery);

        RecordsDto<SubEntitiesPostResponseBody> result = getSubLegalEntitlesFlow.execute(requestDto);

        assertEquals(data.getTotalElements(), result.getTotalNumberOfRecords().longValue());
        assertEquals(data.getContent().get(0).getId(), result.getRecords().get(0).getId());

        verify(accessControlValidator)
            .userHasNoAccessToEntitlementResource(eq(legalEntityId), eq(AccessResourceType.NONE));

        verify(persistenceLegalEntityService)
            .getSubEntities(eq(legalEntityId),
                eq(new SearchAndPaginationParameters(100, 10, searchQuery, cursor)),
                argThat(arg -> arg.containsAll(excludeIds) && arg.size() == excludeIds.size()));

        assertEquals(data.getTotalElements(), result.getTotalNumberOfRecords().longValue());
        assertEquals(data.getContent().get(0).getId(), result.getRecords().get(0).getId());
        assertEquals(data.getContent().get(0).getExternalId(), result.getRecords().get(0).getExternalId());
        assertEquals(data.getContent().get(0).getName(), result.getRecords().get(0).getName());
        assertEquals(data.getContent().get(0).getAdditions().get("key"),
            result.getRecords().get(0).getAdditions().get("key"));
        assertEquals(data.getContent().get(0).getType().toString(), result.getRecords().get(0).getType().toString());
    }

    @Test
    public void shouldThrowForbiddenExceptionIfAuthenticatedUserDoesntHaveAccess() {
        String legalEntityId = "1";
        String userID = "admin";
        String parentEntityId = null;
        String cursor = "cursor";
        Integer from = 100;
        Integer size = 10;
        String searchQuery = "searchQuery";
        List<String> excludeIds = asList("1", "2");

        com.backbase.dbs.user.api.client.v2.model.GetUser userData =
            new com.backbase.dbs.user.api.client.v2.model.GetUser();
        userData.setLegalEntityId(legalEntityId);

        doReturn(userID).when(userContextUtil).getAuthenticatedUserName();

        doReturn(userData)
            .when(userManagementService).getUserByExternalId(eq(userID));

        when(
            accessControlValidator.userHasNoAccessToEntitlementResource(eq(legalEntityId), eq(AccessResourceType.NONE)))
            .thenReturn(true);

        GetLegalEntitiesRequestDto requestDto = new GetLegalEntitiesRequestDto(parentEntityId, newHashSet(excludeIds),
            cursor, from, size, searchQuery);

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> getSubLegalEntitlesFlow.execute(requestDto));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_013.getErrorMessage(), ERR_AG_013.getErrorCode())));

        verify(accessControlValidator)
            .userHasNoAccessToEntitlementResource(eq(legalEntityId), eq(AccessResourceType.NONE));

        verify(persistenceLegalEntityService, times(0))
            .getSubEntities(anyString(),
                any(SearchAndPaginationParameters.class),
                anyCollection());
    }

    @Test
    public void shouldReturnListSubLegalEntitiesWhenInvokedWithParentEntityIdRequestParam() {
        String parentEntityId = "11";
        String cursor = "cursor";
        Integer from = 100;
        Integer size = 10;
        String searchQuery = "searchQuery";
        List<String> excludeIds = asList("1", "2");

        LegalEntity legalEntity = new LegalEntity()
            .withName("LE")
            .withType(LegalEntityType.CUSTOMER)
            .withExternalId("externalId")
            .withId("0001");
        legalEntity.setAddition("key", "value");

        Page<LegalEntity> data = new PageImpl<>(singletonList(legalEntity));

        when(accessControlValidator
            .userHasNoAccessToEntitlementResource(eq(parentEntityId), eq(AccessResourceType.NONE)))
            .thenReturn(false);

        doReturn(data).when(persistenceLegalEntityService).getSubEntities(
            anyString(),
            any(SearchAndPaginationParameters.class),
            anyCollection());

        GetLegalEntitiesRequestDto requestDto = new GetLegalEntitiesRequestDto(parentEntityId, newHashSet(excludeIds),
            cursor, from, size, searchQuery);

        RecordsDto<SubEntitiesPostResponseBody> result = getSubLegalEntitlesFlow.execute(requestDto);

        verify(accessControlValidator)
            .userHasNoAccessToEntitlementResource(eq(parentEntityId), eq(AccessResourceType.NONE));

        verify(persistenceLegalEntityService)
            .getSubEntities(eq(parentEntityId),
                eq(new SearchAndPaginationParameters(from, size, searchQuery, cursor)),
                argThat(arg -> arg.containsAll(excludeIds) && arg.size() == excludeIds.size()));
        assertEquals(data.getTotalElements(), result.getTotalNumberOfRecords().longValue());
        assertEquals(data.getContent().get(0).getId(), result.getRecords().get(0).getId());
        assertEquals(data.getContent().get(0).getExternalId(), result.getRecords().get(0).getExternalId());
        assertEquals(data.getContent().get(0).getName(), result.getRecords().get(0).getName());
        assertEquals(data.getContent().get(0).getAdditions().get("key"),
            result.getRecords().get(0).getAdditions().get("key"));
        assertEquals(data.getContent().get(0).getType().toString(), result.getRecords().get(0).getType().toString());
    }
}