package com.backbase.accesscontrol.service.business;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_062;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_063;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_064;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_065;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.service.ParameterValidationService;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import javax.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class ParameterValidationServiceTest {

    private ParameterValidationService parameterValidationService;

    @Before
    public void setUp() {
        parameterValidationService = new ParameterValidationService(
            Validation.buildDefaultValidatorFactory().getValidator());
    }

    @Test
    public void shouldDoNothingWhenQueryIsValid() {
        String searchQuery = "searchQuery";
        String result = parameterValidationService.validateQueryParameter(searchQuery);
        assertEquals(searchQuery, result);
    }

    @Test
    public void shouldThrowBadRequestWhenQueryIsLong() {
        String searchQuery = StringUtils.repeat("searchQuery", 50);
        assertTrue(searchQuery.length() > 255);

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateQueryParameter(searchQuery));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_062.getErrorMessage(), ERR_AG_062.getErrorCode()));
    }

    @Test
    public void testNegativeFromParameter() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(-2, null));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void testNegativeFromAndValidSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(-2, 5));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void testNegativeFromAndNegativeSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(-2, -5));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_063.getErrorMessage(), ERR_AG_063.getErrorCode()));
    }

    @Test
    public void testNegativeSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(null, -5));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_064.getErrorMessage(), ERR_AG_064.getErrorCode()));
    }

    @Test
    public void testValidFromAndNegativeSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(1, -5));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_064.getErrorMessage(), ERR_AG_064.getErrorCode()));
    }

    @Test
    public void testValidFromAndNullSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(1, null));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_065.getErrorMessage(), ERR_AG_065.getErrorCode()));
    }

    @Test
    public void testNullFromAndValidSizeParameters() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> parameterValidationService.validateFromAndSizeParameter(null, 1));
        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_065.getErrorMessage(), ERR_AG_065.getErrorCode()));
    }
}