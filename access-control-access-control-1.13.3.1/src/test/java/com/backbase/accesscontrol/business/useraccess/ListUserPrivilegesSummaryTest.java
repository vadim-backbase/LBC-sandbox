package com.backbase.accesscontrol.business.useraccess;

import static com.backbase.accesscontrol.util.ExceptionUtil.getForbiddenException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_071;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.UserPrivilegesSummaryGetResponseBodyDto;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.UserAccessPrivilegeService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListUserPrivilegesSummaryTest {

    @Mock
    private ListUserPrivilegesSummary listUserPrivilegesSummary;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private UserAccessPrivilegeService privilegeService;

    @Before
    public void setUp() throws Exception {
        listUserPrivilegesSummary = new ListUserPrivilegesSummary(
            privilegeService, userContextUtil);
    }

    @Test
    public void shouldPassIfGetUserPrivilegesSummaryIsInvoked() {
        String userId = "1";
        String serviceAgreementId = "2";

        InternalRequest<Void> internalRequest = getInternalRequest(null);

        List<UserPrivilegesSummaryGetResponseBodyDto> list = new ArrayList<>();
        UserPrivilegesSummaryGetResponseBodyDto userPrivilegesSummary = new UserPrivilegesSummaryGetResponseBodyDto();
        list.add(userPrivilegesSummary);

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(userId, "leid"));
        when(userContextUtil.getServiceAgreementId())
            .thenReturn(serviceAgreementId);

        when(privilegeService.getPrivilegesSummary(eq(userId), eq(serviceAgreementId)))
            .thenReturn(list);

        listUserPrivilegesSummary.getUserPrivilegesSummary(internalRequest);

        verify(privilegeService, times(1))
            .getPrivilegesSummary(eq(userId), eq(serviceAgreementId));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenNoUserContext() {
        String userId = "1";
        String serviceAgreementId = "2";
        InternalRequest<Void> internalRequest = getInternalRequest(null);

        when(userContextUtil.getServiceAgreementId())
            .thenThrow(getForbiddenException(ERR_AG_071.getErrorMessage(), ERR_AG_071.getErrorCode()));

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> listUserPrivilegesSummary.getUserPrivilegesSummary(internalRequest));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_071.getErrorMessage(), ERR_AG_071.getErrorCode())));

        verify(privilegeService, times(0))
            .getPrivilegesSummary(eq(userId), eq(serviceAgreementId));
    }
}
