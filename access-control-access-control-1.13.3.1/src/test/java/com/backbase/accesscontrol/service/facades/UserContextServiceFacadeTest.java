package com.backbase.accesscontrol.service.facades;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserContextServiceFacadeTest {

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private UserContextServiceFacade userContextServiceFacade;

    private String USER_ID = "user1";

    @Test
    public void testExecuteRequestThrowWhenUserContextIsInvalid() {

        doThrow(getForbiddenException("Invalid user context for user id " + USER_ID, null))
            .when(userContextService).validateUserContext(eq(USER_ID), any());

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> userContextServiceFacade.validateUserContext(USER_ID, "saId"));

        assertThat(exception, new ForbiddenErrorMatcher("Invalid user context for user id " + USER_ID, null));
    }

    @Test
    public void testExecuteRequestDoNothingWhenUserContextIsValid() {

        doNothing().when(userContextService).validateUserContext(anyString(), anyString());

        userContextServiceFacade.validateUserContext(USER_ID, "saId");
        verify(userContextService).validateUserContext(eq(USER_ID), eq("saId"));
    }

    @Test
    public void testGetDataItemsPermissions() {

        String internalUserId = "user-001";
        String serviceAgreementFromContext = "SA-001";
        Set<String> uniqueTypes = Sets.newHashSet("ARRANGEMENTS", "PAYEES");
        com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions dataItemsPermissions =
            new com.backbase.accesscontrol.service.rest.spec.model.DataItemsPermissions();

        doNothing().when(userContextService).checkDataItemsPermissions(anySet(), any(), anyString(), anyString());

        userContextServiceFacade
            .getDataItemsPermissions(uniqueTypes, dataItemsPermissions, internalUserId, serviceAgreementFromContext);
        verify(userContextService).checkDataItemsPermissions(eq(uniqueTypes), eq(dataItemsPermissions),
            eq(internalUserId), eq(serviceAgreementFromContext));
    }

    @Test
    public void testGetUserContextsByUserId() {
        UserContextsGetResponseBody userContextGetResponseBody = new UserContextsGetResponseBody();

        Element mockElement = getGetResponseBodyElement();

        userContextGetResponseBody.setElements(Arrays.asList(mockElement));

        userContextGetResponseBody.setTotalElements(1L);

        when(userContextService.getUserContextsByUserId(USER_ID, "s", 1, 10))
            .thenReturn(userContextGetResponseBody);

        UserContextsGetResponseBody responseBody =
            userContextServiceFacade.getUserContextsByUserId(USER_ID, "s", 1, 10);

        Element element = responseBody.getElements().get(0);

        assertNotNull(element);
        assertEquals(mockElement.getServiceAgreementName(), element.getServiceAgreementName());
        assertEquals(mockElement.getServiceAgreementId(), element.getServiceAgreementId());
    }

    private Element getGetResponseBodyElement() {
        Element element = new Element();
        element.setServiceAgreementId("saId1");
        element.setServiceAgreementName("Service Agreement 1");
        return element;
    }

}
