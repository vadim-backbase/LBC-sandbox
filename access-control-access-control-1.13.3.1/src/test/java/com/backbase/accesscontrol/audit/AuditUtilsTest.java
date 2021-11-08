package com.backbase.accesscontrol.audit;

import com.backbase.audit.client.model.AuditMessage;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class AuditUtilsTest {

    private static final int CHUNK_SIZE = 50;

    @Test(expected = IllegalArgumentException.class)
    public void testThatExceptionIsThrownForNullInputParam() {
        AuditUtils.batches(null, CHUNK_SIZE);
    }

    @Test
    public void testThatEmptyStreamIsReturnedForEmptyListOfAuditMessages() {
        Assert.assertFalse(AuditUtils.batches(Collections.emptyList(), CHUNK_SIZE).findFirst().isPresent());
    }

    @Test
    public void testWhenListSizeIsSmallerThenChunkSize() {
        AuditMessage auditMessage1 = new AuditMessage();
        AuditMessage auditMessage2 = new AuditMessage();
        Assert
            .assertEquals(1, AuditUtils.batches(Lists.newArrayList(auditMessage1, auditMessage2), CHUNK_SIZE).count());
    }

    @Test
    public void testWhenListSizeIsBiggerThenChunkSize() {

        List<AuditMessage> auditMessages = new ArrayList<>();
        for (int i = 0; i < CHUNK_SIZE + 1; i++) {
            auditMessages.add(new AuditMessage());
        }

        Assert.assertEquals(2, AuditUtils.batches(auditMessages, CHUNK_SIZE).count());
    }

    @Test
    public void testWhenListSizeIsEqualsWithChunkSize() {

        List<AuditMessage> auditMessages = new ArrayList<>();
        for (int i = 0; i < CHUNK_SIZE; i++) {
            auditMessages.add(new AuditMessage());
        }

        Assert.assertEquals(1, AuditUtils.batches(auditMessages, CHUNK_SIZE).count());
    }

}
