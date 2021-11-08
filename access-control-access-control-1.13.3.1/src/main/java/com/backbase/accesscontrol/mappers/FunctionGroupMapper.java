package com.backbase.accesscontrol.mappers;

import com.backbase.accesscontrol.domain.ApprovalFunctionGroup;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.dto.FunctionGroupApprovalBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PermissionMatrix;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.approvals.PresentationFunctionGroupState;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.PresentationPermissionFunctionGroupUpdate;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class FunctionGroupMapper {

    @Autowired
    protected DateTimeService dateTimeService;

    @ValueMapping(source = "REGULAR", target = "DEFAULT")
    @ValueMapping(source = "TEMPLATE", target = "TEMPLATE")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
    public abstract PersistenceFunctionGroup.Type fromPresentationEnumToPersistence(
        PresentationFunctionGroup.Type type);

    @ValueMapping(source = "REGULAR", target = "DEFAULT")
    @ValueMapping(source = "TEMPLATE", target = "TEMPLATE")
    @ValueMapping(source = MappingConstants.ANY_REMAINING, target = MappingConstants.NULL)
    public abstract PersistenceFunctionGroup.Type fromPresentationFunctionGroupBaseEnumToPersistence(
        FunctionGroupBase.Type type);

    @ValueMapping(source = "DEFAULT", target = "REGULAR")
    @ValueMapping(source = "TEMPLATE", target = "TEMPLATE")
    @ValueMapping(source = "SYSTEM", target = "SYSTEM")
    public abstract FunctionGroupBase.Type fromPersistenceFunctionGroupBaseEnumToPresentation(
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupBase.Type type);

    @Mapping(target = "validFrom",
        expression =
            "java(dateTimeService.getStartDateFromDateAndTime(item.getValidFromDate(), item.getValidFromTime()))")
    @Mapping(target = "validUntil",
        expression =
            "java(dateTimeService.getEndDateFromDateAndTime(item.getValidUntilDate(), item.getValidUntilTime()))")
    public abstract com.backbase.accesscontrol.domain.dto.FunctionGroupBase
    functionGroupBasePresentationToFunctionGroupBaseDto(FunctionGroupBase item);

    @Mapping(target = "validFromDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidFrom()))")
    @Mapping(target = "validFromTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidFrom()))")
    @Mapping(target = "validUntilDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidUntil()))")
    @Mapping(target = "validUntilTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidUntil()))")
    public abstract FunctionGroupByIdGetResponseBody persistenceFunctionGroupByIdToPresentationFunctionGroupById(
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupByIdGetResponseBody item);


    @Mapping(target = "validFromDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidFrom()))")
    @Mapping(target = "validFromTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidFrom()))")
    @Mapping(target = "validUntilDate",
        expression = "java(dateTimeService.getStringDateFromDate(item.getValidUntil()))")
    @Mapping(target = "validUntilTime",
        expression = "java(dateTimeService.getStringTimeFromDate(item.getValidUntil()))")
    public abstract FunctionGroupsGetResponseBody pandpFunctionGroupsToPresentationFunctionGroups(
        com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupsGetResponseBody item);

    public abstract List<FunctionGroupsGetResponseBody> pandpFunctionGroupsToPresentationFunctionGroups(
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups
            .FunctionGroupsGetResponseBody> body);

    @Mapping(target = "validFrom",
        expression =
            "java(dateTimeService.getStartDateFromDateAndTime(item.getValidFromDate(), item.getValidFromTime()))")
    @Mapping(target = "validUntil",
        expression =
            "java(dateTimeService.getEndDateFromDateAndTime(item.getValidUntilDate(), item.getValidUntilTime()))")
    public abstract FunctionGroupApprovalBase toFunctionGroupCreate(FunctionGroupBase item, String approvalId);

    @Mapping(target = "validFromDate", expression = "java(dateTimeService.getStringDateFromDate(item.getStartDate()))")
    @Mapping(target = "validFromTime", expression = "java(dateTimeService.getStringTimeFromDate(item.getStartDate()))")
    @Mapping(target = "validUntilDate", expression = "java(dateTimeService.getStringDateFromDate(item.getEndDate()))")
    @Mapping(target = "validUntilTime", expression = "java(dateTimeService.getStringTimeFromDate(item.getEndDate()))")
    public abstract PresentationFunctionGroupState createFromApprovalFunctionGroupNewState(
        ApprovalFunctionGroup item);

    @Mapping(target = "validFromDate", expression = "java(dateTimeService.getStringDateFromDate(item.getStartDate()))")
    @Mapping(target = "validFromTime", expression = "java(dateTimeService.getStringTimeFromDate(item.getStartDate()))")
    @Mapping(target = "validUntilDate", expression = "java(dateTimeService.getStringDateFromDate(item.getEndDate()))")
    @Mapping(target = "validUntilTime", expression = "java(dateTimeService.getStringTimeFromDate(item.getEndDate()))")
    public abstract PresentationFunctionGroupState createFromFunctionGroupOldState(
        FunctionGroup item);

    @Mapping(target = "name", source = "functionName")
    @Mapping(target = "functionId", source = "id")
    @Mapping(target = "resource", source = "resourceName")
    public abstract PermissionMatrix toPermissionMatrixDetailsFromBusinessFunction(BusinessFunction businessFunction);

    @Mapping(target = "validFrom",
        expression =
            "java(dateTimeService.getStartDateFromDateAndTime(presentationItem.getFunctionGroup().getValidFromDate(),"
                + " presentationItem.getFunctionGroup().getValidFromTime()))")
    @Mapping(target = "validUntil",
        expression =
            "java(dateTimeService.getEndDateFromDateAndTime(presentationItem.getFunctionGroup().getValidUntilDate(), "
                + "presentationItem.getFunctionGroup().getValidUntilTime()))")
    @Mapping(target = "permissions",
        expression = "java(convertPresentationPermissions(presentationItem.getFunctionGroup().getPermissions()))")
    @Mapping(target = "name", source = "presentationItem.functionGroup.name")
    @Mapping(target = "description", source = "presentationItem.functionGroup.description")
    public abstract com.backbase.accesscontrol.domain.dto.FunctionGroupBase presentationToFunctionGroupBase(
        PresentationFunctionGroupPutRequestBody presentationItem);

    @Mapping(target = "validFrom",
        expression =
            "java(dateTimeService.getStartDateFromDateAndTime(functionGroup.getValidFromDate(),"
                + " functionGroup.getValidFromTime()))")
    @Mapping(target = "validUntil",
        expression =
            "java(dateTimeService.getEndDateFromDateAndTime(functionGroup.getValidUntilDate(),"
                + " functionGroup.getValidUntilTime()))")
    @Mapping(target = "permissions", expression = "java(convertPermissions(functionGroup.getPermissions()))")
    public abstract com.backbase.accesscontrol.domain.dto.FunctionGroupBase presentationToFunctionGroupBase(
        FunctionGroupByIdPutRequestBody functionGroup);

    @Mapping(target = "validFrom",
        expression =
            "java(dateTimeService.getStartDateFromDateAndTime(item.getValidFromDate(), item.getValidFromTime()))")
    @Mapping(target = "validUntil",
        expression =
            "java(dateTimeService.getEndDateFromDateAndTime(item.getValidUntilDate(), item.getValidUntilTime()))")
    @Mapping(target = "permissions", expression = "java(convertPresentationPermission(item.getPermissions()))")
    public abstract FunctionGroupIngest
    presentationFunctionGroupBaseToFunctionGroupIngest(PresentationFunctionGroup item);

    public List<Permission> convertPresentationPermissions(
        List<PresentationPermissionFunctionGroupUpdate> permissions) {
        return Optional.ofNullable(permissions)
            .orElseGet(ArrayList::new)
            .stream().map(permissionFrom -> new Permission()
                .withFunctionId(permissionFrom.getFunctionName())
                .withAssignedPrivileges(permissionFrom.getPrivileges().stream()
                    .map(x -> new PrivilegeDto().withPrivilege(x))
                    .collect(Collectors.toList()))
            ).collect(Collectors.toList());
    }

    public List<Permission> convertPermissions(
        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission> permissions) {
        return Optional.ofNullable(permissions)
            .orElseGet(ArrayList::new)
            .stream().map(permissionFrom -> new Permission()
                .withFunctionId(permissionFrom.getFunctionId())
                .withAssignedPrivileges(permissionFrom.getAssignedPrivileges().stream()
                    .map(x -> new PrivilegeDto().withPrivilege(x.getPrivilege()))
                    .collect(Collectors.toList()))
            ).collect(Collectors.toList());
    }

    public List<Permission> convertPresentationPermission(List<PresentationPermission> permissions) {
        return Optional.ofNullable(permissions)
            .orElseGet(ArrayList::new)
            .stream().map(permissionFrom -> new Permission()
                .withFunctionId(permissionFrom.getFunctionId())
                .withAssignedPrivileges(permissionFrom.getPrivileges().stream()
                    .map(x -> new PrivilegeDto().withPrivilege(x))
                    .collect(Collectors.toList()))
            ).collect(Collectors.toList());
    }
}
