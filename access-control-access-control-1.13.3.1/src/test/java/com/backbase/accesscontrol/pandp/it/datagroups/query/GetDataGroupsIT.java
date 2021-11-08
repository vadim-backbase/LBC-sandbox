package com.backbase.accesscontrol.pandp.it.datagroups.query;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.UserQueryController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;

/**
 * Test for {@link UserQueryController#getPersistenceApprovalPermissions}
 */
public class GetDataGroupsIT extends TestConfig {

    private static String DATA_GROUP_URL = "/service-api/v2/accesscontrol/accessgroups/data-groups/";
    private DataGroup dataGroup;
    private ServiceAgreement serviceAgreement;

    @Before
    public void setUp() throws Exception {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "LegalEntity", "ex-1", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);
        serviceAgreement = ServiceAgreementUtil
            .createServiceAgreement("name", "exid", "desc", legalEntity, null, null);
        serviceAgreementJpaRepository.save(serviceAgreement);
        dataGroup = DataGroupUtil
            .createDataGroup("Data Group", "ARRANGEMENTS", "description", serviceAgreement);
        dataGroup.setDataItemIds(Collections.singleton("item1"));
        dataGroupJpaRepository.save(dataGroup);
    }

    @Test
    public void shouldSuccessfullyGetDataGroups() throws Exception {

        String contentAsString = mockMvc.perform(get(DATA_GROUP_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN).param("serviceAgreementId", serviceAgreement.getId())
            .param("type", dataGroup.getDataItemType()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        DataGroupItemBase[] returnedListOfFunctions = objectMapper
            .readValue(contentAsString,
                DataGroupItemBase[].class);
        assertNotNull(returnedListOfFunctions);
        assertEquals(1, returnedListOfFunctions.length);
        assertEquals(dataGroup.getName(), returnedListOfFunctions[0].getName());
        assertEquals(dataGroup.getServiceAgreement().getId(), returnedListOfFunctions[0].getServiceAgreementId());
        assertEquals(dataGroup.getDescription(), returnedListOfFunctions[0].getDescription());
        assertEquals(singletonList("item1"), returnedListOfFunctions[0].getItems());
    }

    @Test
    public void shouldSuccessfullyGetDataGroupsWithoutItems() throws Exception {

        String contentAsString = mockMvc.perform(get(DATA_GROUP_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN).param("serviceAgreementId", serviceAgreement.getId())
            .param("type", dataGroup.getDataItemType())
            .param("includeItems", "false"))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        DataGroupItemBase[] returnedListOfFunctions = objectMapper
            .readValue(contentAsString,
                DataGroupItemBase[].class);
        assertNotNull(returnedListOfFunctions);
        assertEquals(1, returnedListOfFunctions.length);
        assertEquals(dataGroup.getName(), returnedListOfFunctions[0].getName());
        assertEquals(dataGroup.getServiceAgreement().getId(), returnedListOfFunctions[0].getServiceAgreementId());
        assertEquals(dataGroup.getDescription(), returnedListOfFunctions[0].getDescription());
        assertEquals(new ArrayList<>(), returnedListOfFunctions[0].getItems());
    }
}
