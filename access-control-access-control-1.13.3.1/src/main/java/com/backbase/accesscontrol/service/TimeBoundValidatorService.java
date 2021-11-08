package com.backbase.accesscontrol.service;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_086;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.enums.FunctionGroupType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TimeBoundValidatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeBoundValidatorService.class);

    private Date startDateLimit;
    private Date endDateLimit;

    /**
     * Constructor.
     *
     * @param timeZoneName - configured time zone
     */
    public TimeBoundValidatorService(@Value("${backbase.accesscontrol.timezone}") String timeZoneName) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

            parser.setTimeZone(TimeZone.getTimeZone(timeZoneName));
            parser.setLenient(false);

            startDateLimit = parser.parse("2000-01-01");
            endDateLimit = parser.parse("2100-01-01");
        } catch (ParseException e) {
            LOGGER.error("Error during date parse formatting in TimeBoundValidatorService.");
        }
    }

    /**
     * Checks if the validity period of the service agreement is in right date range.
     *
     * @param from           - beginning of the validity period
     * @param until          - end of the validity period
     * @param functionGroups -all function groups belonging to that service agreement
     * @return true / false
     */
    public boolean isPeriodValid(Date from, Date until, Set<FunctionGroup> functionGroups) {
        Optional<Date> minDate = functionGroups.stream()
            .filter(functionGroup -> FunctionGroupType.DEFAULT.equals(functionGroup.getType()))
            .map(FunctionGroup::getStartDate)
            .filter(Objects::nonNull)
            .min(Date::compareTo);

        Optional<Date> maxDate = functionGroups.stream()
            .filter(functionGroup -> FunctionGroupType.DEFAULT.equals(functionGroup.getType()))
            .map(FunctionGroup::getEndDate)
            .filter(Objects::nonNull)
            .max(Date::compareTo);

        if (nonNull(from) && minDate.isPresent()) {
            validateAndGetStartDateBetweenServiceAgreementAndFunctionGroup(from, minDate.get());
        }

        if (nonNull(until) && maxDate.isPresent()) {
            validateAndGetEndDateBetweenServiceAgreementAndFunctionGroup(until, maxDate.get());
        }

        if (minDate.isPresent() && maxDate.isPresent()) {
            return maxDate.get().after(minDate.get());
        }
        return isNull(from) && isNull(until) || isNull(from) || isNull(until) || until.after(from);
    }

    /**
     * Checks if the validity period of the fuction group is in right date range.
     *
     * @param fgFrom  - beginning of the validity period
     * @param fgUntil - end of the validity period
     * @param saFrom  - service agreement where function group belongs
     * @param saUntil
     * @return true / false
     */
    public boolean isPeriodValid(Date fgFrom, Date fgUntil, Date saFrom, Date saUntil) {

        if (nonNull(fgFrom) && nonNull(saFrom)) {
            validateAndGetStartDateBetweenServiceAgreementAndFunctionGroup(saFrom, fgFrom);
        }

        if (nonNull(fgUntil) && nonNull(saUntil)) {
            validateAndGetEndDateBetweenServiceAgreementAndFunctionGroup(saUntil, fgUntil);
        }

        if (nonNull(fgFrom) && nonNull(saUntil) && fgFrom.after(saUntil)) {
            LOGGER.error("Function group start date is after service agreement end date.");
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }

        if (nonNull(fgUntil) && nonNull(saFrom) && fgUntil.before(saFrom)) {
            LOGGER.error("Function group end date is before service agreement start date.");
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }

        return isNull(fgFrom) && isNull(fgUntil) || isNull(fgFrom) || isNull(fgUntil) || fgUntil.after(fgFrom);
    }

    /**
     * Checks the validity period.
     *
     * @param from  - beginning of the validity period
     * @param until - end of the validity period
     * @return true / false
     */
    public boolean isPeriodValid(Date from, Date until) {

        return (isNull(from) && isNull(until)) || isNull(from) || isNull(until) || until.after(from);
    }

    /**
     * MSA of root LE cannot be configured with time boundaries (prevention of system lock).
     *
     * @param isMaster          is master
     * @param parentLegalEntity parent legal entity of
     * @param startDate         start date
     * @param endDate           end date
     * @return true / false
     */
    public boolean canServiceAgreementHaveStartAndEndDate(boolean isMaster, LegalEntity parentLegalEntity,
        Date startDate, Date endDate) {

        return !isMaster || nonNull(parentLegalEntity) || isNull(startDate) && isNull(endDate);
    }

    /**
     * Gets the right start date.
     *
     * @param serviceAgreementDate - service agreement start date
     * @param functionGroupDate    - function group start date
     * @return the right start date
     */
    public Date getStartDateLimit(Date serviceAgreementDate, Date functionGroupDate) {

        if (nonNull(serviceAgreementDate) && nonNull(functionGroupDate)) {
            return validateAndGetStartDateBetweenServiceAgreementAndFunctionGroup(serviceAgreementDate,
                functionGroupDate);
        }
        if (nonNull(serviceAgreementDate)) {
            return serviceAgreementDate;
        }
        if (nonNull(functionGroupDate)) {
            return functionGroupDate;
        }
        return startDateLimit;
    }

    /**
     * Gets the right end date.
     *
     * @param serviceAgreementDate - service agreement end date
     * @param functionGroupDate    - function group end date
     * @return the right end date
     */
    public Date getEndDateLimit(Date serviceAgreementDate, Date functionGroupDate) {

        if (nonNull(serviceAgreementDate) && nonNull(functionGroupDate)) {
            return validateAndGetEndDateBetweenServiceAgreementAndFunctionGroup(serviceAgreementDate,
                functionGroupDate);
        }
        if (nonNull(serviceAgreementDate)) {
            return serviceAgreementDate;
        }
        if (nonNull(functionGroupDate)) {
            return functionGroupDate;
        }
        return endDateLimit;

    }

    private Date validateAndGetStartDateBetweenServiceAgreementAndFunctionGroup(Date serviceAgreementDate,
        Date functionGroupDate) {
        if (serviceAgreementDate.after(functionGroupDate)) {
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }
        return functionGroupDate;
    }

    private Date validateAndGetEndDateBetweenServiceAgreementAndFunctionGroup(Date serviceAgreementDate,
        Date functionGroupDate) {
        if (serviceAgreementDate.before(functionGroupDate)) {
            throw getBadRequestException(ERR_ACC_086.getErrorMessage(), ERR_ACC_086.getErrorCode());
        }
        return functionGroupDate;
    }
}
