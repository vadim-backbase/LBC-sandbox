package com.backbase.accesscontrol.business.persistence.datagroup;

import com.backbase.accesscontrol.business.persistence.LeanGenericEventEmitter;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.dto.parameterholder.EmptyParameterHolder;
import com.backbase.accesscontrol.service.DataGroupService;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.buildingblocks.persistence.model.Event;
import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.DataGroupEvent;
import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.datagroups.DataGroupsPostResponseBody;
import java.util.Objects;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddDataGroupHandler extends
    LeanGenericEventEmitter<EmptyParameterHolder, DataGroupBase, DataGroupsPostResponseBody> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDataGroupHandler.class);

    private DataGroupService dataGroupService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    public AddDataGroupHandler(EventBus eventBus, DataGroupService dataGroupService,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {
        super(eventBus);
        this.dataGroupService = dataGroupService;
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("squid:S2139")
    protected DataGroupsPostResponseBody executeRequest(EmptyParameterHolder parameterHolder,
        DataGroupBase requestData) {
        LOGGER.info("Handling execute request for saving data group {}", requestData);

        try {
            if (Objects.nonNull(requestData.getExternalServiceAgreementId())) {
                ServiceAgreement serviceAgreement = persistenceServiceAgreementService
                    .getServiceAgreementByExternalId(requestData.getExternalServiceAgreementId());
                requestData.setServiceAgreementId(serviceAgreement.getId());
            }

            String savedDataGroupId = dataGroupService.save(requestData);
            return new DataGroupsPostResponseBody()
                .withId(savedDataGroupId);
        } catch (Exception err) {
            Throwable root = ExceptionUtils.getRootCause(err);
            LOGGER.warn("The execution of the request for saving data group with name {} has encountered exception {}",
                requestData.getName(), err.getMessage());
            if (root instanceof BadRequestException) {
                LOGGER.warn(
                    "BadRequestException with message {} on saving data group with name {}.",
                    err.getMessage(), requestData.getName());
                throw (BadRequestException) root;
            }
            throw err;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataGroupEvent createSuccessEvent(EmptyParameterHolder parameterHolder,
        DataGroupBase dataGroupBase,
        DataGroupsPostResponseBody response) {
        LOGGER.info("Creating success event for saved data group with id {}", response.getId());
        return new DataGroupEvent().withId(response.getId()).withAction(Action.ADD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Event createFailureEvent(EmptyParameterHolder parameterHolder,
        DataGroupBase dataGroupBase, Exception failure) {
        LOGGER.info("Creating failed event for saving data group with data {}", dataGroupBase);
        return null;
    }
}
