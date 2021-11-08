package com.backbase.accesscontrol.business.serviceagreement.participant;

import static com.backbase.accesscontrol.domain.dto.PresentationActionDto.fromValue;
import static com.backbase.accesscontrol.domain.enums.ItemStatusCode.HTTP_STATUS_OK;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.batch.BatchProcessItemOrderProcessor;
import com.backbase.accesscontrol.business.batch.InvalidParticipantItem;
import com.backbase.accesscontrol.business.batch.ProcessableBatchBody;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.mappers.BatchResponseItemExtendedMapper;
import com.backbase.accesscontrol.service.batch.serviceagreement.UpdateServiceAgreementParticipant;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseItemExtended;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.BatchResponseStatusCode;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantsPut;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.assertj.core.util.Lists;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IngestParticipantUpdateProcessorTest {

    @InjectMocks
    private IngestParticipantUpdateProcessor ingestParticipantUpdateProcessor;
    @Mock
    private BatchProcessItemOrderProcessor batchProcessItemOrderProcessor;
    @Mock
    private IngestParticipantUpdateRemoveProcessor removeProcessor;
    @Mock
    private Validator validator;
    @Mock
    private UpdateServiceAgreementParticipant updateServiceAgreementParticipant;
    @Spy
    private BatchResponseItemExtendedMapper batchResponseItemMapperExtended =
        Mappers.getMapper(BatchResponseItemExtendedMapper.class);

    @Test
    public void shouldReturnAllItemsProcessedAndInTheSameOrder() {
        when(validator.validate(any()))
            .thenReturn(newHashSet());
        PresentationParticipantPutBody invalidBody = new PresentationParticipantPutBody()
            .withAction(PresentationAction.ADD)
            .withExternalServiceAgreementId("id")
            .withExternalParticipantId("id");
        PresentationParticipantPutBody invalidRemoveBody = new PresentationParticipantPutBody()
            .withAction(PresentationAction.REMOVE)
            .withExternalServiceAgreementId("id")
            .withExternalParticipantId("not-removable");
        PresentationParticipantPutBody processableBody = new PresentationParticipantPutBody()
            .withAction(PresentationAction.REMOVE)
            .withExternalServiceAgreementId("valid")
            .withExternalParticipantId("valid");
        ConstraintViolation<PresentationParticipantPutBody> constraintViolation = mock(ConstraintViolation.class);

        ProcessableBatchBody<PresentationParticipantPutBody> invalidRemove = new ProcessableBatchBody<>(
            invalidRemoveBody, 3);
        ProcessableBatchBody<PresentationParticipantPutBody> batchBody = new ProcessableBatchBody<>(processableBody, 2);
        List<ProcessableBatchBody<PresentationParticipantPutBody>> processableBatchBodies = Lists.newArrayList(
            batchBody,
            invalidRemove
        );
        when(batchProcessItemOrderProcessor.transformProcessableBody(
            Lists.newArrayList(
                invalidBody,
                processableBody,
                invalidRemoveBody
            )
        ))
            .thenReturn(
                Lists.newArrayList(
                    new ProcessableBatchBody<>(invalidBody, 1),
                    batchBody,
                    invalidRemove
                )
            );
        when(constraintViolation.getMessage())
            .thenReturn("invalid body message");

        when(constraintViolation.getPropertyPath())
            .thenReturn(PathImpl.createPathFromString("path"));

        when(validator.validate(invalidBody))
            .thenReturn(newHashSet(constraintViolation));

        when(removeProcessor.processItems(eq(processableBatchBodies)))
            .thenReturn(Lists.newArrayList(new InvalidParticipantItem(invalidRemove.getOrder(), Lists
                .newArrayList("error1"))));
        ResponseItemExtended validResponseBody = new ResponseItemExtended(
            batchBody.getItem().getExternalParticipantId(),
            batchBody.getItem().getExternalServiceAgreementId(), HTTP_STATUS_OK,
            fromValue(batchBody.getItem().getAction().toString()), new ArrayList<>()
        );
        when(updateServiceAgreementParticipant.processBatchItems(eq(asList(batchBody.getItem()))))
            .thenReturn(Lists.newArrayList(
                validResponseBody
            ));
        PresentationParticipantsPut data = new PresentationParticipantsPut()
            .withParticipants(Lists.newArrayList(
                invalidBody,
                processableBody,
                invalidRemoveBody
            ));
        List<BatchResponseItemExtended> resultingData = ingestParticipantUpdateProcessor
            .processParticipantUpdate(data);
        assertThat(resultingData, hasSize(3));
        assertThat(resultingData, contains(
            new BatchResponseItemExtended()
                .withAction(invalidBody.getAction())
                .withResourceId(invalidBody.getExternalParticipantId())
                .withExternalServiceAgreementId(invalidBody.getExternalServiceAgreementId())
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withErrors(Lists.newArrayList("path invalid body message")),
            new BatchResponseItemExtended()
                .withAction(PresentationAction.REMOVE)
                .withResourceId(validResponseBody.getResourceId())
                .withExternalServiceAgreementId(validResponseBody.getExternalServiceAgreementId())
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_OK),
            new BatchResponseItemExtended()
                .withAction(invalidRemoveBody.getAction())
                .withResourceId(invalidRemoveBody.getExternalParticipantId())
                .withExternalServiceAgreementId(invalidRemoveBody.getExternalServiceAgreementId())
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withErrors(Lists.newArrayList("error1"))
        ));
    }

    @Test
    public void shouldReturnOnlyOneElement() {
        PresentationParticipantPutBody invalidBody = new PresentationParticipantPutBody()
            .withAction(PresentationAction.ADD)
            .withExternalServiceAgreementId("id")
            .withExternalParticipantId("id");
        ConstraintViolation<PresentationParticipantPutBody> constraintViolation = mock(ConstraintViolation.class);

        when(batchProcessItemOrderProcessor.transformProcessableBody(
            Lists.newArrayList(
                invalidBody
            )
        ))
            .thenReturn(
                Lists.newArrayList(
                    new ProcessableBatchBody<>(invalidBody, 1)
                )
            );
        when(constraintViolation.getMessage())
            .thenReturn("invalid body message");

        when(constraintViolation.getPropertyPath())
            .thenReturn(PathImpl.createPathFromString("path"));

        when(validator.validate(invalidBody))
            .thenReturn(newHashSet(constraintViolation));

        when(removeProcessor.processItems(eq(Lists.newArrayList())))
            .thenReturn(Lists.newArrayList());
        PresentationParticipantsPut data = new PresentationParticipantsPut()
            .withParticipants(Lists.newArrayList(
                invalidBody
            ));
        List<BatchResponseItemExtended> resultingData = ingestParticipantUpdateProcessor
            .processParticipantUpdate(data);
        assertThat(resultingData, hasSize(1));
        assertThat(resultingData, contains(
            new BatchResponseItemExtended()
                .withAction(invalidBody.getAction())
                .withResourceId(invalidBody.getExternalParticipantId())
                .withExternalServiceAgreementId(invalidBody.getExternalServiceAgreementId())
                .withStatus(BatchResponseStatusCode.HTTP_STATUS_BAD_REQUEST)
                .withErrors(Lists.newArrayList("path invalid body message"))
        ));
    }
}