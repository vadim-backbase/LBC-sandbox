package com.backbase.accesscontrol.api.client.it.approval;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_001;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_006;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_060;
import static com.backbase.accesscontrol.util.helpers.DataGroupUtil.createDataGroup;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.ENTITLEMENTS_MANAGE_DATA_GROUPS;
import static com.backbase.accesscontrol.util.helpers.ResourceAndFunctionNameConstants.PRIVILEGE_APPROVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalDataGroup;
import com.backbase.accesscontrol.domain.ApprovalDataGroupDetails;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.approval.api.client.v2.model.PresentationApprovalDetailDto;
import com.backbase.dbs.approval.api.client.v2.model.PresentationGetApprovalDetailResponse;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationDataGroupApprovalDetailsItem;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.json.JSONException;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class GetDataGroupApprovalDetailsItemIT extends TestDbWireMock {

    private String url = "/accessgroups/approvals/%s/data-group";

    private String approvalId = "b3e4692d-6772-4fb2-9446-b8f4607f49b1";

    @Test
    public void shouldGetDetailsForPendingCreateDataGroup() throws IOException, JSONException {
        String dgName = "DG-NAME";
        String dgDescription = "DG-DESC";
        String type = "ARRANGEMENTS";
        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setType(type);
        approvalDetails.setServiceAgreementId(rootMsa.getId());
        approvalDetails.setName(dgName);
        approvalDetails.setDescription(dgDescription);
        approvalDetails.setItems(new HashSet<>(Collections.singletonList("item-1")));
        approvalDetails.setType(type);

        approvalDetails = approvalDataGroupDetailsJpaRepository.saveAndFlush(approvalDetails);

        String userId = contextUserId;

        String serviceAgreementId = rootMsa.getId();

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(userId)
                .serviceAgreementId(serviceAgreementId));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", userId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        String response = executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
            HttpMethod.GET, userId, ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        PresentationDataGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationDataGroupApprovalDetailsItem.class);
        assertNull(responseObject.getDataGroupId());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertEquals(serviceAgreementId, responseObject.getServiceAgreementId());
        assertEquals(PresentationApprovalAction.CREATE, responseObject.getAction());
        assertEquals(rootMsa.getName(), responseObject.getServiceAgreementName());
        assertEquals(type, responseObject.getType());
        assertEquals(dgName, responseObject.getNewState().getName());
        assertEquals(dgDescription, responseObject.getNewState().getDescription());
        assertTrue(responseObject.getAddedDataItems().contains("item-1"));
    }


    @Test
    public void shouldGetDetailsForPendingUpdateDataGroup() throws Exception {
        String type = "ARRANGEMENTS";
        String updatedName = "NEW-DG-NAME";
        String updatedDescription = "NEW-DG-DESC";
        DataGroup dataGroup =
            createDataGroup("dg-name", type, "description", rootMsa, Lists.newArrayList("unmodified-1"));
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(dataGroup.getId());
        approvalDetails.setType(type);
        approvalDetails.setServiceAgreementId(rootMsa.getId());
        approvalDetails.setName(updatedName);
        approvalDetails.setDescription(updatedDescription);
        approvalDetails.setItems(new HashSet<>(Arrays.asList("unmodified-1", "added-1")));
        approvalDetails.setType(type);

        approvalDetails = approvalDataGroupJpaRepository.save(approvalDetails);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        String response = executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
            HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        PresentationDataGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationDataGroupApprovalDetailsItem.class);
        assertNotNull(responseObject.getDataGroupId());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertEquals(rootMsa.getId(), responseObject.getServiceAgreementId());
        assertEquals(PresentationApprovalAction.EDIT, responseObject.getAction());
        assertEquals(rootMsa.getName(), responseObject.getServiceAgreementName());
        assertEquals(type, responseObject.getType());
        assertEquals(updatedDescription, responseObject.getNewState().getDescription());
        assertTrue(responseObject.getAddedDataItems().contains("added-1"));
        assertTrue(responseObject.getUnmodifiedDataItems().contains("unmodified-1"));
    }

    @Test
    public void shouldGetDetailsForPendingDeleteDataGroup() throws Exception {
        String type = "ARRANGEMENTS";
        String name = "dg-name";
        String description = "description";
        DataGroup dataGroup =
            createDataGroup(name, type, description, rootMsa, Collections.singletonList("item-1"));
        dataGroup = dataGroupJpaRepository.save(dataGroup);

        ApprovalDataGroup approvalDetails = new ApprovalDataGroup();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(dataGroup.getId());

        approvalDetails = approvalDataGroupJpaRepository.save(approvalDetails);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        String response = executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
            HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE);

        PresentationDataGroupApprovalDetailsItem responseObject = readValue(response,
            PresentationDataGroupApprovalDetailsItem.class);
        assertNotNull(responseObject.getDataGroupId());
        assertEquals(approvalId, responseObject.getApprovalId());
        assertEquals(rootMsa.getId(), responseObject.getServiceAgreementId());
        assertEquals(PresentationApprovalAction.DELETE, responseObject.getAction());
        assertEquals(rootMsa.getName(), responseObject.getServiceAgreementName());
        assertEquals(type, responseObject.getType());
        assertEquals(description, responseObject.getNewState().getDescription());
        assertTrue(responseObject.getRemovedDataItems().contains("item-1"));
    }

    @Test
    public void shouldThrowNotFoundWhenApprovalIdDoesNotExist() {

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(String.format(url, approvalId),
                HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_060.getErrorMessage(), ERR_ACQ_060.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundWhenServiceAgreementDoesNotExistOnCreate() {
        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(getUuid());
        approvalDetails.setName("DG-NAME");
        approvalDetails.setDescription("DG-DESC");
        approvalDetails.setItems(new HashSet<>(Collections.singletonList("item-1")));
        approvalDetails.setType("ARRANGEMENTS");

        approvalDataGroupDetailsJpaRepository.save(approvalDetails);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
                HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_006.getErrorMessage(), ERR_ACQ_006.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundWhenDataGroupDoesNotExistOnUpdate() {
        ApprovalDataGroupDetails approvalDetails = new ApprovalDataGroupDetails();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(getUuid());
        approvalDetails.setType("ARRANGEMENTS");
        approvalDetails.setServiceAgreementId(rootMsa.getId());
        approvalDetails.setName("NEW-DG-NAME");
        approvalDetails.setDescription("NEW-DG-DESC");
        approvalDetails.setItems(new HashSet<>(Arrays.asList("unmodified-1", "added-1")));
        approvalDetails.setType("ARRANGEMENTS");

        approvalDataGroupDetailsJpaRepository.save(approvalDetails);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
                HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

    @Test
    public void shouldThrowNotFoundWhenDataGroupDoesNotExistOnDelete() {
        ApprovalDataGroup approvalDetails = new ApprovalDataGroup();
        approvalDetails.setApprovalId(approvalId);
        approvalDetails.setDataGroupId(getUuid());

        approvalDataGroupJpaRepository.save(approvalDetails);

        PresentationGetApprovalDetailResponse presentationGetApprovalDetailResponse = new PresentationGetApprovalDetailResponse()
            .approvalDetails(new PresentationApprovalDetailDto()
                .function("Manage Data Groups")
                .userId(contextUserId)
                .serviceAgreementId(rootMsa.getId()));

        addStubGet(new UrlBuilder("/service-api/v2/approvals/")
                .addPathParameter(approvalId)
                .addQueryParameter("serviceAgreementId", rootMsa.getId())
                .addQueryParameter("userId", contextUserId)
                .addQueryParameter("enrichUsersWithFullName", "false").build(),
            presentationGetApprovalDetailResponse, 200, new HashMap<>(), new HashMap<>());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> executeClientRequest(String.format(url, approvalDetails.getApprovalId()),
                HttpMethod.GET, "user", ENTITLEMENTS_MANAGE_DATA_GROUPS, PRIVILEGE_APPROVE));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_001.getErrorMessage(), ERR_ACQ_001.getErrorCode()));
    }

}