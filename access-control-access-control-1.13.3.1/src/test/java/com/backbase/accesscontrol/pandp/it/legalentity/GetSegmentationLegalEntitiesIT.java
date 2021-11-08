package com.backbase.accesscontrol.pandp.it.legalentity;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_064;
import static com.backbase.accesscontrol.util.helpers.FunctionGroupUtil.getFunctionGroup;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivilege;
import static com.backbase.accesscontrol.util.helpers.GroupedFunctionPrivilegeUtil.getGroupedFunctionPrivileges;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backbase.accesscontrol.api.service.LegalEntityQueryController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.GroupedFunctionPrivilege;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.pandp.it.TestConfig;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.legalentities.SegmentationGetResponseBodyQuery;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Test for {@link LegalEntityQueryController#getSegmentation}
 */
public class GetSegmentationLegalEntitiesIT extends TestConfig {

    private static final String GET_SEGMENTATION_LEGAL_ENTITIES_URL = "/service-api/v2/accesscontrol/legalentities/segmentation";
    private LegalEntity bank;
    private ServiceAgreement serviceAgreement;
    private LegalEntity customerDataGroupLegalEntityBank;
    private final String USER_ID = getUuid();

    @Before
    public void setUp() {
        repositoryCleaner.clean();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.execute(transactionStatus -> {
            customerDataGroupLegalEntityBank = createLegalEntity(null, "customerDataGroupLegalEntityBank",
                "bankExternalId", bank,
                LegalEntityType.CUSTOMER);

            bank = createLegalEntity(null, "bank", "bank", null, LegalEntityType.BANK);
            HashMap<String, String> additions = new HashMap<>();
            additions.put("leExternalId", "asdads");
            additions.put("second", "asdads");
            bank.setAdditions(additions);
            customerDataGroupLegalEntityBank.setAdditions(additions);
            bank = legalEntityJpaRepository.save(bank);
            customerDataGroupLegalEntityBank = legalEntityJpaRepository.save(customerDataGroupLegalEntityBank);

            // create SA
            serviceAgreement = createServiceAgreement(
                "BB between self", "id.external", "desc", customerDataGroupLegalEntityBank,
                customerDataGroupLegalEntityBank.getId(),
                customerDataGroupLegalEntityBank.getId());
            serviceAgreement.setMaster(true);
            serviceAgreement = serviceAgreementJpaRepository.save(serviceAgreement);

            DataGroup dataGroup = DataGroupUtil
                .createDataGroup("name2", "CUSTOMERS", "desc", serviceAgreement);
            dataGroup.setDataItemIds(Collections.singleton(customerDataGroupLegalEntityBank.getId()));
            dataGroup = dataGroupJpaRepository.save(dataGroup);
            DataGroup dataGroupArrangements = DataGroupUtil
                .createDataGroup("name21", "ARRANGEMENTS", "desc1", serviceAgreement);
            dataGroupArrangements.setDataItemIds(Collections.singleton(customerDataGroupLegalEntityBank.getId()));
            dataGroupArrangements = dataGroupJpaRepository.save(dataGroupArrangements);

            GroupedFunctionPrivilege viewLe = getGroupedFunctionPrivilege(null, apfBf1011View, null);

            FunctionGroup testFg = functionGroupJpaRepository.saveAndFlush(
                getFunctionGroup(null, "function-group-name", "function-group-description",
                    getGroupedFunctionPrivileges(
                        viewLe
                    ),
                    FunctionGroupType.DEFAULT, serviceAgreement)
            );

            UserContext userContext = userContextJpaRepository
                .saveAndFlush(new UserContext(USER_ID, serviceAgreement.getId()));
            UserAssignedFunctionGroup userAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository
                .saveAndFlush(new UserAssignedFunctionGroup(testFg, userContext));
            userAssignedCombinationRepository
                .saveAndFlush(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));
            userAssignedCombinationRepository
                .saveAndFlush(
                    new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroupArrangements.getId()), userAssignedFunctionGroup));
            return true;
        });
    }

    @Test
    public void shouldGetLegalEntitiesWithDataGroupOfTypeCustomersFilterByExternalIdAndLegalEntityIdProvided()
        throws Exception {
        String key = "leExternalId";

        String contentAsString = mockMvc.perform(get(GET_SEGMENTATION_LEGAL_ENTITIES_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .param("query", "bankExternalId")
            .param("businessFunction", bf1011.getFunctionName())
            .param("legalEntityId", customerDataGroupLegalEntityBank.getId())
            .param("userId", USER_ID)
            .param("privilege", "view")
            .param("from", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        SegmentationGetResponseBodyQuery[] data = objectMapper.readValue(contentAsString,
            SegmentationGetResponseBodyQuery[].class);

        assertEquals(1, data.length);
        SegmentationGetResponseBodyQuery legalEntityReturned = data[0];
        assertThat(legalEntityReturned.getAdditions().size(), is(2));
        assertTrue(legalEntityReturned.getAdditions().containsKey(key));
        assertEquals("bankExternalId", legalEntityReturned.getExternalId());
    }

    @Test
    public void shouldGetLegalEntitiesWithDataGroupOfTypeCustomersFilterByNameServiceAgreementIdProvided()
        throws Exception {
        String key = "leExternalId";

        String contentAsString = mockMvc.perform(get(GET_SEGMENTATION_LEGAL_ENTITIES_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .param("query", "customer")
            .param("businessFunction", bf1011.getFunctionName())
            .param("serviceAgreementId", serviceAgreement.getId())
            .param("userId", USER_ID)
            .param("privilege", "view")
            .param("from", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        SegmentationGetResponseBodyQuery[] data = objectMapper.readValue(contentAsString,
            SegmentationGetResponseBodyQuery[].class);

        assertEquals(1, data.length);
        SegmentationGetResponseBodyQuery legalEntityReturned = data[0];
        assertThat(legalEntityReturned.getAdditions().size(), is(2));
        assertTrue(legalEntityReturned.getAdditions().containsKey(key));
        assertEquals("customerDataGroupLegalEntityBank", legalEntityReturned.getName());
    }

    @Test
    public void shouldThrowErrorWhenGetLegalEntitiesWithDataGroupOfTypeCustomersFilterByNameNoLegalEntityIdOrServiceAgreementIdProvided()
        throws Exception {

        mockMvc.perform(get(GET_SEGMENTATION_LEGAL_ENTITIES_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .param("query", "customer")
            .param("businessFunction", bf1011.getFunctionName())
            .param("userId", USER_ID)
            .param("privilege", "view")
            .param("from", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(mvcResult -> {
                BadRequestException exception = (BadRequestException) mvcResult.getResolvedException();
                assertEquals(ERR_ACQ_064.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(ERR_ACQ_064.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }

    @Test
    public void shouldThrowErrorWhenGetLegalEntitiesWithDataGroupOfTypeCustomersFilterByNameNotExistentLegalEntityIdProvided()
        throws Exception {

        mockMvc.perform(get(GET_SEGMENTATION_LEGAL_ENTITIES_URL)
            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_TOKEN)
            .param("query", "customer")
            .param("businessFunction", bf1011.getFunctionName())
            .param("userId", USER_ID)
            .param("legalEntityId", serviceAgreement.getId())
            .param("privilege", "view")
            .param("from", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andDo(mvcResult -> {
                NotFoundException exception = (NotFoundException) mvcResult.getResolvedException();
                assertEquals(ERR_ACQ_006.getErrorCode(), exception.getErrors().get(0).getKey());
                assertEquals(ERR_ACQ_006.getErrorMessage(), exception.getErrors().get(0).getMessage());
            });
    }


}
