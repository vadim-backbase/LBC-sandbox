package com.backbase.accesscontrol.api.service.it.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_070;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;
import static junit.framework.TestCase.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.service.ServiceAgreementServiceApiController;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.accesscontrol.util.helpers.FunctionGroupUtil;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

/**
 * Test for {@link ServiceAgreementServiceApiController#putServiceAgreementItem} }
 */
public class UpdateServiceAgreementServiceApiIT extends TestDbWireMock {

    private static final String url = "/accessgroups/serviceagreements/{serviceAgreementId}";

    private ServiceAgreement serviceAgreementCustom;
    private ServiceAgreement serviceAgreementTimeBound;
    private ServiceAgreement serviceAgreementTimeBound2;

    @Before
    public void setUp() {

        LegalEntity legalEntity2 = createLegalEntity(null, "le-name2", "ex-id32", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        legalEntity2 = legalEntityJpaRepository.save(legalEntity2);

        serviceAgreementTimeBound = createServiceAgreement("name.sa2", "exid.sa2", "desc.sa2", legalEntity2, null,
            null);
        serviceAgreementTimeBound.setMaster(false);
        serviceAgreementTimeBound = serviceAgreementJpaRepository.save(serviceAgreementTimeBound);

        serviceAgreementTimeBound2 = createServiceAgreement("name.sa22", "exid.sa22", "desc.sa22", legalEntity2, null,
            null);
        serviceAgreementTimeBound2.setMaster(false);
        serviceAgreementTimeBound2 = serviceAgreementJpaRepository.save(serviceAgreementTimeBound2);

        serviceAgreementCustom = createServiceAgreement("name.sa1", "exid.sa1", "desc.sa1", legalEntity2, null, null);
        serviceAgreementCustom.setMaster(false);
        serviceAgreementCustom = serviceAgreementJpaRepository.save(serviceAgreementCustom);

        FunctionGroup functionGroup = FunctionGroupUtil
            .getFunctionGroup(null, "fg-name", "fg-description", new HashSet<>(),
                FunctionGroupType.DEFAULT,
                serviceAgreementCustom);
        functionGroup.setStartDate(new Date(0));
        functionGroup.setEndDate(new Date(1111));

        FunctionGroup functionGroupPendingForUpdate = FunctionGroupUtil
            .getFunctionGroup(null, "fg-namePending", "fg-descriptionPending", new HashSet<>(),
                FunctionGroupType.DEFAULT,
                serviceAgreementTimeBound2);
        functionGroupPendingForUpdate.setStartDate(new Date(0));
        functionGroupPendingForUpdate.setEndDate(new Date(1111));
        functionGroupJpaRepository.save(functionGroupPendingForUpdate);

        ApprovalFunctionGroup approvalFunctionGroup = new ApprovalFunctionGroup();
        approvalFunctionGroup.setName(functionGroupPendingForUpdate.getName());
        approvalFunctionGroup.setDescription(functionGroupPendingForUpdate.getDescription());
        approvalFunctionGroup.setStartDate(functionGroupPendingForUpdate.getStartDate());
        approvalFunctionGroup.setEndDate(functionGroupPendingForUpdate.getEndDate());
        approvalFunctionGroup.setServiceAgreementId(functionGroupPendingForUpdate.getServiceAgreementId());
        approvalFunctionGroup.setApprovalId(UUID.randomUUID().toString());
        approvalFunctionGroup.setFunctionGroupId(functionGroupPendingForUpdate.getId());
        approvalFunctionGroup.setApprovalTypeId(UUID.randomUUID().toString());
        approvalFunctionGroup.setPrivileges(new HashSet<>());
        approvalFunctionGroupJpaRepository.save(approvalFunctionGroup);
        functionGroupJpaRepository.save(functionGroup);
    }

    @Test
    public void testSuccessfulUpdatedServiceAgreement() throws IOException, JSONException {
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        String updatedName = "sa-name-update";
        String updatedDescription = "sa-description-update";
        Status updatedStatus = Status.DISABLED;
        Date from = new Date((System.currentTimeMillis() / 1000) * 1000 + 3600 * 1000);
        Date until = new Date((System.currentTimeMillis() / 1000) * 1000 + 2 * 3600 * 1000);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = createServiceAgreementPutRequestBody(
            updatedName, updatedDescription, updatedStatus, from, until);
        serviceAgreementPutRequestBody.setAdditions(additions);
        String response = executeServiceRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementTimeBound.getId())
                .build()
            , serviceAgreementPutRequestBody, "USERNAME", rootMsa.getId(), HttpMethod.PUT);
        assertNull(response);

