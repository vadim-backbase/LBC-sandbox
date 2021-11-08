package com.backbase.accesscontrol.business.service.approvers;

import static com.backbase.accesscontrol.util.ExceptionUtil.getInternalServerErrorException;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApproverFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApproverFactory.class);

    private Approver defaultApprover;
    private Map<ApproverKey, Approver> approvers;

    /**
     * ApproverFactory constructor.
     *
     * @param approvers list of Approver
     */
    public ApproverFactory(List<Approver> approvers) {
        this.defaultApprover = approvers.stream()
            .filter(approver -> isNull(approver.getKey()))
            .findFirst()
            .orElseThrow(() -> {
                LOGGER.error("Failed extracting approver factory. Function not existing");
                return getInternalServerErrorException("Error");
            });

        this.approvers = approvers.stream()
            .filter(approver -> nonNull(approver.getKey()))
            .collect(toMap(Approver::getKey, approver -> approver));
    }

    /**
     * Retrieve approver item based on ApproverKey's function and action.
     * @param key ApproverKey
     * @return {@link Approver}
     */
    public Approver getApprover(ApproverKey key) {
        if (approvers.containsKey(key)) {
            return approvers.get(key);
        } else {
            return defaultApprover;
        }
    }
}
