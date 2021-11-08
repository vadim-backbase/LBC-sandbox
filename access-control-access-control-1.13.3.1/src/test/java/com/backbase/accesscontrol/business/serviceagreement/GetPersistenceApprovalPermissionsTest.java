package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.PersistenceApprovalPermissionsPresentationApprovalPermissionMapper;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissionsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationApprovalPermissions;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetPersistenceApprovalPermissionsTest {

    @Mock
    private ApprovalService approvalService;

    @Spy
    private PersistenceApprovalPermissionsPresentationApprovalPermissionMapper mapper = Mappers
        .getMapper(PersistenceApprovalPermissionsPresentationApprovalPermissionMapper.class);

    @InjectMocks
    private GetPersistenceApprovalPermissions getPersistenceApprovalPermissions;

    private static final String USER_ID = "userID";
    private static final String USER_AGREEMENT_ID = "agreementId";
    private static final String FUNCTION_GROUP_ID = "fg1";
    private static final String DG_1 = "dg1";
    private static final String DG_2 = "dg2";
    private static final int RESULT_SIZE = 1;

    @Before
    public void setUp() throws Exception {
        PersistenceApprovalPermissionsGetResponseBody persistenceApprovalPermissionsGetResponseBody
            = new PersistenceApprovalPermissionsGetResponseBody()
            .withFunctionGroupId(FUNCTION_GROUP_ID)
            .withDataGroupIds(Arrays.asList(DG_1, DG_2));

        PersistenceApprovalPermissions persistenceApprovalPermissions = new PersistenceApprovalPermissions()
            .withApprovalId(USER_AGREEMENT_ID)
            .withItems(new ArrayList<>(Collections.singletonList(persistenceApprovalPermissionsGetResponseBody)));

        when(approvalService.getPersistenceApprovalPermissions(eq(USER_ID), eq(USER_AGREEMENT_ID)))
            .thenReturn(persistenceApprovalPermissions);
    }

    @Test
    public void shouldGetUserPermissions() {

        InternalRequest<PresentationApprovalPermissions> response = getPersistenceApprovalPermissions
            .getAssignedUsersPermissions(getVoidInternalRequest(), USER_AGREEMENT_ID, USER_ID);

        PresentationApprovalPermissions presentationApprovalPermissions = response.getData();

        Assert.assertNotNull(response);
        assertEquals(RESULT_SIZE, presentationApprovalPermissions.getItems().size());
        assertEquals(USER_AGREEMENT_ID, presentationApprovalPermissions.getApprovalId());

        PresentationFunctionDataGroup permissions = presentationApprovalPermissions.getItems().get(0);
        assertEquals(FUNCTION_GROUP_ID, permissions.getFunctionGroupId());
        assertEquals(DG_1, permissions.getDataGroupIds().get(0).getId());
        assertEquals(DG_2, permissions.getDataGroupIds().get(1).getId());
    }
}
