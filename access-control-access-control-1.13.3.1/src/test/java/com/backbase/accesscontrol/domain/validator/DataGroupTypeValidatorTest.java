package com.backbase.accesscontrol.domain.validator;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_053;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.configuration.ValidationConfig;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import javax.validation.ConstraintValidatorContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataGroupTypeValidatorTest {

    @InjectMocks
    private DataGroupTypeValidator dataGroupTypeValidator;
    @Mock
    private ValidationConfig config;

    @Test
    public void shouldReturnErrorWhenEnumerationTypeIsNotValid() {
        String invalid_type = "INVALID_TYPE";
        when(config.getTypes()).thenReturn(asList("ARRANGEMENTS"));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dataGroupTypeValidator.isValid(invalid_type, mock(ConstraintValidatorContext.class)));
        assertEquals(ERR_ACC_053.getErrorMessage(), exception.getErrors().get(0).getMessage());
    }

    @Test
    public void shouldReturnTrueWhenDataGroupTypeIsValid() {
        String type = "ARRANGEMENTS";
        when(config.getTypes()).thenReturn(asList(type));
        assertTrue(dataGroupTypeValidator.isValid(type, mock(ConstraintValidatorContext.class)));
    }

}
