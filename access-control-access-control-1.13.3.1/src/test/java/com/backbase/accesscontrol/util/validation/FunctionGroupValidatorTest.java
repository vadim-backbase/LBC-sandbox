package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_115;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_003;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.FunctionGroupItem;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.matchers.NotFoundErrorMatcher;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FunctionGroupValidatorTest {

    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @InjectMocks
    private FunctionGroupValidator functionGroupValidator;

    @Test
    void validateFunctionGroupAssignedWithPrivilegesShouldSkipValidationWhenNoPrivilegesProvided() {
        functionGroupValidator.validateFunctionGroupAssignedWithPrivileges("fgId", Collections.emptyList());

        verifyNoInteractions(functionGroupJpaRepository);
    }

    @Test
    void validateFunctionGroupAssignedWithPrivileges() {
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afpId1");

        FunctionGroupItem item1 = new FunctionGroupItem("afpId1");
        FunctionGroupItem item2 = new FunctionGroupItem("afpId2");
        FunctionGroup functionGroup = mock(FunctionGroup.class);
        when(functionGroup.getPermissions()).thenReturn(Set.of(item1, item2));

        when(functionGroupJpaRepository.findByIdWithPermissions("fgId")).thenReturn(Optional.of(functionGroup));

        functionGroupValidator
            .validateFunctionGroupAssignedWithPrivileges("fgId", List.of(applicableFunctionPrivilege));

        verify(functionGroupJpaRepository).findByIdWithPermissions("fgId");
    }

    @Test
    void validateFunctionGroupAssignedWithPrivilegesShouldThrowNotFoundException() {
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afpId1");

        when(functionGroupJpaRepository.findByIdWithPermissions("fgId")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> functionGroupValidator
            .validateFunctionGroupAssignedWithPrivileges("fgId", List.of(applicableFunctionPrivilege)));

        assertThat(exception, new NotFoundErrorMatcher(ERR_ACQ_003.getErrorMessage(), ERR_ACQ_003.getErrorCode()));
    }

    @Test
    void validateFunctionGroupAssignedWithPrivilegesShouldThrowBadRequest() {
        ApplicableFunctionPrivilege applicableFunctionPrivilege = new ApplicableFunctionPrivilege();
        applicableFunctionPrivilege.setId("afpId1");
        applicableFunctionPrivilege.setPrivilegeName("approve");
        applicableFunctionPrivilege.setBusinessFunctionName("Manage Data Groups");

        FunctionGroupItem item2 = new FunctionGroupItem("afpId2");
        FunctionGroupItem item3 = new FunctionGroupItem("afpId3");
        FunctionGroup functionGroup = mock(FunctionGroup.class);
        when(functionGroup.getPermissions()).thenReturn(Set.of(item2, item3));

        when(functionGroupJpaRepository.findByIdWithPermissions("fgId")).thenReturn(Optional.of(functionGroup));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> functionGroupValidator
            .validateFunctionGroupAssignedWithPrivileges("fgId", List.of(applicableFunctionPrivilege)));

        String errorMessage = "Business Function 'Manage Data Groups' with privilege 'approve' not assigned to functionGroup";
        assertThat(exception, new BadRequestErrorMatcher(errorMessage, ERR_ACC_115.getErrorCode()));
    }
}