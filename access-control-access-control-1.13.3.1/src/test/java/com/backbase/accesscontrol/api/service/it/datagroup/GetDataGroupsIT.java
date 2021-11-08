package com.backbase.accesscontrol.api.service.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.matchers.ForbiddenErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashSet;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link DataGroupClientController#getDataGroups}
 */
public class GetDataGroupsIT extends TestDbWireMock {

    private String validUrl = "/accessgroups/data-groups";

    @Test
    public void testGetAllDataGroups() throws Exception {
        createDataGroup("name1", newHashSet("001", "002", "003"));
        createDataGroup("name2", newHashSet("004", "005", "006"));

        String contentAsString = executeClientRequest(
            new UrlBuilder(validUrl)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("type", "ARRANGEMENTS")
                .build(), HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_VIEW);

        List<DataGroupItemBase> response = readValue(
            contentAsString,
            new TypeReference<List<DataGroupItemBase>>() {
            });

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    public void testRequiredQueryParametersForListAllDataGroupMissingServiceAgreement() {
        String url = "/accessgroups/data-groups?type=ARRANGEMENTS";

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequest(
            new UrlBuilder(url).build(), HttpMethod.GET, null, ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_VIEW));

        assertEquals("Required request parameter 'serviceAgreementId' for method parameter type String is not present", exception.getMessage());
    }

    @Test
    public void shouldtWRtWhenUserHasNoAccessToEntitlementResource() {
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> executeClientRequest(
            new UrlBuilder(validUrl)
                .addQueryParameter("serviceAgreementId", "serviceAgreementId")
                .addQueryParameter("type", "ARRANGEMENTS")
                .build(), HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_VIEW));
        assertThat(exception, new ForbiddenErrorMatcher(ERR_AG_032.getErrorMessage(), ERR_AG_032.getErrorCode()));
    }

    public void createDataGroup(String name, HashSet<String> items) {
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup(name, "ARRANGEMENTS", "DESCRIPTION", rootMsa);
        dataGroup.setDataItemIds(items);
        dataGroupJpaRepository.save(dataGroup);
    }


}
