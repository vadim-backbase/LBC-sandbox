package com.backbase.accesscontrol.business.persistence.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.serviceagreements.ServiceAgreementItem;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementTransformerPersistenceTest {

    @InjectMocks
    private ServiceAgreementTransformerPersistence serviceAgreementTransformerPersistence;

    @Test
    public void testTransformServiceAgreement() {
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("SA-01");
        serviceAgreement.setName("Service Agreement");
        serviceAgreement.setDescription("description");
        serviceAgreement.setState(ServiceAgreementState.ENABLED);
        serviceAgreement.setMaster(false);
        serviceAgreement.setStartDate(new Date(System.currentTimeMillis() / 1000 * 1000));
        serviceAgreement.setEndDate(new Date(System.currentTimeMillis() / 1000 * 1000));

        ServiceAgreementItem responseBody = serviceAgreementTransformerPersistence
            .transformServiceAgreement(ServiceAgreementItem.class, serviceAgreement);

        assertThat(responseBody, allOf(
            hasProperty("validFrom", is(serviceAgreement.getStartDate())),
            hasProperty("validUntil", is(serviceAgreement.getEndDate())),
            hasProperty("id", is(serviceAgreement.getId())),
            hasProperty("description", is(serviceAgreement.getDescription())),
            hasProperty("name", is(serviceAgreement.getName())),
            hasProperty("isMaster", is(serviceAgreement.isMaster())))

        );

    }

}
