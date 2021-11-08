package com.backbase.accesscontrol.api;

import static org.junit.Assert.assertNotNull;

import com.backbase.accesscontrol.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles({"it"})
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
    "backbase.communication.outbound=HTTP",
    "backbase.communication.inbound=HTTP"
})
public class ApplicationIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void shouldCreateApplicationContext() {
        assertNotNull(applicationContext);
    }
}