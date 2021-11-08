package com.backbase.account.mock.controllers;

import com.backbase.account.mock.util.RecipientArrangementsData;
import com.backbase.mock.outbound.recipient.api.RecipientArrangementIdsApi;
import com.backbase.mock.outbound.recipient.model.RecipientArrangementIdsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controllers for mocked Balance data.
 */
@RestController
@Slf4j
public class RecipientArrangementIdsController implements RecipientArrangementIdsApi {

    /**
     * Method for retrieving list of recipient from bank.
     *
     * @param arrangementId external arrangement id
     * @return RecipientArrangementIdsDto
     */
    @Override
    public ResponseEntity<RecipientArrangementIdsDto> getRecipientArrangementIds(String arrangementId) {
        LOG.info("Retrieving recipient arrangements for arrangement with id [{}]", arrangementId);
        return ResponseEntity.ok(RecipientArrangementsData.getRecipientArrangements(arrangementId));
    }

}