package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_094;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_095;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DateTimeService {

    private SimpleDateFormat dateTimeFormatter;
    private SimpleDateFormat rfc3339Formatter;
    private SimpleDateFormat dateOnlyFormatter;
    private SimpleDateFormat timeOnlyFormatter;

    /**
     * Constructor for {@link DateTimeService} class with predefined format.
     *
     * @param timeZoneName system time zone
     */
    public DateTimeService(@Value("${backbase.accessgroup.timezone}") String timeZoneName) {
        dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateOnlyFormatter = new SimpleDateFormat("yyyy-MM-dd");
        timeOnlyFormatter = new SimpleDateFormat("HH:mm:ss");
        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone(timeZoneName));

        rfc3339Formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        rfc3339Formatter.setTimeZone(TimeZone.getTimeZone(timeZoneName));
        dateOnlyFormatter.setTimeZone(TimeZone.getTimeZone(timeZoneName));
        timeOnlyFormatter.setTimeZone(TimeZone.getTimeZone(timeZoneName));

        dateTimeFormatter.setLenient(false);
        dateOnlyFormatter.setLenient(false);
        timeOnlyFormatter.setLenient(false);
        rfc3339Formatter.setLenient(false);
    }

    /**
     * Get date from date and time parts without time zone.
     *
     * @param dateOnly - date part
     * @param timeOnly - time part
     * @return datetime object from input
     */
    public Date getStartDateFromDateAndTime(String dateOnly, String timeOnly) {
        return getDateFromDateAndTime(dateOnly, timeOnly, "00:00:00");
    }

    /**
     * Get date from date and time parts without time zone.
     *
     * @param dateOnly - date part
     * @param timeOnly - time part
     * @return datetime object from input
     */
    public Date getEndDateFromDateAndTime(String dateOnly, String timeOnly) {
        return getDateFromDateAndTime(dateOnly, timeOnly, "23:59:59");
    }

    private Date getDateFromDateAndTime(String dateOnly, String timeOnly, String defaultTime) {
        if (Objects.isNull(dateOnly) && Objects.isNull(timeOnly)) {
            return null;
        }

        if (Objects.isNull(timeOnly)) {
            timeOnly = defaultTime;
        }

        try {
            return dateTimeFormatter.parse(dateOnly + " " + timeOnly);
        } catch (ParseException e) {
            throw getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode());
        }
    }

    /**
     * Returns string date from date object.
     *
     * @param date - date object
     * @return date only string
     */
    public String getStringDateFromDate(Date date) {

        if (Objects.isNull(date)) {
            return null;
        }
        return dateOnlyFormatter.format(date);
    }

    /**
     * Returns string time from date object.
     *
     * @param date - date object
     * @return time only string
     */
    public String getStringTimeFromDate(Date date) {

        if (Objects.isNull(date)) {
            return null;
        }
        return timeOnlyFormatter.format(date);
    }

    /**
     * Validate if time is set without date.
     *
     * @param date - date string
     * @param time - time string
     * @throws BadRequestException if time is set without a date
     */
    public void validateTimeWithoutDate(String date, String time) {
        if (Strings.isNullOrEmpty(date) && !Strings.isNullOrEmpty(time)) {
            throw getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode());
        }
    }


    /**
     * Validate if from date is before until date.
     *
     * @param from  - date
     * @param until - date
     * @throws BadRequestException if from date is after until date
     */
    public void validatePeriod(Date from, Date until) {
        if (Objects.nonNull(from) && Objects.nonNull(until) && from.after(until)) {
            throw getBadRequestException(ERR_AG_095.getErrorMessage(), ERR_AG_095.getErrorCode());
        }
    }

    /**
     * Validates the time bounds. Validates if fromTime is set without fromDate or if untilTime is set without
     * untilDate. Validates if from date/time or until date/time have valid format. Validates if from date/time is
     * before until date/time.
     *
     * @param fromDate  from date in format yyyy-MM-dd
     * @param fromTime  from time in format HH:mm:ss
     * @param untilDate until date in format yyyy-MM-dd
     * @param untilTime until time in format HH:mm:ss
     * @throws BadRequestException if validation fails
     */
    public void validateTimebound(String fromDate, String fromTime, String untilDate, String untilTime) {
        validateTimeWithoutDate(fromDate, fromTime);
        validateTimeWithoutDate(untilDate, untilTime);

        Date from = getStartDateFromDateAndTime(fromDate, fromTime);
        Date until = getEndDateFromDateAndTime(untilDate, untilTime);

        validatePeriod(from, until);
    }

    /**
     * Returns string date from date object in RFC3339 format.
     *
     * @param date - date object
     */
    public String getRfc3339(Date date) {
        return rfc3339Formatter.format(date);
    }

    /**
     * Returns concatenated date and time.
     *
     * @param date - date bound
     * @param time - time bound
     * @return formatted datetime
     */
    public String getStringDateTime(String date, String time) {
        if (Objects.isNull(date) && Objects.isNull(time)) {
            return "";
        }
        SimpleDateFormat sdf;
        if (Objects.isNull(time)) {
            sdf = new SimpleDateFormat("dd-MM-yyyy");
        } else {
            sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        }
        String dateTime = (Objects.toString(date, "") + " " + Objects.toString(time, "")).trim();
        try {
            sdf.parse(dateTime);
        } catch (ParseException e) {
            throw getBadRequestException(ERR_AG_094.getErrorMessage(), ERR_AG_094.getErrorCode());
        }
        return dateTime;
    }


}
