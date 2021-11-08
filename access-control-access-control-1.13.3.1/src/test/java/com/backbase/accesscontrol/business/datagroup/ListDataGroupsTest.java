package com.backbase.accesscontrol.business.datagroup;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.mappers.DataGroupMapper;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.util.helpers.RequestUtils;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.DataGroupItemBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsGetResponseBody;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ListDataGroupsTest {

    @Mock
    private DataGroupService dataGroupService;

    private ListDataGroups listDataGroups;

    @Spy
    private DataGroupMapper mapper = Mappers.getMapper(DataGroupMapper.class);

    @Before
    public void setUp() {
        listDataGroups = new ListDataGroups(dataGroupService, mapper);
    }

    @Test
    public void shouldCallDataGroupPAndPService() {
        String serviceAgreementId = "service-agreement-id";

        InternalRequest<Void> request = RequestUtils.getInternalRequest(null);

        List<DataGroupItemBase> responseFromService = Collections.singletonList(
            new DataGroupItemBase().withId("001")
                .withName("Data Group 1")
                .withDescription("Data Group 1")
                .withItems(Arrays.asList("1", "2"))
                .withServiceAgreementId(serviceAgreementId)
                .withType("ARRANGEMENTS")
                .withApprovalId("approvalId")
        );

        when(dataGroupService
            .getByServiceAgreementIdAndDataItemType(eq(serviceAgreementId), eq("ARRANGEMENTS"), eq(true)))
            .thenReturn(responseFromService);

        InternalRequest<List<DataGroupsGetResponseBody>> dataGroups = listDataGroups
            .getDataGroups(request, serviceAgreementId, "ARRANGEMENTS", true);
        assertThat(dataGroups.getData(), hasItems(
            getDataGroupMatcher(is(responseFromService.get(0).getName()),
                is(responseFromService.get(0).getDescription()), is(responseFromService.get(0).getServiceAgreementId()),
                is(responseFromService.get(0).getType()), is(responseFromService.get(0).getApprovalId()),
                is(responseFromService.get(0).getId()), is(responseFromService.get(0).getItems()))));

    }

    private <T> Matcher<? super T> getDataGroupMatcher(Matcher<String> name, Matcher<String> description,
        Matcher<String> serviceAgreementId,
        Matcher<String> type, Matcher<String> approvalId, Matcher<String> id, Matcher<List<String>> items) {
        return allOf(
            hasProperty("name", name),
            hasProperty("description", description),
            hasProperty("serviceAgreementId", serviceAgreementId),
            hasProperty("type", type),
            hasProperty("approvalId", approvalId),
            hasProperty("id", id),
            hasProperty("items", items)

        );
    }
}