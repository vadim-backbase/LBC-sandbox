package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_055;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.buildingblocks.presentation.errors.Error;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationAction;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreement.PresentationParticipantPutBody;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ParticipantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantService.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private Validator validator;

    public ParticipantService(PersistenceServiceAgreementService persistenceServiceAgreementService, Validator validator) {
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
        this.validator = validator;
    }

    /**
     * Updates service agreement participants.
     * @param item - object of {@link PresentationParticipantPutBody}
     * @return service agreement id
     */
    public String updateServiceAgreementParticipants(PresentationParticipantPutBody item) {
        if (PresentationAction.ADD.equals(item.getAction())) {
            LOGGER.info("Adding participant to service agreement with external id {}",
                item.getExternalServiceAgreementId());
            Set<ConstraintViolation<PresentationParticipantPutBody>> violations = validator.validate(item);
            if (violations.isEmpty()) {
                return persistenceServiceAgreementService.addParticipant(item);
            } else {
                LOGGER.warn("Participant {} cannot be added in service agreement .", item);
                throw new BadRequestException()
                    .withErrors(createViolationErrors(violations))
                    .withMessage(ERR_ACC_055.getErrorMessage());
            }
        } else {
            LOGGER.info("Removing participant from service agreement with external id {}",
                item.getExternalServiceAgreementId());
            return persistenceServiceAgreementService.removeParticipant(item);
        }
    }

    private List<Error> createViolationErrors(Set<ConstraintViolation<PresentationParticipantPutBody>> violations) {
        return violations.stream().map(violation -> new Error()
            .withMessage(violation.getMessage())
            .withKey(violation.getPropertyPath().toString()))
            .collect(Collectors.toList());
    }
}
