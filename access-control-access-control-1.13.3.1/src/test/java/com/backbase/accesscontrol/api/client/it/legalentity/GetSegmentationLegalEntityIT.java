package com.backbase.accesscontrol.api.client.it.legalentity;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITY_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_064;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.MANAGE_LEGAL_ENTITIES_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.LegalEntityController;
import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link LegalEntityController#getSegmentation(String, String, String, Integer, String, Integer,
 * HttpServletRequest, HttpServletResponse)}
 */
public class GetSegmentationLegalEntityIT extends TestDbWireMock {

    private static final String SEGMENTATION_SEARCH_LEGAL_ENTITY_URL = "/legalentities/segmentation";

    private ApplicableFunctionPrivilege apfBf1020View;

    @Before
    public void setUp() {
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup("name2", "CUSTOMERS", "desc", rootMsa);
        dataGroup.setDataItemIds(Collections.singleton(rootLegalEntity.getId()));
        dataGroup = dataGroupJpaRepository.save(dataGroup);
        DataGroup dataGroupArrangements = DataGroupUtil
            .createDataGroup("name21", "ARRANGEMENTS", "desc1", rootMsa);
        dataGroupArrangements.setDataItemIds(Collections.singleton(rootLegalEntity.getId()));
        dataGroupArrangements = dataGroupJpaRepository.save(dataGroupArrangements);

        apfBf1020View = businessFunctionCache
            .getByFunctionIdAndPrivilege("1020", "view");

        FunctionGroup testFg = createFunctionGroup("function-group-name", "function-group-description",
            rootMsa,
            Collections.singletonList(apfBf1020View.getId()), FunctionGroupType.SYSTEM);

        UserContext userContext = userContextJpaRepository
            .saveAndFlush(new UserContext(contextUserId, rootMsa.getId()));
        UserAssignedFunctionGroup userAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository
            .saveAndFlush(new UserAssignedFunctionGroup(testFg, userContext));
        userAssignedCombinationRepository
            .saveAndFlush(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup.getId()), userAssignedFunctionGroup));
        userAssignedCombinationRepository
            .saveAndFlush(
                new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroupArrangements.getId()), userAssignedFunctionGroup));
    }

    @Test
    public void shouldGetSegmentationLegalEntities()
        throws IOException, JSONException {

        String response = executeClientRequest(
            new UrlBuilder(SEGMENTATION_SEARCH_LEGAL_ENTITY_URL)
                .addQueryParameter("query", rootLegalEntity.getExternalId())
                .addQueryParameter("businessFunction", apfBf1020View.getBusinessFunctionName())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("privilege", "view")
                .addQueryParameter("from", "0")
                .addQueryParameter("cursor", "")
                .addQueryParameter("size", "10").build(),
            HttpMethod.GET, "user", MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW);

        List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody> result =
            objectMapper.readValue(response,
                new TypeReference<List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities
                    .SegmentationGetResponseBody>>() {
                });

        List<com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody> expected = Lists
            .newArrayList(
                new com.backbase.presentation.legalentity.rest.spec.v2.legalentities.SegmentationGetResponseBody()
                    .withExternalId(rootLegalEntity.getExternalId())
                    .withId(rootLegalEntity.getId())
                    .withType(LegalEntityType.BANK)
                    .withName(rootLegalEntity.getName())
                    .withIsParent(true));

        assertEquals(expected, result);
    }

    @Test
    public void shouldFailNegativeFromValue() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(
                "/legalentities/segmentation?query=LE1&businessFunction=ManageAccounts&privilege=view&from=-1&cursor=&size=10",
                HttpMethod.GET, "user", MANAGE_LEGAL_ENTITIES_FUNCTION_NAME, PRIVILEGE_VIEW));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void shouldFailNegativeSize() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> executeClientRequest(
                "/legalentities/segmentation?query=LE1&businessFunction=ManageAccounts&privilege=view&from=0&cursor=&size=-4",
                HttpMethod.GET, null, MANAGE_LEGAL_ENTITY_FUNCTION_NAME, PRIVILEGE_VIEW));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_064.getErrorMessage(), ERR_AG_064.getErrorCode()));
    }
}
