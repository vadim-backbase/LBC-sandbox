package com.backbase.legalentity.integration.external.mock.rest;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.integration.legalentity.external.outbound.rest.spec.v2.legalentities.LegalEntityItem;
import com.backbase.legalentity.integration.external.mock.service.LegalEntitiesService;
import com.backbase.legalentity.integration.external.mock.util.PaginationDto;
import com.backbase.legalentity.integration.external.mock.util.ParameterValidationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@WebAppConfiguration
public class MockLegalEntitiesControllerTest {

    @Mock
    private LegalEntitiesService legalEntitiesService;

    @Mock
    private ParameterValidationUtil validationUtil;
    private MockMvc mockMvc;
    @Spy
    private ObjectMapper objectMapper;
    @InjectMocks
    private MockLegalEntitiesServiceController legalEntitiesController;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(legalEntitiesController)
            .build();
    }

    @Test
    public void testGetLegalEntities() throws Exception {
        PaginationDto<LegalEntityItem> mockResponse = new PaginationDto<>(15L, asList(new LegalEntityItem()));
        when(legalEntitiesService
            .getLegalEntities(eq("externalId"), eq("SA"), eq(0), eq(""), eq(1)))
            .thenReturn(mockResponse);
        MockHttpServletResponse mockHttpServletResponse = mockMvc.perform(
            get("/service-api/v2/legal-entities")
                .param("field", "externalId")
                .param("term", "SA")
                .param("from", "0")
                .param("cursor", "")
                .param("size", "1"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        String resultTotalCount = mockHttpServletResponse.getHeader("X-Total-Count");
        String contentAsString = mockHttpServletResponse
            .getContentAsString();

        List<LegalEntityItem> actualResponse = objectMapper.readValue(
            contentAsString,
            new TypeReference<List<LegalEntityItem>>() {
            });

        verify(legalEntitiesService)
            .getLegalEntities(eq("externalId"), eq("SA"), eq(0), eq(""), eq(1));

        verifyNoMoreInteractions(legalEntitiesService);
        assertEquals(1, actualResponse.size());
        assertEquals("15", resultTotalCount);
    }

}