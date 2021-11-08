package com.backbase.accesscontrol.service.batch.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.ExceptionUtil.getNotFoundException;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroup;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import javax.validation.Validator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateBatchFunctionGroupByIdentifierTest {

    @Mock
    private Validator validator;

    @Mock
    private EventBus eventBus;

    @Mock
    private FunctionGroupMapper functionGroupMapper = Mappers.getMapper(FunctionGroupMapper.class);

    @Mock
    private FunctionGroupService functionGroupService;

    @Mock
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    @InjectMocks
    private UpdateBatchFunctionGroupByIdentifier updateBatchFunctionGroupByIdentifier;

    @Test
    public void shouldReturnBadRequestWhenServiceAgreementWithGivenExternalIdNotFound() {
        String externalServiceAgreementId = "sa.external";
        String nameIdentifier = "someName";
        PresentationFunctionGroupPutRequestBody functionGroup = createCommandFunctionGroupRequestBodyName(null,
            externalServiceAgreementId, nameIdentifier);
        ResponseItemExtended expectedBean = new ResponseItemExtended(nameIdentifier, externalServiceAgreementId,
            ItemStatusCode.HTTP_STATUS_BAD_REQUEST, null,
            singletonList(CommandErrorCodes.ERR_ACC_052.getErrorMessage()));

        when(validator.validate(eq(functionGroup))).thenReturn(new HashSet<>());
        when(functionGroupJpaRepository.findByServiceAgreementExternalIdAndName(anyString(), anyString()))
            .thenReturn(Optional.empty());

        List<ResponseItemExtended> responseItemExtendedList = updateBatchFunctionGroupByIdentifier
            .processBatchItems(Lists.newArrayList(functionGroup));
        assertThat(responseItemExtendedList, hasSize(1));
        assertThat(responseItemExtendedList, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnValidUpdatedFunctionGroup() {
        String internalServiceAgreementId = "sa.id";

        PresentationFunctionGroupPutRequestBody functionGroup = createCommandFunctionGroupRequestBodyId("id", null,
            null);
        ResponseItemExtended expectedBean = new ResponseItemExtended();
        expectedBean.setStatus(ItemStatusCode.HTTP_STATUS_OK);
        expectedBean.setResourceId("id");

        com.backbase.accesscontrol.domain.FunctionGroup functionGroupJpa = new com.backbase.accesscontrol.domain.FunctionGroup();
        functionGroupJpa.setId(internalServiceAgreementId);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("exSaId");
        functionGroupJpa.setServiceAgreement(serviceAgreement);
        functionGroupJpa.setName("someName");

        when(validator.validate(eq(functionGroup))).thenReturn(new HashSet<>());
        FunctionGroupBase functionGroupBase = new FunctionGroupBase().withName("newName");
        when(functionGroupMapper.presentationToFunctionGroupBase(refEq(functionGroup))).thenReturn(functionGroupBase);

        List<ResponseItemExtended> responseItemExtendedList = updateBatchFunctionGroupByIdentifier
            .processBatchItems(Lists.newArrayList(functionGroup));
        verify(functionGroupService).updateFunctionGroupWithoutLegalEntity(eq("id"), refEq(functionGroupBase));
        assertThat(responseItemExtendedList, hasSize(1));
        assertThat(responseItemExtendedList, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnBadRequestWhenUpdateFailed() {
        String internalServiceAgreementId = "sa.id";

        PresentationFunctionGroupPutRequestBody functionGroup = createCommandFunctionGroupRequestBodyId("id", null,
            null);
        ResponseItemExtended expectedBean = new ResponseItemExtended();
        expectedBean.setStatus(ItemStatusCode.HTTP_STATUS_BAD_REQUEST);
        expectedBean.setErrors(singletonList("error-message"));
        expectedBean.setResourceId("id");

        com.backbase.accesscontrol.domain.FunctionGroup functionGroupJpa = new com.backbase.accesscontrol.domain.FunctionGroup();
        functionGroupJpa.setId(internalServiceAgreementId);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("exSaId");
        functionGroupJpa.setServiceAgreement(serviceAgreement);
        functionGroupJpa.setName("someName");

        when(validator.validate(eq(functionGroup))).thenReturn(new HashSet<>());

        FunctionGroupBase functionGroupBase = new FunctionGroupBase().withName("newName");
        when(functionGroupMapper.presentationToFunctionGroupBase(refEq(functionGroup))).thenReturn(functionGroupBase);

        doThrow(getBadRequestException("error-message", null))
            .when(functionGroupService)
            .updateFunctionGroupWithoutLegalEntity(eq("id"), refEq(functionGroupBase));

        List<ResponseItemExtended> responseItemExtendedList = updateBatchFunctionGroupByIdentifier
            .processBatchItems(Lists.newArrayList(functionGroup));
        assertThat(responseItemExtendedList, hasSize(1));
        assertThat(responseItemExtendedList, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnNotFoundRequestWhenUpdateFailed() {
        String internalServiceAgreementId = "sa.id";

        PresentationFunctionGroupPutRequestBody functionGroup = createCommandFunctionGroupRequestBodyId("id", null,
            null);
        ResponseItemExtended expectedBean = new ResponseItemExtended();
        expectedBean.setStatus(ItemStatusCode.HTTP_STATUS_NOT_FOUND);
        expectedBean.setErrors(singletonList("error-message"));
        expectedBean.setResourceId("id");

        com.backbase.accesscontrol.domain.FunctionGroup functionGroupJpa = new com.backbase.accesscontrol.domain.FunctionGroup();
        functionGroupJpa.setId(internalServiceAgreementId);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("exSaId");
        functionGroupJpa.setServiceAgreement(serviceAgreement);
        functionGroupJpa.setName("someName");

        when(validator.validate(eq(functionGroup))).thenReturn(new HashSet<>());

        FunctionGroupBase functionGroupBase = new FunctionGroupBase().withName("newName");
        when(functionGroupMapper.presentationToFunctionGroupBase(refEq(functionGroup))).thenReturn(functionGroupBase);

        doThrow(getNotFoundException("error-message", null))
            .when(functionGroupService)
            .updateFunctionGroupWithoutLegalEntity(
                eq("id"),
                refEq(functionGroupBase)
            );

        List<ResponseItemExtended> responseItemExtendedList = updateBatchFunctionGroupByIdentifier
            .processBatchItems(Lists.newArrayList(functionGroup));
        assertThat(responseItemExtendedList, hasSize(1));
        assertThat(responseItemExtendedList, hasItem(samePropertyValuesAs(expectedBean)));
    }

    @Test
    public void shouldReturnInvalidRequestWhenUpdateFailed() {
        String internalServiceAgreementId = "sa.id";

        PresentationFunctionGroupPutRequestBody functionGroup = createCommandFunctionGroupRequestBodyId("id", null,
            null);
        ResponseItemExtended expectedBean = new ResponseItemExtended();
        expectedBean.setStatus(ItemStatusCode.HTTP_STATUS_INTERNAL_SERVER_ERROR);
        expectedBean.setErrors(singletonList("error-message"));
        expectedBean.setResourceId("id");

        com.backbase.accesscontrol.domain.FunctionGroup functionGroupJpa = new com.backbase.accesscontrol.domain.FunctionGroup();
        functionGroupJpa.setId(internalServiceAgreementId);
        ServiceAgreement serviceAgreement = new ServiceAgreement();
        serviceAgreement.setId("saId");
        serviceAgreement.setExternalId("exSaId");
        functionGroupJpa.setServiceAgreement(serviceAgreement);
        functionGroupJpa.setName("someName");

        when(validator.validate(eq(functionGroup))).thenReturn(new HashSet<>());

        FunctionGroupBase functionGroupBase = new FunctionGroupBase().withName("newName");
        when(functionGroupMapper.presentationToFunctionGroupBase(refEq(functionGroup))).thenReturn(functionGroupBase);

        doThrow(new RuntimeException("error-message"))
            .when(functionGroupService)
            .updateFunctionGroupWithoutLegalEntity(
                eq("id"),
                refEq(functionGroupBase)
            );

        List<ResponseItemExtended> responseItemExtendedList = updateBatchFunctionGroupByIdentifier
            .processBatchItems(Lists.newArrayList(functionGroup));
        assertThat(responseItemExtendedList, hasSize(1));
        assertThat(responseItemExtendedList, hasItem(samePropertyValuesAs(expectedBean)));
    }

    private PresentationFunctionGroupPutRequestBody createCommandFunctionGroupRequestBodyName(String id,
        String externalServiceAgreementId, String nameIdentifier) {
        return new PresentationFunctionGroupPutRequestBody().withIdentifier(
            new PresentationIdentifier().withNameIdentifier(new NameIdentifier().withName(nameIdentifier)
                .withExternalServiceAgreementId(externalServiceAgreementId)))
            .withFunctionGroup(createFunctionGroup("someName1", "someDesc1"));
    }

    private PresentationFunctionGroupPutRequestBody createCommandFunctionGroupRequestBodyId(String id,
        String externalServiceAgreementId, String nameIdentifier) {
        return new PresentationFunctionGroupPutRequestBody().withIdentifier(
            new PresentationIdentifier().withIdIdentifier(id))
            .withFunctionGroup(createFunctionGroup("someName2", "someDesc2"));
    }

    private FunctionGroup createFunctionGroup(String name, String description) {
        return new FunctionGroup().withName(name).withDescription(description);
    }
}