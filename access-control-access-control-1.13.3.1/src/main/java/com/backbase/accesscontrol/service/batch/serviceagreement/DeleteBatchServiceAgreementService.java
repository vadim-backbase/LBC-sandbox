package com.backbase.accesscontrol.service.batch.serviceagreement;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.AccessGroupErrorCodes.ERR_AG_105;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.backbase.accesscontrol.business.serviceagreement.service.ServiceAgreementBusinessRulesService;
import com.backbase.accesscontrol.domain.dto.ResponseItem;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.accesscontrol.util.validation.AccessToken;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.ServiceAgreementEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationDeleteServiceAgreements;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.PresentationServiceAgreementIdentifier;
import com.google.common.collect.Lists;
import java.util.List;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteBatchServiceAgreementService extends
    LeanGenericBatchProcessor<PresentationServiceAgreementIdentifier, ResponseItem, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBatchServiceAgreementService.class);

    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    private ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService;

    private AccessToken accessToken;

    /**
     * Constructor.
     *
     * @param validator                          - validator service
     * @param persistenceServiceAgreementService - service agreement service
     * @param accessToken                        - token
     */
    public DeleteBatchServiceAgreementService(Validator validator,
        EventBus eventBus,
        PersistenceServiceAgreementService persistenceServiceAgreementService,
        AccessToken accessToken, ServiceAgreementBusinessRulesService serviceAgreementBusinessRulesService) {
        super(validator, eventBus);
        this.persistenceServiceAgreementService = persistenceServiceAgreementService;
        this.accessToken = accessToken;
        this.serviceAgreementBusinessRulesService = serviceAgreementBusinessRulesService;
    }

    /**
     * Batch delete service agreement.
     *
     * @param serviceAgreementBatch that need to be deleted.
     * @return List of BatchResponseItem.
     */
    public List<ResponseItem> deleteBatchServiceAgreement(
        PresentationDeleteServiceAgreements serviceAgreementBatch) {
        LOGGER.info("Deleting batch service agreement...");
        accessToken.validateAccessToken(serviceAgreementBatch.getAccessToken(), serviceAgreementBatch);

        return processBatchItems(serviceAgreementBatch.getServiceAgreementIdentifiers());
    }

    @Override
    protected String performBatchProcess(PresentationServiceAgreementIdentifier listServiceAgreementIdentifiers) {
        if ((nonNull(listServiceAgreementIdentifiers.getIdIdentifier())
            && serviceAgreementBusinessRulesService.isServiceAgreementInPendingState(
            listServiceAgreementIdentifiers.getIdIdentifier()))
            || (nonNull(listServiceAgreementIdentifiers.getExternalIdIdentifier())
            && serviceAgreementBusinessRulesService.isServiceAgreementInPendingStateByExternalId(
            listServiceAgreementIdentifiers.getExternalIdIdentifier()))) {

            LOGGER.warn("Service agreement {} is in pending state.", listServiceAgreementIdentifiers);
            throw getBadRequestException(ERR_AG_105.getErrorMessage(), ERR_AG_105.getErrorCode());
        }
        return persistenceServiceAgreementService
            .deleteServiceAgreementByIdentifier(listServiceAgreementIdentifiers);
    }

    @Override
    protected List<String> customValidateConstraintsForRequestBody(
        PresentationServiceAgreementIdentifier requestBody) {
        boolean notProvidedIdIdentifier = isEmpty(requestBody.getIdIdentifier());
        boolean notProvidedNameIdentifier = isEmpty(requestBody.getNameIdentifier());
        boolean notProvidedExternalIdIdentifier = isEmpty(requestBody.getExternalIdIdentifier());
        //Ternary Elusive Or (XOR)
        if ((notProvidedIdIdentifier ^ notProvidedNameIdentifier ^ notProvidedExternalIdIdentifier) ^ (
            notProvidedIdIdentifier && notProvidedNameIdentifier && notProvidedExternalIdIdentifier) || (
            notProvidedIdIdentifier && notProvidedNameIdentifier && notProvidedExternalIdIdentifier)) {
            return Lists.newArrayList("Identifier is not valid.");
        }
        return super.customValidateConstraintsForRequestBody(requestBody);

    }

    /**
     * Create the event indicating the successful execution of the request.
     *
     * @param request    The request.
     * @param internalId the internal id of the item.
     * @return The event to fire.
     */
    @Override
    protected ServiceAgreementEvent createEvent(PresentationServiceAgreementIdentifier request, String internalId) {
        return new ServiceAgreementEvent().withId(internalId).withAction(Action.DELETE);
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
    protected ResponseItem getBatchResponseItem(PresentationServiceAgreementIdentifier item,
        ItemStatusCode statusCode, List<String> errorMessages) {
        ResponseItem batchResponseItem = new ResponseItem(null, statusCode, errorMessages);
        if (isNotEmpty(item.getIdIdentifier())) {
            batchResponseItem.setResourceId(item.getIdIdentifier());
        }
        if (isNotEmpty(item.getExternalIdIdentifier())) {
            batchResponseItem.setResourceId(item.getExternalIdIdentifier());
        }
        if (isNotEmpty(item.getNameIdentifier())) {
            batchResponseItem.setResourceId(item.getNameIdentifier());
        }
        return batchResponseItem;
    }
}
