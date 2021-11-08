package com.backbase.accesscontrol.business.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_098;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.contact.api.client.v2.ContactsApi;
import com.backbase.dbs.contact.api.client.v2.model.ContactsInternalIdsFilterPostRequestBody;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContactsServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContactsApi contactsApi;

    @InjectMocks
    private ContactsService contactsService;

    @Test
    public void shouldConvertSingleExternalContactId() {

        ContactsInternalIdsFilterPostRequestBody requestBody = new ContactsInternalIdsFilterPostRequestBody()
            .externalContactIds(singletonList("ext1"));
        when(contactsApi.postContactsInternalIdsFilter(eq("sa1"), eq(requestBody)))
            .thenReturn(new HashMap<>() {{
                put("ext1", singletonList("id1"));
            }});

        List<String> response = contactsService.convertSingleExternalContactId("ext1", "sa1");
        assertEquals(singletonList("id1"), response);
    }

    @Test
    public void shouldCallConvertExternalContactIds() {

        Map<String, List<String>> response = new HashMap<>();
        response.put("ext1", singletonList("id1"));
        ContactsInternalIdsFilterPostRequestBody requestBody = new ContactsInternalIdsFilterPostRequestBody()
            .externalContactIds(singletonList("ext1"));
        when(contactsApi.postContactsInternalIdsFilter(eq("sa1"), eq(requestBody)))
            .thenReturn(response);

        Map<String, List<String>> res = contactsService.convertExternalContactIds(Lists.newArrayList("ext1"), "sa1");

        verify(contactsApi, times(1)).postContactsInternalIdsFilter(eq("sa1"), eq(requestBody));

        assertTrue(res.containsKey("ext1"));
        assertTrue(res.get("ext1").contains("id1"));
    }

    @Test
    public void shouldReturnEmptyListWhenContactNotFound() {

        ContactsInternalIdsFilterPostRequestBody requestBody = new ContactsInternalIdsFilterPostRequestBody()
            .externalContactIds(singletonList("ext1"));
        when(contactsApi.postContactsInternalIdsFilter(eq("sa1"), eq(requestBody)))
            .thenReturn(new HashMap<>());

        List<String> response = contactsService.convertSingleExternalContactId("ext1", "sa1");
        assertTrue(response.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenServiceAgreementIdIsNull() {

        assertThat(assertThrows(BadRequestException.class,
            () -> contactsService.convertExternalContactIds(Lists.newArrayList("ext1"), null)),
            new BadRequestErrorMatcher(ERR_AG_098.getErrorMessage(), ERR_AG_098.getErrorCode()));
    }
}