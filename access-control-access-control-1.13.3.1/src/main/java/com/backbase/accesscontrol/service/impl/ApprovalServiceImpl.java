package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_049;

import com.backbase.accesscontrol.domain.AccessControlApproval;
import com.backbase.accesscontrol.domain.ApprovalUserContext;
import com.backbase.accesscontrol.domain.UserContext;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.domain.enums.ApprovalCategory;
import com.backbase.accesscontrol.repository.AccessControlApprovalJpaRepository;
import com.backbase.accesscontrol.repository.ApprovalUserContextJpaRepository;
import com.backbase.accesscontrol.service.ApprovalService;
import com.backbase.accesscontrol.util.PersistenceApprovalPermissionResponseFactory;
import com.backbase.accesscontrol.service.impl.strategy.approval.ApprovalFactory;
import com.backbase.accesscontrol.service.impl.strategy.approval.ApprovalItem;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.PersistenceApprovalPermissions;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@AllArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private AccessControlApprovalJpaRepository accessControlApprovalJpaRepository;
    private ApprovalUserContextJpaRepository approvalUserContextJpaRepository;
    private UserContextService userContextService;
    private ApprovalFactory approvalFactory;
    private PersistenceApprovalPermissionResponseFactory persistenceApprovalPermissionResponseFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void approveApprovalRequest(String approvalId) {
        AccessControlApproval approvalData = accessControlApprovalJpaRepository.findByApprovalId(approvalId)
            .orElseThrow(() -> {
                log.warn("Approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode());
            });

        ApprovalCategory approvalCategory = approvalData.getApprovalCategory();
        ApprovalAction approvalAction = approvalData.getApprovalAction();

        ApprovalItem<AccessControlApproval, String> approvalItem = approvalFactory
            .getApprovalItem(approvalAction, approvalCategory);
        approvalItem.execute(approvalData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void rejectApprovalRequest(String approvalId) {
        AccessControlApproval approvalUserContext = accessControlApprovalJpaRepository.findByApprovalId(approvalId)
            .orElseThrow(() -> {
                log.warn("Approval with id {} does not exist", approvalId);
                return getNotFoundException(ERR_ACQ_049.getErrorMessage(), ERR_ACQ_049.getErrorCode());
            });

        accessControlApprovalJpaRepository.delete(approvalUserContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistenceApprovalPermissions getPersistenceApprovalPermissions(String userId, String serviceAgreementId) {

        log.debug("Trying to get permissions for user {} under service agreement {}.", userId, serviceAgreementId);
        PersistenceApprovalPermissions persistenceApprovalPermissions = new PersistenceApprovalPermissions();

        Optional<UserContext> userContext =
            userContextService.getUserContextByUserIdAndServiceAgreementIdWithFunctionAndDataGroupIds(userId,
                serviceAgreementId);

        if (userContext.isPresent()) {
            log.debug("User access is found for user {} under service agreement {}."
                + " Populating permission details.", userId, serviceAgreementId);
            persistenceApprovalPermissions.setItems(
                userContext.get().getUserAssignedFunctionGroups()
                    .stream()
                    .map(persistenceApprovalPermissionResponseFactory::createPersistenceApprovalPermissions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));

        }

        Optional<ApprovalUserContext> approvalUserContext = approvalUserContextJpaRepository
            .findByUserIdAndServiceAgreementId(userId, serviceAgreementId);
        if (approvalUserContext.isPresent()) {

            String approvalId = approvalUserContext.get().getApprovalId();
            log.debug("There is approval request for user {} under service agreement {} with id {}.",
                userId, serviceAgreementId, approvalId);
            persistenceApprovalPermissions.setApprovalId(approvalId);
        }

        return persistenceApprovalPermissions;
    }
}
