package com.backbase.account.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.mock.outbound.account.link.model.ArrangementItemDto;
import com.backbase.mock.outbound.balance.model.BalanceItemDto;
import com.backbase.mock.outbound.details.model.ArrangementDetailsDto;
import com.backbase.mock.outbound.recipient.model.RecipientArrangementIdsDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles("it")
@SpringBootTest(classes = {MockApplication.class})
public class MockApiIT {

    private static final String ARRANGEMENT_ID = "A01";
    private static final String ARRANGEMENT_IDS = "A01,A02,A03,A04";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private TypeReference<List<BalanceItemDto>> listTypeReference;
    private TypeReference<ArrangementDetailsDto> arrangementDetailsTypeReference;
    private TypeReference<RecipientArrangementIdsDto> recipientArrangementIdsTypeReference;
    private TypeReference<List<ArrangementItemDto>> byLegalEntityIdTypeReference;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        listTypeReference = new TypeReference<List<BalanceItemDto>>() {};
        arrangementDetailsTypeReference = new TypeReference<ArrangementDetailsDto>() {};
        recipientArrangementIdsTypeReference = new TypeReference<RecipientArrangementIdsDto>() {};
        byLegalEntityIdTypeReference = new TypeReference<List<ArrangementItemDto>>() {};
    }

    @Test
    public void shouldLoadContext() {
        assertNotNull(applicationContext);
    }

    @Test
    public void shouldRetrieveBalanceByArrangementId() throws Exception {
        String contentAsString = this.mockMvc.perform(get("/service-api/v2/balances?arrangementIds=" + ARRANGEMENT_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<BalanceItemDto> balances = objectMapper.readValue(contentAsString, listTypeReference);

        assertTrue(balances.size() == 1);
        assertTrue(balances.get(0).getArrangementId().equals(ARRANGEMENT_ID));
        assertTrue(balances.get(0).getAvailableBalance().intValue() > 0);
        assertTrue(balances.get(0).getBookedBalance().intValue() > 0);
        assertTrue(balances.get(0).getCreditLimit().intValue() > 0);
        assertTrue(balances.get(0).getCurrentInvestmentValue().intValue() > 0);
        assertTrue(balances.get(0).getOutstandingPrincipalAmount().intValue() > 0);
    }

    @Test
    public void shouldRetrieveBalanceByArrangementIds() throws Exception {
        String[] arrangementIds = ARRANGEMENT_IDS.split(",");
        String contentAsString = this.mockMvc.perform(get("/service-api/v2/balances?arrangementIds=" + ARRANGEMENT_IDS))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<BalanceItemDto> balances = objectMapper.readValue(contentAsString, listTypeReference);

        assertTrue(balances.size() == arrangementIds.length);
        assertTrue(balances.get(0).getArrangementId().equals(arrangementIds[0]));
        assertTrue(balances.get(1).getArrangementId().equals(arrangementIds[1]));
        assertTrue(balances.get(2).getArrangementId().equals(arrangementIds[2]));
        assertTrue(balances.get(3).getArrangementId().equals(arrangementIds[3]));
    }

    @Test
    public void shouldRetrieveArrangementDetailsWithNoNullValues() throws Exception {
        String contentAsString = this.mockMvc.perform(get("/service-api/v2/arrangement-details/?arrangementId=" + ARRANGEMENT_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Validates result an instance of ArrangementDetailsDto
        objectMapper.readValue(contentAsString, arrangementDetailsTypeReference);
        //Validate each value is not null
        objectMapper.readValue(contentAsString, Map.class).values().stream().forEach(Assert::assertNotNull);
    }

    @Test
    public void shouldRetrieveRecipientArrangementIds() throws Exception {
        String contentAsString = mockMvc
            .perform(get("/service-api/v2/recipientArrangementIds?arrangementId=" + ARRANGEMENT_ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        RecipientArrangementIdsDto recipientArrangementIds = objectMapper
            .readValue(contentAsString, recipientArrangementIdsTypeReference);

        assertEquals(4, recipientArrangementIds.getArrangementIds().size());
        assertEquals("A03", recipientArrangementIds.getArrangementIds().get(0));
        assertEquals("A05", recipientArrangementIds.getArrangementIds().get(1));
        assertEquals("A07", recipientArrangementIds.getArrangementIds().get(2));
        assertEquals("A09", recipientArrangementIds.getArrangementIds().get(3));
    }

    @Test
    public void shouldRetrieveRecipientArrangementIdsForEvenArrangementId() throws Exception {
        String contentAsString = mockMvc
            .perform(get("/service-api/v2/recipientArrangementIds?arrangementId=A04"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        RecipientArrangementIdsDto recipientArrangementIds = objectMapper
            .readValue(contentAsString, recipientArrangementIdsTypeReference);

        assertEquals(3, recipientArrangementIds.getArrangementIds().size());
        assertEquals("A02", recipientArrangementIds.getArrangementIds().get(0));
        assertEquals("A06", recipientArrangementIds.getArrangementIds().get(1));
        assertEquals("A08", recipientArrangementIds.getArrangementIds().get(2));
    }

    @Test
    public void shouldGetArrangementsByLegalEntityIds() throws Exception {
        String contentAsString = mockMvc
            .perform(get("/service-api/v2/arrangements")
                .param("legalEntityId", "BANK0001"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<ArrangementItemDto> arrangementItems = objectMapper
            .readValue(contentAsString, byLegalEntityIdTypeReference);

        assertEquals(3, arrangementItems.size());
        assertEquals("LINKING_ARRANGEMENT_01", arrangementItems.get(0).getId());
        assertNull(arrangementItems.get(0).getParentId());
        assertEquals("LINKING_ARRANGEMENT_02", arrangementItems.get(1).getId());
        assertNull(arrangementItems.get(1).getParentId());
        assertEquals("LINKING_ARRANGEMENT_03", arrangementItems.get(2).getId());
        assertNull(arrangementItems.get(2).getParentId());
    }

}