package com.backbase.accesscontrol.service.batch.functiongroup;

import static com.backbase.pandp.accesscontrol.event.spec.v1.Action.DELETE;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.NameIdentifier;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.PresentationIdentifier;
import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.Validator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Delete function group.
 */
@Service
public class DeleteFunctionGroupPersistence extends
    LeanGenericBatchProcessor<PresentationIdentifier, ResponseItemExtended, String> {

    private static final String DUPLICATED_IDENTIFIERS_MESSAGE = "Multiple identifiers detected, single expected";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFunctionGroupPersistence.class);

    private FunctionGroupService functionGroupService;
    private PersistenceServiceAgreementService persistenceServiceAgreementService;

    /**
     * Constructor for {@link DeleteFunctionGroupPersistence} class.
     *
     * @param validator                          - validator service
     * @param eventBus                           - eventBus
     * @param functionGroupService               - function group service
     * @param persistenceServiceAgreementService - service agreement service
     */
    public DeleteFunctionGroupPersistence(Validator validator, EventBus eventBus,
        FunctionGroupService functionGroupService,
        PersistenceServiceAgreementService persistenceServiceAgreementService) {
        super(validator, eventBus);
        this.functionGroupService = functionGroupService;
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
    }

    @Override
    protected String performBatchProcess(PresentationIdentifier presentationIdentifier) {
        LOGGER.info("Deleting function group with ID {}", presentationIdentifier);
        return functionGroupService.deleteFunctionGroup(idProvider(presentationIdentifier));
    }

    @Override
    protected List<String> customValidateConstraintsForRequestBody(PresentationIdentifier requestItem) {
        if (requestItem != null && !hasSingleIdentifier(requestItem)) {
            return Lists.newArrayList(DUPLICATED_IDENTIFIERS_MESSAGE);
        }
        return super.customValidateConstraintsForRequestBody(requestItem);
    }

    private boolean hasSingleIdentifier(PresentationIdentifier identifier) {
        return identifier.getIdIdentifier() == null
            ^ identifier.getNameIdentifier() == null;
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }

    /**
     * Create single response item.
     *
     * @return single batch response.
     */
    @Override
    protected ResponseItemExtended getBatchResponseItem(PresentationIdentifier item, ItemStatusCode statusCode,
        List<String> errorMessages) {
        String resourceId = null;
        String externalServiceAgreementId = null;
        if (item != null) {
            if (!StringUtils.isEmpty(item.getIdIdentifier())) {
                resourceId = item.getIdIdentifier();
            } else {
                NameIdentifier nameIdentifier = item.getNameIdentifier();
                if (!ObjectUtils.isEmpty(nameIdentifier)) {
                    resourceId = nameIdentifier.getName();
                    externalServiceAgreementId = nameIdentifier.getExternalServiceAgreementId();
                }
            }
        }
        return new ResponseItemExtended(resourceId, externalServiceAgreementId, statusCode, null, errorMessages);
    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request    The request.
     * @param internalId the internal id of the item.
     * @return The event to fire.
     */
    @Override
    protected FunctionGroupEvent createEvent(PresentationIdentifier request, String internalId) {
        return new FunctionGroupEvent()
            .withAction(DELETE)
            .withId(internalId);
    }

    private String idProvider(PresentationIdentifier presentationIdentifier) {
        if (presentationIdentifier.getIdIdentifier() != null) {
            return presentationIdentifier.getIdIdentifier();
        } else {
            ServiceAgreement serviceAgreement = persistenceServiceAgreementService.getServiceAgreementByExternalId(
                presentationIdentifier.getNameIdentifier().getExternalServiceAgreementId());
            return functionGroupService
                .getFunctionGroupsByNameAndServiceAgreementId(presentationIdentifier.getNameIdentifier().getName(),
                    serviceAgreement.getId());
        }
    }
}
