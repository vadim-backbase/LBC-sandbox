package com.backbase.accesscontrol.service.facades;


import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.routes.useraccess.GetUserContextsRouteProxy;
import com.backbase.accesscontrol.routes.useraccess.ValidateServiceAgreementRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserContextServiceTest {

    @Mock
    private GetUserContextsRouteProxy getUserContextsRouteProxy;

    @Mock
    private ValidateServiceAgreementRouteProxy validateServiceAgreementRouteProxy;

    @Mock
    private InternalRequest<String> internalRequestToken;

    @Mock
    private InternalRequest<ListElementsWrapper<UserContextServiceAgreementsGetResponseBody>> serviceAgreementMock;

    @InjectMocks
    private UserContextServiceImpl userContextService;

    @Before
    public void setUp() throws Exception {
        when(getUserContextsRouteProxy
            .getUserContextsByUserId(any(), anyString(), anyString(), anyInt(), anyString(), anyInt()))
            .thenReturn(serviceAgreementMock);

    }

    @Test
    public void shouldFindAllUserContexts() {
        final int NUMBER_SERVICE_AGREEMENTS = 5;
        when(serviceAgreementMock.getData())
            .thenReturn(new ListElementsWrapper<>(getServiceAgreements(NUMBER_SERVICE_AGREEMENTS), (long) NUMBER_SERVICE_AGREEMENTS));

        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> response = userContextService
            .getUserContextByUserId("user", "", 0, "", NUMBER_SERVICE_AGREEMENTS);

        assertEquals(NUMBER_SERVICE_AGREEMENTS, response.getTotalNumberOfRecords().longValue());

        assertEquals(NUMBER_SERVICE_AGREEMENTS, response.getRecords().size());
    }

    @Test
    public void shouldGetATokenAfterValidateServiceAgreementId() {
        String ENCRYPTED_TOKEN = "zaq1xsw2cde3";
        when(internalRequestToken.getData()).thenReturn(ENCRYPTED_TOKEN);
        when(validateServiceAgreementRouteProxy
            .validate(any(), anyString(), anyString())).thenReturn(internalRequestToken);

        String token = userContextService.validate("userId", "serviceAgreementId");

        assertEquals(ENCRYPTED_TOKEN, token);
    }

    private List<UserContextServiceAgreementsGetResponseBody> getServiceAgreements(long n) {
        return LongStream.range(0, n)
            .mapToObj(num -> getElementServiceAgreement("s" + num, "serviceagreement" + num, true))
            .collect(Collectors.toList());
    }

    private UserContextServiceAgreementsGetResponseBody getElementServiceAgreement(String id, String name, boolean master) {
        return new UserContextServiceAgreementsGetResponseBody().withId(id).withName(name).withIsMaster(master);
    }
}