        verifyServiceAgreementEvents(Sets.newHashSet(new ServiceAgreementEvent()
            .withAction(UPDATE)
            .withId(serviceAgreementTimeBound.getId())));

    }

    @Test
    public void shouldThrowBadRequestExceptionWhenBelongingFunctionGroupStartDateIsBeforeStartDate() {
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "123456789";
        additions.put(key, value);

        String updatedName = "sa-name-update";
        String updatedDescription = "sa-description-update";
        Status updatedStatus = Status.DISABLED;
        Date from = new Date((System.currentTimeMillis() / 1000) * 1000 + 3600 * 1000);
        Date until = new Date((System.currentTimeMillis() / 1000) * 1000 + 2 * 3600 * 1000);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = createServiceAgreementPutRequestBody(
            updatedName, updatedDescription, updatedStatus, from, until);
        serviceAgreementPutRequestBody.setAdditions(additions);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeServiceRequest(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustom.getId())
                .build(), serviceAgreementPutRequestBody, "USERNAME", rootMsa.getId(), HttpMethod.PUT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldFailToUpdateServiceAgreementStatusWhenSAisRootMSA() {
        Map<String, String> additions = new HashMap<>();
        String key = "externalId";
        String value = "1234567";
        additions.put(key, value);

        String updatedName = "rootName";
        String updatedDescription = "sa-description-root";
        Status updatedStatus = Status.DISABLED;
        Date from = new Date((System.currentTimeMillis() / 1000) * 1000 + 3600 * 1000);
        Date until = new Date((System.currentTimeMillis() / 1000) * 1000 + 2 * 3600 * 1000);

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = createServiceAgreementPutRequestBody(
            updatedName, updatedDescription, updatedStatus, from, until);
        serviceAgreementPutRequestBody.setAdditions(additions);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeServiceRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), serviceAgreementPutRequestBody, "USERNAME", rootMsa.getId(), HttpMethod.PUT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_070.getErrorMessage(), ERR_AG_070.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenInValidTimePeriod() {
        Date from = new Date(1111);
        Date until = new Date(0);
        String updatedName = "rootName";
        String updatedDescription = "sa-description-root";
        Status updatedStatus = Status.DISABLED;

        ServiceAgreementPutRequestBody serviceAgreementPutRequestBody = createServiceAgreementPutRequestBody(
            updatedName, updatedDescription, updatedStatus, from, until);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeServiceRequest(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build(), serviceAgreementPutRequestBody, "USERNAME", rootMsa.getId(), HttpMethod.PUT));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode()));
    }

    private ServiceAgreementPutRequestBody createServiceAgreementPutRequestBody(String name, String description,
        Status status, Date from, Date until) {
        return new ServiceAgreementPutRequestBody()
            .withName(name)
            .withDescription(description)
            .withValidFromDate(DateFormatterUtil.utcFormatDateOnly(from))
            .withValidFromTime(DateFormatterUtil.utcFormatTimeOnly(from))
            .withValidUntilDate(DateFormatterUtil.utcFormatDateOnly(until))
            .withValidUntilTime(DateFormatterUtil.utcFormatTimeOnly(until))
            .withStatus(status);
    }
}
