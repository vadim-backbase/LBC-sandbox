package com.backbase.legalentity.integration.external.mock;

import static org.junit.Assert.assertNotNull;

import com.backbase.legalentity.integration.external.mock.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("it")
@SpringBootTest(classes = Application.class)
public class ApplicationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldCreateApplicationContext() {
        assertNotNull(applicationContext);
    }
}