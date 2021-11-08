package com.backbase.account.mock.controllers;

import static com.backbase.account.mock.util.ArrangementDetailsData.getArrangementDetailsDto;

import com.backbase.account.mock.MockConfiguration;
import com.backbase.mock.outbound.details.api.ArrangementDetailsApi;
import com.backbase.mock.outbound.details.model.ArrangementDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controllers for mocked Balance data.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ArrangementDetailsController implements ArrangementDetailsApi {

    private final MockConfiguration configuration;

    /**
     * Method for retrieving latest arrangement details from bank.
     *
     * @param arrangementId external arrangement id
     * @return ArrangementDetailsDto
     */
    @Override
    public ResponseEntity<ArrangementDetailsDto> getArrangementDetails(String arrangementId) {
        LOG.debug("Retrieving details for arrangement with id [{}]", arrangementId);

        if (configuration.isReturnNullDetails()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.ok(getArrangementDetailsDto(arrangementId, configuration.getTenantId()));
        }
    }

}
