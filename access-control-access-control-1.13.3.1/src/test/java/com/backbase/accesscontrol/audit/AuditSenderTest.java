package com.backbase.accesscontrol.audit;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.backbase.audit.client.AuditClient;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequestContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AuditSenderTest {

    private static final int CHUNK_SIZE = 100;
    @Mock
    private AuditClient auditClient;
    @Mock
    private InternalRequestContext internalRequestContext;

    private AuditSender auditSender;

    @Before
    public void setUp() throws Exception {

        auditSender = new AuditSender(auditClient, internalRequestContext);
        ReflectionTestUtils.setField(auditSender, "chunkSize", CHUNK_SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThatExceptionIsThrownFroNullInputParam() {
        auditSender.sendAuditMessages(null);
    }

    @Test
    public void testThatAuditClientIsCalledOnceWhenListIsSmallerThenChunkSize() {

        List<AuditMessage> auditMessages = Collections.singletonList(new AuditMessage());

        auditSender.sendAuditMessages(auditMessages);
        verify(auditClient, times(1))
            .audit(auditMessages, internalRequestContext);
    }

    @Test
    public void testThatAuditClientIsCalledOnceWithSmallChunk() {

        List<AuditMessage> auditMessages = new ArrayList<>();
        for (int i = 0; i < CHUNK_SIZE + 1; i++) {
            auditMessages.add(new AuditMessage());
        }

        auditSender.sendAuditMessages(auditMessages);
        verify(auditClient, times(2))
            .audit(anyList(), eq(internalRequestContext));
    }
}