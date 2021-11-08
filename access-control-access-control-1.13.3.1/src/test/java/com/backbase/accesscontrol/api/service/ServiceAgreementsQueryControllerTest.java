package com.backbase.accesscontrol.api.service;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_045;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.dto.UserParameters;
import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.mappers.model.query.service.FunctionsGetResponseBodyToFunctionsGetResponseBodyMapper;
import com.backbase.accesscontrol.mappers.model.query.service.PersistenceServiceAgreementsToListServiceAgreementsMapper;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.rest.spec.model.ListServiceAgreements;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.facades.ServiceAgreementServiceFacade;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.config.functions.FunctionsGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.PersistencePrivilege;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceServiceAgreement;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.serviceagreements.PersistenceServiceAgreements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementsQueryControllerTest {

    @InjectMocks
    private ServiceAgreementsQueryController serviceAgreementsQueryController;

    @Mock
    private ServiceAgreementServiceFacade serviceAgreementServiceFacade;
    @Mock
    private FunctionGroupService functionGroupService;
    @Spy
    private PayloadConverter payloadConverter =
        new PayloadConverter(asList(
            spy(Mappers.getMapper(PersistenceServiceAgreementsToListServiceAgreementsMapper.class)),
            spy(Mappers.getMapper(FunctionsGetResponseBodyToFunctionsGetResponseBodyMapper.class))
        ));

    @Test
    public void testGetServiceAgreements() {
        String creatorId = "LE-1";
        long totalElements = 1000L;
        List<PersistenceServiceAgreement> serviceAgreementsList = new ArrayList<>();
        serviceAgreementsList.add(new PersistenceServiceAgreement().withId("1"));
        serviceAgreementsList.add(new PersistenceServiceAgreement().withId("2"));
        serviceAgreementsList.add(new PersistenceServiceAgreement().withId("3"));
        PersistenceServiceAgreements serviceAgreements = new PersistenceServiceAgreements()
            .withServiceAgreements(serviceAgreementsList)
            .withTotalElements(totalElements);

        when(serviceAgreementServiceFacade.listServiceAgreements(
            eq(creatorId),
            eq(new UserParameters("userId", "userLegalEntityId")),
            any(SearchAndPaginationParameters.class)))
            .thenReturn(serviceAgreements);

        ListServiceAgreements serviceAgreementsResult =
            serviceAgreementsQueryController.getAgreements(
                creatorId, "userId", "userLegalEntityId",
                null, null, null, null).getBody();

        assertEquals(serviceAgreements.getServiceAgreements().size(),
            serviceAgreementsResult.getServiceAgreements().size());
        assertEquals(serviceAgreementsResult.getTotalElements().longValue(),
            serviceAgreements.getTotalElements().longValue());
    }

    @Test
    public void testGetBusinessFunctionByExternalServiceAgreementId() {
        List<FunctionsGetResponseBody> businessFunctions = new ArrayList<>();
        FunctionsGetResponseBody bf1 = new FunctionsGetResponseBody()
            .withFunctionId("1001")
            .withName("privilege 1")
            .withResource("resource 1")
            .withResourceCode("resource code 1")
            .withFunctionCode("function code 1")
            .withPrivileges(Collections.singletonList(new PersistencePrivilege()
                .withPrivilege("privilege 1")));
        FunctionsGetResponseBody bf2 = new FunctionsGetResponseBody()
            .withFunctionId("1002")
            .withName("privilege 2")
            .withResource("resource 2")
            .withResourceCode("resource code 2")
            .withFunctionCode("function code 2")
            .withPrivileges(Collections.singletonList(new PersistencePrivilege()
                .withPrivilege("privilege 2")));
        businessFunctions.add(bf1);
        businessFunctions.add(bf2);

        when(functionGroupService.findAllBusinessFunctionsByServiceAgreement(eq("ex-sa1"), eq(true)))
            .thenReturn(businessFunctions);

        List<com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody> participantsResult =
            serviceAgreementsQueryController
                .getExternalexternalIdbusinessfunctions("ex-sa1").getBody();

        assertEquals(businessFunctions.size(), participantsResult.size());
    }

    @Test
    public void testGetBusinessFunctionByInternalServiceAgreementId() {
        List<FunctionsGetResponseBody> businessFunctions = new ArrayList<>();
        FunctionsGetResponseBody bf1 = new FunctionsGetResponseBody()
            .withFunctionId("1001")
            .withName("privilege 1")
            .withResource("resource 1")
            .withResourceCode("resource code 1")
            .withFunctionCode("function code 1")
            .withPrivileges(Collections.singletonList(new PersistencePrivilege()
                .withPrivilege("privilege 1")));
        FunctionsGetResponseBody bf2 = new FunctionsGetResponseBody()
            .withFunctionId("1002")
            .withName("privilege 2")
            .withResource("resource 2")
            .withResourceCode("resource code 2")
            .withFunctionCode("function code 2")
            .withPrivileges(Collections.singletonList(new PersistencePrivilege()
                .withPrivilege("privilege 2")));
        businessFunctions.add(bf1);
        businessFunctions.add(bf2);

        when(functionGroupService.findAllBusinessFunctionsByServiceAgreement(eq("sa-id"), eq(false)))
            .thenReturn(businessFunctions);

        List<com.backbase.accesscontrol.service.rest.spec.model.FunctionsGetResponseBody> participantsResult =
            serviceAgreementsQueryController
                .getIdinternalIdbusinessfunctions("sa-id").getBody();

        assertEquals(businessFunctions.size(), participantsResult.size());
    }

    @Test
    public void shouldThrowBadRequestWhenGettingSaByApsIdIfSizeBiggerThen1000() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementsQueryController
                .getGetServiceAgremeentByPermissionSetId("1", 1, "", 1002));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_045.getErrorMessage(), ERR_ACQ_045.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestWhenGettingSaByApsNameIfSizeBiggerThen1000() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> serviceAgreementsQueryController
                .getGetServiceAgremeentByPermissionSetName("apsName", 1, "", 1002));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACQ_045.getErrorMessage(), ERR_ACQ_045.getErrorCode()));
    }
}
