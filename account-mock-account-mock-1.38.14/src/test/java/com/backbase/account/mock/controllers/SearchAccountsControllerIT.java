package com.backbase.account.mock.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.mock.outbound.account.search.model.SearchAccountsRequestDto;
import com.backbase.mock.outbound.account.search.model.SearchAccountsResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.EntityExchangeResult;


class SearchAccountsControllerIT extends AbstractSeachAccountsControllerIT {

    @Test
    void searchAHN() {
        final SearchAccountsRequestDto request =
            (SearchAccountsRequestDto) new SearchAccountsRequestDto()
                .withAccountHolderNames("9");
        final EntityExchangeResult<List<SearchAccountsResponseDto>> response = exchange(request);
        final List<SearchAccountsResponseDto> body = response.getResponseBody();

        assertThat(response)
            .extracting(EntityExchangeResult::getResponseHeaders)
            .extracting(h -> h.getFirst("x-total-count"))
            .isEqualTo("2");

        assertThat(body)
            .isNotNull()
            .hasSize(2);

        assertThat(body.get(0))
            .extracting(SearchAccountsResponseDto::getAccountHolderNames)
            .isEqualTo("Holder 491, Holder 492");
        assertThat(body.get(1))
            .extracting(SearchAccountsResponseDto::getAccountHolderNames)
            .isEqualTo("Holder 591, Holder 592");
    }

    @Test
    void checkFields() {
        final SearchAccountsRequestDto request = new SearchAccountsRequestDto();
        final EntityExchangeResult<List<SearchAccountsResponseDto>> response = exchange(request);
        final List<SearchAccountsResponseDto> body = response.getResponseBody();
        final SearchAccountsResponseDto dto = body.get(5);

        assertThat(dto)
            .extracting("externalId", "name", "BBAN", "BIC")
            .allMatch(v -> ((String) v).contains("47"));
    }

}
