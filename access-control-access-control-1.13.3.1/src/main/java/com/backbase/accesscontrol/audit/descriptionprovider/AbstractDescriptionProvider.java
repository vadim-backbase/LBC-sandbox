package com.backbase.accesscontrol.audit.descriptionprovider;

import static java.util.Collections.singletonList;

import com.backbase.accesscontrol.audit.DescriptionProvider;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Provides methods for getting specific arguments and path parameters from request.
 */
public abstract class AbstractDescriptionProvider implements DescriptionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        return singletonList(new AuditMessage().withStatus(Status.INITIATED));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        return singletonList(new AuditMessage().withStatus(Status.FAILED));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        return singletonList(new AuditMessage().withStatus(Status.SUCCESSFUL));
    }

}
