package com.backbase.accesscontrol.audit.descriptionprovider.serviceapi.datagroup;

import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_DESCRIPTION_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_NAME_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.DATA_GROUP_TYPE_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME;
import static com.backbase.accesscontrol.audit.descriptionprovider.DescriptorFieldNames.SERVICE_AGREEMENT_ID_FIELD_NAME;
import static com.backbase.accesscontrol.util.helpers.RequestUtils.getResponseEntity;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItemSystemBase;
import com.backbase.accesscontrol.service.rest.spec.model.IdItem;
import com.backbase.audit.client.model.AuditMessage;
import com.backbase.audit.client.model.AuditMessage.Status;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class CreateDataGroupServiceDescriptorTest {

    @InjectMocks
    private CreateDataGroupServiceDescriptor createDataGroupDescriptor;
    @Mock
    private ProceedingJoinPoint joinPoint;

    @Test
    public void getSuccessEventDataList() {
        String dgName = "Name";

        DataGroupItemSystemBase presentationFunctionGroup = new DataGroupItemSystemBase()
            .description("DE-01")
            .name(dgName)
            .serviceAgreementId("ex")
            .type("type");

        IdItem dataGroupsPostResponseBody = new IdItem();
        dataGroupsPostResponseBody.setId("idDG");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.SUCCESSFUL)
            .withEventMetaDatum(DATA_GROUP_ID_FIELD_NAME, "idDG")
            .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, dgName)
            .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, "DE-01")
            .withEventMetaDatum(SERVICE_AGREEMENT_ID_FIELD_NAME, "ex")
            .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, "type")
            .withEventDescription("Create | Data Group | Successful | name Name, service agreement ID ex, type type");
        List<AuditMessage> actualEventList = createDataGroupDescriptor
            .getSuccessEventDataList(joinPoint, getResponseEntity(dataGroupsPostResponseBody, HttpStatus.CREATED));
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getInitEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupItemSystemBase presentationFunctionGroup = new DataGroupItemSystemBase()
            .description("DE-01")
            .name(dgName)
            .externalServiceAgreementId("ex")
            .type("type");

        when(joinPoint.getArgs()).thenReturn(singletonList(presentationFunctionGroup).toArray());

        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.INITIATED)
            .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, dgName)
            .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, "DE-01")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ex")
            .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, "type")
            .withEventDescription("Create | Data Group | Initiated | name Name, external service agreement ID ex, "
                + "type type");

        List<AuditMessage> actualEventList = createDataGroupDescriptor.getInitEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }

    @Test
    public void getFailedEventDataListWithExternalSaId() {
        String dgName = "Name";

        DataGroupItemSystemBase presentationFunctionGroup = new DataGroupItemSystemBase()
            .description("DE-01")
            .name(dgName)
            .externalServiceAgreementId("ex")
            .type("type");

        when(joinPoint.getArgs())
            .thenReturn(singletonList(presentationFunctionGroup).toArray());
        AuditMessage expectedEventList = new AuditMessage()
            .withStatus(Status.FAILED)
            .withEventMetaDatum(DATA_GROUP_NAME_FIELD_NAME, dgName)
            .withEventMetaDatum(DATA_GROUP_DESCRIPTION_FIELD_NAME, "DE-01")
            .withEventMetaDatum(SERVICE_AGREEMENT_EXTERNAL_ID_FIELD_NAME, "ex")
            .withEventMetaDatum(DATA_GROUP_TYPE_FIELD_NAME, "type")
            .withEventDescription("Create | Data Group | Failed | name Name, external service agreement ID ex, "
                + "type type");
        List<AuditMessage> actualEventList = createDataGroupDescriptor.getFailedEventDataList(joinPoint);
        assertEquals(expectedEventList, actualEventList.get(0));
    }
}
