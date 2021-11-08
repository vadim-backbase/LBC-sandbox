package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.impl.PersistenceLegalEntityService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustomerItemServiceTest {

    @Mock
    private PersistenceLegalEntityService persistenceLegalEntityService;

    @InjectMocks
    private CustomerItemService customerItemService;

    @Test
    public void shouldReturnCustomersType() {
        assertEquals("CUSTOMERS", customerItemService.getType());
    }

    @Test
    public void shouldCallPersistenceAndReturnInternalId() {
        Map<String, String> result = new HashMap<>();
        result.put("externalId1", "internalId1");
        when(persistenceLegalEntityService
            .findInternalByExternalIdsForLegalEntity(eq(Collections.singleton("externalId1"))))
            .thenReturn(result);
        assertEquals(singletonList("internalId1"), customerItemService.getInternalId("externalId1", null));
    }

    @Test
    public void shouldConvertNotFoundToBadRequestExceptionWhenGettingInternalId() {
        when(persistenceLegalEntityService
            .findInternalByExternalIdsForLegalEntity(any()))
            .thenReturn(new HashMap<>());

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> customerItemService.getInternalId("externalId1", null));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode())));
    }

    @Test
    public void shouldGetEmptyInternalIdsWhenEmptyExternalIdsArePassed() {
        Set<String> externalIds = emptySet();
        Map<String, List<String>> externalToInternalIds = customerItemService
            .mapExternalToInternalIds(externalIds, null);

        assertNotNull(externalToInternalIds);
        assertEquals(0, externalToInternalIds.keySet().size());
        verify(persistenceLegalEntityService, times(0))
            .findInternalByExternalIdsForLegalEntity(any());
    }

    @Test
    public void shouldGetExternalToInternalIds() {
        Set<String> externalIds = Sets.newHashSet(asList("externalId1", "externalId2"));

        Map<String, String> legalEntityIds = new HashMap<>();
        legalEntityIds.put("externalId1", "internalId1");
        legalEntityIds.put("externalId2", "internalId2");

        when(persistenceLegalEntityService
            .findInternalByExternalIdsForLegalEntity(eq(externalIds)))
            .thenAnswer(answer -> legalEntityIds);

        Map<String, List<String>> externalToInternalIds = customerItemService
            .mapExternalToInternalIds(externalIds, null);

        assertNotNull(externalToInternalIds);
        assertEquals(externalIds.size(), externalToInternalIds.keySet().size());
        assertEquals(singletonList("internalId1"), externalToInternalIds.get("externalId1"));
        assertEquals(singletonList("internalId2"), externalToInternalIds.get("externalId2"));
    }

}