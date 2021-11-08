package com.backbase.accesscontrol.api.routeextension.datagroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getVoidInternalRequest;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.business.datagroup.GetDataGroupById;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.routes.datagroup.GetDataGroupByIdRouteProxy;
import com.backbase.accesscontrol.util.constants.EndpointConstants;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdGetResponseBody;
import org.apache.camel.Produce;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test for {@link GetDataGroupById#getDataGroupById}
 */
@ActiveProfiles({"live", "routes", "h2"})
public class GetDataGroupHookIT extends TestDbWireMock {

    public static final String DATA_GROUP_NAME = "dgName";

    @Produce(value = EndpointConstants.DIRECT_BUSINESS_GET_DATA_GROUP_BY_ID)
    private GetDataGroupByIdRouteProxy getDataGroupRouteProxy;

    private String dgId;

    @Before
    public void setup() {
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup(DATA_GROUP_NAME, "ARRANGEMENTS", "desc1", rootMsa);
        dataGroup.setDataItemIds(newHashSet("001", "002", "003"));
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        dgId = dataGroup.getId();
    }

    @Test
    public void testGetDataGroupByIdHook() {

        DataGroupByIdGetResponseBody response = getDataGroupRouteProxy.getDataGroupById(getVoidInternalRequest(), dgId)
            .getData();

        assertEquals(DATA_GROUP_NAME, response.getName());
        assertEquals("Data Group has been in hook.", response.getDescription());
    }
}
