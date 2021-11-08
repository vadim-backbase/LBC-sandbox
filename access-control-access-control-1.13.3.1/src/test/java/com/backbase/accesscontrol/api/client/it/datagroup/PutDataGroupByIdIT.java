package com.backbase.accesscontrol.api.client.it.datagroup;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_028;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.LegalEntityUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupByIdPutRequestBody;
import com.google.common.collect.Sets;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "backbase.approval.validation.enabled=false"}
)
public class PutDataGroupByIdIT extends TestDbWireMock {

    private String url = "/accessgroups/data-groups/";

    private DataGroup dataGroup;

    @Before
    public void setUp() {
        LegalEntity legalEntity = LegalEntityUtil
            .createLegalEntity(null, "le-name", "ex-id3", null, LegalEntityType.BANK);
        legalEntityJpaRepository.save(legalEntity);

        dataGroup = DataGroupUtil
            .createDataGroup("dg-name", "ARRANGEMENTS", "description", rootMsa);

        dataGroup = dataGroupJpaRepository.save(dataGroup);
    }


    @Test
    @SuppressWarnings("squid:S2699")
    public void shouldSuccessfullyUpdateDataGroup() throws Exception {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        executeClientRequest(url + "/" + updateBody.getId(),
            HttpMethod.PUT, updateBody, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT);
        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
            .withAction(UPDATE)
            .withId(dataGroup.getId())));

    }

    @Test
    public void shouldThrowBadRequestIfDataGroupDoesNotExist() {
        DataGroupByIdPutRequestBody updateBody = new DataGroupByIdPutRequestBody()
            .withId(getUuid())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName("dg-name")
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(url + "/" + updateBody.getId(), HttpMethod.PUT, updateBody, "user",
                ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestIfDataGroupNameIsNotUnique() {
        String existingDgName = "random-name";
        DataGroup existingDataGroup = DataGroupUtil
            .createDataGroup(existingDgName, "ARRANGEMENTS", "desc", rootMsa);

        dataGroupJpaRepository.save(existingDataGroup);

        DataGroupByIdPutRequestBody dataGroupWithNonExistingSa = new DataGroupByIdPutRequestBody()
            .withId(dataGroup.getId())
            .withDescription("desc.dg")
            .withApprovalId("1235e686d31e4216b3dd5d66161d536d")
            .withName(existingDgName)
            .withServiceAgreementId(rootMsa.getId())
            .withType("ARRANGEMENTS")
            .withItems(Collections.singletonList("item 1"));

        BadRequestException exception = assertThrows(BadRequestException.class, () ->
            executeClientRequest(url + "/" + dataGroupWithNonExistingSa.getId(),
                HttpMethod.PUT, dataGroupWithNonExistingSa, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_028.getErrorMessage(), ERR_ACC_028.getErrorCode()));
    }


}