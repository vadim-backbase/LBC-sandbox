package com.backbase.accesscontrol.audit;

import com.backbase.accesscontrol.util.CommonUtils;
import com.backbase.audit.client.model.AuditMessage;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditUtils {

    /**
     * Splits list of messages into chunks.
     *
     * @param source    list of audit messages
     * @param chunkSize Size of the chunk.
     * @return Stream of chunks.
     */
    public static Stream<List<AuditMessage>> batches(List<AuditMessage> source, int chunkSize) {

        Assert.notNull(source, "List of audit message can not be null.");

        return CommonUtils.getBatchRequestOnChunks(source, chunkSize);
    }

}
