package com.backbase.accesscontrol.api.client.it.seviceagreement;

import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME;
import static com.backbase.accesscontrol.util.constants.ResourceAndFunctionNameConstants.PRIVILEGE_EDIT;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_070;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.backbase.accesscontrol.api.TestDbWireMock;
import com.backbase.accesscontrol.api.client.ServiceAgreementController;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.domain.enums.LegalEntityType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.UrlBuilder;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementStatePutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test for {@link ServiceAgreementController#putServiceAgreementState }
 */
public class UpdateServiceAgreementStateIT extends TestDbWireMock {

    private static final String url = "/accessgroups/serviceagreements/{serviceAgreementId}/state";

    private ServiceAgreement serviceAgreementCustomer;

    @Before
    public void setUp() {

        LegalEntity legalEntity2 = createLegalEntity(null, "le-name2", "ex-id32", rootLegalEntity,
            LegalEntityType.CUSTOMER);
        legalEntity2 = legalEntityJpaRepository.save(legalEntity2);

        serviceAgreementCustomer = createServiceAgreement("name.sa2", "exid.sa2", "desc.sa2", legalEntity2, null,
            null);
        serviceAgreementCustomer.setMaster(false);
        serviceAgreementCustomer.setState(ServiceAgreementState.ENABLED);
        serviceAgreementCustomer = serviceAgreementJpaRepository.save(serviceAgreementCustomer);
    }

    @Test
    public void testSuccessfulUpdatedServiceAgreement() throws IOException, JSONException {

        ServiceAgreementStatePutRequestBody serviceAgreementStatePutRequestBodyPutRequestBody =
            new ServiceAgreementStatePutRequestBody().withState(Status.DISABLED);

        ResponseEntity<String> response = executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(serviceAgreementCustomer.getId())
                .build()
            , HttpMethod.PUT, serviceAgreementStatePutRequestBodyPutRequestBody,
            "user", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testShouldThrowBadRequestDisableMSA() {

        ServiceAgreementStatePutRequestBody serviceAgreementStatePutRequestBodyPutRequestBody =
            new ServiceAgreementStatePutRequestBody().withState(Status.DISABLED);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> executeClientRequestEntity(
            new UrlBuilder(url)
                .addPathParameter(rootMsa.getId())
                .build()
            , HttpMethod.PUT, serviceAgreementStatePutRequestBodyPutRequestBody,
            "user", MANAGE_SERVICE_AGREEMENTS_FUNCTION_NAME, PRIVILEGE_EDIT));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_070.getErrorMessage(), ERR_AG_070.getErrorCode()));
    }

}
