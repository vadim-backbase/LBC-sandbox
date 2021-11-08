package com.backbase.accesscontrol.audit;

import com.backbase.audit.client.AuditClient;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Async request sender to the Audit services.
 */
@Service
public class AuditSender {

    private static final String EVENT_AUDITING_FAILED_BAD_REQUEST
        = "Event Auditing was not performed due to Bad Request exception.";
    private static final String EVENT_AUDITING_FAILED_EXCEPTION
        = "Event Auditing was not performed due to an exception.";

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditSender.class);

    private AuditClient auditClient;
    private InternalRequestContext internalRequestContext;

    @Value("${backbase.accessgroup.audit.size}")
    private int chunkSize;

    public AuditSender(AuditClient auditClient, InternalRequestContext internalRequestContext) {
        this.auditClient = auditClient;
        this.internalRequestContext = internalRequestContext;
    }

    /**
     * Sends an audit message to Audit service.
     */
    public void sendAuditMessages(List<AuditMessage> bodyData) {

        Assert.notNull(bodyData, "Audit messages can not be null.");

        try {
            AuditUtils.batches(bodyData, chunkSize).forEach(chunk ->
                auditClient.audit(chunk, internalRequestContext)
            );
        } catch (BadRequestException exception) {
            LOGGER.warn("Invalid request to Audit service", exception);
            LOGGER.info(EVENT_AUDITING_FAILED_BAD_REQUEST);
        } catch (Exception exception) {
            LOGGER.warn("Communication with Audit service failed: {}", exception.getMessage());
            LOGGER.info(EVENT_AUDITING_FAILED_EXCEPTION);
        }
    }


}
