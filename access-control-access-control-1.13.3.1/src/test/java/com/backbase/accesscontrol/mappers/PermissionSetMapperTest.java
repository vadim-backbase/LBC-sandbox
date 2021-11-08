package com.backbase.accesscontrol.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.AssignablePermissionSet;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.enums.AssignablePermissionType;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationPermissionSetResponseItem;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSetMapperTest {

    @InjectMocks
    private com.backbase.accesscontrol.mappers.PermissionSetMapperImpl permissionSetMapper;
    @Mock
    private BusinessFunctionCache businessFunctionCache;

    @Test
    public void shouldConvertListOfApsToPresentationModel() {
        AssignablePermissionSet aps = new AssignablePermissionSet();
        aps.setName("Test");
        aps.setId(10L);
        aps.setDescription("Description");
        aps.setType(AssignablePermissionType.CUSTOM);
        aps.setPermissions(Sets.newHashSet("10"));

        BusinessFunction bf = new BusinessFunction();
        bf.setId("bf1");
        bf.setFunctionName("bf_name");
        bf.setResourceName("resource");
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("10");
        applicableFunctionPrivilege.setBusinessFunction(bf);
        applicableFunctionPrivilege.setPrivilegeName("read");

        when(businessFunctionCache.getApplicableFunctionPrivileges(eq(Sets.newHashSet("10"))))
            .thenReturn(Sets.newHashSet(applicableFunctionPrivilege));


        List<PresentationPermissionSetResponseItem> response = permissionSetMapper
            .sourceToDestination(Lists.newArrayList(aps));

        assertEquals(aps.getId().longValue(), response.get(0).getId().longValue());
        assertEquals(aps.getName(), response.get(0).getName());
        assertEquals(aps.getDescription(), response.get(0).getDescription());
        assertEquals(aps.getType().toString(), response.get(0).getType());
        assertEquals(1, response.get(0).getPermissions().size());
        assertEquals("bf_name", response.get(0).getPermissions().get(0).getFunctionName());
        assertEquals(aps.getPermissions().size(), response.get(0).getPermissions().get(0).getPrivileges().size());
    }
}