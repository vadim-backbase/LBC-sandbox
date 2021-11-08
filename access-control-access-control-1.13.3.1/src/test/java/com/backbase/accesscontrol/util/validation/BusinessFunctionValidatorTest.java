package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_109;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_110;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_111;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_114;
import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_065;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.BusinessFunctionCache;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessFunctionValidatorTest {

    @Mock
    private BusinessFunctionCache businessFunctionCache;

    @InjectMocks
    private BusinessFunctionValidator businessFunctionValidator;

    @Test
    void validateBusinessFunctionExist() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("Manage Data Groups");

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("Manage Limits");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        businessFunctionValidator.validateBusinessFunctionsExist(List.of("Manage Limits"));

        verify(businessFunctionCache).getAllApplicableFunctionPrivileges();
    }

    @Test
    void validateBusinessFunctionExistShouldThrowBadRequest() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("Manage Data Groups");

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("Manage Limits");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> businessFunctionValidator
                .validateBusinessFunctionsExist(List.of("Manage Limits", "Assign Entities")));

        String errorMessage = "Business Function 'Assign Entities' is not supported";
        assertThat(exception, new BadRequestErrorMatcher(errorMessage, ERR_ACC_109.getErrorCode()));
    }

    @Test
    void validateBusinessFunctionsSupportLimits() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("SEPA CT");
        privilege1.setSupportsLimit(true);

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("US Domestic Wire");
        privilege2.setSupportsLimit(true);

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        businessFunctionValidator.validateBusinessFunctionsExist(List.of("SEPA CT"));

        verify(businessFunctionCache).getAllApplicableFunctionPrivileges();
    }

    @Test
    void validateBusinessFunctionsSupportLimitsShouldThrowBadRequest() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("SEPA CT");
        privilege1.setSupportsLimit(true);

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("US Domestic Wire");
        privilege2.setSupportsLimit(true);

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> businessFunctionValidator.validateBusinessFunctionsSupportLimits(List.of("Assign Permissions")));

        String errorMessage = "Business Function 'Assign Permissions' does not support limits";
        assertThat(exception, new BadRequestErrorMatcher(errorMessage, ERR_ACC_110.getErrorCode()));
    }

    @Test
    void validateBusinessFunctionsSupportPrivilege() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("SEPA CT");
        privilege1.setPrivilegeName("approve");

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("SEPA CT");
        privilege2.setPrivilegeName("view");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        businessFunctionValidator.validateBusinessFunctionsSupportPrivilege(List.of("SEPA CT"), "approve");

        verify(businessFunctionCache).getAllApplicableFunctionPrivileges();
    }

    @Test
    void validateBusinessFunctionsSupportPrivilegeShouldThrowBadRequest() {
        ApplicableFunctionPrivilege privilege1 = new ApplicableFunctionPrivilege();
        privilege1.setBusinessFunctionName("SEPA CT");
        privilege1.setPrivilegeName("approve");

        ApplicableFunctionPrivilege privilege2 = new ApplicableFunctionPrivilege();
        privilege2.setBusinessFunctionName("SEPA CT");
        privilege2.setPrivilegeName("view");

        when(businessFunctionCache.getAllApplicableFunctionPrivileges()).thenReturn(List.of(privilege1, privilege2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> businessFunctionValidator.validateBusinessFunctionsSupportPrivilege(List.of("SEPA CT"), "create"));

        String errorMessage = "Business Function 'SEPA CT' does not support 'create' privilege";
        assertThat(exception, new BadRequestErrorMatcher(errorMessage, ERR_ACC_114.getErrorCode()));
    }
}