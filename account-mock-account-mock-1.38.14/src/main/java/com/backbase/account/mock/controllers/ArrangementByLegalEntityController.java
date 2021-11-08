package com.backbase.account.mock.controllers;

import static com.backbase.account.mock.util.ArrangementItemsData.getArrangementsByLegalEntityId;

import com.backbase.buildingblocks.presentation.errors.BadRequestException;
import com.backbase.dbs.arrangement.commons.model.DebitCardItemDto;
import com.backbase.mock.outbound.account.link.api.ArrangementsByLegalEntityApi;
import com.backbase.mock.outbound.account.link.model.ArrangementItemDto;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controllers for mocked Balance data.
 */
@RestController
@Slf4j
public class ArrangementByLegalEntityController implements ArrangementsByLegalEntityApi {

    @Override
    public ResponseEntity<List<ArrangementItemDto>> getArrangementsByLegalEntity(String externalLegalEntityId) {

        validateAndGetExternalLegalEntityId(externalLegalEntityId);

        LOG.info("Retrieving arrangement items with legal entity id {}", externalLegalEntityId);

        Set<DebitCardItemDto> debitCards = Sets.newHashSet(
            (DebitCardItemDto) new DebitCardItemDto()
                .withNumber("2")
                .withExpiryDate("2017-11-11")
                .withCardId("id1")
                .withCardholderName("John Doe")
                .withCardType("Visa Electron")
                .withCardStatus("Active"),
            (DebitCardItemDto) new DebitCardItemDto()
                .withNumber("1")
                .withExpiryDate("2016-12-12")
                .withCardId("id2")
                .withCardholderName("Jack Sparrow")
                .withCardType("Maestro")
                .withCardStatus("Active"));

        return ResponseEntity.ok(getArrangementsByLegalEntityId(externalLegalEntityId, debitCards));
    }

    private void validateAndGetExternalLegalEntityId(String externalLegalEntityId) {
        if (Strings.isBlank(externalLegalEntityId)) {
            throw new BadRequestException(("External legal entity id is missing."));
        }
    }
}
