package com.backbase.accesscontrol.service.impl;

import static com.backbase.accesscontrol.util.ExceptionUtil.getBadRequestException;
import static com.backbase.accesscontrol.util.errorcodes.CommandErrorCodes.ERR_ACC_029;

import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.dto.FunctionGroupBase;
import com.backbase.accesscontrol.domain.dto.FunctionGroupIngest;
import com.backbase.accesscontrol.repository.ServiceAgreementJpaRepository;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.IngestFunctionGroupTransformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IngestFunctionGroupTransformServiceImpl implements IngestFunctionGroupTransformService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestFunctionGroupTransformServiceImpl.class);

    private FunctionGroupService functionGroupServiceImpl;
    private ServiceAgreementJpaRepository serviceAgreementJpaRepository;

    public IngestFunctionGroupTransformServiceImpl(FunctionGroupService functionGroupService,
        ServiceAgreementJpaRepository serviceAgreementJpaRepository) {
        this.functionGroupServiceImpl = functionGroupService;
        this.serviceAgreementJpaRepository = serviceAgreementJpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String addFunctionGroup(FunctionGroupIngest requestData) {
        return serviceAgreementJpaRepository.findByExternalId(requestData.getExternalServiceAgreementId())
            .map(serviceAgreement -> functionGroupServiceImpl
                .addFunctionGroup(transformToFunctionGroupBase(requestData, serviceAgreement)))
            .orElseThrow(() -> {
                LOGGER.warn("Unable to ingest function group for not existing service agreement {}",
                    requestData.getExternalServiceAgreementId());
                return getBadRequestException(ERR_ACC_029.getErrorMessage(), ERR_ACC_029.getErrorCode());

            });
    }

    private FunctionGroupBase transformToFunctionGroupBase(FunctionGroupIngest requestData,
        ServiceAgreement serviceAgreement) {
        return new FunctionGroupBase()
            .withName(requestData.getName())
            .withServiceAgreementId(serviceAgreement.getId())
            .withDescription(requestData.getDescription())
            .withPermissions(requestData.getPermissions())
            .withType(requestData.getType())
            .withValidFrom(requestData.getValidFrom())
            .withValidUntil(requestData.getValidUntil())
            .withApsName(requestData.getApsName())
            .withApsId(requestData.getApsId());
    }
}