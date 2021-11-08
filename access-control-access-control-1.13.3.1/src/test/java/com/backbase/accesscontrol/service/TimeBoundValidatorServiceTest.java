package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeBoundValidatorServiceTest {

    @Spy
    private TimeBoundValidatorService timeBoundValidatorService = new TimeBoundValidatorService(
        "UTC");
    private Date from = new Date(0);
    private Date until = new Date(1);


    @Test
    public void isPeriodValidTrue() {
        boolean periodValid = timeBoundValidatorService.isPeriodValid(from, until, new HashSet<>());
        assertTrue(periodValid);
    }

    @Test
    public void isPeriodValidWithFunctionGroupsTrue() {
        Date saStart = new Date(0);
        Date saEnd = new Date(5);
        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(2))
            .withEndDate(new Date(3))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(1))
            .withEndDate(new Date(4))
            .withType(FunctionGroupType.DEFAULT);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        boolean periodValid = timeBoundValidatorService.isPeriodValid(saStart, saEnd, functionGroups);

        assertTrue(periodValid);
    }

    @Test
    public void shouldReturnTrueIfFunctionGroupOfTypeTemplateHasTimeBoundOutsideServiceAgreementTimeBound() {
        Date saStart = new Date(0);
        Date saEnd = new Date(5000);
        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(2000))
            .withEndDate(new Date(3000))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(4000))
            .withEndDate(new Date(8000))
            .withType(FunctionGroupType.TEMPLATE);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        boolean periodValid = timeBoundValidatorService.isPeriodValid(saStart, saEnd, functionGroups);

        assertTrue(periodValid);
    }

    @Test
    public void isPeriodValidWithFunctionGroupsThrowBadRequestInvalidStartDate() {
        Date saStart = new Date(2);
        Date saEnd = new Date(5);
        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(2))
            .withEndDate(new Date(3))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(1))
            .withEndDate(new Date(4))
            .withType(FunctionGroupType.DEFAULT);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> timeBoundValidatorService.isPeriodValid(saStart, saEnd, functionGroups));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void isPeriodValidWithFunctionGroupsThrowBadRequestInvalidEndDate() {
        Date saStart = new Date(0);
        Date saEnd = new Date(3);
        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(2))
            .withEndDate(new Date(3))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(1))
            .withEndDate(new Date(4))
            .withType(FunctionGroupType.DEFAULT);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () ->timeBoundValidatorService.isPeriodValid(saStart, saEnd, functionGroups));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void isPeriodValidWithFunctionGroupsAndServiceAgreementTimeNullTrue() {

        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(2))
            .withEndDate(new Date(3))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(1))
            .withEndDate(new Date(4))
            .withType(FunctionGroupType.DEFAULT);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        boolean periodValid = timeBoundValidatorService.isPeriodValid(null, null, functionGroups);

        assertTrue(periodValid);
    }

    @Test
    public void isPeriodValidWithFunctionGroupsAndServiceAgreementTimeNullFalse() {

        FunctionGroup functionGroup1 = new FunctionGroup()
            .withStartDate(new Date(8))
            .withEndDate(new Date(2))
            .withType(FunctionGroupType.DEFAULT);
        FunctionGroup functionGroup2 = new FunctionGroup()
            .withStartDate(new Date(4))
            .withEndDate(new Date(1))
            .withType(FunctionGroupType.DEFAULT);
        Set<FunctionGroup> functionGroups = new HashSet<>(asList(functionGroup1, functionGroup2));

        boolean periodValid = timeBoundValidatorService.isPeriodValid(null, null, functionGroups);

        assertFalse(periodValid);
    }

    @Test
    public void isPeriodValidFalse() {
        boolean periodValid = timeBoundValidatorService.isPeriodValid(until, from, new HashSet<>());
        assertFalse(periodValid);
    }

    @Test
    public void shouldReturnFalseIfIsMasterRootLegalEntityWithStartAndEndDate() {
        boolean isMaster = true;
        LegalEntity parentLe = null;

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster,parentLe, from, until);
        assertFalse(isRootLe);
    }

    @Test
    public void shouldReturnFalseIfIsMasterRootLegalEntityWithStartAndWithouEndDate() {
        boolean isMaster = true;
        LegalEntity parentLe = null;

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, from, null );
        assertFalse(isRootLe);
    }

    @Test
    public void shouldReturnFalseIfIsMasterRootLegalEntityWithoutStartAndWithEndDate() {
        boolean isMaster = true;
        LegalEntity parentLe = null;

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, null, until);
        assertFalse(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsNotMasterRootLegalEntityWithStartAndEndDate() {
        boolean isMaster = false;
        LegalEntity parentLe = null;

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, from, until);
        assertTrue(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsMasterRootLegalEntityMasterWithoutStartAndEndDate() {
        boolean isMaster = true;
        LegalEntity parentLe = null;

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, null, null);
        assertTrue(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsMasterNotRootLegalEntityWithStartAndEndDate() {
        boolean isMaster = true;
        LegalEntity parentLe = new LegalEntity();

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, from, until);
        assertTrue(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsNotMasterNotRootLegalEntityWithStartAndEndDate() {
        boolean isMaster = false;
        LegalEntity parentLe = new LegalEntity();

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, from, until);
        assertTrue(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsNotMasterNotRootLegalEntityWithStartAndWithoutEndDate() {
        boolean isMaster = false;
        LegalEntity parentLe = new LegalEntity();

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, from, null);
        assertTrue(isRootLe);
    }

    @Test
    public void shouldReturnTrueIfIsNotMasterNotRootLegalEntityWithoutStartAndWithEndDate() {
        boolean isMaster = false;
        LegalEntity parentLe = new LegalEntity();

        boolean isRootLe = timeBoundValidatorService
            .canServiceAgreementHaveStartAndEndDate(isMaster, parentLe, null, until);
        assertTrue(isRootLe);
    }


    @Test
    public void shouldGetStartDateLimitOfServiceAgreement() {
        Date startDateLimit = timeBoundValidatorService.getStartDateLimit(from, null);
        assertEquals(startDateLimit, from);
    }

    @Test
    public void shouldGetStartDateLimitOfFunctionGroup() {
        Date startDateLimit = timeBoundValidatorService.getStartDateLimit(null, from);
        assertEquals(startDateLimit, from);
    }

    @Test
    public void shouldGetStartDateLimitDefault() {
        Date startDateLimit = timeBoundValidatorService.getStartDateLimit(null, null);
        assertNotNull(startDateLimit);
    }

    @Test
    public void shouldGetServiceAgreementEndDateLimit() {
        Date endDateLimit = timeBoundValidatorService.getEndDateLimit(until, null);
        assertEquals(endDateLimit, until);
    }

    @Test
    public void shouldGetFunctionGroupEndDateLimit() {
        Date endDateLimit = timeBoundValidatorService.getEndDateLimit(null, until);
        assertEquals(endDateLimit, until);
    }

    @Test
    public void shouldGetEndDateLimitDefault() {
        Date endDateLimit = timeBoundValidatorService.getEndDateLimit(null, null);
        assertNotNull(endDateLimit);
    }

    @Test
    public void shouldReturnTrueForBothNull() {
        assertTrue(timeBoundValidatorService.isPeriodValid(null, null, new HashSet<>()));
    }

    @Test
    public void shouldReturnTrueForFromBeforeUntil() {
        assertTrue(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000), new HashSet<>()));
    }

    @Test
    public void shouldReturnTrueForUntilIsNull() {
        assertTrue(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            null, new HashSet<>()));
    }

    @Test
    public void shouldReturnTrueForFromIsNull() {
        assertTrue(timeBoundValidatorService.isPeriodValid(
            null,
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000), new HashSet<>()));
    }

    @Test
    public void shouldReturnFalseUntilIsBeforeFrom() {
        assertFalse(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000),
            new Date(System.currentTimeMillis() + 3600 * 1000), new HashSet<>()));
    }

    @Test
    public void shouldFailWhenInitiateUpdatePendingRequestWithStartDateBiggerThatEndDateOfSa() {
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withStartDate(new Date(System.currentTimeMillis() + 8 * 3600 * 1000));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> timeBoundValidatorService.isPeriodValid(
            null,
            new Date(System.currentTimeMillis() + 3600 * 1000), serviceAgreement.getStartDate(),
            serviceAgreement.getEndDate()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldFailWhenInitiateUpdatePendingRequestWithEndDateSmallerThatSaStartDate() {
        ServiceAgreement serviceAgreement = new ServiceAgreement()
            .withStartDate(new Date(System.currentTimeMillis() + 4 * 3600 * 1000))
            .withEndDate(new Date(System.currentTimeMillis() + 8 * 3600 * 1000));

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            null, serviceAgreement.getStartDate(), serviceAgreement.getEndDate()));

        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldGetBadRequestWhenStartDateOfFunctionGroupBeforeServiceAgreement() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> timeBoundValidatorService.getStartDateLimit(new Date(1), new Date(0)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldGetBadRequestWhenEndDateOfFunctionGroupAfterServiceAgreement() {
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> timeBoundValidatorService.getEndDateLimit(new Date(0), new Date(1)));
        assertThat(exception, new BadRequestErrorMatcher(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode()));
    }

    @Test
    public void shouldGetTheStartDateOfFunctionGroup() {
        Date startDateLimit = timeBoundValidatorService.getStartDateLimit(new Date(0), new Date(1));
        assertEquals(startDateLimit, new Date(1));
    }

    @Test
    public void shouldGetTheEndDateOfFunctionGroup() {
        Date startDateLimit = timeBoundValidatorService.getEndDateLimit(new Date(2), new Date(1));
        assertEquals(startDateLimit, new Date(1));
    }

    @Test
    public void periodValidationShouldReturnTrueForUntilIsNull() {
        assertTrue(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000), null));
    }

    @Test
    public void periodValidationShouldReturnTrueForFromIsNull() {
        assertTrue(timeBoundValidatorService
            .isPeriodValid(null, new Date(System.currentTimeMillis() + 2 * 3600 * 1000)));
    }

    @Test
    public void periodValidationShouldReturnTrueUntilIsAfterFrom() {
        assertTrue(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 3600 * 1000),
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000)));
    }

    @Test
    public void periodValidationShouldReturnFalseUntilIsBeforeFrom() {
        assertFalse(timeBoundValidatorService.isPeriodValid(
            new Date(System.currentTimeMillis() + 2 * 3600 * 1000),
            new Date(System.currentTimeMillis() + 3600 * 1000)));
    }
}