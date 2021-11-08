package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.domain.GraphConstants.DATA_GROUP_SERVICE_AGREEMENT;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_032;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_CREATE;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.ADD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.DataGroupClientController;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link DataGroupClientController#postDataGroups}
 */
public class PostDataGroupsIT extends TestDbWireMock {

    private static final String DATA_GROUP_URL = "/accessgroups/data-groups";

    @Test
    public void shouldSuccessfullySaveDataGroup() throws Exception {
        DataGroupBase dataGroup = new DataGroupBase()
            .withDescription("desc.dg")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        String responseAsString = executeClientRequest(DATA_GROUP_URL, HttpMethod.POST, dataGroup, "user",
            ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);

        String responseId = readValue(responseAsString, DataGroupsPostResponseBody.class)
            .getId();

        List<DataGroup> dataGroups = dataGroupJpaRepository
            .findByServiceAgreementId(rootMsa.getId(), DATA_GROUP_SERVICE_AGREEMENT);
        Assert.assertEquals(dataGroups.get(0).getId(), responseId);
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(ADD)
            .withId(responseId)));
    }

    @Test
    public void shouldThrowForbiddenIfServiceAgreementDoesNotExist() {
        DataGroupBase dataGroupWithNonExistingSa = new DataGroupBase()
            .withDescription("desc.dg")
            .withName("dg-name")
            .withServiceAgreementId(getUuid())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
            () -> {
                executeClientRequest(DATA_GROUP_URL, HttpMethod.POST, dataGroupWithNonExistingSa, "user",
                    ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);
            });

        assertEquals(ERR_AG_032.getErrorCode(), exception.getErrors().get(0).getKey());
        assertEquals(ERR_AG_032.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }


    @Test
    public void shouldThrowBadRequestIfDataGroupNameIsNotUnique() {
        String existingDgName = "dg-name";
        DataGroup existingDataGroup = DataGroupUtil
            .createDataGroup(existingDgName, "ARRANGEMENTS", "desc", rootMsa);

        dataGroupJpaRepository.save(existingDataGroup);

        DataGroupBase dataGroupWithNonExistingSa = new DataGroupBase()
            .withDescription("desc.dg")
            .withName(existingDgName)
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> {
                executeClientRequest(DATA_GROUP_URL, HttpMethod.POST, dataGroupWithNonExistingSa, "user",
                    ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_CREATE);
            });

        assertEquals(ERR_ACC_028.getErrorCode(), exception.getErrors().get(0).getKey());
        assertEquals(ERR_ACC_028.getErrorMessage(), exception.getErrors().get(0).getMessage());

    }
}
