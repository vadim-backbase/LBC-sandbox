package com.backbase.accesscontrol.mappers;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.functiongroups.FunctionGroupsGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Permission;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.function.Privilege;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroup.Type;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationPermission;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FunctionGroupMapperTest {

    @InjectMocks
    private com.backbase.accesscontrol.mappers.FunctionGroupMapperImpl functionGroupMapper;

    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");

    @Test
    public void shouldConvertSuccessfullyFunctionGroupBasePresentationToFunctionGroupBaseDto() {
        FunctionGroupBase functionGroupBasePresentation = new FunctionGroupBase()
            .withName("name")
            .withServiceAgreementId("saId")
            .withDescription("description")
            .withValidFromDate("2019-03-10")
            .withValidFromTime("01:10:05")
            .withValidUntilDate("2019-05-16")
            .withValidUntilTime("10:10:10")
            .withType(FunctionGroupBase.Type.TEMPLATE)
            .withPermissions(
                asList(new Permission().withFunctionId("functionId").withAssignedPrivileges(asList(new Privilege()
                    .withPrivilege("view"), new Privilege().withPrivilege("create")
                ))));

        com.backbase.accesscontrol.domain.dto.FunctionGroupBase functionGroupBasePersistence = functionGroupMapper
            .functionGroupBasePresentationToFunctionGroupBaseDto(functionGroupBasePresentation);

        assertEquals(functionGroupBasePresentation.getName(), functionGroupBasePersistence.getName());
        assertEquals(functionGroupBasePresentation.getServiceAgreementId(),
            functionGroupBasePersistence.getServiceAgreementId());
        assertEquals(functionGroupBasePresentation.getDescription(), functionGroupBasePersistence.getDescription());
        assertEquals(functionGroupBasePresentation.getType().toString(),
            functionGroupBasePersistence.getType().toString());

        List<Permission> permissionsPresentation = functionGroupBasePresentation.getPermissions();
        List<com.backbase.accesscontrol.domain.dto.Permission> permissionsPersistence = functionGroupBasePersistence
            .getPermissions();
        for (int i = 0; i < permissionsPresentation.size(); i++) {
            Permission permissionPresentation = permissionsPresentation.get(i);
            com.backbase.accesscontrol.domain.dto.Permission permissionPersistence = permissionsPersistence
                .get(i);

            assertEquals(permissionPresentation.getFunctionId(),
                permissionPersistence.getFunctionId());

            List<Privilege> assignedPrivileges = permissionPresentation.getAssignedPrivileges();
            for (int j = 0; j < assignedPrivileges.size(); j++) {
                Privilege privilegePresentation = assignedPrivileges.get(j);
                PrivilegeDto privilegePersistence = permissionPersistence
                    .getAssignedPrivileges().get(j);

                assertEquals(privilegePresentation.getPrivilege(), privilegePersistence.getPrivilege());
            }

        }
        assertEquals(dateTimeService.getStartDateFromDateAndTime(functionGroupBasePresentation.getValidFromDate(),
            functionGroupBasePresentation.getValidFromTime()), functionGroupBasePersistence.getValidFrom());
        assertEquals(dateTimeService.getEndDateFromDateAndTime(functionGroupBasePresentation.getValidUntilDate(),
            functionGroupBasePresentation.getValidUntilTime()), functionGroupBasePersistence.getValidUntil());
    }

    @Test
    public void shouldConvertSuccessfullyPresentationFunctionGroupBaseToFunctionGroupIngest() {
        PresentationFunctionGroup presentationFunctionGroup = new PresentationFunctionGroup()
            .withName("name")
            .withExternalServiceAgreementId("externalSaId")
            .withDescription("description")
            .withValidFromDate("2019-03-10")
            .withValidFromTime("01:10:05").withValidUntilDate("2019-05-16")
            .withValidUntilTime("10:10:10")
            .withType(Type.REGULAR)
            .withPermissions(asList(
                new PresentationPermission().withFunctionId("functionId").withPrivileges(asList("view", "create"))));

        FunctionGroupIngest functionGroupIngest = functionGroupMapper
            .presentationFunctionGroupBaseToFunctionGroupIngest(presentationFunctionGroup);

        assertEquals(presentationFunctionGroup.getName(), functionGroupIngest.getName());
        assertEquals(presentationFunctionGroup.getDescription(), functionGroupIngest.getDescription());
        assertEquals(presentationFunctionGroup.getExternalServiceAgreementId(),
            functionGroupIngest.getExternalServiceAgreementId());
        assertEquals("DEFAULT",
            functionGroupIngest.getType().toString());

        List<PresentationPermission> presentationPermissions = presentationFunctionGroup.getPermissions();
        List<com.backbase.accesscontrol.domain.dto.Permission> persistencePermissions = functionGroupIngest
            .getPermissions();
        for (int i = 0; i < presentationPermissions.size(); i++) {
            PresentationPermission presentationPermission = presentationPermissions.get(i);
            com.backbase.accesscontrol.domain.dto.Permission persistencePermission = persistencePermissions
                .get(i);

            assertEquals(presentationPermission.getFunctionId(), persistencePermission.getFunctionId());
            assertEquals(presentationPermission.getPrivileges(), persistencePermission.getAssignedPrivileges()
                .stream().map(x -> x.getPrivilege()).collect(Collectors.toList()));
        }
    }

    @Test
    public void shouldConvertSuccessfullyPersistanceFunctionGroupByIdToPresentationFunctionGroupById() {
        Date validFromDate = new Date(10000);
        Date validUntilDate = new Date(50000);
        FunctionGroupByIdGetResponseBody functionGroupByIdGetResponseBody = new FunctionGroupByIdGetResponseBody()
            .withName("name")
            .withServiceAgreementId("fnId").withDescription("description").withValidFrom(validFromDate)
            .withValidUntil(validUntilDate)
            .withServiceAgreementId("saId")
            .withPermissions(asList(
                new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission()
                    .withFunctionId("functionId").withAssignedPrivileges(asList(
                    new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                        .withPrivilege("view"),
                    new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                        .withPrivilege("create")
                ))));

        com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupByIdGetResponseBody functionGroupByIdGetResponseBodyPresentation = functionGroupMapper
            .persistenceFunctionGroupByIdToPresentationFunctionGroupById(functionGroupByIdGetResponseBody);

        List<Permission> permissionsPresentation = functionGroupByIdGetResponseBodyPresentation.getPermissions();
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission> permissionsPersistence = functionGroupByIdGetResponseBody
            .getPermissions();
        for (int i = 0; i < permissionsPresentation.size(); i++) {
            Permission permissionPresentation = permissionsPresentation.get(i);
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission permissionPersistence = permissionsPersistence
                .get(i);
            assertEquals(permissionPresentation.getFunctionId(),
                permissionPersistence.getFunctionId());

        }

        assertEquals(functionGroupByIdGetResponseBody.getName(),
            functionGroupByIdGetResponseBodyPresentation.getName());
        assertEquals(functionGroupByIdGetResponseBody.getServiceAgreementId(),
            functionGroupByIdGetResponseBody.getServiceAgreementId());
        assertEquals(functionGroupByIdGetResponseBody.getDescription(),
            functionGroupByIdGetResponseBodyPresentation.getDescription());

        assertEquals(DateFormatterUtil.utcFormatDateOnly(validFromDate),
            functionGroupByIdGetResponseBodyPresentation.getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(validFromDate),
            functionGroupByIdGetResponseBodyPresentation.getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(validUntilDate),
            functionGroupByIdGetResponseBodyPresentation.getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(validUntilDate),
            functionGroupByIdGetResponseBodyPresentation.getValidUntilTime());
    }

    @Test
    public void shouldConvertSuccessfullyPersistenceFunctionGroupsToPresentationFunctionGroups() {
        Date validFromDate = new Date(10000);
        Date validUntilDate = new Date(50000);
        FunctionGroupsGetResponseBody functionGroupByIdGetResponseBody = new FunctionGroupsGetResponseBody()
            .withName("name")
            .withServiceAgreementId("fnId").withDescription("description")
            .withValidFrom(validFromDate)
            .withValidUntil(validUntilDate)
            .withServiceAgreementId("saId")
            .withPermissions(asList(
                new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission()
                    .withFunctionId("functionId").withAssignedPrivileges(asList(
                    new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                        .withPrivilege("view"),
                    new com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Privilege()
                        .withPrivilege("create")
                ))));

        List<com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupsGetResponseBody>
            functionGroupByIdGetResponseBodyPresentation = functionGroupMapper
            .pandpFunctionGroupsToPresentationFunctionGroups(asList(functionGroupByIdGetResponseBody));

        List<Permission> permissionsPresentation = functionGroupByIdGetResponseBodyPresentation.get(0).getPermissions();
        List<com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission>
            permissionsPersistence = functionGroupByIdGetResponseBody
            .getPermissions();
        for (int i = 0; i < permissionsPresentation.size(); i++) {
            Permission permissionPresentation = permissionsPresentation.get(i);
            com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.function.Permission
                permissionPersistence = permissionsPersistence
                .get(i);
            assertEquals(permissionPresentation.getFunctionId(),
                permissionPersistence.getFunctionId());

        }

        assertEquals(functionGroupByIdGetResponseBody.getName(),
            functionGroupByIdGetResponseBodyPresentation.get(0).getName());
        assertEquals(functionGroupByIdGetResponseBody.getServiceAgreementId(),
            functionGroupByIdGetResponseBody.getServiceAgreementId());
        assertEquals(functionGroupByIdGetResponseBody.getDescription(),
            functionGroupByIdGetResponseBodyPresentation.get(0).getDescription());

        assertEquals(DateFormatterUtil.utcFormatDateOnly(validFromDate),
            functionGroupByIdGetResponseBodyPresentation.get(0).getValidFromDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(validFromDate),
            functionGroupByIdGetResponseBodyPresentation.get(0).getValidFromTime());
        assertEquals(DateFormatterUtil.utcFormatDateOnly(validUntilDate),
            functionGroupByIdGetResponseBodyPresentation.get(0).getValidUntilDate());
        assertEquals(DateFormatterUtil.utcFormatTimeOnly(validUntilDate),
            functionGroupByIdGetResponseBodyPresentation.get(0).getValidUntilTime());
    }
}
