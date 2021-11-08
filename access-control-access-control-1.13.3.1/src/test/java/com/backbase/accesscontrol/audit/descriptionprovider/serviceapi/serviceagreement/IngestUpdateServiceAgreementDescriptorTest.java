package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.serviceagreement;

import static com.backbase.accesscontrol.matchers.MatcherUtil.getAuditMessageMatcher;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.ServiceAgreementPut;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestUpdateServiceAgreementDescriptorTest {

    private static final String VALID_FROM_DATE = "08-08-2019";
    private static final String VALID_UNTIL_DATE = "11-11-2019";
    private static final String TIME = "11:11";

    @InjectMocks
    private IngestUpdateServiceAgreementDescriptor ingestUpdateServiceAgreementDescriptor;
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @Mock
    private ProceedingJoinPoint joinPoint;


    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "SA-01";

        ServiceAgreementPut request = new ServiceAgreementPut()
            .name("SA name")
            .description("description")
            .externalId("SA-EX-01")
            .validFromDate(VALID_FROM_DATE)
            .validUntilDate(VALID_UNTIL_DATE)
            .status(com.backbase.accesscontrol.service.rest.spec.model.Status.ENABLED);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> successEventDataList = ingestUpdateServiceAgreementDescriptor
            .getInitEventDataList(joinPoint);

        AuditMessage expected = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Initiated | name SA name")
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status",
                Objects.nonNull(request.getStatus()) ? request.getStatus().toString() : "")
            .withEventMetaDatum("Start DateTime", "08-08-2019")
            .withEventMetaDatum("End DateTime", "11-11-2019")
            .withStatus(Status.INITIATED);

        assertEquals(successEventDataList.get(0), expected);
    }


    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementId = "SA-01";

        ServiceAgreementPut request = new ServiceAgreementPut()
            .name("SA name")
            .description("description")
            .externalId("SA-EX-01")
            .validFromDate(VALID_FROM_DATE)
            .validFromTime(TIME)
            .validUntilDate(VALID_UNTIL_DATE)
            .validUntilTime(TIME)
            .status(com.backbase.accesscontrol.service.rest.spec.model.Status.ENABLED);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> successEventDataList = ingestUpdateServiceAgreementDescriptor
            .getSuccessEventDataList(joinPoint, request);

        assertEquals(7, successEventDataList.get(0).getEventMetaData().size());
        assertEquals("Update | Service Agreement | Successful | name SA name"
            , successEventDataList.get(0).getEventDescription());
        assertThat(successEventDataList,
            hasItems(
                getAuditMessageMatcher(
                    is(Status.SUCCESSFUL),
                    allOf(
                        hasEntry("External Service Agreement ID", request.getExternalId()),
                        hasEntry("Service Agreement ID", serviceAgreementId),
                        hasEntry("Service Agreement Name", request.getName()),
                        hasEntry("Service Agreement Description", request.getDescription()),
                        hasEntry("Service Agreement Status", request.getStatus().toString()),
                        hasEntry("Start DateTime", "08-08-2019" + " " + "11:11"),
                        hasEntry("End DateTime", "11-11-2019" + " " + "11:11")
                    ))
            ));
    }

    @Test
    public void getSuccessEventDataListWhenOptionalFieldsAreNotPopulated() {
        String serviceAgreementId = "SA-01";
        ServiceAgreementPut request = new ServiceAgreementPut();

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> successEventDataList = ingestUpdateServiceAgreementDescriptor
            .getSuccessEventDataList(joinPoint, request);

        assertEquals(3, successEventDataList.get(0).getEventMetaData().size());
        assertThat(successEventDataList,
            hasItems(
                getAuditMessageMatcher(
                    is(Status.SUCCESSFUL),
                    allOf(
                        hasEntry("Service Agreement ID", serviceAgreementId)
                    ))
            ));
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementId = "SA-01";

        ServiceAgreementPut request = new ServiceAgreementPut()
            .name("SA name")
            .description("description")
            .externalId("SA-EX-01")
            .validFromDate(VALID_FROM_DATE)
            .validFromTime(TIME)
            .validUntilDate(VALID_UNTIL_DATE)
            .status(com.backbase.accesscontrol.service.rest.spec.model.Status.ENABLED);

        when(joinPoint.getArgs()).thenReturn(new Object[]{request, serviceAgreementId});

        List<AuditMessage> successEventDataList = ingestUpdateServiceAgreementDescriptor
            .getFailedEventDataList(joinPoint);

        AuditMessage expected = new AuditMessage()
            .withEventDescription("Update | Service Agreement | Failed | name SA name")
            .withEventMetaDatum("External Service Agreement ID", request.getExternalId())
            .withEventMetaDatum("Service Agreement ID", serviceAgreementId)
            .withEventMetaDatum("Service Agreement Name", request.getName())
            .withEventMetaDatum("Service Agreement Description", request.getDescription())
            .withEventMetaDatum("Service Agreement Status",
                Objects.nonNull(request.getStatus()) ? request.getStatus().toString() : "")
            .withEventMetaDatum("Start DateTime", "08-08-2019" + " " + "11:11")
            .withEventMetaDatum("End DateTime", "11-11-2019")
            .withStatus(Status.FAILED);
        assertEquals(successEventDataList.get(0), expected);

    }
}
