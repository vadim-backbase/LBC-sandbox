package com.backbase.accesscontrol.audit;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.audit.descriptionprovider.AuditDescriptionContext;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link AuditAspect}
 */
@RunWith(MockitoJUnitRunner.class)
public class AuditAspectTest {


    private static final String LOGGED_USERNAME = "user1";
    private static final String USER_ID = "U-01";
    private static final String SERVICE_AGREEMENT_ID = "service agreement ID";
    private static final String EVENT_CATEGORY = "Access Control";


    @Mock
    private UserManagementService userManagementService;
    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;
    @Mock
    private AuditSender auditSender;
    @Mock
    private AuditDescriptionContext descriptionContext;
    @Captor
    private ArgumentCaptor<List<AuditMessage>> auditMessageCaptor;
    @Mock
    private UserContextUtil userContextUtil;
    @InjectMocks
    private AuditAspect auditAspect;
    private List<String> messageIds = new ArrayList<>();

    @Before
    public void setUp() throws NoSuchMethodException {
        when(descriptionContext.getInitEventDataList(any(ProceedingJoinPoint.class)))
            .thenReturn(singletonList(new AuditMessage().withStatus(
                Status.INITIATED)));
        when(descriptionContext.getSuccessEventDataList(any(ProceedingJoinPoint.class), any()))
            .thenReturn(singletonList(new AuditMessage().withStatus(
                Status.SUCCESSFUL)));
        when(descriptionContext.getFailedEventDataList(any(ProceedingJoinPoint.class)))
            .thenReturn(singletonList(new AuditMessage().withStatus(
                Status.FAILED)));

        messageIds = singletonList(UUID.randomUUID().toString());
        when(descriptionContext.getMessageIds(any(ProceedingJoinPoint.class))).thenReturn(messageIds);
    GetUser userByExternalIdData = new GetUser();
        userByExternalIdData.setId(USER_ID);
        userByExternalIdData.setExternalId(LOGGED_USERNAME);

        mockGetUserByExternalId(LOGGED_USERNAME, userByExternalIdData);
        mockAuditEventParameters();
    }

    @Test
    public void shouldPerformAuditingWhenSuccessfulActionIsPerformed() throws Throwable {

        mockGetAuthenticatedUserName(Optional.of(LOGGED_USERNAME));
        when(userContextUtil.getServiceAgreementId()).thenReturn(SERVICE_AGREEMENT_ID);

        when(proceedingJoinPoint.proceed()).thenReturn(new Object());

        auditAspect.performAuditing(proceedingJoinPoint);

        verify(auditSender, times(2)).sendAuditMessages(auditMessageCaptor.capture());

        performAsserts(singletonList(Status.SUCCESSFUL), LOGGED_USERNAME, USER_ID);
    }


    @Test
    public void shouldPerformAuditingWhenNotSuccessfulActionIsPerformed() throws Throwable {

        mockGetAuthenticatedUserName(Optional.empty());

        when(proceedingJoinPoint.proceed()).thenThrow(new Exception());

        try {
            auditAspect.performAuditing(proceedingJoinPoint);
        } catch (Exception e) {

        }
        verify(auditSender, times(2)).sendAuditMessages(auditMessageCaptor.capture());

        performAsserts((singletonList(Status.FAILED)), "anonymous", "anonymous");
    }

    @Test
    public void shouldPerformAuditingWhenNotSuccessfulActionIsPerformedWithForbiddenException() throws Throwable {

        mockGetAuthenticatedUserName(Optional.empty());
        List<Error> errors = new ArrayList<>();
        errors.add(new Error().withMessage("error").withKey("403"));
        AuditMessage auditMessage = new AuditMessage();
        auditMessage.withStatus(Status.FAILED);
        List<AuditMessage> eventList = singletonList(auditMessage);
        when(descriptionContext.getFailedEventDataList(any())).thenReturn(eventList);

        when(proceedingJoinPoint.proceed()).thenThrow(new ForbiddenException());

        try {
            auditAspect.performAuditing(proceedingJoinPoint);
        } catch (ForbiddenException e) {

        }
        verify(auditSender, times(2)).sendAuditMessages(auditMessageCaptor.capture());

        performAsserts((singletonList(Status.FAILED)), "anonymous", "anonymous");
    }

