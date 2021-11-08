package com.backbase.account.mock;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.mock.outbound.account.link.model.ArrangementItemDto;
import com.backbase.mock.outbound.balance.model.BalanceItemDto;
import com.backbase.mock.outbound.details.model.ArrangementDetailsDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"it", "additions"})
@SpringBootTest(classes = {MockApplication.class})
public class MockApiAdditionsEnabledIT {

    private static final String ARRANGEMENT_ID = "A01";
    private static final String EXTERNAL_LEGAL_ENTITY_ID = "BANK0001";
    private static final String BASE_URL = "/service-api/v2/";
    private static final String ARRANGEMENT_DETAILS_URL = BASE_URL + "arrangement-details";
    private static final String BALANCES_URI = BASE_URL + "balances";
    private static final String ARRANGEMENTS_URL = BASE_URL + "arrangements";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private TypeReference<List<BalanceItemDto>> balanceListTypeReference;
    private TypeReference<List<ArrangementItemDto>> arrangementItemsByLegalEntityTypeReference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .build();
        balanceListTypeReference = new TypeReference<List<BalanceItemDto>>() {
        };
        arrangementItemsByLegalEntityTypeReference = new TypeReference<List<ArrangementItemDto>>() {
        };
    }

    @Test
    public void shouldRetrieveBalanceByArrangementIdWithAdditions() throws Exception {
        String contentAsString = this.mockMvc.perform(get(BALANCES_URI).param("arrangementIds", ARRANGEMENT_ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<BalanceItemDto> balances = objectMapper.readValue(contentAsString, balanceListTypeReference);

        assertEquals(1, balances.size());
        assertEquals(balances.get(0).getArrangementId(), ARRANGEMENT_ID);
        assertTrue(balances.get(0).getAvailableBalance().intValue() > 0);
        assertTrue(balances.get(0).getBookedBalance().intValue() > 0);
        assertTrue(balances.get(0).getCurrentInvestmentValue().intValue() > 0);
        assertTrue(balances.get(0).getCreditLimit().intValue() > 0);
        assertTrue(balances.get(0).getOutstandingPrincipalAmount().intValue() > 0);
        assertThat(balances.get(0).getAdditions().size(), is(2));
        assertThat(balances.get(0).getAdditions().get("checkingBalance"), is("100"));
        assertThat(balances.get(0).getAdditions().get("loanBalance"), is("200"));
    }


    @Test
    public void shouldRetrieveArrangementDetailsWithAdditions() throws Exception {
        String contentAsString = this.mockMvc
            .perform(get(ARRANGEMENT_DETAILS_URL).param("arrangementId", ARRANGEMENT_ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        ArrangementDetailsDto arrangementDetails = objectMapper
            .readValue(contentAsString, ArrangementDetailsDto.class);

        assertThat(arrangementDetails.getAdditions().size(), is(2));
        assertThat(arrangementDetails.getAdditions().get("propName1"), is("value1"));
        assertThat(arrangementDetails.getAdditions().get("propName2"), is("value2"));
    }

    @Test
    public void shouldRetrieveArrangementItemsByLegalEntityWithAdditions() throws Exception {
        String contentAsString = this.mockMvc
            .perform(get(ARRANGEMENTS_URL).param("legalEntityId", EXTERNAL_LEGAL_ENTITY_ID))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<ArrangementItemDto> arrangementItems = objectMapper
            .readValue(contentAsString, arrangementItemsByLegalEntityTypeReference);
        assertThat(arrangementItems.get(0).getId(), is("LINKING_ARRANGEMENT_01"));
        assertThat(arrangementItems.get(0).getAdditions().size(), is(2));
        assertThat(arrangementItems.get(0).getAdditions().get("propName1"), is("value1"));
        assertThat(arrangementItems.get(0).getAdditions().get("propName2"), is("value2"));
    }

}
