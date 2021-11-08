package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;
import static com.backbase.accesscontrol.util.helpers.LegalEntityUtil.createLegalEntity;
import static com.backbase.accesscontrol.util.helpers.ServiceAgreementUtil.createServiceAgreement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.domain.dto.Permission;
import com.backbase.accesscontrol.domain.dto.PersistenceFunctionGroup;
import com.backbase.accesscontrol.domain.dto.PrivilegeDto;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestFunctionGroupTransformServiceImplTest {

    @InjectMocks
    private IngestFunctionGroupTransformServiceImpl ingestFunctionGroupTransformService;
    @Mock
    private FunctionGroupService functionGroupService;
    @Mock
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    @Test
    public void shouldThrowExceptionWhenServiceAgreementNotExists() {
        when(serviceAgreementJpaRepository.findByExternalId("notExisting")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> ingestFunctionGroupTransformService
                .addFunctionGroup(new FunctionGroupIngest().withExternalServiceAgreementId("notExisting")));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode()));
    }

    @Test
    public void shouldTransformPayloadAndPersistFunctionGroup() {

        ServiceAgreement serviceAgreement = createServiceAgreement(null,
            "exists",
            null,
            createLegalEntity("le_id", null, null, null, null),
            null,
            null);
        serviceAgreement.setId("sa_id");
        when(serviceAgreementJpaRepository.findByExternalId("exists"))
            .thenReturn(Optional.of(serviceAgreement));

        Date validFrom = new Date();
        Date validUntil = new Date();
        FunctionGroupIngest requestData = new FunctionGroupIngest()
            .withExternalServiceAgreementId("exists")
            .withName("name")
            .withDescription("description")
            .withPermissions(Lists.newArrayList(
                createPersistencePermission("1", new PrivilegeDto().withPrivilege("view"),
                    new PrivilegeDto().withPrivilege("edit")),
                createPersistencePermission("2", new PrivilegeDto().withPrivilege("execute"))
            ))
            .withType(PersistenceFunctionGroup.Type.DEFAULT)
            .withValidFrom(validFrom)
            .withValidUntil(validUntil);

        when(functionGroupService.addFunctionGroup(new FunctionGroupBase()
                .withName(requestData.getName())
                .withServiceAgreementId(serviceAgreement.getId())
                .withDescription(requestData.getDescription())
                .withPermissions(requestData.getPermissions())
                .withType(requestData.getType())
                .withValidFrom(requestData.getValidFrom())
                .withValidUntil(requestData.getValidUntil())
                .withApsName(requestData.getApsName())
                .withApsId(requestData.getApsId())
            )
        )
            .thenReturn("fg_id");

        String functionGroup = ingestFunctionGroupTransformService.addFunctionGroup(requestData);
        assertThat(functionGroup, is("fg_id"));
    }

    private Permission createPermission(String functionId, String... privileges) {
        return new Permission().withFunctionId(functionId)
            .withAssignedPrivileges(createPrivileges(privileges));
    }

    private List<PrivilegeDto> createPrivileges(String... privileges) {
        return Stream.of(privileges)
            .map(p -> new PrivilegeDto().withPrivilege(p))
            .collect(Collectors.toList());
    }

    private Permission createPersistencePermission(String functionId, PrivilegeDto... privileges) {
        return new Permission().withFunctionId(functionId)
            .withAssignedPrivileges(Lists.newArrayList(privileges));
    }
}