package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import com.backbase.accesscontrol.matchers.BadRequestErrorMatcher;
import com.backbase.accesscontrol.util.helpers.DateFormatterUtil;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

public class DateTimeServiceTest {

    private DateTimeService testy = new DateTimeService("GMT+2");

    @Test
    public void testValidateTimeBoundValidDateTime() {

        String fromDate = "2016-02-29";
        String fromTime = "07:48:23";
        String untilDate = "2017-01-31";
        String untilTime = "07:48:24";
        testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
    }

    @Test
    public void testValidateTimeBoundValidDateTimeShouldParseSingleChar() {

        String fromDate = "2016-9-9";
        String fromTime = "7:48:3";
        String untilDate = "2017-5-3";
        String untilTime = "9:8:24";
        testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);

    }

    @Test
    public void testValidateTimeBoundInvalidMonthDay() {

        String fromDate = "2017-02-31";
        String fromTime = "07:48:23";
        String untilDate = "2025-01-31";
        String untilTime = "07:48:24";
        try {
            testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
            fail("2017-02-31 is invalid date");
        } catch (BadRequestException ex) {
            assertEquals("Wrong date/time format", ex.getErrors().get(0).getMessage());
            assertEquals("datetime.valid.period.INVALID_FORMAT", ex.getErrors().get(0).getKey());
        }
    }

    @Test
    public void testValidateTimeBoundInvalidMinute() {

        String fromDate = "2017-02-31";
        String fromTime = "07:48:23";
        String untilDate = "2025-01-31";
        String untilTime = "07:99:24";
        try {
            testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
            fail("07:99:24 is invalid time");
        } catch (BadRequestException ex) {
            assertEquals("Wrong date/time format", ex.getErrors().get(0).getMessage());
            assertEquals("datetime.valid.period.INVALID_FORMAT", ex.getErrors().get(0).getKey());
        }
    }


    @Test
    public void testValidateTimeBoundFromDatAfterUntilDate() {

        String fromDate = "2026-12-31";
        String fromTime = "07:48:23";
        String untilDate = "2025-01-31";
        String untilTime = "07:48:24";
        try {
            testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
            fail("2026-12-31 > 2025-01-31");
        } catch (BadRequestException ex) {
            assertEquals("Invalid validity period.", ex.getErrors().get(0).getMessage());
            assertEquals("datetime.valid.period.INVALID_VALUE", ex.getErrors().get(0).getKey());
        }
    }

    @Test
    public void testValidateTimeBoundFromTimeAfterUntilTime() {

        String fromDate = "2020-02-29";
        String fromTime = "07:48:50";
        String untilDate = "2020-02-29";
        String untilTime = "07:48:24";
        try {
            testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
            fail("07:48:50 > 07:48:24");
        } catch (BadRequestException ex) {
            assertEquals("Invalid validity period.", ex.getErrors().get(0).getMessage());
            assertEquals("datetime.valid.period.INVALID_VALUE", ex.getErrors().get(0).getKey());
        }
    }

    @Test
    public void shouldReturnNullForStartDate() {
        assertNull(testy.getStartDateFromDateAndTime(null, null));
    }

    @Test
    public void shouldReturnForMidnightForStartDate() {

        assertEquals("22:00:00", DateFormatterUtil
            .utcFormatTimeOnly(testy.getStartDateFromDateAndTime("2019-01-01", null)));
    }

    @Test
    public void shouldReturn2HoursBackInUTCForStartDate() {

        Date date = testy.getStartDateFromDateAndTime("2019-01-02", "01:21:05");

        assertEquals("23:21:05", DateFormatterUtil.utcFormatTimeOnly(date));
        assertEquals("2019-01-01", DateFormatterUtil.utcFormatDateOnly(date));
    }

    @Test
    public void shouldThrowBadRequestFormInvalidFormatForStartDate() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.getStartDateFromDateAndTime("2000-01-01", "08:00"));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestFormInvalidFormatForEndDate() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.getEndDateFromDateAndTime("2000-01-01", "08:00"));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldReturnNullForEndDate() {
        assertNull(testy.getEndDateFromDateAndTime(null, null));
    }

    @Test
    public void shouldReturnForSecondBeforeMidnightForEndDate() {

        assertEquals("21:59:59", DateFormatterUtil
            .utcFormatTimeOnly(testy.getEndDateFromDateAndTime("2019-01-01", null)));
    }

    @Test
    public void shouldReturn2HoursBackInUTCForEndDate() {
        Date date = testy.getEndDateFromDateAndTime("2019-01-02", "01:21:05");

        assertEquals("23:21:05", DateFormatterUtil.utcFormatTimeOnly(date));
        assertEquals("2019-01-01", DateFormatterUtil.utcFormatDateOnly(date));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenValidatingTimeWithoutDate() {

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimeWithoutDate(null, "01:21:00"));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldValidateSuccessfullyWhenTimeaAndDateProvided() {
        testy.validateTimeWithoutDate("2019-01-02", "01:21:00");
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenValidatingPeriodAndStartDateAfterUntilDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = formatter.parse("2019-03-02 01:21:05");
        Date endDate = formatter.parse("2019-02-02 01:21:05");

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validatePeriod(startDate, endDate));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode()));
    }

    @Test
    public void shouldValidatePeriodSuccessfullyWhenStartDateBeforeUntilDate() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = formatter.parse("2019-01-02 01:21:05");
        Date endDate = formatter.parse("2019-02-02 01:21:05");

        testy.validatePeriod(startDate, endDate);
    }

    @Test
    public void shouldValidateSuccessfullyWhenUntilDateIsNull() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = formatter.parse("2019-01-02 01:21:05");
        Date endDate = null;

        testy.validatePeriod(startDate, endDate);
    }

    @Test
    public void shouldValidateSuccessfullyWhenStartDateIsNull() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = null;
        Date endDate = formatter.parse("2019-02-02 01:21:05");

        testy.validatePeriod(startDate, endDate);
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenFromDateNullAndFromTimeNotNull() {

        String fromDate = null;
        String fromTime = "01:21:05";
        String untilDate = null;
        String untilTime = null;
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimebound(fromDate, fromTime, untilDate, untilTime));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenUntilDateNullAndUntilTimeNotNull() {

        String fromDate = null;
        String fromTime = null;
        String untilDate = null;
        String untilTime = "01:21:05";

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimebound(fromDate, fromTime, untilDate, untilTime));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenFromDateHasInvalidFormat() {

        String fromDate = "2019/12/23";
        String fromTime = null;
        String untilDate = null;
        String untilTime = null;

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimebound(fromDate, fromTime, untilDate, untilTime));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenUntilDateHasInvalidFormat() {

        String fromDate = null;
        String fromTime = null;
        String untilDate = null;
        String untilTime = "2019/12/23";
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimebound(fromDate, fromTime, untilDate, untilTime));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode()));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenFromDateTimeIsAfterUntilDateTime() {

        String fromDate = "2019-03-23";
        String fromTime = "01:05:10";
        String untilDate = "2019-02-23";
        String untilTime = "01:05:14";

        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> testy.validateTimebound(fromDate, fromTime, untilDate, untilTime));

        assertThat(exception, new BadRequestErrorMatcher(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode()));
    }

    @Test
    public void shouldDoNothingWhenValidatingTimeboundSuccessfully() {
        String fromDate = "2019-01-23";
        String fromTime = "01:05:10";
        String untilDate = "2019-02-23";
        String untilTime = "01:05:14";
        testy.validateTimebound(fromDate, fromTime, untilDate, untilTime);
    }
}