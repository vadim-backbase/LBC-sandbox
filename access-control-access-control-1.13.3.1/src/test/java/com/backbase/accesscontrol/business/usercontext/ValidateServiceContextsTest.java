package com.backbase.accesscontrol.business.usercontext;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_073;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserContextEncryptionService;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.dto.UserContextDto;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jmx.access.InvalidInvocationException;

@RunWith(MockitoJUnitRunner.class)
public class ValidateServiceContextsTest {

    private final String EXTERNAL_USER_ID = "externalUserId";
    @Mock
    private UserContextService userContextService;
    @Mock
    private UserContextUtil userContextUtil;
    @Mock
    private UserContextEncryptionService userContextEncryptionService;
    @Mock
    private InternalRequest<Void> request = getInternalRequest(null);
    private ValidateServiceContexts validateServiceContexts;

    @Before
    public void setUp() throws Exception {
        validateServiceContexts = new ValidateServiceContexts(
            userContextService,
            userContextEncryptionService,
            userContextUtil
        );
        String INTERNAL_USER_ID = "internalUserId";
        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto(INTERNAL_USER_ID, "leId"));
    }

    @Test
    public void testValidServiceAgreement() throws Exception {
        final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

        String ENCRYPTED_TOKEN = "zaq1xsw2cde3vfr4";
        when(userContextEncryptionService.encryptUserContextToJwt(
            eq(new UserContextDto(SERVICE_AGREEMENT_ID, EXTERNAL_USER_ID)))).thenReturn(ENCRYPTED_TOKEN);

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userId", "leid"));

        doNothing().when(userContextService)
            .validateUserContext(anyString(), anyString());

        InternalRequest<String> internalRequestToken = validateServiceContexts
            .validateServiceAgreement(request, EXTERNAL_USER_ID, SERVICE_AGREEMENT_ID);
        assertEquals(ENCRYPTED_TOKEN, internalRequestToken.getData());
    }

    @Test
    public void testEncryptionServiceError() throws Exception {
        final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

        when(userContextEncryptionService.encryptUserContextToJwt(
            eq(new UserContextDto(SERVICE_AGREEMENT_ID, EXTERNAL_USER_ID))))
            .thenThrow(new InvalidInvocationException("Token encryption process failed."));

        doNothing().when(userContextService)
            .validateUserContext(anyString(), anyString());

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userId", "leid"));

        assertThrows(InvalidInvocationException.class,
            () -> validateServiceContexts
                .validateServiceAgreement(request, EXTERNAL_USER_ID, SERVICE_AGREEMENT_ID));

    }

    @Test
    public void testNotValidServiceAgreement() {
        final String SERVICE_AGREEMENT_ID = "serviceAgreementId";

        doThrow(ForbiddenException.class).when(userContextService)
            .validateUserContext(anyString(), anyString());

        when(userContextUtil.getUserContextDetails())
            .thenReturn(new UserContextDetailsDto("userId", "leid"));

        ForbiddenException forbiddenException = assertThrows(ForbiddenException.class,
            () -> validateServiceContexts
                .validateServiceAgreement(request, EXTERNAL_USER_ID, SERVICE_AGREEMENT_ID));

        assertThat(forbiddenException,
            is(new ForbiddenErrorMatcher(ERR_AG_073.getErrorMessage(), ERR_AG_073.getErrorCode())));
    }
}