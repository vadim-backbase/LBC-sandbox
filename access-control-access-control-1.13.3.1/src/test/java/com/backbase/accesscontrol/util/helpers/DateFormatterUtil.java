package com.backbase.accesscontrol.util.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public final class DateFormatterUtil {

    private static SimpleDateFormat dateOnlyFormatter = new UTCDateFormatter("yyyy-MM-dd");
    private static SimpleDateFormat timeOnlyFormatter = new UTCDateFormatter("HH:mm:ss");
    private static SimpleDateFormat RFC3339Formatter = new UTCDateFormatter("yyyy-MM-dd'T'HH:mm:ssZ");

    private DateFormatterUtil() {
    }

    public static String utcFormatDateOnly(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return dateOnlyFormatter.format(date);
    }

    public static String utcFormatTimeOnly(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return timeOnlyFormatter.format(date);
    }

    public static String utcFormatRFC3339(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        return RFC3339Formatter.format(date);
    }

    private static class UTCDateFormatter extends SimpleDateFormat {

        UTCDateFormatter(String s) {
            super(s);
            setTimeZone(TimeZone.getTimeZone("UTC"));
        }
    }
}
