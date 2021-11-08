package com.backbase.accesscontrol.audit.descriptionprovider.permissions;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import com.backbase.accesscontrol.audit.AuditEventAction;
import com.backbase.accesscontrol.audit.AuditObjectType;
import com.backbase.accesscontrol.audit.EventAction;
import com.backbase.accesscontrol.audit.descriptionprovider.AbstractDescriptionProvider;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames;
import com.backbase.accesscontrol.audit.descriptionprovider.DescriptorUtils;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Bound;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationFunctionDataGroupItems;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.SelfApprovalPolicy;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
public class UpdatePermissionsDescriptor extends AbstractDescriptionProvider {

    private static final AuditEventAction auditEventAction = new AuditEventAction()
        .withEventAction(EventAction.UPDATE_PERMISSIONS).withObjectType(AuditObjectType.UPDATE_USER_PERMISSIONS);

    private static final String UPDATE_PERMISSIONS_PREFIX = "Update Permissions";
    private static final String INITIATE_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Initiated | for user %s in service agreement %s";
    private static final String SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Successful | for user %s in service agreement %s";
    private static final String FAILED_UPDATE_PERMISSIONS_DESCRIPTION = UPDATE_PERMISSIONS_PREFIX
        + " | Failed | for user %s in service agreement %s";

    @Override
    public AuditEventAction getAuditEventAction() {
        return auditEventAction;
    }

    private PresentationFunctionDataGroupItems getPresentationFunctionDataGroupItemsPutRequestBody(
        ProceedingJoinPoint joinPoint) {

        return DescriptorUtils.getArgument(joinPoint, PresentationFunctionDataGroupItems.class);
    }

    protected UserContextData getUserContextData(ProceedingJoinPoint joinPoint) {
        return new UserContextData((String) joinPoint.getArgs()[2], (String) joinPoint.getArgs()[3]);
    }

    @Override
    public List<AuditMessage> getInitEventDataList(ProceedingJoinPoint joinPoint) {
        final UserContextData userContextData = getUserContextData(joinPoint);

        return createEventDatumPermissions(joinPoint, Status.INITIATED,
            getInitDescription(userContextData.getUserId(), userContextData.getServiceAgreementId()));
    }

    @Override
    public List<AuditMessage> getSuccessEventDataList(ProceedingJoinPoint joinPoint, Object actionResult) {
        final UserContextData userContextData = getUserContextData(joinPoint);

        return createEventDatumPermissions(joinPoint, Status.SUCCESSFUL,
            getSuccessDescription(userContextData.getUserId(), userContextData.getServiceAgreementId()));
    }

    @Override
    public List<AuditMessage> getFailedEventDataList(ProceedingJoinPoint joinPoint) {
        final UserContextData userContextData = getUserContextData(joinPoint);

        return createEventDatumPermissions(joinPoint, Status.FAILED,
            getFailedDescription(userContextData.getUserId(), userContextData.getServiceAgreementId()));
    }

    private List<AuditMessage> createEventDatumPermissions(ProceedingJoinPoint joinPoint, Status status,
        String description) {
        final UserContextData userContextData = getUserContextData(joinPoint);
        List<PresentationFunctionDataGroup> functionDataGroupItems =
            getPresentationFunctionDataGroupItemsPutRequestBody(joinPoint).getItems();

        if (!functionDataGroupItems.isEmpty()) {
            return functionDataGroupItems.stream()
                .flatMap(functionGroup -> {
                    List<SelfApprovalPolicy> selfApprovalPolicies = functionGroup.getSelfApprovalPolicies();
                    if (functionGroup.getDataGroupIds().isEmpty()) {
                        return Stream.of(
                            getAuditMessage(userContextData, status, description, functionGroup.getFunctionGroupId(),
                                null, selfApprovalPolicies));
                    } else {
                        return functionGroup.getDataGroupIds().stream()
                            .map(dataGroupId -> getAuditMessage(userContextData, status, description,
                                functionGroup.getFunctionGroupId(), dataGroupId.getId(), selfApprovalPolicies));
                    }
                }).collect(toList());
        }
        return Collections.singletonList(getAuditMessage(userContextData, status, description, null, null, null));
    }

    private AuditMessage getAuditMessage(UserContextData userContextData, Status status, String description,
        String functionGroupId, String dataGroupId, List<SelfApprovalPolicy> selfApprovalPolicies) {
        return new AuditMessage()
            .withStatus(status).withEventDescription(description)
            .withEventMetaData(
                createAuditMessageMetadataMap(userContextData, functionGroupId, dataGroupId, selfApprovalPolicies));
    }

    private Map<String, String> createAuditMessageMetadataMap(UserContextData userContextData, String functionGroupId,
        String dataGroupId, List<SelfApprovalPolicy> selfApprovalPolicies) {

        Map<String, String> res = new HashMap<>();
        res.put(DescriptorFieldNames.USER_ID_FIELD_NAME, userContextData.getUserId());
        res.put(DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME, userContextData.getServiceAgreementId());
        if (nonNull(functionGroupId)) {
            res.put(DescriptorFieldNames.FUNCTION_GROUP_ID_FIELD_NAME, functionGroupId);
        }
        if (nonNull(dataGroupId)) {
            res.put(DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME, dataGroupId);
        }
        if (CollectionUtils.isNotEmpty(selfApprovalPolicies)) {
            enrichWithSelfApprovalPolicies(res, selfApprovalPolicies);
        }
        return res;
    }
    
    private void enrichWithSelfApprovalPolicies(Map<String, String> metadata,
        List<SelfApprovalPolicy> selfApprovalPolicies) {
        int counter = 0;
        for (SelfApprovalPolicy policy : selfApprovalPolicies) {
            addSelfApprovalPolicyMetadata(metadata, policy, ++counter);
        }
    }
    
    private void addSelfApprovalPolicyMetadata(Map<String, String> metadata, SelfApprovalPolicy selfApprovalPolicy,
        int number) {
        metadata.put(String.format(DescriptorFieldNames.SAP_ENABLED, number),
            String.valueOf(selfApprovalPolicy.getCanSelfApprove()));
        metadata.put(String.format(DescriptorFieldNames.SAP_BF_NAME, number),
            selfApprovalPolicy.getBusinessFunctionName());
        if (CollectionUtils.isNotEmpty(selfApprovalPolicy.getBounds())) {
            int counter = 0;
            for (Bound bound : selfApprovalPolicy.getBounds()) {
                counter++;
                metadata.put(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_AMOUNT, number, counter),
                    bound.getAmount().toString());
                metadata.put(String.format(DescriptorFieldNames.SAP_BOUND_CURRENCY_CODE, number, counter),
                    bound.getCurrencyCode());
            }
        }
    }

    protected String getInitDescription(String userId, String serviceAgreementId) {
        return String.format(INITIATE_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    protected String getSuccessDescription(String userId, String serviceAgreementId) {
        return String.format(SUCCESSFUL_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    protected String getFailedDescription(String userId, String serviceAgreementId) {
        return String.format(FAILED_UPDATE_PERMISSIONS_DESCRIPTION, userId, serviceAgreementId);
    }

    @AllArgsConstructor
    @Getter
    protected class UserContextData {

        private String serviceAgreementId;
        private String userId;
    }
}
