package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.functiongroup;

import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.DateTimeService;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationIngestFunctionGroup.TypeEnum;
import com.backbase.accesscontrol.service.rest.spec.model.PresentationPermission;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class IngestFunctionGroupDescriptorTest {

    private static final String VALID_FROM_DATE = "08-08-2019";
    private static final String VALID_UNTIL_DATE = "11-11-2019";
    private static final String TIME = "11:11";
    @Spy
    private DateTimeService dateTimeService = new DateTimeService("UTC");
    @InjectMocks
    private IngestFunctionGroupDescriptor ingestFunctionGroupDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getInitEventDataList() {
        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";

        PresentationIngestFunctionGroup functionGroupBase = new PresentationIngestFunctionGroup()
            .externalServiceAgreementId(serviceAgreementId)
            .description(description)
            .name(fgName)
            .type(TypeEnum.TEMPLATE)
            .apsId(new BigDecimal(10))
            .validFromDate(VALID_FROM_DATE)
            .validUntilDate(VALID_UNTIL_DATE);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(functionGroupBase).toArray());

        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventDescription(
                "Create | Function Group Template | Initiated | name Name, external service agreement ID SA-01")
            .withEventMetaDatum("Function Group Name", functionGroupBase.getName())
            .withEventMetaDatum("Function Group Description", functionGroupBase.getDescription())
            .withEventMetaDatum("External Service Agreement ID",
                functionGroupBase.getExternalServiceAgreementId())
            .withEventMetaDatum("Start DateTime", VALID_FROM_DATE)
            .withEventMetaDatum("End DateTime", VALID_UNTIL_DATE)
            .withEventMetaDatum("Function Group Type", functionGroupBase.getType().toString())
            .withEventMetaDatum("Assignable Permission Set ID", functionGroupBase.getApsId().toString());

        List<AuditMessage> actualEventList = ingestFunctionGroupDescriptor
            .getInitEventDataList(joinPoint);
        assertTrue(actualEventList.contains(auditMessage1));
    }

    @Test
    public void getSuccessEventDataList() {
        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";
        String function1 = "function1";
        String function2 = "function2";

        List<PresentationPermission> permissions = Lists.newArrayList(
            new PresentationPermission()
                .functionId(function1)
                .privileges(Lists.newArrayList("view", "edit")),
            new PresentationPermission()
                .functionId(function2)
                .privileges(Lists.newArrayList("view", "create"))
        );

        PresentationIngestFunctionGroup functionGroupBase = new PresentationIngestFunctionGroup()
            .externalServiceAgreementId(serviceAgreementId)
            .description(description)
            .name(fgName)
            .permissions(permissions)
            .validFromDate(VALID_FROM_DATE)
            .validFromTime(TIME)
            .validUntilDate(VALID_UNTIL_DATE)
            .validUntilTime(TIME)
            .apsName("My APS")
            .type(TypeEnum.REGULAR);

        IdItem response = new IdItem().id("id");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(functionGroupBase).toArray());

        String eventDescription = "Create | Function Group Regular | Successful | name Name, external service agreement ID SA-01";
        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(eventDescription)
            .withEventMetaDatum("Function Group ID", response.getId())
            .withEventMetaDatum("Function Group Name", functionGroupBase.getName())
            .withEventMetaDatum("Function Group Description", functionGroupBase.getDescription())
            .withEventMetaDatum("External Service Agreement ID",
                functionGroupBase.getExternalServiceAgreementId())
            .withEventMetaDatum("Business Function ID",
                functionGroupBase.getPermissions().get(0).getFunctionId())
            .withEventMetaDatum("Privileges", "view,edit")
            .withEventMetaDatum("Start DateTime", VALID_FROM_DATE + " " + TIME)
            .withEventMetaDatum("End DateTime", VALID_UNTIL_DATE + " " + TIME)
            .withEventMetaDatum("Function Group Type", functionGroupBase.getType().toString())
            .withEventMetaDatum("Assignable Permission Set Name", functionGroupBase.getApsName());

        AuditMessage auditMessage2 = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventDescription(eventDescription)
            .withEventMetaDatum("Function Group ID", response.getId())
            .withEventMetaDatum("Function Group Name", functionGroupBase.getName())
            .withEventMetaDatum("Function Group Description", functionGroupBase.getDescription())
            .withEventMetaDatum("External Service Agreement ID",
                functionGroupBase.getExternalServiceAgreementId())
            .withEventMetaDatum("Business Function ID",
                functionGroupBase.getPermissions().get(1).getFunctionId())
            .withEventMetaDatum("Privileges", "view,create")
            .withEventMetaDatum("Start DateTime", VALID_FROM_DATE + " " + TIME)
            .withEventMetaDatum("End DateTime", VALID_UNTIL_DATE + " " + TIME)
            .withEventMetaDatum("Function Group Type", functionGroupBase.getType().toString())
            .withEventMetaDatum("Assignable Permission Set Name", functionGroupBase.getApsName());

        List<AuditMessage> actualEventList = ingestFunctionGroupDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(response, HttpStatus.CREATED));

        assertTrue(actualEventList.containsAll(asList(auditMessage1, auditMessage2)));
    }

    @Test
    public void getFailedEventDataList() {
        String serviceAgreementId = "SA-01";
        String description = "description";
        String fgName = "Name";

        PresentationIngestFunctionGroup functionGroupBase = new PresentationIngestFunctionGroup()
            .externalServiceAgreementId(serviceAgreementId)
            .description(description)
            .name(fgName)
            .validFromDate(VALID_FROM_DATE)
            .validFromTime(TIME)
            .validUntilDate(VALID_UNTIL_DATE)
            .validUntilTime(TIME)
            .apsId(new BigDecimal(1))
            .type(TypeEnum.TEMPLATE);

        when(joinPoint.getArgs())
            .thenReturn(singletonList(functionGroupBase).toArray());

        AuditMessage auditMessage1 = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventDescription(
                "Create | Function Group Template | Failed | name Name, external service agreement ID SA-01")
            .withEventMetaDatum("Function Group Name", functionGroupBase.getName())
            .withEventMetaDatum("Function Group Description", functionGroupBase.getDescription())
            .withEventMetaDatum("External Service Agreement ID", functionGroupBase.getExternalServiceAgreementId())
            .withEventMetaDatum("Start DateTime", VALID_FROM_DATE + " " + TIME)
            .withEventMetaDatum("End DateTime", VALID_UNTIL_DATE + " " + TIME)
            .withEventMetaDatum("Function Group Type", functionGroupBase.getType().toString())
            .withEventMetaDatum("Assignable Permission Set ID", functionGroupBase.getApsId().toString());
        List<AuditMessage> actualEventList = ingestFunctionGroupDescriptor
            .getFailedEventDataList(joinPoint);
        assertTrue(actualEventList.contains(auditMessage1));
    }
}