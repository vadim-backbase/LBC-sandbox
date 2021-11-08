package com.backbase.accesscontrol.api.service.it.approvals;

import static com.backbase.accesscontrol.matchers.BatchResponseItemExtendedMatcher.getMatchers;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.domain.ApprovalServiceAgreementRef;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUserPair;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementUsersUpdate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@TestPropertySource(properties = "backbase.approval.validation.enabled=true")
public class IngestBatchAdminsUpdateApprovalOnIT extends TestDbWireMock {

    private String url = "/accessgroups/serviceagreements/ingest/service-agreements/admins";
    private String userUrl = baseServiceUrl + "/users/bulk/externalids";

    private String validInternalUserId = getUuid();

    @Before
    public void setUp() {

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(transactionStatus -> {
            ApprovalServiceAgreementRef sa = new ApprovalServiceAgreementRef();

            sa.setApprovalId("ApprovalId");
            sa.setServiceAgreementId(rootMsa.getId());
            approvalServiceAgreementRefJpaRepository.saveAndFlush(sa);

            return true;
        });
    }

    @Test
    public void shouldFirstAddAndThanRemoveAdmin() throws Exception {

        String validExternalUserId = "U-01";
        String legalEntityId = rootLegalEntity.getId();
        String externalServiceAgreementId = rootMsa.getExternalId();

        PresentationServiceAgreementUsersUpdate data = new PresentationServiceAgreementUsersUpdate()
            .withAction(PresentationAction.ADD)
            .withUsers(singletonList(new PresentationServiceAgreementUserPair()
                .withExternalServiceAgreementId(externalServiceAgreementId)
                .withExternalUserId(validExternalUserId)));

        BatchResponseItemExtended validItem = new BatchResponseItemExtended()
            .withExternalServiceAgreementId(externalServiceAgreementId)
            .withResourceId(validExternalUserId)
            .withErrors(Lists.newArrayList(ERR_AG_105.getErrorMessage()))
            .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST);

        List<BatchResponseItemExtended> pandpAllResponces = singletonList(
            validItem);

        com.backbase.dbs.user.api.client.v2.model.GetUser user = new GetUser();
        user.setLegalEntityId(legalEntityId);
        user.setId(validInternalUserId);
        user.setExternalId(validExternalUserId);
        List<GetUser> usersResponse = singletonList(
            user);

        addStubPost(userUrl, usersResponse, 200);

        String contentAsString = executeRequest(url, data, HttpMethod.PUT);

        List<BatchResponseItemExtended> responseData = readValue(
            contentAsString,
            new TypeReference<>() {
            });

        assertThat(responseData,
            containsInAnyOrder(
                getMatchers(pandpAllResponces)
            )
        );
    }
}
