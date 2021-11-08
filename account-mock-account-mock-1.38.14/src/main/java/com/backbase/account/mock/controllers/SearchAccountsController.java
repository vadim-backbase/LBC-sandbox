package com.backbase.account.mock.controllers;

import static com.backbase.account.mock.util.EntityUtil.buildTestInstance;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.backbase.account.mock.MockConfiguration;
import com.backbase.mock.outbound.account.search.api.SearchAccountsApi;
import com.backbase.mock.outbound.account.search.model.SearchAccountsRequestDto;
import com.backbase.mock.outbound.account.search.model.SearchAccountsResponseDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SearchAccountsController implements SearchAccountsApi {

    private final MockConfiguration configuration;
    private final List<SearchAccountsResponseDto> accounts = new ArrayList<>();

    @Override
    public ResponseEntity<List<SearchAccountsResponseDto>> searchAccounts(
        @Valid @NotNull SearchAccountsRequestDto request) {

        final int size = ofNullable(request.getSize()).orElse(10);
        final int from = size * ofNullable(request.getFrom()).orElse(0);

        final List<SearchAccountsResponseDto> filtered = ofNullable(request.getAccountHolderNames())
            .map(ahn -> this.accounts.stream()
                .filter(dto -> dto.getAccountHolderNames().contains(ahn))
                .collect(toList()))
            .orElse(this.accounts);
        List<SearchAccountsResponseDto> results;

        if (from < filtered.size()) {
            results = filtered.subList(from, Math.min(filtered.size(), from + size));
        } else {
            results = emptyList();
        }

        return ResponseEntity.ok()
            .header("x-total-count", Integer.toString(filtered.size()))
            .body(results);
    }

    @PostConstruct
    void postConstruct() {
        this.accounts.clear();

        for (int k = 0; k < 23; k++) {
            final int id = this.configuration.getTenantId() * 314 + k + 42;

            LOG.debug("ID = {}", id);

            this.accounts.add(buildTestInstance("search-accounts-response", SearchAccountsResponseDto.class, id));
        }

        LOG.info("Created {} records, for tenantId {}", this.accounts.size(), this.configuration.getTenantId());
    }
}


