package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.domain.dto.PresentationActionDto.fromValue;
import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.UPDATE;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.impl.ParticipantService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import java.util.List;
import javax.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class UpdateServiceAgreementParticipant extends
    LeanGenericBatchProcessor<PresentationParticipantPutBody, ResponseItemExtended, String> {

    private ParticipantService participantService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    public UpdateServiceAgreementParticipant(Validator validator, EventBus eventBus,
        ParticipantService participantService,
        ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService) {
        super(validator, eventBus);
        this.participantService = participantService;
        this.serviceAgreementBusinessRulesService = serviceAgreementBusinessRulesService;
    }

    @Override
    protected String performBatchProcess(PresentationParticipantPutBody item) {
        if (serviceAgreementBusinessRulesService
            .isServiceAgreementInPendingStateByExternalId(item.getExternalServiceAgreementId())) {

            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
        serviceAgreementBusinessRulesService
            .checkPendingDeleteOfFunctionOrDataGroupInServiceAgreementExternalId(item.getExternalServiceAgreementId());
        serviceAgreementBusinessRulesService
            .checkPendingValidationsInServiceAgreementExternalServiceAgreementId(item.getExternalServiceAgreementId());

        return participantService.updateServiceAgreementParticipants(item);
    }

    /**
     * Create single response item.
     *
     * @return single batch response.
     */
    @Override
    protected ResponseItemExtended getBatchResponseItem(PresentationParticipantPutBody item,
        ItemStatusCode statusCode, List<String> errorMessages) {
        return new ResponseItemExtended(item.getExternalParticipantId(), item.getExternalServiceAgreementId(),
            statusCode, fromValue(item.getAction().toString()), errorMessages);
    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request    The request.
     * @param internalId the internal id of the item.
     * @return The event to fire.
     */
    @Override
    protected Event createEvent(PresentationParticipantPutBody request, String internalId) {
        return new ServiceAgreementEvent().withAction(UPDATE).withId(internalId);
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }
}
