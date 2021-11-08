package com.backbase.accesscontrol.business.usercontext;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ListElementsWrapper;
import com.backbase.accesscontrol.dto.UserContextDetailsDto;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.Element;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.usercontext.UserContextsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.usercontext.UserContextServiceAgreementsGetResponseBody;
import java.util.Collections;
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
public class GetUserContextsTest {

    @Mock
    private UserContextService userContextService;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private UserContextUtil userContextUtil;

    @InjectMocks
    private GetUserContexts getUserContexts;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetUserContextsByUserId() {
        setupGetUserByExternalId();
        String serviceAgreementId = "saId";
        String serviceAgreementName = "saName";
        Element serviceAgreement = new Element().withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementMaster(false).withServiceAgreementName(serviceAgreementName);
        UserContextsGetResponseBody body = new UserContextsGetResponseBody()
            .withElements(Collections.singletonList(serviceAgreement)).withTotalElements(1L);

        when(userContextService
            .getUserContextsByUserId(anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(body);

        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "", 0, "", 5).getData();

        assertEquals(1, listElementsWrapper.getRecords().size());
        assertEquals(1, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(serviceAgreementId, listElementsWrapper.getRecords().get(0).getId());
        assertEquals(serviceAgreementName, listElementsWrapper.getRecords().get(0).getName());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void testGetUserContextsByUserIdFromToken() {
        String serviceAgreementId = "saId";
        String serviceAgreementName = "saName";
        Element serviceAgreement = new Element().withServiceAgreementId(serviceAgreementId)
            .withServiceAgreementMaster(false).withServiceAgreementName(serviceAgreementName);
        UserContextsGetResponseBody body = new UserContextsGetResponseBody()
            .withElements(Collections.singletonList(serviceAgreement)).withTotalElements(1L);

        when(userContextUtil.getAuthenticatedUserName()).thenReturn("user");
        when(userContextUtil.getUserContextDetails()).thenReturn(new UserContextDetailsDto("userId", "leId"));
        when(userContextService
            .getUserContextsByUserId(anyString(), anyString(), anyInt(), anyInt()))
            .thenReturn(body);

        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "query", 0, "", 5).getData();

        assertEquals(1, listElementsWrapper.getRecords().size());
        assertEquals(1, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(serviceAgreementId, listElementsWrapper.getRecords().get(0).getId());
        assertEquals(serviceAgreementName, listElementsWrapper.getRecords().get(0).getName());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(1)).getUserContextDetails();
        verify(userManagementService, times(0)).getUserByExternalId(anyString());
        verify(userContextService).getUserContextsByUserId(eq("userId"), eq("query"), eq(0), eq(5));
    }

    @Test
    public void shouldFindAllUserContexts() {
        setupGetUserByExternalId();
        int numberServiceAgreements = 5;

        setupAServiceAgreementPageResult(numberServiceAgreements, numberServiceAgreements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "", 0, "", numberServiceAgreements).getData();

        assertEquals(numberServiceAgreements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberServiceAgreements, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindAllUserContextsWithNullQuery() {
        setupGetUserByExternalId();
        int numberServiceAgreements = 5;

        setupAServiceAgreementPageResult(numberServiceAgreements, numberServiceAgreements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", null, 0, "",
                numberServiceAgreements).getData();

        assertEquals(numberServiceAgreements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberServiceAgreements, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindMasterServiceAgreementFirstPagePageSizeOne() {
        setupGetUserByExternalId();
        int numberServiceAgreements = 5;
        int numberPerPage = 1;

        setupAServiceAgreementPageResult(numberPerPage, numberServiceAgreements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "", 0, "",
                numberPerPage).getData();

        assertEquals(numberServiceAgreements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberPerPage, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindFirstPageOfTwoUserContexts() {
        setupGetUserByExternalId();
        int numberServiceAgreements = 5;
        int numberPerPage = 2;

        setupAServiceAgreementPageResult(numberPerPage, numberServiceAgreements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "", 0, "", numberPerPage).getData();

        assertEquals(numberServiceAgreements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberPerPage, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindSecondPageUserContexts() {
        setupGetUserByExternalId();
        int numberServiceAgreements = 5;
        int numberPerPage = 2;
        int firstIndexSecondPage = 2;

        setupAServiceAgreementPageResult(numberPerPage, numberServiceAgreements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "",
                firstIndexSecondPage, "", numberPerPage).getData();

        assertEquals(numberServiceAgreements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberPerPage, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindFirstPageWithoutMasterAgreementFittingQueryParameter() {
        setupGetUserByExternalId();
        int numberElements = 5;
        int numberPerPage = 2;

        setupAServiceAgreementPageResult(numberPerPage, numberElements);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "serviceagreement", 0, "",
                numberPerPage).getData();

        assertEquals(numberElements, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(numberPerPage, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    @Test
    public void shouldFindMasterAgreementFittingQueryParameter() {
        setupGetUserByExternalId();
        int numberPerPage = 5;

        setupAServiceAgreementPageResult(1, 1);
        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), "user", "master", 0, "", numberPerPage).getData();
        assertEquals(1, listElementsWrapper.getTotalNumberOfRecords().longValue());
        assertEquals(1, listElementsWrapper.getRecords().size());
        verify(userContextUtil).getAuthenticatedUserName();
        verify(userContextUtil, times(0)).getUserContextDetails();
    }

    private void setupGetUserByExternalId() {
        com.backbase.dbs.user.api.client.v2.model.GetUser body = new GetUser();
        body.setExternalId("exUser");
        body.setId("userId");

        when(userContextUtil.getAuthenticatedUserName()).thenReturn("other_user");
        when(userManagementService.getUserByExternalId(anyString()))
            .thenReturn(body);
    }

    private void setupAServiceAgreementPageResult(long numberElements, long totalNumberOfElements) {
        UserContextsGetResponseBody userContextResposeBody = new UserContextsGetResponseBody()
            .withElements(getServiceAgreements(numberElements))
            .withTotalElements(totalNumberOfElements);
        when(userContextService.getUserContextsByUserId(anyString(), any(), anyInt(), anyInt()))
            .thenReturn(userContextResposeBody);
    }

    @Test
    public void shouldReturnContextsWithoutMasterServiceAgreementWhenUserHasPermissionUnderMasterServiceAgreement() {
        String userId = "U-001";
        String internalUserId = "Internal-U-001";
        String legalEntityId = "LE-001";
        String serviceAgreementId = "SA-001";

        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(internalUserId);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(userId);
        mockGetUserByExternalId(userId, user1);

        UserContextsGetResponseBody userContexts = new UserContextsGetResponseBody()
            .withElements(singletonList(new Element().withServiceAgreementId("SA-002")));
        mockGetUserContexts(internalUserId, userContexts);

        ListElementsWrapper<UserContextServiceAgreementsGetResponseBody> listElementsWrapper = getUserContexts
            .getUserContextsByUserId(getInternalRequest(null), userId, "", 0, "", Integer.MAX_VALUE).getData();

        List<String> serviceAgreementsInContext = listElementsWrapper.getRecords()
            .stream()
            .map(UserContextServiceAgreementsGetResponseBody::getId)
            .collect(Collectors.toList());

        assertEquals(userContexts.getElements().size(), listElementsWrapper.getRecords().size());
        assertTrue(serviceAgreementsInContext.contains("SA-002"));
        assertFalse(serviceAgreementsInContext.contains(serviceAgreementId));
    }


    private void mockGetUserContexts(String userId, UserContextsGetResponseBody userContexts) {
        when(userContextService
            .getUserContextsByUserId(eq(userId), anyString(), anyInt(), anyInt()))
            .thenReturn(userContexts);
    }

    private List<Element> getServiceAgreements(long n) {
        return LongStream.range(0, n).mapToObj(num -> new Element()
            .withServiceAgreementId("s" + num)
            .withServiceAgreementName("serviceagreement" + num)
            .withServiceAgreementMaster(false)
        ).collect(Collectors.toList());
    }

    private void mockGetUserByExternalId(String userId, GetUser user) {
        when(userManagementService.getUserByExternalId(eq(userId)))
            .thenReturn(user);
    }
}