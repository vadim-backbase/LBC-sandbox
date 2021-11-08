package com.backbase.accesscontrol.domain;


import static com.backbase.accesscontrol.util.errorcodes.QueryErrorCodes.ERR_ACQ_020;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import org.junit.Test;
import org.mockito.InjectMocks;

public class FunctionGroupTypeTest {

    @InjectMocks
    private FunctionGroupType functionGroupType;

    @Test
    public void shouldReturnErrorWhenEnumerationTypeIsNotValid() {
        String invalid_type = "INVALID_TYPE";
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> FunctionGroupType.fromString(invalid_type));
        assertEquals(ERR_ACQ_020.getErrorMessage(), exception.getErrors().get(0).getMessage());

    }

    @Test
    public void shouldReturnFunctionGroupType() {
        String type = "DEFAULT";

        FunctionGroupType functionGroupType = FunctionGroupType.fromString(type);

        assertEquals(FunctionGroupType.DEFAULT.toString(), functionGroupType.name());
    }

}
