package com.backbase.accesscontrol.dto;

import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_043;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import org.junit.Test;

public class UserParametersTest {

    @Test
    public void shouldReturnValidObject() {
        UserParameters userParameters = new UserParameters("user", "legaEntityId");
        assertNotNull(userParameters);
    }


    @Test
    public void shouldReturnValidObjectWhenNulAndEmptySet() {
        UserParameters userParameters = new UserParameters(null, "");
        assertNotNull(userParameters);
    }

    @Test
    public void shouldReturnValidObjectEmptyAndNullSet() {
        UserParameters userParameters = new UserParameters("", null);
        assertNotNull(userParameters);
    }

    @Test
    public void shouldReturnValidObjectWhenNullSet() {
        UserParameters userParameters = new UserParameters(null, null);
        assertNotNull(userParameters);
    }

    @Test
    public void shouldReturnValidObjectWhenEmptySet() {
        assertNotNull(new UserParameters("", ""));
    }

    @Test
    public void shouldThrowExceptionWhenEmptyAndIdSent() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> new UserParameters("", "id"));
        assertEquals(ERR_ACQ_043.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenNullAndIdSent() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> new UserParameters(null, "id"));
        assertEquals(ERR_ACQ_043.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenIdAndEmptySent() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> new UserParameters("id", ""));
        assertEquals(ERR_ACQ_043.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenIdAndNullSent() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> new UserParameters("id", null));
        assertEquals(ERR_ACQ_043.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }
}