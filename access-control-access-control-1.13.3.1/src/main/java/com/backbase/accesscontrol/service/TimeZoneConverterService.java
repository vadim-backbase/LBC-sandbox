package com.backbase.accesscontrol.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TimeZoneConverterService {

    private @Value("${backbase.accesscontrol.timezone}") String timeZoneName;

    /**
     * Calculate current time by time zone id set in the backbase.accesscontrol.timezone property.
     *
     * @return Timestamp
     */
    public Timestamp getCurrentTime(){
        LocalDateTime localNow = LocalDateTime.now(ZoneId.of(timeZoneName));
        return Timestamp.valueOf(localNow);
    }


}
