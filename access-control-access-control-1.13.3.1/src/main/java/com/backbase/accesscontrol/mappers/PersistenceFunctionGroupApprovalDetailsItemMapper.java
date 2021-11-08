package com.backbase.accesscontrol.mappers;

import static java.util.Objects.isNull;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.ApprovalFunctionGroupRef;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.domain.enums.ApprovalAction;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Approve;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Cancel;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Create;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Delete;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Edit;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.Execute;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.OldNewPrivileges;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrixAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationApprovalAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupApprovalDetailsItem;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.View;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PersistenceFunctionGroupApprovalDetailsItemMapper {

    protected BusinessFunctionCache businessFunctionCache;

    protected FunctionGroupMapperPersistence functionGroupMapperPersistence;

    protected FunctionGroupMapper  functionGroupMapper;

    @Autowired
    public PersistenceFunctionGroupApprovalDetailsItemMapper(
        BusinessFunctionCache businessFunctionCache,
        FunctionGroupMapperPersistence functionGroupMapperPersistence,
        FunctionGroupMapper  functionGroupMapper) {
        this.businessFunctionCache = businessFunctionCache;
        this.functionGroupMapperPersistence = functionGroupMapperPersistence;
        this.functionGroupMapper = functionGroupMapper;
    }

    /**
     * Generates PersistenceFunctionGroupApprovalDetailsItem object from exsisting function group and new
     * ApprovalFunctionGroup.
     *
     * @param functionGroup old function group present in database
     * @param approvalFunctionGroupRef function group waiting for approval
     * @param serviceAgreementName service agreement name
     * @return {@link PresentationFunctionGroupApprovalDetailsItem}
     */
    public PresentationFunctionGroupApprovalDetailsItem getResult(FunctionGroup functionGroup,
        ApprovalFunctionGroupRef approvalFunctionGroupRef, String serviceAgreementName) {
        PresentationFunctionGroupApprovalDetailsItem result;

        PresentationFunctionGroupState newState = null;
        PresentationFunctionGroupState oldState = null;
        ApprovalFunctionGroup approvalFunctionGroup = null;

        if (!approvalFunctionGroupRef.getApprovalAction().equals(ApprovalAction.DELETE)) {
            approvalFunctionGroup = (ApprovalFunctionGroup) approvalFunctionGroupRef;
        }

        result = new PresentationFunctionGroupApprovalDetailsItem();
        if (approvalFunctionGroupRef.getApprovalAction().equals(ApprovalAction.CREATE)) {
            result = functionGroupMapperPersistence.createFromApprovalFunctionGroup(approvalFunctionGroup);
            result.setAction(PresentationApprovalAction.CREATE);
            newState = functionGroupMapper.createFromApprovalFunctionGroupNewState(approvalFunctionGroup);
            result.setOldState(newState);
        } else if (approvalFunctionGroupRef.getApprovalAction().equals(ApprovalAction.DELETE)) {
            result = functionGroupMapperPersistence.createFromFunctionGroup(functionGroup);
            result.setAction(PresentationApprovalAction.DELETE);
            oldState = functionGroupMapper.createFromFunctionGroupOldState(functionGroup);
            result.setNewState(oldState);
            result.setApprovalId(approvalFunctionGroupRef.getApprovalId());
        } else if (approvalFunctionGroupRef.getApprovalAction().equals(ApprovalAction.EDIT)) {
            result = functionGroupMapperPersistence.createFromApprovalFunctionGroup(approvalFunctionGroup);
            result.setAction(PresentationApprovalAction.EDIT);
            newState = functionGroupMapper.createFromApprovalFunctionGroupNewState(approvalFunctionGroup);
            oldState = functionGroupMapper.createFromFunctionGroupOldState(functionGroup);
        }
        result.setOldState(oldState);
        result.setNewState(newState);
        result.setServiceAgreementName(serviceAgreementName);
        createConversationMatrix(functionGroup, approvalFunctionGroup, result);

        return result;
    }

    private void createConversationMatrix(FunctionGroup functionGroup, ApprovalFunctionGroup approvalFunctionGroup,
        PresentationFunctionGroupApprovalDetailsItem result) {
        Map<String, Map<String, ApplicableFunctionPrivilegeWithOrigin>> businessFunctionIdsWithAlps = new HashMap<>();
        if (!isNull(functionGroup)) {
            functionGroup.getPermissions().forEach(
                e -> createPermissionMatrix(businessFunctionIdsWithAlps, e.getApplicableFunctionPrivilegeId(), false));
        }
        if (!isNull(approvalFunctionGroup)) {
            approvalFunctionGroup.getPrivileges()
                .forEach(e -> createPermissionMatrix(businessFunctionIdsWithAlps, e, true));
        }
        result.setPermissionMatrix(convertToPermissionMatrix(businessFunctionIdsWithAlps));
    }


    private void createPermissionMatrix(
        Map<String, Map<String, ApplicableFunctionPrivilegeWithOrigin>> businessFunctionIdsWithAlps, String afpId,
        boolean isNew) {
        ApplicableFunctionPrivilegeWithOrigin afp = new ApplicableFunctionPrivilegeWithOrigin(businessFunctionCache
            .getApplicableFunctionPrivilegeById(afpId));
        if (!businessFunctionIdsWithAlps.containsKey(afp.getBusinessFunction().getId())) {
            Map<String, ApplicableFunctionPrivilegeWithOrigin> originMap = new HashMap<>();
            setAfpStateOrigin(isNew, afp);
            originMap.put(afp.getId(), afp);
            businessFunctionIdsWithAlps.put(afp.getBusinessFunction().getId(), originMap);
        } else {
            Map<String, ApplicableFunctionPrivilegeWithOrigin> originMap = businessFunctionIdsWithAlps
                .get(afp.getBusinessFunction().getId());
            if (originMap.containsKey(afp.getId())) {
                ApplicableFunctionPrivilegeWithOrigin obj = originMap.get(afp.getId());
                setAfpStateOrigin(isNew, obj);
                originMap.put(afp.getId(), obj);
                businessFunctionIdsWithAlps.put(obj.getBusinessFunction().getId(), originMap);
            } else {
                Map<String, ApplicableFunctionPrivilegeWithOrigin> newOriginMap = businessFunctionIdsWithAlps
                    .get(afp.getBusinessFunction().getId());
                setAfpStateOrigin(isNew, afp);
                newOriginMap.put(afp.getId(), afp);
                businessFunctionIdsWithAlps.put(afp.getBusinessFunction().getId(), newOriginMap);
            }
        }
    }

    private void setAfpStateOrigin(boolean isNew, ApplicableFunctionPrivilegeWithOrigin afp) {
        if (isNew) {
            afp.setNewAfp(true);
        } else {
            afp.setOldAfp(true);
        }
    }

    private List<PermissionMatrix> convertToPermissionMatrix(
        Map<String, Map<String, ApplicableFunctionPrivilegeWithOrigin>> businessFunctionIdsWithAfps) {
        List<PermissionMatrix> resultMatrix = new ArrayList<>();
        businessFunctionIdsWithAfps.forEach((key, value) -> {
            final BusinessFunction[] businessFunction = {null};
            PermissionMatrixActionDeterminator determinator = new PermissionMatrixActionDeterminator();
            OldNewPrivileges oldNewPrivileges = new OldNewPrivileges();
            value.forEach((key1, value1) -> {
                if (isNull(businessFunction[0])) {
                    businessFunction[0] = value1.getBusinessFunction();
                }
                switch (value1.getPrivilegeName()) {
                    case "view":
                        oldNewPrivileges
                            .withView(new View().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "create":
                        oldNewPrivileges
                            .withCreate(new Create().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "edit":
                        oldNewPrivileges
                            .withEdit(new Edit().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "delete":
                        oldNewPrivileges
                            .withDelete(new Delete().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "approve":
                        oldNewPrivileges
                            .withApprove(new Approve().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "cancel":
                        oldNewPrivileges
                            .withCancel(new Cancel().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    case "execute":
                        oldNewPrivileges
                            .withExecute(new Execute().withOld(value1.getOldAfpSafe()).withNew(value1.getNewAfpSafe()));
                        determinator.validate(value1.getOldAfp(), value1.getNewAfp());
                        break;
                    default:
                        break;
                }
            });
            List<String> applicableFunctionPrivilegies = businessFunctionCache
                .getApplicableFunctionPrivilegeByBusinessFunctionId(key).stream()
                .map(ApplicableFunctionPrivilege::getPrivilegeName).collect(Collectors.toList());
            createOldNewPrivilegesSetup(oldNewPrivileges, applicableFunctionPrivilegies, determinator.getAction());
            PermissionMatrix permissions;
            permissions = functionGroupMapper
                .toPermissionMatrixDetailsFromBusinessFunction(businessFunction[0]);
            permissions.setPrivileges(oldNewPrivileges);
            permissions.setAction(determinator.getAction());
            resultMatrix.add(permissions);
        });
        return resultMatrix;
    }

    private void createOldNewPrivilegesSetup(OldNewPrivileges oldNewPrivileges,
        List<String> applicableFunctionPrivilegies,
        PermissionMatrixAction action) {
        for (String applicableFunctionPrivilegy : applicableFunctionPrivilegies) {
            switch (applicableFunctionPrivilegy) {
                case "view":
                    createViewObject(oldNewPrivileges, action);
                    break;
                case "create":
                    createCreateObject(oldNewPrivileges, action);
                    break;
                case "edit":
                    createEditObject(oldNewPrivileges, action);
                    break;
                case "delete":
                    createDeleteObject(oldNewPrivileges, action);
                    break;
                case "approve":
                    createApproveObject(oldNewPrivileges, action);
                    break;
                case "cancel":
                    createCancelObject(oldNewPrivileges, action);
                    break;
                case "execute":
                    createExecuteObject(oldNewPrivileges, action);
                    break;
                default:
                    break;
            }
        }
    }

    private void createExecuteObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getExecute())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withExecute(new Execute().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withExecute(new Execute().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withExecute(new Execute().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getExecute().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getExecute().setNew(null);
            }
        }
    }

    private void createCancelObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getCancel())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withCancel(new Cancel().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withCancel(new Cancel().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withCancel(new Cancel().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getCancel().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getCancel().setNew(null);
            }
        }
    }

    private void createApproveObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getApprove())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withApprove(new Approve().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withApprove(new Approve().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withApprove(new Approve().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getApprove().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getApprove().setNew(null);
            }
        }
    }

    private void createDeleteObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getDelete())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withDelete(new Delete().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withDelete(new Delete().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withDelete(new Delete().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getDelete().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getDelete().setNew(null);
            }
        }
    }

    private void createEditObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getEdit())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withEdit(new Edit().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withEdit(new Edit().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withEdit(new Edit().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getEdit().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getEdit().setNew(null);
            }
        }
    }

    private void createCreateObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getCreate())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withCreate(new Create().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withCreate(new Create().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withCreate(new Create().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getCreate().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getCreate().setNew(null);
            }
        }
    }

    private void createViewObject(OldNewPrivileges oldNewPrivileges, PermissionMatrixAction action) {
        if (isNull(oldNewPrivileges.getView())) {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .withView(new View().withNew(false).withOld(null));
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .withView(new View().withOld(false).withNew(null));
            } else {
                oldNewPrivileges
                    .withView(new View().withOld(false).withNew(false));
            }
        } else {
            if (action.equals(PermissionMatrixAction.ADDED)) {
                oldNewPrivileges
                    .getView().setOld(null);
            } else if (action.equals(PermissionMatrixAction.REMOVED)) {
                oldNewPrivileges
                    .getView().setNew(null);
            }
        }
    }

    private class ApplicableFunctionPrivilegeWithOrigin {

        private ApplicableFunctionPrivilege afp;
        private Boolean newAfp;
        private Boolean oldAfp;

        public ApplicableFunctionPrivilegeWithOrigin(ApplicableFunctionPrivilege afp) {
            this.afp = afp;
        }

        public Boolean getNewAfp() {
            return newAfp;
        }

        public Boolean getNewAfpSafe() {
            return isNull(newAfp) ? false : true;
        }

        public void setNewAfp(Boolean newAfp) {
            this.newAfp = newAfp;
        }

        public Boolean getOldAfp() {
            return oldAfp;
        }

        public Boolean getOldAfpSafe() {
            return isNull(oldAfp) ? false : true;
        }

        public void setOldAfp(Boolean oldAfp) {
            this.oldAfp = oldAfp;
        }

        public String getId() {
            return afp.getId();
        }


        public BusinessFunction getBusinessFunction() {
            return afp.getBusinessFunction();
        }

        public Privilege getPrivilege() {
            return afp.getPrivilege();
        }

        public boolean isSupportsLimit() {
            return afp.isSupportsLimit();
        }

        public String getBusinessFunctionName() {
            return afp.getBusinessFunctionName();
        }

        public String getBusinessFunctionResourceName() {
            return afp.getBusinessFunctionResourceName();
        }

        public String getPrivilegeName() {
            return afp.getPrivilegeName();
        }


        public boolean isUpdated() {
            return oldAfp && newAfp;
        }
    }

    @Getter
    @Setter
    private class PermissionMatrixActionDeterminator {

        int newCounter = 0;
        int oldCounter = 0;
        boolean chnaged = false;


        public void validate(Boolean oldV, Boolean newV) {
            if (Objects.nonNull(oldV) && oldV) {
                oldCounter++;
            }
            if (Objects.nonNull(newV) && newV) {
                newCounter++;
            }
            if (!Objects.equals(oldV, newV)) {
                chnaged = true;
            }
        }

        public PermissionMatrixAction getAction() {
            if (oldCounter == 0) {
                return PermissionMatrixAction.ADDED;
            }
            if (newCounter == 0) {
                return PermissionMatrixAction.REMOVED;
            }
            if (chnaged) {
                return PermissionMatrixAction.CHANGED;
            }
            return PermissionMatrixAction.UNCHANGED;

        }

    }
}

