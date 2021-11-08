package com.backbase.accesscontrol.service.batch.functiongroup;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_052;

import com.backbase.accesscontrol.domain.FunctionGroup;
import com.backbase.accesscontrol.domain.dto.ResponseItemExtended;
import com.backbase.accesscontrol.domain.enums.ItemStatusCode;
import com.backbase.accesscontrol.mappers.FunctionGroupMapper;
import com.backbase.accesscontrol.repository.FunctionGroupJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.LeanGenericBatchProcessor;
import com.backbase.buildingblocks.backend.communication.event.proxy.EventBus;
import com.backbase.pandp.accesscontrol.event.spec.v1.Action;
import com.backbase.pandp.accesscontrol.event.spec.v1.FunctionGroupEvent;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.PresentationFunctionGroupPutRequestBody;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Update batch function group.
 */
@Service
public class UpdateBatchFunctionGroupByIdentifier extends
    LeanGenericBatchProcessor<PresentationFunctionGroupPutRequestBody, ResponseItemExtended, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateBatchFunctionGroupByIdentifier.class);

    private FunctionGroupMapper functionGroupMapper;
    private FunctionGroupService functionGroupService;
    private FunctionGroupJpaRepository functionGroupJpaRepository;

    /**
     * Constructor {@link UpdateBatchFunctionGroupByIdentifier} class.
     *
     * @param validator                  - validator service
     * @param functionGroupMapper        - mapper
     * @param functionGroupService       - function group service
     * @param functionGroupJpaRepository - function group jpa repository
     */
    public UpdateBatchFunctionGroupByIdentifier(Validator validator, EventBus eventBus,
        FunctionGroupMapper functionGroupMapper,
        FunctionGroupService functionGroupService, FunctionGroupJpaRepository functionGroupJpaRepository) {
        super(validator, eventBus);
        this.functionGroupMapper = functionGroupMapper;
        this.functionGroupService = functionGroupService;
        this.functionGroupJpaRepository = functionGroupJpaRepository;
    }

    @Override
    public String performBatchProcess(PresentationFunctionGroupPutRequestBody functionGroup) {
        String functionGroupId;
        if (StringUtils.isEmpty(functionGroup.getIdentifier().getIdIdentifier())) {
            functionGroupId = fillId(functionGroup);
        } else {
            functionGroupId = functionGroup.getIdentifier().getIdIdentifier();
        }

        LOGGER.info("Updating function group {}.", functionGroupId);

        return functionGroupService.updateFunctionGroupWithoutLegalEntity(functionGroupId,
            functionGroupMapper.presentationToFunctionGroupBase(functionGroup));
    }

    private String fillId(PresentationFunctionGroupPutRequestBody functionGroup) {
        Optional<FunctionGroup> functionGroupEntity = functionGroupJpaRepository
            .findByServiceAgreementExternalIdAndName(
                functionGroup.getIdentifier().getNameIdentifier().getExternalServiceAgreementId(),
                functionGroup.getIdentifier().getNameIdentifier().getName());
        if (functionGroupEntity.isPresent()) {
            return functionGroupEntity.get().getId();
        } else {
            throw getBadRequestException(ERR_ACC_052.getErrorMessage(), ERR_ACC_052.getErrorCode());
        }
    }

    @Override
    protected ResponseItemExtended getBatchResponseItem(PresentationFunctionGroupPutRequestBody item,
        ItemStatusCode statusCode, List<String> errorMessages) {
        ResponseItemExtended returnValue = new ResponseItemExtended();
        returnValue.setStatus(statusCode);
        returnValue.setErrors(errorMessages);

        if (!StringUtils.isEmpty(item.getIdentifier().getIdIdentifier())) {
            returnValue.setResourceId(item.getIdentifier().getIdIdentifier());
        } else if (item.getIdentifier().getNameIdentifier() != null) {
            returnValue.setResourceId(item.getIdentifier().getNameIdentifier().getName());
            returnValue.setExternalServiceAgreementId(
                item.getIdentifier().getNameIdentifier().getExternalServiceAgreementId());
        }

        return returnValue;
    }

    @Override
    protected List<String> customValidateConstraintsForRequestBody(
        PresentationFunctionGroupPutRequestBody requestBody) {
        if ((StringUtils.isNotEmpty(requestBody.getIdentifier().getIdIdentifier())
            ^ requestBody.getIdentifier().getNameIdentifier() == null)) {
            return Lists.newArrayList("Identifier is not valid");
        }
        return new ArrayList<>();
    }

    @Override
    protected FunctionGroupEvent createEvent(PresentationFunctionGroupPutRequestBody request, String internalId) {
        return new FunctionGroupEvent()
            .withAction(Action.UPDATE)
            .withId(internalId);
    }

    @Override
    protected boolean sortResponse() {
        return false;
    }
}
