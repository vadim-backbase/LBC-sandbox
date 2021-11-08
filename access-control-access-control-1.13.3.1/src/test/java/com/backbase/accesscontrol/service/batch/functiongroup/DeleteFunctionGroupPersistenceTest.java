package com.backbase.accesscontrol.service.batch.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.EnvelopedEvent;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DeleteFunctionGroupPersistenceTest {

    @Mock
    private FunctionGroupService functionGroupService;

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    @Mock
    private EventBus eventBus;

    @Mock
    private Validator validator;

    @InjectMocks
    private DeleteFunctionGroupPersistence deleteFunctionGroupPersistence;

    @Test
    public void shouldReturnValidDeletedFunctionGroup() {
        String functionGroupId = "id";
        String functionGroupName = "name";
        String externalServiceAgreementId = "externalServiceAgreementId";
        String internalServiceAgreementId = "internalServiceAgreementId";
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId(internalServiceAgreementId);

        when(persistenceServiceAgreementService.getServiceAgreementByExternalId(externalServiceAgreementId))
            .thenReturn(serviceAgreement);
        when(functionGroupService
            .getFunctionGroupsByNameAndServiceAgreementId(functionGroupName, internalServiceAgreementId))
            .thenReturn(functionGroupId);

        PresentationIdentifier body1 = new PresentationIdentifier().withIdIdentifier(functionGroupId);
        PresentationIdentifier body2 = new PresentationIdentifier().withNameIdentifier(
            new NameIdentifier().withName(functionGroupName)
                .withExternalServiceAgreementId(externalServiceAgreementId));

        ResponseItemExtended expectedBean = new ResponseItemExtended(functionGroupId, serviceAgreement.getExternalId(),
            ItemStatusCode.HTTP_STATUS_OK, null, new ArrayList<>());

        List<ResponseItemExtended> batchResponseItems = deleteFunctionGroupPersistence
            .processBatchItems(Arrays.asList(body1, body2));
        assertThat(batchResponseItems, hasSize(2));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
        verify(eventBus, times(2)).emitEvent(ArgumentMatchers.any(EnvelopedEvent.class));
    }

    @Test
    public void shouldReturnBadRequestWhenDeleteFailed() {
        String functionGroupId = "id";
        ResponseItemExtended expectedBean = new ResponseItemExtended(functionGroupId, null,
            ItemStatusCode.HTTP_STATUS_BAD_REQUEST, null, asList("error-message"));

        doThrow(getBadRequestException("error-message", null))
            .when(functionGroupService).deleteFunctionGroup(eq(functionGroupId));

        List<ResponseItemExtended> batchResponseItems = deleteFunctionGroupPersistence
            .processBatchItems(asList(new PresentationIdentifier().withIdIdentifier(functionGroupId)));

        assertThat(batchResponseItems, hasSize(1));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnNotFoundRequestWhenDeleteFailed() {
        String functionGroupId = "id";
        ResponseItemExtended expectedBean = new ResponseItemExtended(functionGroupId, null,
            ItemStatusCode.HTTP_STATUS_NOT_FOUND, null, asList("error-message"));

        doThrow(getNotFoundException("error-message", null))
            .when(functionGroupService)
            .deleteFunctionGroup(
                eq(functionGroupId));

        List<ResponseItemExtended> batchResponseItems = deleteFunctionGroupPersistence
            .processBatchItems(asList(new PresentationIdentifier().withIdIdentifier(functionGroupId)));

        assertThat(batchResponseItems, hasSize(1));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnInvalidRequestWhenDeleteFailed() {
        String functionGroupId = "id";
        ResponseItemExtended expectedBean = new ResponseItemExtended(functionGroupId, null,
            ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR, null, asList("error-message"));

        doThrow(new RuntimeException("error-message"))
            .when(functionGroupService)
            .deleteFunctionGroup(eq(functionGroupId));

        List<ResponseItemExtended> batchResponseItems = deleteFunctionGroupPersistence
            .processBatchItems(asList(new PresentationIdentifier()
                .withIdIdentifier(functionGroupId)));

        assertThat(batchResponseItems, hasSize(1));
        assertThat(batchResponseItems, hasItem(samePropertyValuesAs(expectedBean)));
    }

}