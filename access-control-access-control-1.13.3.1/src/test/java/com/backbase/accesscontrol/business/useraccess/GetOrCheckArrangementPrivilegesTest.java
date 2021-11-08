package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_092;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ArrangementPrivilegesDto;
import com.backbase.accesscontrol.dto.parameterholder.DataItemPermissionsSearchParametersHolder;
import com.backbase.accesscontrol.mappers.ArrangementPrivilegesMapper;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users.ArrangementPrivilegesGetResponseBody;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetOrCheckArrangementPrivilegesTest {

    @Mock
    private UserAccessPrivilegeService userAccessPrivilegeService;
    @Mock
    private InternalRequestContext internalRequestContext;

    @Spy
    private ArrangementPrivilegesMapper arrangementPrivilegesMapper = Mappers
        .getMapper(ArrangementPrivilegesMapper.class);

    @InjectMocks
    private GetOrCheckArrangementPrivileges getOrCheckArrangementPrivileges;

    @Test
    public void checkValidPermissions() {

        String userId = "001";
        String arrangementId = "arrangementId";
        String privilege = "read";
        String serviceAgreementId = "001";
        String functionName = "Contacts";
        String resourceName = "Contacts";

        InternalRequest<DataItemPermissionsSearchParametersHolder> request =
            getInternalRequest(new DataItemPermissionsSearchParametersHolder()
                    .withUserId(userId)
                    .withServiceAgreementId(serviceAgreementId)
                    .withFunctionName(functionName)
                    .withResourceName(resourceName)
                    .withPrivilege(privilege),
                internalRequestContext);

        List<ArrangementPrivilegesDto> result = Collections
            .singletonList(new ArrangementPrivilegesDto().withArrangementId(arrangementId));

        when(userAccessPrivilegeService
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(privilege),
                eq(null), eq(arrangementId)))
            .thenReturn(result);

        getOrCheckArrangementPrivileges.checkPermissions(request, arrangementId);
        verify(userAccessPrivilegeService, times(1))
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(privilege),
                eq(null), eq(arrangementId));
    }

    @Test
    public void checkForbiddenPermissions() {
        String userId = "001";
        String arrangementId = "001";
        String privilege = "read";
        String serviceAgreementId = "001";
        String functionName = "Contacts";
        String resourceName = "Contacts";

        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(arrangementId)
                .withFunctionName(functionName)
                .withResourceName(resourceName)
                .withPrivilege(privilege),
            internalRequestContext);

        when(userAccessPrivilegeService
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(privilege),
                eq(null), eq(arrangementId)))
            .thenReturn(new ArrayList<>());

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> getOrCheckArrangementPrivileges.checkPermissions(request, "001"));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_092.getErrorMessage(), ERR_AG_092.getErrorCode())));

        verify(userAccessPrivilegeService, times(1))
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(privilege),
                eq(null),
                eq(arrangementId));
    }

    @Test
    public void shouldPassWhenGetArrangementPrivilegesIsInvoked() {
        List<ArrangementPrivilegesDto> getResponseBodyList = new ArrayList<>();
        getResponseBodyList.add(new ArrangementPrivilegesDto());

        String userId = "001";
        String serviceAgreementId = "001";
        String functionName = "Contacts";
        String resourceName = "Contacts";

        when(userAccessPrivilegeService
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(null),
                eq(null), eq(null)))
            .thenAnswer(ans -> getResponseBodyList);
        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(functionName)
                .withResourceName(resourceName),
            null);
        InternalRequest<List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.users
            .ArrangementPrivilegesGetResponseBody>> arrangementPrivileges = getOrCheckArrangementPrivileges
            .getArrangementPrivileges(request);
        assertThat(arrangementPrivileges.getData(), hasSize(1));
        verify(userAccessPrivilegeService)
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName),
                eq(null),
                eq(null), eq(null));
    }

    @Test
    public void shouldPassWhenPrivilegeNameIsNullAndResponseBodyIsNotEmpty() {
        String functionName = "Contacts";
        String resourceName = "Contacts";
        String userId = "001";
        String serviceAgreementId = "001";

        when(userAccessPrivilegeService
            .getArrangementPrivileges(eq(userId), eq(serviceAgreementId), eq(functionName), eq(resourceName), eq(null),
                eq(null), eq(null)))
            .thenReturn(getCreatedArrangementPrivileges());

        InternalRequest<DataItemPermissionsSearchParametersHolder> request = getInternalRequest(
            new DataItemPermissionsSearchParametersHolder()
                .withUserId(userId)
                .withServiceAgreementId(serviceAgreementId)
                .withFunctionName(functionName)
                .withResourceName(resourceName),
            null);

        InternalRequest<List<ArrangementPrivilegesGetResponseBody>> arrangementPrivileges = getOrCheckArrangementPrivileges
            .getArrangementPrivileges(request);

        assertEquals(3, arrangementPrivileges.getData().size());
    }

    private List<ArrangementPrivilegesDto> getCreatedArrangementPrivileges() {
        ArrangementPrivilegesDto arrangementPrivilege1 = new ArrangementPrivilegesDto()
            .withArrangementId("1")
            .withPrivileges(asList(new Privilege().withPrivilege("read"), new Privilege().withPrivilege("execute")));
        ArrangementPrivilegesDto arrangementPrivilege2 = new ArrangementPrivilegesDto()
            .withArrangementId("2")
            .withPrivileges(Collections.singletonList(new Privilege().withPrivilege("read")));
        ArrangementPrivilegesDto arrangementPrivilege3 = new ArrangementPrivilegesDto()
            .withArrangementId("1")
            .withPrivileges(asList(new Privilege().withPrivilege("execute"), new Privilege().withPrivilege("delete"),
                new Privilege().withPrivilege("read")));
        List<ArrangementPrivilegesDto> allArrangementPrivileges = new ArrayList<>();
        allArrangementPrivileges.add(arrangementPrivilege1);
        allArrangementPrivileges.add(arrangementPrivilege2);
        allArrangementPrivileges.add(arrangementPrivilege3);
        return allArrangementPrivileges;
    }
}