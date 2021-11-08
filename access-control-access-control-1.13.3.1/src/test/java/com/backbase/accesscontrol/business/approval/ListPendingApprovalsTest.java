package com.backbase.accesscontrol.business.approval;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.service.ApprovalService;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.ApprovalsListDto;
import com.backbase.accesscontrol.dto.parameterholder.ApprovalsParametersHolder;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsRequest;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalCategory;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalItem;
import com.google.common.collect.Lists;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import java.util.ArrayList;
import java.util.Date;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListPendingApprovalsTest {

    @Mock
    private EventBus eventBus;
    @Mock
    private ApprovalService approvalService;
    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private ListPendingApprovals listPendingApprovals;

    @Before
    public void setUp() {
        listPendingApprovals = new ListPendingApprovals(userManagementService, approvalService);
    }

    @Test
    public void shouldListPendingApprovals() {
        String loggedUserId = "U-01";
        String loggedUserLegalEntityId = "LE-01";
        String contextServiceAgreementId = "SA-01";
        String function = "Assign Permissions";
        String approvalId1 = "AP-01";
        String action = "EDIT";
        String userId1 = "U-1";
        String userFullName1 = "user 1";
        Date createdAt1 = new Date();

        ApprovalsParametersHolder parametersHolder = new ApprovalsParametersHolder()
            .withUserId(loggedUserId)
            .withLegalEntityId(loggedUserLegalEntityId)
            .withServiceAgreementId(contextServiceAgreementId)
            .withFrom(0)
            .withSize(10)
            .withCursor("");

        PresentationPostFilterApprovalsResponse approvalResponse = new PresentationPostFilterApprovalsResponse()
            .approvals(Lists.newArrayList(new ApprovalDto()
                .function(function)
                .id(approvalId1)
                .serviceAgreementId(contextServiceAgreementId)
                .userId(userId1)
                .action(action)
                .createdAt(createdAt1)));

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setId(userId1);
        user.setFullName(userFullName1);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList usersResponseMock =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersList().addUsersItem(user);

        mockGetApprovalsFromApprovalsClient(approvalResponse);

        mockGetUsers(userId1, usersResponseMock);

        ApprovalsListDto response = listPendingApprovals
            .listApprovals(getInternalRequest(parametersHolder)).getData();

        assertThat(response.getPresentationApprovalItems(), hasItems(
            getApprovalItemMatcher(is(userId1), is(userFullName1), is(createdAt1),
                is(PresentationApprovalAction.valueOf(action)),
                is(PresentationApprovalCategory.fromValue(function)), is(approvalId1))
        ));
    }

    private void mockGetUsers(String userId1,GetUsersList usersResponseMock) {
        when(userManagementService.getUsers(eq(userId1)))
            .thenReturn(usersResponseMock);
    }

    private void mockGetApprovalsFromApprovalsClient(PresentationPostFilterApprovalsResponse approvalResponse) {
        when(approvalService
            .postFilterApprovals(anyInt(), anyString(), anyInt(), any(PresentationPostFilterApprovalsRequest.class)))
            .thenReturn(approvalResponse);
    }

    @Test
    public void shouldReturnEmptyListWhenNoPendingApprovals() {
        String loggedUserId = "U-01";
        String loggedUserLegalEntityId = "LE-01";
        String contextServiceAgreementId = "SA-01";

        ApprovalsParametersHolder parametersHolder = new ApprovalsParametersHolder()
            .withUserId(loggedUserId)
            .withLegalEntityId(loggedUserLegalEntityId)
            .withServiceAgreementId(contextServiceAgreementId)
            .withFrom(0)
            .withSize(10)
            .withCursor("");

        mockGetApprovalsFromApprovalsClient(
            new PresentationPostFilterApprovalsResponse().approvals(Lists.newArrayList()));

        ApprovalsListDto response = listPendingApprovals
            .listApprovals(getInternalRequest(parametersHolder)).getData();

        assertTrue(response.getPresentationApprovalItems().isEmpty());
    }

    private Matcher<PresentationApprovalItem> getApprovalItemMatcher(Matcher<?> userIdMatcher,
        Matcher<?> userFullNameMatcher,
        Matcher<?> createdAtMatcher, Matcher<?> actionMatcher, Matcher<?> categoryMatcher,
        Matcher<?> approvalIdMatcher) {
        return allOf(
            hasProperty("creatorUserFullName", userFullNameMatcher),
            hasProperty("creatorUserId", userIdMatcher),
            hasProperty("createdAt", createdAtMatcher),
            hasProperty("action", actionMatcher),
            hasProperty("category", categoryMatcher),
            hasProperty("approvalId", approvalIdMatcher)
        );
    }
}