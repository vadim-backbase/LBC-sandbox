package com.backbase.accesscontrol.api.service;

import com.backbase.accesscontrol.mappers.model.PayloadConverter;
import com.backbase.accesscontrol.service.FunctionGroupService;
import com.backbase.accesscontrol.service.rest.spec.api.FunctionGroupApi;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupItem;
import com.backbase.accesscontrol.service.rest.spec.model.FunctionGroupsIds;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class FunctionGroupQueryController implements FunctionGroupApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionGroupQueryController.class);
    private FunctionGroupService functionGroupService;
    private PayloadConverter payloadConverter;

    /**
     * GET /accesscontrol/accessgroups/function-groups/{id} : Function Group GET. # Function Group GET    Request method
     * GET for fetching function group by id.    **Warning: Calling this endpoint will bypass the validation of user
     * permissions.**    **Recommendation: Use the corresponding endpoint on presentation service or use Auth Security
     * library.**
     *
     * @param id Function group id (required)
     * @return Function group retrieved. (status code 200) or BadRequest (status code 400) or NotFound (status code 404)
     */
    @Override
    public ResponseEntity<FunctionGroupItem> getFunctionGroupById(String id) {

        LOGGER.info("Getting function group by id {}", id);
        return ResponseEntity.ok(payloadConverter
            .convert(functionGroupService.getFunctionGroupById(id), FunctionGroupItem.class));
    }

    /**
     * GET /accesscontrol/accessgroups/function-groups : Function Groups GET. # Function Groups GET    Request method
     * GET for fetching function groups by serviceAgreementId.    **Warning: Calling this endpoint will bypass the
     * validation of user permissions.**    **Recommendation: Use the corresponding endpoint on presentation service or
     * use Auth Security library.**
     *
     * @param serviceAgreementId Service Agreement id (required)
     * @return List of function groups retrieved. (status code 200) or BadRequest (status code 400)
     */
    @Override
    public ResponseEntity<List<FunctionGroupItem>> getFunctionGroups(String serviceAgreementId) {
        LOGGER.info("Getting Function Groups by service agreement id {}", serviceAgreementId);

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(functionGroupService.getFunctionGroupsByServiceAgreementId(serviceAgreementId),
                FunctionGroupItem.class));
    }

    /**
     * POST /accesscontrol/accessgroups/function-groups/bulk : Function Groups Bulk POST. # Function Groups Bulk POST
     * Request for fetching function groups by ids.
     *
     * @param functionGroupsIds # Function Groups Bulk POST    Request for fetching function groups by ids.
     *     (optional)
     * @return Function Groups retrieved successfully. (status code 200) or # Reasons for getting HTTP status code 400:
     *     * One of the function group ids does not exists    | Message | key   --- | --- |  |Function group does not
     *     exist.|functionAccessGroup.get.error.message.E_NOT_EXISTS| (status code 400)
     */
    @Override
    public ResponseEntity<List<FunctionGroupItem>> postBulkFunctionGroups(FunctionGroupsIds functionGroupsIds) {
        LOGGER.info("Getting Bulk Function Groups");

        return ResponseEntity.ok(payloadConverter
            .convertListPayload(functionGroupService.getBulkFunctionGroups(functionGroupsIds.getIds()),
                FunctionGroupItem.class));
    }
}
