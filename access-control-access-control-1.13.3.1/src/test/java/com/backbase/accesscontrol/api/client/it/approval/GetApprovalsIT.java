package com.backbase.accesscontrol.api.client.it.approval;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalAction;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalCategory;
import com.backbase.accesscontrol.client.rest.spec.model.PresentationApprovalItem;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.dbs.approval.api.client.v2.model.ApprovalDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationPostFilterApprovalsResponse;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class GetApprovalsIT extends TestDbWireMock {

    private static final String USER = "USER";
    private static final String POST_FILTER_APPROVAL_URL = "/service-api/v2/approvals/filter";
    private static final String GET_USERS_URL = "/service-api/v2/users/bulk";

    @Test
    public void shouldListAllPendingApprovals() throws Exception {
        String from = "0";
        String size = "10";
        String cursor = "";
        String url = "/accessgroups/approvals";

        String approvalId1 = "AP-01";
        String userId1 = "U-1";
        String serviceAgreementId1 = "SA-01";
        String function = "Assign Permissions";
        String action = "EDIT";
        String approvalId2 = "AP-02";
        String userId2 = "U-2";
        String serviceAgreementId2 = "SA-02";
        String userFullName1 = "user 1";
        String userFullName2 = "user 2";
        Date createdAt = new Date();

        PresentationPostFilterApprovalsResponse approvalResponse = new PresentationPostFilterApprovalsResponse()
            .approvals(Lists.newArrayList(
                new ApprovalDto()
                    .id(approvalId1)
                    .createdAt(createdAt)
                    .userId(userId1)
                    .serviceAgreementId(serviceAgreementId1)
                    .function(function)
                    .action(action),
                new ApprovalDto()
                    .id(approvalId2)
                    .createdAt(createdAt)
                    .userId(userId2)
                    .serviceAgreementId(serviceAgreementId2)
                    .function(function)
                    .action(action)
            ));
        GetUser user1 = new GetUser();
        user1.setId(userId1);
        user1.setFullName(userFullName1);
        GetUser user2 = new GetUser();
        user2.setId(userId2);
        user2.setFullName(userFullName2);
        com.backbase.dbs.user.api.client.v2.model.GetUsersList usersResponseMock =
            new com.backbase.dbs.user.api.client.v2.model.GetUsersList();
        usersResponseMock.setUsers(Lists.newArrayList(user1, user2));

        addStubPost(new UrlBuilder(POST_FILTER_APPROVAL_URL).build(), approvalResponse,
            200);

        addStubGet(new UrlBuilder(GET_USERS_URL).addQueryParameter("id",  asList(userId1, userId2)).build(),
            usersResponseMock, 200);

        ResponseEntity<String> response = executeClientRequestEntity(new UrlBuilder(url)
                .addQueryParameter("from", from)
                .addQueryParameter("size", size)
                .addQueryParameter("cursor", cursor).build(),
            HttpMethod.GET, "", USER);

        List<PresentationApprovalItem> actualResponse = readValue(
            response.getBody(),
            new TypeReference<>() {
            });

        assertThat(actualResponse, hasItems(
            getApprovalItemMatcher(is(userId1), is(userFullName1), is(createdAt),
                is(PresentationApprovalAction.fromValue(action)),
                is(PresentationApprovalCategory.fromValue(function)), is(approvalId1)),
            getApprovalItemMatcher(is(userId2), is(userFullName2), is(createdAt),
                is(PresentationApprovalAction.fromValue(action)),
                is(PresentationApprovalCategory.fromValue(function)), is(approvalId2))
        ));
        assertFalse(response.getHeaders().containsValue("X-Cursor"));
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
