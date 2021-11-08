package com.backbase.legalentity.integration.external.mock.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.buildingblocks.backend.security.accesscontrol.accesscontrol.AccessControlValidator;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebAppConfiguration
public class MockPermissionCheckClientControllerTest {

    @Mock
    private AccessControlValidator accessControlValidator;

    private MockMvc mockMvc;

    @InjectMocks
    private MockPermissionCheckClientController legalEntitiesController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(legalEntitiesController)
            .build();
    }

    @Test
    public void checkAccessToDataItemShouldReturnOk() throws Exception {
        when(accessControlValidator.userHasNoAccessToDataItem("Payments", "edit", "CUSTOMERS", "item1"))
            .thenReturn(false);
        mockMvc.perform(
            get("/client-api/v2/permissions/data-item")
                .param("businessFunction", "Payments")
                .param("privilege", "edit")
                .param("dataType", "CUSTOMERS")
                .param("dataItemId", "item1"))
            .andExpect(status().isOk());
    }

    @Test
    public void checkAccessToDataItemShouldReturnForbidden() throws Exception {
        when(accessControlValidator.userHasNoAccessToDataItem("Payments", "edit", "CUSTOMERS", "item1"))
            .thenReturn(true);
        mockMvc.perform(
            get("/client-api/v2/permissions/data-item")
                .param("businessFunction", "Payments")
                .param("privilege", "edit")
                .param("dataType", "CUSTOMERS")
                .param("dataItemId", "item1"))
            .andExpect(status().isForbidden())
            .andDo(mvcResult -> {
                ForbiddenException exception = (ForbiddenException) mvcResult.getResolvedException();
                assertEquals("User has no access to data item", exception.getErrors().get(0).getMessage());
            });
    }

}