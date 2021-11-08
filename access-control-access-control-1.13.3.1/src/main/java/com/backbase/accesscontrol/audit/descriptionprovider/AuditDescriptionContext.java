package com.backbase.accesscontrol.audit.descriptionprovider;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.DescriptionProvider;
import com.backbase.accesscontrol.audit.annotation.AuditEvent;
import com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes;
import com.backbase.audit.client.model.AuditMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditDescriptionContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        AuditDescriptionContext.class);

    private Map<AuditEventAction, DescriptionProvider> allProviders;

    /**
     * Injects all available descriptor providers.
     */
    public AuditDescriptionContext(List<DescriptionProvider> allDescriptionProviders) {
        allProviders = new HashMap<>();
        for (DescriptionProvider descriptionProvider : allDescriptionProviders) {
            allProviders.put(descriptionProvider.getAuditEventAction(), descriptionProvider);
        }
    }

    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        return getDescriptionProvider(joinPoint).getInitEventDataList(joinPoint);
    }

    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        return getDescriptionProvider(joinPoint).getSuccessEventDataList(joinPoint, actionResult);
    }

    public List<String> getMessageIds(ProceedingJoinPoint joinPoint) {
        return getDescriptionProvider(joinPoint).getMessageIds(joinPoint);
    }

    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        return getDescriptionProvider(joinPoint).getFailedEventDataList(joinPoint);
    }

    /**
     * Returns the appropriate description provider from audit event action.
     *
     * @param auditEventAction - audit event action containing the object type and event action
     * @return descriptionProvider
     */
    private DescriptionProvider getDescriptionProvider(AuditEventAction auditEventAction) {
        DescriptionProvider descriptionProvider = allProviders.get(auditEventAction);
        if (descriptionProvider == null) {
            LOGGER.warn("Descriptor for Audit is not provided");
            throw getInternalServerErrorException(AccessGroupErrorCodes.ERR_AG_068.getErrorMessage());
        }
        return descriptionProvider;
    }

    private DescriptionProvider getDescriptionProvider(ProceedingJoinPoint joinPoint) {
        AuditEventAction auditEventAction = getAuditEventAction(joinPoint);
        return getDescriptionProvider(auditEventAction);
    }

    /**
     * Returns Audit event action from the method being called containing the appropriate event action and object type.
     *
     * @param joinPoint - proceeding join point
     * @return AuditEventAction
     */
    private AuditEventAction getAuditEventAction(ProceedingJoinPoint joinPoint) {
        AuditEvent auditEvent = ((MethodSignature) joinPoint.getSignature()).getMethod()
            .getAnnotation(AuditEvent.class);
        return new AuditEventAction()
            .withEventAction(auditEvent.eventAction())
            .withObjectType(auditEvent.objectType());
    }
}
