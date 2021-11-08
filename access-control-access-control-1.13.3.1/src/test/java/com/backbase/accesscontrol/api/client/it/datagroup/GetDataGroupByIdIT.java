package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_VIEW;
import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link DataGroupClientController#getDataGroupById}
 */
public class GetDataGroupByIdIT extends TestDbWireMock {

    private static final String GET_DATA_GROUP_BY_ID = "/accessgroups/data-groups/{id}";

    public static final String DESCRIPTION = "desc1";
    public static final String DATA_ITEM_TYPE = "ARRANGEMENTS";
    public static final String DG_NAME = "dgName";
    public static final HashSet<String> DATA_ITEM_IDS = newHashSet("001", "002", "003");

    private String dgId;

    @Before
    public void setup() {
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup(DG_NAME, DATA_ITEM_TYPE, DESCRIPTION, rootMsa);
        dataGroup.setDataItemIds(DATA_ITEM_IDS);
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        dgId = dataGroup.getId();
    }

    @Test
    public void testSuccessfulGetDataGroupById() throws Exception {

        String contentAsString = executeClientRequest(
            new UrlBuilder(GET_DATA_GROUP_BY_ID)
                .addPathParameter(dgId)
                .build(), HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_VIEW);

        DataGroupItemBase responseBody = objectMapper.readValue(contentAsString,
            DataGroupItemBase.class);
        assertEquals(dgId, responseBody.getId());
        assertEquals(DESCRIPTION, responseBody.getDescription());
        assertEquals(rootMsa.getId(), responseBody.getServiceAgreementId());
        assertEquals(DG_NAME, responseBody.getName());
        assertTrue(responseBody.getItems().containsAll(DATA_ITEM_IDS));
    }

    @Test
    public void testNotFoundGetDataGroupById() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> executeClientRequest(
            new UrlBuilder(GET_DATA_GROUP_BY_ID)
                .addPathParameter("WRONG_DG_ID")
                .build(), HttpMethod.GET, "USER", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_VIEW));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }
}
