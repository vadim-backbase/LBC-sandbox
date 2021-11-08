package com.backbase.accesscontrol.business.serviceagreement;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_087;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_102;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_106;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_117;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getInternalRequest;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipant;
import static com.backbase.accesscontrol.util.helpers.TestDataUtils.getParticipants;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mapstruct.ap.internal.util.Collections.asSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.persistence.serviceagreement.IngestServiceAgreementHandler;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.dto.ServiceAgreementData;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.PermissionSetValidationUtil;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.aps.PresentationUserApsIdentifiers;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ParticipantIngest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementIngestPostResponseBody;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestServiceAgreementTest {

    @InjectMocks
    private IngestServiceAgreement ingestServiceAgreement;
    private ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody;
    private Map<String, com.backbase.dbs.user.api.client.v2.model.GetUser> usersMap;

    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;
    @Mock
    private IngestServiceAgreementHandler ingestServiceAgreementHandler;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @Spy
    private PermissionSetValidationUtil permissionSetValidationUtil;

    @Before
    public void setUp() throws Exception {

        ingestServiceAgreementData();

        when(userManagementService.getUsersGroupedByExternalId(any(ServiceAgreementIngestPostRequestBody.class)))
            .thenReturn(usersMap);
    }

    @Test
    public void shouldCreateServiceAgreement() {
        String saId = "saId";

        ServiceAgreementIngestPostResponseBody serviceAgreementIngestPostResponseBody = new ServiceAgreementIngestPostResponseBody()
            .withId(saId);
        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        when(ingestServiceAgreementHandler.handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class)
        )).thenReturn(serviceAgreementIngestPostResponseBody);

        ingestServiceAgreement
            .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody));

        verify(ingestServiceAgreementHandler).handleRequest(any(EmptyParameterHolder.class),
            eq(new ServiceAgreementData<>(getServiceAgreementIngestPostRequestBody(), usersMap)));
        verify(serviceAgreementBusinessRulesService)
            .existsPendingServiceAgreementWithExternalId(eq(serviceAgreementIngestPostRequestBody.getExternalId()));
    }

    @Test
    public void shouldThrowExceptionForPendingExternalId() {

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);
        when(serviceAgreementBusinessRulesService
            .existsPendingServiceAgreementWithExternalId(serviceAgreementIngestPostRequestBody.getExternalId()))
            .thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_106.getErrorMessage(), ERR_AG_106.getErrorCode())));
    }

    @Test
    public void shouldTrowBadRequestOnNullParticipant() {

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(new ServiceAgreementIngestPostRequestBody()
                    .withParticipantsToIngest(
                        Sets.newHashSet(null, new ParticipantIngest())))));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_087.getErrorMessage(), ERR_AG_087.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowExceptionForInvalidDateFormat() {
        serviceAgreementIngestPostRequestBody.setValidUntilTime("08:00");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowExceptionForInvalidDates() {
        serviceAgreementIngestPostRequestBody.setValidUntilDate("2009-01-01");

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowBadRequestWhenCreatingServiceAgreementWithRegularUserApsWithNoIdentifiers() {
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody = getServiceAgreementIngestPostRequestBody();

        serviceAgreementIngestPostRequestBody.setRegularUserAps(new PresentationUserApsIdentifiers());

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowBadRequestWhenCreatingServiceAgreementWithAdminUserApsWithNoIdentifiers() {
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody = getServiceAgreementIngestPostRequestBody()
            .withAdminUserAps(new PresentationUserApsIdentifiers());

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowBadRequestWhenCreatingServiceAgreementWithRegularUserApsWithBothIdentifiers() {
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody = getServiceAgreementIngestPostRequestBody()
            .withRegularUserAps(
                new PresentationUserApsIdentifiers()
                    .withIdIdentifiers(asSet(new BigDecimal(1)))
                    .withNameIdentifiers(asSet("apsName")));

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowBadRequestWhenCreatingServiceAgreementWithAdminUserApsWithBothIdentifiers() {
        ServiceAgreementIngestPostRequestBody serviceAgreementIngestPostRequestBody = getServiceAgreementIngestPostRequestBody()
            .withAdminUserAps(
                new PresentationUserApsIdentifiers()
                    .withIdIdentifiers(asSet(new BigDecimal(1)))
                    .withNameIdentifiers(asSet("apsName")));

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement
                .ingestServiceAgreement(getInternalRequest(serviceAgreementIngestPostRequestBody)));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_102.getErrorMessage(), ERR_AG_102.getErrorCode())));

        verify(ingestServiceAgreementHandler, times(0)).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    @Test
    public void shouldThrowBadRequestWhenCreatorLegalEntityProvidedForMasterServiceAgreement() {
        serviceAgreementIngestPostRequestBody.setIsMaster(true);
        serviceAgreementIngestPostRequestBody.setCreatorLegalEntity("creatorLegalEntity");

        when(serviceAgreementBusinessRulesService.isPeriodValid(any(Date.class), any(Date.class))).thenReturn(true);

        InternalRequest<ServiceAgreementIngestPostRequestBody> internalRequest = getInternalRequest(
            serviceAgreementIngestPostRequestBody);

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
            () -> ingestServiceAgreement.ingestServiceAgreement(internalRequest));

        assertThat(badRequestException,
            is(new BadRequestErrorMatcher(ERR_AG_117.getErrorMessage(), ERR_AG_117.getErrorCode())));

        verify(ingestServiceAgreementHandler, never()).handleRequest(any(EmptyParameterHolder.class),
            any(ServiceAgreementData.class));
    }

    private void ingestServiceAgreementData() {
        serviceAgreementIngestPostRequestBody = getServiceAgreementIngestPostRequestBody();

        usersMap = new LinkedHashMap<>();
        usersMap.put("u1_1", getUsers("u1_1", "id1", "id1_1"));
        usersMap.put("u1_2", getUsers("u1_2", "id1", "id1_2"));
        usersMap.put("u2_1", getUsers("u2_1", "id2", "id2_1"));
        usersMap.put("u2_2", getUsers("u2_2", "id2", "id2_2"));
        usersMap.put("u3_1", getUsers("u3_1", "id3", "id3_1"));
        usersMap.put("u3_2", getUsers("u3_2", "id3", "id3_2"));
    }

    private ServiceAgreementIngestPostRequestBody getServiceAgreementIngestPostRequestBody() {
        return new ServiceAgreementIngestPostRequestBody()
            .withExternalId("id.external")
            .withName("name")
            .withDescription("desc")
            .withValidFromDate("2010-01-01")
            .withValidFromTime("00:00:00")
            .withValidUntilDate("2011-01-01")
            .withValidUntilTime("00:00:00")
            .withRegularUserAps(new PresentationUserApsIdentifiers().withIdIdentifiers(asSet(new BigDecimal(1))))
            .withAdminUserAps(new PresentationUserApsIdentifiers().withNameIdentifiers(asSet("apsName")))
            .withParticipantsToIngest(getParticipants(
                getParticipant("1", asList("U1_1", "u1_2"), true, true, Collections.singletonList("U1_1")),
                getParticipant("2", asList("u2_1", "u2_2"), false, true, new ArrayList<>()),
                getParticipant("3", asList("u3_1", "u3_2"), true, false, asList("U3_1", "u3_2"))

            ));
    }

    private GetUser getUsers(String externalId, String legalEntityId, String id) {
        com.backbase.dbs.user.api.client.v2.model.GetUser user1 = new com.backbase.dbs.user.api.client.v2.model.GetUser();
        user1.setId(id);
        user1.setLegalEntityId(legalEntityId);
        user1.setExternalId(externalId);
        return user1;
    }

}