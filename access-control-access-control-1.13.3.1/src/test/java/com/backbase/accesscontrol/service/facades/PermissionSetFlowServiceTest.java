package com.backbase.accesscontrol.service.facades;

import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.accesscontrol.business.flows.permissionset.CreatePermissionSetFlow;
import com.backbase.accesscontrol.business.flows.permissionset.DeletePermissionSetFlow;
import com.backbase.accesscontrol.business.flows.permissionset.GetPermissionSetsFlow;
import com.backbase.accesscontrol.business.flows.permissionset.UpdatePermissionSetFlow;
import com.backbase.accesscontrol.dto.DeletePermissionSetParameters;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSet;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetItemPut;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSetFlowServiceTest {

    @Mock
    private CreatePermissionSetFlow createPermissionSetFlow;

    @Mock
    private DeletePermissionSetFlow deletePermissionSetFlow;
    @Mock
    private GetPermissionSetsFlow getPermissionSetsFlow;
    @Mock
    private UpdatePermissionSetFlow updatePermissionSetFlow;
    @InjectMocks
    private PermissionSetFlowService service;

    @Test
    public void shouldCreatePermissionSet() {
        PresentationPermissionSet permissionSet = new PresentationPermissionSet().withName("apsName")
            .withDescription("apsDescription");
        service.createPermissionSet(permissionSet);
        verify(createPermissionSetFlow, times(1)).start(eq(permissionSet));
    }

    @Test
    public void shouldCallDeletePermissionSetFlow() {

        service.deletePermissionSet("id", "1");
        verify(deletePermissionSetFlow).start(eq(new DeletePermissionSetParameters("id", "1")));
    }

    @Test
    public void shouldCallUpdatePermissionSetFlow() {
        PresentationPermissionSetItemPut itemPut = new PresentationPermissionSetItemPut()
            .withExternalServiceAgreementId("ex-sa-id")
            .withAdminUserAps(new PresentationUserApsIdentifiers()
                .withIdIdentifiers(asSet(new BigDecimal(1L))))
            .withRegularUserAps(new PresentationUserApsIdentifiers()
                .withNameIdentifiers(asSet("APS name")));

        service.updatePermissionSet(itemPut);
        verify(updatePermissionSetFlow).start(eq(itemPut));
    }

    @Test
    public void shouldCallGetPermissionSetsFlow() {
        String name = "name";
        service.getPermissionSetFilteredByName(name);
        verify(getPermissionSetsFlow).start(eq(name));
    }
}