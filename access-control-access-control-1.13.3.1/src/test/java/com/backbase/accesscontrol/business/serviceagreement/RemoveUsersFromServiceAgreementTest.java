package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.RemoveUsersFromServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.dto.UsersDto;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.dbs.user.api.client.v2.model.GetUsersList;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationUsersForServiceAgreementRequestBody;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoveUsersFromServiceAgreementTest {

    @Mock
    private RemoveUsersFromServiceAgreementHandler removeUsersFromServiceAgreementHandler;
    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private RemoveUsersFromServiceAgreement removeUserInServiceAgreement;

    @Test
    public void shouldPassIfRemoveUserInServiceAgreementIsInvoked() {
        String userId = "U2";
        String legalEntityId = "LE-01";
        String serviceAgreementId = "SA-01";

        com.backbase.dbs.user.api.client.v2.model.GetUsersList users = new GetUsersList();
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(userId);
        user1.setLegalEntityId(legalEntityId);
        users.setUsers(Lists.newArrayList(user1));

        UsersDto usersDto = new UsersDto()
            .withLegalEntityId(legalEntityId)
            .withUsers(Collections.singletonList(userId));

        List<UsersDto> usersRemoveRequestBody = Collections.singletonList(usersDto);

        mockGetUsersByIds(Collections.singletonList(userId), users);

        PresentationUsersForServiceAgreementRequestBody requestBody = new PresentationUsersForServiceAgreementRequestBody()
            .withUsers(Collections.singletonList(userId));
        InternalRequest<PresentationUsersForServiceAgreementRequestBody> request = getInternalRequest(
            requestBody);
        removeUserInServiceAgreement.removeUserInServiceAgreement(request, serviceAgreementId);

        verify(removeUsersFromServiceAgreementHandler, times(1))
            .handleRequest(any(), eq(usersRemoveRequestBody));
    }

    private void mockGetUsersByIds(List<String> usersToAdd, GetUsersList data) {
        when(userManagementService
            .getUsersByIds(eq(usersToAdd), eq(null), eq(null), eq(null), eq(null)))
            .thenReturn(data);
    }
}
