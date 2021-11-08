package com.backbase.accesscontrol.audit;

import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.audit.descriptionprovider.AuditDescriptionContext;
import com.backbase.accesscontrol.business.service.UserManagementService;
import com.backbase.accesscontrol.util.UserContextUtil;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.buildingblocks.presentation.errors.ForbiddenException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import com.backbase.dbs.user.api.client.v2.model.GetUser;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect that sends messages to Audit service.
 */
@Component
@Aspect
@ConditionalOnProperty(name = "backbase.audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditAspect {

    private static final String SENDING_FAILED_MESSAGE_TO_AUDIT_SERVICE = "Sending failed message {} to Audit service";
    private static final String ACCESS_CONTROL = "Access Control";
    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuditAspect.class);
    private static final String ANONYMOUS = "anonymous";
    private static final String ERROR_CODE = "Error code";
    private static final String ERROR_MESSAGE = "Error message";

    private AuditSender auditSender;
    private UserManagementService userManagementService;
    private AuditDescriptionContext descriptionContext;
    private UserContextUtil userContextUtil;

    /**
     * Initialize Audit aspect.
     */
    public AuditAspect(UserManagementService userManagementService, AuditDescriptionContext descriptionContext,
        AuditSender auditSender,
        UserContextUtil userContextUtil) {
        this.auditSender = auditSender;
        this.userManagementService = userManagementService;
        this.descriptionContext = descriptionContext;
        this.userContextUtil = userContextUtil;
    }

    /**
     * Sends audit messages to Audit service first when action is initiated and then again when action is completed or
     * failed. The message contains information about the action and the object type.
     *
     * @param proceedingJoinPoint - join point
     */
    @Around("@annotation(com.backbase.accesscontrol.audit.annotation.AuditEvent)")
    public Object performAuditing(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        List<String> messageIds = new ArrayList<>();
        GetUser user = getUser();
        AuditEvent auditEvent = getAuditEvent(proceedingJoinPoint);

        try {
            messageIds = descriptionContext.getMessageIds(proceedingJoinPoint);
            createAndSendInitiationMessage(proceedingJoinPoint, messageIds, user, auditEvent);
        } catch (Exception exception) {
            LOGGER.warn("Initial audit failed {}", exception.getMessage());
        }

        Object actionResult = performAction(proceedingJoinPoint, messageIds, user, auditEvent);

        createAndSendSuccessfulActionMessage(proceedingJoinPoint, actionResult, messageIds, user, auditEvent);

        return actionResult;
    }

    private com.backbase.dbs.user.api.client.v2.model.GetUser getUser() {
        Optional<String> authenticatedUserName = userContextUtil.getOptionalAuthenticatedUserName();
        if (authenticatedUserName.isPresent()) {
            return userManagementService.getUserByExternalId(authenticatedUserName.get());
        } else {
            GetUser user = new GetUser();
            user.setFullName(ANONYMOUS);
            user.setId(ANONYMOUS);
            user.setExternalId(ANONYMOUS);
            return user;
        }
    }

    /**
     * Creates and sends the message to Audit service, when action is successfully completed.
     *
     * @param messageIds - list of message ids
     * @param user       - logged user
     * @param auditEvent - annotation with parameters about object type and event action
     */
    private void createAndSendSuccessfulActionMessage(ProceedingJoinPoint proceedingJoinPoint, Object actionResult,
        List<String> messageIds, GetUser user, AuditEvent auditEvent) {
        List<AuditMessage> performedActionMessage = createAuditMessage(messageIds,
            auditEvent,
            user,
            getServiceAgreementId(),
            descriptionContext.getSuccessEventDataList(proceedingJoinPoint, actionResult)
        );

        LOGGER.debug("Sending successful message {} to Audit service", performedActionMessage);
        auditSender.sendAuditMessages(performedActionMessage);
    }

    /**
     * Creates and sends the message to Audit service, when action is initiated.
     *
     * @param messageIds - list of message ids
     * @param user       - logged user
     * @param auditEvent - annotation with parameters about object type and event action
     */
    private void createAndSendInitiationMessage(ProceedingJoinPoint proceedingJoinPoint, List<String> messageIds,
        GetUser user, AuditEvent auditEvent) {
        List<AuditMessage> initiateActionMessage = createAuditMessage(messageIds,
            auditEvent,
            user,
            getServiceAgreementId(),
            descriptionContext.getInitEventDataList(proceedingJoinPoint)
        );

        LOGGER.debug("Sending init message {} to Audit service", initiateActionMessage);
        auditSender.sendAuditMessages(initiateActionMessage);
    }

    /**
     * Performs the initiated action and sends message to Audit service if the action fails.
     *
     * @param proceedingJoinPoint - join point
     * @param messageIds          - list of message ids
     * @param user                - logged user
     * @param auditEvent          - annotation with parameters about object type and event action
     */
    private Object performAction(ProceedingJoinPoint proceedingJoinPoint, List<String> messageIds,
        GetUser user, AuditEvent auditEvent) throws Throwable {
        try {
            return proceedingJoinPoint.proceed();
        } catch (ForbiddenException e) {
            auditException(proceedingJoinPoint, e.getErrors(), messageIds, auditEvent, user, "403");
            throw e;
        } catch (NotFoundException e) {
            auditException(proceedingJoinPoint, e.getErrors(), messageIds, auditEvent, user, "404");
            throw e;
        } catch (BadRequestException e) {
            auditException(proceedingJoinPoint, e.getErrors(), messageIds, auditEvent, user, "400");
            throw e;
        } catch (Exception e) {
            auditException(proceedingJoinPoint, Lists.newArrayList(
                new Error().withKey("errorMessage").withMessage(e.getMessage())), messageIds, auditEvent, user, "500");
            throw e;
        }
    }

    private void auditException(ProceedingJoinPoint proceedingJoinPoint, List<Error> errors,
        List<String> messageIds, AuditEvent auditEvent,
        GetUser user, String code) {

        List<AuditMessage> failureAuditMessage = new ArrayList<>();

        try {
            failureAuditMessage = createAuditMessage(
                messageIds,
                auditEvent,
                user,
                getServiceAgreementId(),
                addErrorsToMetaData(descriptionContext.getFailedEventDataList(proceedingJoinPoint),
                    errors, code));
        } catch (Exception exception) {
            LOGGER.warn("Audit exception failed {} ", exception.getMessage());
        }

        LOGGER.debug(SENDING_FAILED_MESSAGE_TO_AUDIT_SERVICE, failureAuditMessage);
        auditSender.sendAuditMessages(failureAuditMessage);
    }

    @SuppressWarnings("squid:S3864")
    private List<AuditMessage> addErrorsToMetaData(List<AuditMessage> failedEventDataList,
        List<Error> errors, String code) {

        return failedEventDataList.stream().peek(event -> {
            event.withEventMetaDatum(ERROR_CODE, code);
            event.withEventMetaDatum(ERROR_MESSAGE, !errors.isEmpty() ? errors.get(0).getMessage() : "");
        }).collect(Collectors.toList());
    }

    /**
     * Returns auditEvent annotation with parameters.
     *
     * @return Audit Event containing all parameters
     */
    private AuditEvent getAuditEvent(ProceedingJoinPoint proceedingJoinPoint) {
        return ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod().getAnnotation(AuditEvent.class);
    }

    /**
     * Creates post body to be send to Audit service.
     *
     * @param messageIds  - list of message ids
     * @param eventAction - action being performed
     * @param user        - user containing username and id
     * @return List containing the Audit message
     */
    private List<AuditMessage> createAuditMessage(
        List<String> messageIds,
        AuditEvent eventAction,
        GetUser user,
        String serviceAgreementId,
        List<AuditMessage> auditMessageList
    ) {

        List<AuditMessage> body = new ArrayList<>();

        for (int i = 0; i < auditMessageList.size(); i++) {
            AuditMessage message = auditMessageList.get(i);
            message
                .withServiceAgreementId(serviceAgreementId)
                .withMessageSetId(getMessageId(messageIds, i))
                .withEventCategory(ACCESS_CONTROL)
                .withObjectType(eventAction.objectType().getObjectTypeName())
                .withEventAction(eventAction.eventAction().getActionEvent())
                .withUsername(user.getExternalId())
                .withUserId(user.getId())
                .withTimestamp(new Date())
                .withStatus(message.getStatus());

            body.add(message);
        }

        return body;
    }

    private String getMessageId(List<String> messageIds, int i) {
        if (messageIds.size() > i) {
            return messageIds.get(i);
        }

        return messageIds.get(0);
    }

    private String getServiceAgreementId() {
        try {
            return userContextUtil.getServiceAgreementId();
        } catch (ForbiddenException forbiddenException) {
            return null;
        }
    }
}