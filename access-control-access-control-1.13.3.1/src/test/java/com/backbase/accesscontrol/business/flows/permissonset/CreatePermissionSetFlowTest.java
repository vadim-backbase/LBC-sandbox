package com.backbase.accesscontrol.business.flows.permissonset;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.flows.permissionset.CreatePermissionSetFlow;
import com.backbase.accesscontrol.business.service.PermissionSetPersistenceService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.response.PresentationInternalIdResponse;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreatePermissionSetFlowTest {

    @Mock
    private PermissionSetPersistenceService persistenceService;

    @InjectMocks
    private CreatePermissionSetFlow createPermissionSetFlow;

    @Test
    public void shouldCallPersistenceService() {
        PresentationInternalIdResponse expectedInternalIdResponse = new PresentationInternalIdResponse()
            .withId(new BigDecimal(1234));

        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");

        when(persistenceService.createPermissionSet(eq(permissionSet))).thenReturn(expectedInternalIdResponse);

        PresentationInternalIdResponse presentationInternalIdResponse = createPermissionSetFlow.start(permissionSet);

        verify(persistenceService, times(1)).createPermissionSet(eq(permissionSet));
        assertEquals(expectedInternalIdResponse, presentationInternalIdResponse);
    }
}
