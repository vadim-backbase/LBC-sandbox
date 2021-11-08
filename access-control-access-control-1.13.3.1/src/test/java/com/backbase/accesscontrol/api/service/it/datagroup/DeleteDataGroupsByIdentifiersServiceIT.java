package com.backbase.accesscontrol.api.service.it.datagroup;

import static com.backbase.accesscontrol.matchers.BatchResponseItemExtendedMatcher.getMatchers;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_013;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_015;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_051;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_074;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.ApprovalUserContextAssignFunctionGroup;
import com.backbase.accesscontrol.domain.DataGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroupCombination;
import com.backbase.accesscontrol.domain.UserAssignedFunctionGroup;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.util.helpers.DataGroupUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

public class DeleteDataGroupsByIdentifiersServiceIT extends TestDbWireMock {

    private static final String URL = "/accessgroups/data-groups/batch/delete";

    private DataGroup dataGroup1;
    private DataGroup dataGroup2;
    private FunctionGroup functionGroup;

    @Before
    public void setUp() throws Exception {

        dataGroup1 = DataGroupUtil.createDataGroup("dg-name", "ARRANGEMENTS", "desc", rootMsa);
        dataGroup1.setDataItemIds(Sets.newHashSet(getUuid(), getUuid()));
        dataGroupJpaRepository.save(dataGroup1);
        dataGroup2 = DataGroupUtil.createDataGroup("dg-name-2", "ARRANGEMENTS", "desc", rootMsa);
        dataGroup2.setDataItemIds(Sets.newHashSet(getUuid(), getUuid()));
        dataGroupJpaRepository.save(dataGroup2);
    }

    @Test
    public void shouldDeleteDataGroupBatch() throws Exception {

        PresentationIdentifier identifierById = new PresentationIdentifier()
            .withIdIdentifier(dataGroup1.getId());

        PresentationIdentifier identifierByName = new PresentationIdentifier()
            .withNameIdentifier(new NameIdentifier()
                .withName(dataGroup2.getName())
                .withExternalServiceAgreementId(rootMsa.getExternalId()));

        List<PresentationIdentifier> dataGroupsToDelete = asList(identifierById, identifierByName);

        BatchResponseItemExtended successfulBatchResponseItemIdIdentifier =
            new BatchResponseItemExtended()
                .withStatus(
                    BatchResponseStatusCode.HTTP_STATUS_OK)
                .withResourceId(dataGroup1.getId());

        BatchResponseItemExtended successfulBatchResponseItemNameIdentifier =
            new BatchResponseItemExtended()
                .withStatus(
                    BatchResponseStatusCode.HTTP_STATUS_OK)
                .withResourceId(dataGroup2.getName())
                .withExternalServiceAgreementId(rootMsa.getExternalId());

        List<BatchResponseItemExtended> responseList = asList(
            successfulBatchResponseItemIdIdentifier,
            successfulBatchResponseItemNameIdentifier);

        String contentAsString = executeRequest(URL, asList(identifierById, identifierByName), HttpMethod.POST);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertThat(responseData, hasSize(dataGroupsToDelete.size()));
        assertThat(responseData,
            containsInAnyOrder(
                getMatchers(responseList)
            ));

        verifyDataGroupEvents(Sets.newHashSet(new DataGroupEvent()
                .withAction(DELETE)
                .withId(dataGroup1.getId()),
            new DataGroupEvent()
                .withAction(DELETE)
                .withId(dataGroup2.getId())));
    }

