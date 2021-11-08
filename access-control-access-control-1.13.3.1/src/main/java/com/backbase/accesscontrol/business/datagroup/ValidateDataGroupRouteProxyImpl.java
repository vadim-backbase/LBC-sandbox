package com.backbase.accesscontrol.business.datagroup;

import static com.backbase.accesscontrol.util.constants.EndpointConstants.DIRECT_DEFAULT_DATA_GROUP_VALIDATE;

import com.backbase.accesscontrol.business.datagroup.dataitems.DataItemsValidationService;
import com.backbase.accesscontrol.dto.DataItemsValidatable;
import com.backbase.accesscontrol.routes.datagroup.ValidateDataGroupRouteProxy;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.camel.Body;
import org.apache.camel.Consume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ValidateDataGroupRouteProxyImpl implements ValidateDataGroupRouteProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateDataGroupRouteProxyImpl.class);
    private Map<String, DataItemsValidationService> dataItemServices;

    /**
     * Constructor.
     * @param dataItemExternalIdConverterServices list of {@link DataItemsValidationService}
     */
    public ValidateDataGroupRouteProxyImpl(List<DataItemsValidationService> dataItemExternalIdConverterServices) {
        this.dataItemServices = dataItemExternalIdConverterServices.stream()
            .collect(Collectors.toMap(DataItemsValidationService::getType, item -> item));
    }

    /**
     * Method that listens on the direct:dataGroupValidate endpoint and uses the service to validate the input.
     *
     * @param body internal request with {@link DataItemsValidatable}
     */
    @Override
    @Consume(value = DIRECT_DEFAULT_DATA_GROUP_VALIDATE)
    public void validate(@Body InternalRequest<DataItemsValidatable> body) {
        DataItemsValidatable data = body.getData();
        validate(data.getType(), data.getItems(), data.getServiceAgreementId());
    }

    private void validate(String type, List<String> items, String serviceAgreementId) {

        if (!items.isEmpty() && dataItemServices.containsKey(type)) {
            LOGGER.info("Validating type {} items", type);
            dataItemServices.get(type).validate(items, serviceAgreementId);
        }
    }
}
