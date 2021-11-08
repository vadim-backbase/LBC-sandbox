package com.backbase.accesscontrol.util.validation;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_089;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.buildingblocks.presentation.errors.NotFoundException;
import org.junit.Test;


public class ValidatePermissionSetIdentifiersTest {

    @Test
    public void shouldValidateIdentifiers() {
        ValidatePermissionSetIdentifiers.validateIdentifiers("id", "123");
    }

    @Test
    public void shouldThrowErrorInvalidType() {
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ValidatePermissionSetIdentifiers.validateIdentifiers("idd", "123"));
        assertEquals(ERR_ACC_089.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowErrorInvalidId() {
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> ValidatePermissionSetIdentifiers.validateIdentifiers("id", "123a"));
        assertEquals(ERR_ACC_089.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldCheckIfTypeIsId() {
        assertTrue(ValidatePermissionSetIdentifiers.isIdIdentifier("id"));
    }

}