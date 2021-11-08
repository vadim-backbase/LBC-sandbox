package com.backbase.accesscontrol.business.datagroup.dataitems;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_077;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.ContactsService;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Sets;
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
public class ContactItemServiceTest {

    @InjectMocks
    private ContactItemService contactItemService;
    @Mock
    private ContactsService contactsService;

    @Test
    public void shouldReturnContactType() {
        assertEquals("PAYEES", contactItemService.getType());
    }

    @Test
    public void shouldCallGetInternalId() {
        String internalId = "internalId";
        String externalId = "externalId";
        String saId = "saId";
        when(contactsService
            .convertSingleExternalContactId(eq(externalId), eq(saId)))
            .thenReturn(singletonList(internalId));
        assertEquals(singletonList(internalId), contactItemService.getInternalId(externalId, saId));
    }

    @Test
    public void shouldGetExternalToInternalIds() {
        Set<String> externalIds = Sets.newHashSet(asList("externalId1", "externalId2"));

        Map<String, String> response = new HashMap<>();
        response.put("externalId1", "internalId1");
        response.put("externalId2", "internalId2");

        when(contactsService
            .convertExternalContactIds(eq(externalIds), eq("saId")))
            .thenAnswer(answer -> response);

        Map<String, List<String>> externalToInternalIds = contactItemService
            .mapExternalToInternalIds(externalIds, "saId");

        assertNotNull(externalToInternalIds);
        assertEquals(externalIds.size(), externalToInternalIds.keySet().size());
        assertEquals("internalId1", externalToInternalIds.get("externalId1"));
        assertEquals("internalId2", externalToInternalIds.get("externalId2"));
    }

    @Test
    public void shouldThrowExceptionWhenServiceAgreementIdIsNull() {

        when(contactsService
            .convertSingleExternalContactId(eq("ext1"), eq(null)))
            .thenThrow(getBadRequestException(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode()));

        assertThat(assertThrows(BadRequestException.class,
            () -> contactItemService.getInternalId("ext1", null)),
            new BadRequestErrorMatcher(ERR_ACQ_077.getErrorMessage(), ERR_ACQ_077.getErrorCode()));
    }
}