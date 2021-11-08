package com.backbase.accesscontrol.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class TimeZoneConverterServiceTest {

    private TimeZoneConverterService service = new TimeZoneConverterService();

    @Test
    public void getCurrentTimeNow() {
        ReflectionTestUtils.setField(service, "timeZoneName", "Europe/Amsterdam");
        Timestamp currentTime = service.getCurrentTime();
        assertNotNull(currentTime);
        ReflectionTestUtils.setField(service, "timeZoneName", "Europe/Bucharest");
        Timestamp currentTime2 = service.getCurrentTime();
        assertNotNull(currentTime2);
        long diffHours = Math.abs(currentTime2.getTime() - currentTime.getTime()) / (60 * 60 * 1000);
        assertEquals(1, diffHours);
    }
}