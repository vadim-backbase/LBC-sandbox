package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.facades.DataGroupServiceFacade;
import com.backbase.accesscontrol.service.rest.spec.api.DataGroupApi;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupItem;
import com.backbase.accesscontrol.service.rest.spec.model.DataGroupsIds;
import com.backbase.pandp.accesscontrol.query.rest.spec.v2.accesscontrol.accessgroups.datagroups.BulkSearchDataGroupsPostRequestBody;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DataGroupQueryController implements DataGroupApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGroupQueryController.class);

    private DataGroupServiceFacade dataGroupServiceFacade;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/accessgroups/data-groups/{id} : Data Group - &#x60;GET&#x60;. # Data Group - &#x60;GET&#x60;
     * Request method GET for fetching data group by internal data group id.    **Warning: Calling this endpoint will
     * bypass the validation of user permissions.**    **Recommendation: Use the corresponding endpoint on presentation
     * service or use Auth Security library.**
     *
     * @param id Data group ID (required)
     * @param includeItems Indicates whether data group items should be returned or not. (optional, default to
     *     true)
     * @return Data group by id retrieved successfully. (status code 200) or BadRequest (status code 400) or NotFound
     *     (status code 404)
     */
    @Override
    public ResponseEntity<DataGroupItem> getDataGroupById(String id, Boolean includeItems) {
        LOGGER.info("GET Request received for retrieving Data Groups by Id {}", id);

        return ResponseEntity.ok(payloadConverter
            .convert(dataGroupServiceFacade.getDataGroupById(id, includeItems), DataGroupItem.class));
    }

    /**
     * GET /accesscontrol/accessgroups/data-groups : Data Groups GET. # Data Groups GET    Request method GET for
     * fetching data groups by serviceAgreementId and/or type.    **Warning: Calling this endpoint will bypass the
     * validation of user permissions.**    **Recommendation: Use the corresponding endpoint on presentation service or
     * use Auth Security library.**
     *
     * @param serviceAgreementId Service Agreement Id (required)
     * @param type Data group type (optional)
     * @param includeItems Defines if the items of the data groups will be returned in the response (optional,
     *     default to true)
     * @return Data groups retrieved successfully. (status code 200) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<List<DataGroupItem>> getDataGroups(String serviceAgreementId, String type,
        Boolean includeItems) {
        LOGGER.info(
            "GET Request received for retrieving Data Groups by "
                + "Service Agreement Id {} and Data Group Type {}", serviceAgreementId, type);
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(dataGroupServiceFacade.getDataGroups(serviceAgreementId, type, includeItems),
                DataGroupItem.class));
    }

    /**
     * POST /accesscontrol/accessgroups/data-groups/bulk/search : Data Groups Bulk POST. # Data Groups Bulk POST Request
     * for fetching data groups by ids.
     *
     * @param dataGroupsIds # Data Groups Bulk POST    Request for fetching data groups by ids. (optional)
     * @return Data Groups retrieved successfully. (status code 200) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<List<DataGroupItem>> postBulkSearchDataGroups(DataGroupsIds dataGroupsIds) {
        return ResponseEntity.ok(payloadConverter
            .convertListPayload(
                dataGroupServiceFacade.getBulkDataGroups(
                    payloadConverter.convertAndValidate(dataGroupsIds, BulkSearchDataGroupsPostRequestBody.class)),
                DataGroupItem.class));
    }
}