    @Test
    public void shouldPerformAuditingWhenNotSuccessfulActionIsPerformedWithBadRequest() throws Throwable {
        mockGetAuthenticatedUserName(Optional.empty());

        List<Error> errors = new ArrayList<>();
        errors.add(new Error().withMessage("error"));
        when(proceedingJoinPoint.proceed()).thenThrow(new BadRequestException().withErrors(errors));

        try {
            auditAspect.performAuditing(proceedingJoinPoint);
        } catch (BadRequestException e) {
            assertEquals(e.getErrors().get(0).getMessage(), errors.get(0).getMessage());

        }
        verify(auditSender, times(2)).sendAuditMessages(auditMessageCaptor.capture());

        performAsserts((singletonList(Status.FAILED)), "anonymous", "anonymous");
    }

    @Test
    public void shouldPerformAuditingWhenNotSuccessfulActionIsPerformedWithNotFoundException() throws Throwable {

        mockGetAuthenticatedUserName(Optional.empty());

        when(proceedingJoinPoint.proceed()).thenThrow(new NotFoundException());

        try {
            auditAspect.performAuditing(proceedingJoinPoint);
        } catch (NotFoundException e) {

        }
        verify(auditSender, times(2)).sendAuditMessages(auditMessageCaptor.capture());

        performAsserts((singletonList(Status.FAILED)), "anonymous", "anonymous");
    }

    private void performAsserts(List<Status> status, String loggedUser, String userId) {

        List<AuditMessage> initiatedMassages = auditMessageCaptor.getAllValues().get(0);
        for (int i = 0; i < initiatedMassages.size(); i++) {
            assertEquals(Status.INITIATED, initiatedMassages.get(i).getStatus());
            assertEquals(loggedUser, initiatedMassages.get(i).getUsername());
            assertEquals(userId, initiatedMassages.get(i).getUserId());
            assertEquals(EventAction.CREATE.getActionEvent(), initiatedMassages.get(i).getEventAction());
            assertEquals(EVENT_CATEGORY, initiatedMassages.get(i).getEventCategory());
            assertEquals(messageIds.get(i), initiatedMassages.get(i).getMessageSetId());
        }

        List<AuditMessage> auditMessages = auditMessageCaptor.getAllValues().get(1);
        for (int i = 0; i < initiatedMassages.size(); i++) {
            assertEquals(status.get(i), auditMessages.get(i).getStatus());
            assertEquals(loggedUser, auditMessages.get(i).getUsername());
            assertEquals(userId, auditMessages.get(i).getUserId());
            assertEquals(EVENT_CATEGORY, auditMessages.get(i).getEventCategory());
            assertEquals(messageIds.get(i), auditMessages.get(i).getMessageSetId());
        }
    }

    private void mockAuditEventParameters() throws NoSuchMethodException {
        MethodSignature signature = mock(MethodSignature.class);
        when(proceedingJoinPoint.getSignature())
            .thenReturn(signature);
        Method method = AuditAspectTest.class
            .getMethod("testMethodForAudit", DataGroupBase.class);
        when(signature.getMethod())
            .thenReturn(method);
    }

    @AuditEvent(eventAction = EventAction.CREATE, objectType = AuditObjectType.DATA_GROUP)
    public DataGroupsPostResponseBody testMethodForAudit(DataGroupBase dataGroupBase) {
        return new DataGroupsPostResponseBody().withId("id");
    }

    private void mockGetAuthenticatedUserName(Optional<String> loggedUserOptional) {
        when(userContextUtil.getOptionalAuthenticatedUserName()).thenReturn(loggedUserOptional);
    }

    private void mockGetUserByExternalId(String loggedUser,
        GetUser userByExternalIdGetResponseBody) {
        when(userManagementService.getUserByExternalId(eq(loggedUser)))
            .thenReturn(userByExternalIdGetResponseBody);
    }

}
