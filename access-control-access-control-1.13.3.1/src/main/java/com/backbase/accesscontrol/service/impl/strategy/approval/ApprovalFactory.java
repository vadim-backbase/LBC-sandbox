package com.backbase.accesscontrol.service.impl.strategy.approval;


import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApprovalFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalFactory.class);

    private Map<ApprovalType, ApprovalItem<? extends AccessControlApproval, ?>> approvalMap;

    public ApprovalFactory(List<ApprovalItem<? extends AccessControlApproval, ?>> approvalItems) {
        this.approvalMap = approvalItems.stream()
            .collect(Collectors.toMap(ApprovalItem::getKey, ai -> ai));
    }

    /**
     * Retrieve approval item base on type and category.
     *
     * @param approvalAction   approval action
     * @param approvalCategory approval category
     * @return {@link ApprovalItem}
     */
    @SuppressWarnings("squid:S3740")
    public ApprovalItem getApprovalItem(ApprovalAction approvalAction,
        ApprovalCategory approvalCategory) {

        ApprovalType approvalType = new ApprovalType(approvalAction, approvalCategory);
        ApprovalItem<? extends AccessControlApproval, ?> approvalItem = approvalMap.get(approvalType);

        if (Objects.isNull(approvalItem)) {
            LOGGER.error("Invalid approval action/category {} ", approvalType);
            throw getInternalServerErrorException("Invalid approval action/category");
        }

        return approvalItem;
    }
}


