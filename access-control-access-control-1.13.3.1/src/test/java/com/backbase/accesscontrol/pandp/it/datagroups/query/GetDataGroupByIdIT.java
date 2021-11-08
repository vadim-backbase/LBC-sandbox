package com.backbase.accesscontrol.pandp.it.datagroups.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.DataGroupQueryController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.google.common.collect.Sets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Tests for {@link DataGroupQueryController#getDataGroupById(String, Boolean, HttpServletRequest,
 * HttpServletResponse)}
 */
public class GetDataGroupByIdIT extends TestConfig {

    private static String DATA_GROUP_URL = "/service-api/v2/accesscontrol/accessgroups/data-groups/";
    private DataGroup dataGroup;

    @Before
    public void setUp() throws Exception {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntity = legalEntityJpaRepository.save(legalEntity);
        ServiceAgreement serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("sa-name", "sa-exid", "sa-desc", legalEntity, null, null);
        serviceAgreement.setMaster(true);
        serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

        dataGroup = DataGroupUtil.createDataGroup("dg-name", "ARRANGEMENTS", "desc", serviceAgreement);
        dataGroup.setDataItemIds(Sets.newHashSet("item1", "item2"));
        dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    public void shouldSuccessfullyGetDataGroupById() throws Exception {

        String contentAsString = mockMvc.perform(get(DATA_GROUP_URL + dataGroup.getId())
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        DataGroupItemBase response = objectMapper
            .readValue(contentAsString, DataGroupItemBase.class);

        assertNotNull(response);
        assertEquals(dataGroup.getId(), response.getId());
        assertEquals(dataGroup.getDescription(), response.getDescription());
        assertEquals(dataGroup.getName(), response.getName());
        assertEquals(dataGroup.getServiceAgreement().getId(), response.getServiceAgreementId());
        assertEquals(dataGroup.getDataItemType(), response.getType());
        assertEquals(2, dataGroup.getDataItemIds().size());
    }

    @Test
    public void shouldSuccessfullyGetDataGroupByIdWithoutItems() throws Exception {

        String contentAsString = mockMvc.perform(get(DATA_GROUP_URL + dataGroup.getId() +
            "?includeItems=false")
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        DataGroupItemBase response = objectMapper
            .readValue(contentAsString, DataGroupItemBase.class);

        assertNotNull(response);
        assertEquals(dataGroup.getId(), response.getId());
        assertEquals(dataGroup.getDescription(), response.getDescription());
        assertEquals(dataGroup.getName(), response.getName());
        assertEquals(dataGroup.getServiceAgreement().getId(), response.getServiceAgreementId());
        assertEquals(dataGroup.getDataItemType(), response.getType());
        assertEquals(0, response.getItems().size());
    }
}
