package com.backbase.accesscontrol.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateUtil.class);

    public static final Date MIN_DATE = getDate("2000-01-01");
    public static final Date MAX_DATE = getDate("2100-01-01");

    private static Date getDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            //Unreachable clause.
        }

        LOGGER.warn("Something bad has happened while parsing the date {}", date);
        return null;
    }
}
