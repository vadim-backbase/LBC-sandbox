package com.backbase.accesscontrol.api;

import com.backbase.accesscontrol.Application;
import com.backbase.buildingblocks.multitenancy.TenantContext;
import com.backbase.buildingblocks.multitenancy.TenantProvider;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles({"mt"})
@RunWith(SpringRunner.class)
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "wiremock=true",
    "backbase.audit.enabled=false"
})

@SpringBootTest(
    classes = {Application.class, TestDbWireMock.TestProcessor.class},
    webEnvironment = WebEnvironment.RANDOM_PORT
)
public abstract class MultiTenantTestDbWireMock extends TestDbWireMock {

    @Autowired
    protected TenantProvider tenantProvider;

    @Override
    protected void setUpInitialData() {

    }

    @Override
    public void tearDown() {

    }

    @Override
    protected void setTenant(String tenantId) {
        TenantContext.setTenant(tenantProvider.findTenantById(tenantId).orElse(null));
    }
}