    @Test
    public void shouldReturnErrorsOnInvalidIdentifiers() throws Exception {
        String randomId = getUuid();
        PresentationIdentifier idIdentifier = new PresentationIdentifier()
            .withIdIdentifier(randomId);
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .withNameIdentifier(
                new NameIdentifier()
                    .withName(dataGroup2.getName())
                    .withExternalServiceAgreementId("invalid-service-agreement-external-id")
            );
        PresentationIdentifier nameIdentifier2 = new PresentationIdentifier()
            .withNameIdentifier(
                new NameIdentifier()
                    .withName("invalid-name")
                    .withExternalServiceAgreementId(rootMsa.getExternalId())
            );

        String contentAsString = executeRequest(URL, asList(idIdentifier, nameIdentifier, nameIdentifier2),
            HttpMethod.POST);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        assertEquals(3, responseData.size());
        assertEquals(randomId, responseData.get(0).getResourceId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_NOT_FOUND, responseData.get(0).getStatus());
        assertEquals(ERR_ACC_015.getErrorMessage(), responseData.get(0).getErrors().get(0));

        assertEquals(dataGroup2.getName(), responseData.get(1).getResourceId());
        assertEquals("invalid-service-agreement-external-id",
            responseData.get(1).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseData.get(1).getStatus());
        assertEquals(ERR_ACC_051.getErrorMessage(), responseData.get(1).getErrors().get(0));

        assertEquals("invalid-name", responseData.get(2).getResourceId());
        assertEquals(responseData.get(2).getExternalServiceAgreementId(),
            responseData.get(2).getExternalServiceAgreementId());
        assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseData.get(2).getStatus());
        assertEquals(ERR_ACC_051.getErrorMessage(), responseData.get(2).getErrors().get(0));

    }

    @Test
    public void shouldReturnErrorsIfDataGroupIsAssignedToUserOrPendingAssignment() throws Exception {
        assignFunctionGroupDataGroupToUser();
        createPendingRequestForDataGroup();

        PresentationIdentifier idIdentifier = new PresentationIdentifier()
            .withIdIdentifier(dataGroup1.getId());
        PresentationIdentifier nameIdentifier = new PresentationIdentifier()
            .withNameIdentifier(
                new NameIdentifier()
                    .withName(dataGroup2.getName())
                    .withExternalServiceAgreementId(rootMsa.getExternalId())
            );

        String contentAsString = executeRequest(URL, asList(idIdentifier, nameIdentifier),
            HttpMethod.POST);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<List<BatchResponseItemExtended>>() {
            });

        TestCase.assertEquals(2, responseData.size());
        TestCase.assertEquals(dataGroup1.getId(), responseData.get(0).getResourceId());
        TestCase.assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseData.get(0).getStatus());
        TestCase.assertEquals(ERR_ACC_013.getErrorMessage(), responseData.get(0).getErrors().get(0));

        TestCase.assertEquals(dataGroup2.getName(), responseData.get(1).getResourceId());
        TestCase.assertEquals(rootMsa.getExternalId(),
            responseData.get(1).getExternalServiceAgreementId());
        TestCase.assertEquals(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST, responseData.get(1).getStatus());
        TestCase.assertEquals(ERR_ACC_074.getErrorMessage(), responseData.get(1).getErrors().get(0));

    }

    private void assignFunctionGroupDataGroupToUser() {
        functionGroup = functionGroupJpaRepository.save(
            FunctionGroupUtil
                .getFunctionGroup(null, "fg-name", "desc", new HashSet<>(), FunctionGroupType.DEFAULT,
                    rootMsa)
        );

        UserContext userContext = new UserContext(UUID.randomUUID().toString(), rootMsa.getId());
        userContext = userContextJpaRepository.save(userContext);

        UserAssignedFunctionGroup userAssignedFunctionGroup = userAssignedFunctionGroupJpaRepository
            .save(new UserAssignedFunctionGroup(functionGroup, userContext));

        userAssignedCombinationRepository
            .save(new UserAssignedFunctionGroupCombination(Sets.newHashSet(dataGroup1.getId()), userAssignedFunctionGroup));
    }

    private void createPendingRequestForDataGroup() {

        ApprovalUserContext approvalUserContext = new ApprovalUserContext(UUID.randomUUID().toString(),
            rootMsa.getId(),
            rootLegalEntity.getId(), new HashSet<>());
        approvalUserContext.setApprovalId("1");
        approvalUserContext = approvalUserContextJpaRepository.save(approvalUserContext);

        ApprovalUserContextAssignFunctionGroup approvalUserContextAssignFunctionGroup =
            new ApprovalUserContextAssignFunctionGroup(null, functionGroup.getId(),
                approvalUserContext, Sets.newHashSet(dataGroup2.getId()), null);
        approvalUserContextAssignFunctionGroupJpaRepository.save(approvalUserContextAssignFunctionGroup);
    }

    public void createDataGroup(String name, HashSet<String> items) {
        DataGroup dataGroup = DataGroupUtil
            .createDataGroup(name, "ARRANGEMENTS", "DESCRIPTION", rootMsa);
        dataGroup.setDataItemIds(items);
        dataGroupJpaRepository.save(dataGroup);
    }

}

