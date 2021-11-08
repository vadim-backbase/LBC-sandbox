package com.backbase.accesscontrol.business.flows.permissonset;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.flows.permissionset.GetPermissionSetsFlow;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.mappers.PermissionSetMapper;
import com.backbase.accesscontrol.service.PermissionSetService;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetPermissionSetsFlowTest {

    @Mock
    private PermissionSetService permissionSetService;
    @Mock
    private PermissionSetMapper permissionSetMapper;

    @InjectMocks
    private GetPermissionSetsFlow getPermissionSetsFlow;

    @Test
    public void shouldCallPersistenceService() {
        String name = "name";
        when(permissionSetService.getPermissionSetFilteredByName(anyString()))
            .thenReturn(Lists.newArrayList(new AssignablePermissionSet()));
        getPermissionSetsFlow.start(name);
        verify(permissionSetService).getPermissionSetFilteredByName(eq(name));
        verify(permissionSetMapper).sourceToDestination(eq(Lists.newArrayList(new AssignablePermissionSet())));
    }
}
