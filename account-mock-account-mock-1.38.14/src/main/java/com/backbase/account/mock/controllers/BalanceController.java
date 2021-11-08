package com.backbase.account.mock.controllers;

import com.backbase.mock.outbound.balance.api.BalanceApi;
import com.backbase.mock.outbound.balance.model.BalanceItemDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controllers for mocked Balance data.
 */
@RestController
@Slf4j
public class BalanceController implements BalanceApi {

    private static final int ORIGIN = 1000;
    private static final int BOUND = 10000;

    /**
     * Method for retrieving balances by arrangement ID.
     *
     * @param arrangementIds ID of the arrangement of the balance that needs to be retrieved
     * @return balance
     */
    @Override
    public ResponseEntity<List<BalanceItemDto>> getBalances(Set<String> arrangementIds) {
        LOG.debug("Retrieving balances for arrangementIds [{}]", arrangementIds);
        return ResponseEntity.ok(arrangementIds.stream()
            .map(this::createBalance)
            .collect(Collectors.toList()));
    }

    private BalanceItemDto createBalance(String arrangementId) {
        BalanceItemDto balance = new BalanceItemDto()
            .withArrangementId(arrangementId)
            .withBookedBalance(new BigDecimal(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND)))
            .withAvailableBalance(new BigDecimal(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND)))
            .withCurrentInvestmentValue(new BigDecimal(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND)))
            .withCreditLimit(new BigDecimal(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND)))
            .withOutstandingPrincipalAmount(new BigDecimal(ThreadLocalRandom.current().nextInt(ORIGIN, BOUND)));

        balance.setAddition("checkingBalance", "100");
        balance.setAddition("loanBalance", "200");

        return balance;
    }

}
