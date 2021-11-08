package com.backbase.accesscontrol.audit;

import static java.util.Collections.singletonList;

import com.backbase.audit.client.model.AuditMessage;
import java.util.List;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;

public interface DescriptionProvider {

    /**
     * Gets the audit event.
     *
     * @return {@link AuditEventAction}
     */
    AuditEventAction getAuditEventAction();

    /**
     * Gets init AuditMessages.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @return list of {@link AuditMessage}
     */
    List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint);

    /**
     * Gets failed AuditMessages.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @return list of {@link AuditMessage}
     */
    List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint);

    /**
     * Gets successful AuditMessages.
     *
     * @param joinPoint    {@link ProceedingJoinPoint}
     * @param actionResult - object of the return value
     * @return list of {@link AuditMessage}
     */
    List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult);

    /**
     * Unique message id.
     *
     * @param joinPoint {@link ProceedingJoinPoint}
     * @return list of message ids
     */
    default List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return singletonList(UUID.randomUUID().toString());
    }
}
