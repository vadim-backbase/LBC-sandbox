package com.backbase.account.mock.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.account.mock.MockConfiguration;
import com.backbase.mock.outbound.account.search.model.SearchAccountsRequestDto;
import com.backbase.mock.outbound.account.search.model.SearchAccountsResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class SearchAccountsControllerTest {

    final MockConfiguration configuration = new MockConfiguration();
    final SearchAccountsController controller = new SearchAccountsController(this.configuration);

    @BeforeEach
    void beforeEach() {
        this.controller.postConstruct();
    }

    @Test
    void searchAny() {
        final SearchAccountsRequestDto request = new SearchAccountsRequestDto();
        final List<SearchAccountsResponseDto> body = this.controller.searchAccounts(request).getBody();

        assertThat(body)
            .isNotNull()
            .hasSize(request.getSize())
            .allSatisfy(dto -> {
                assertThat(dto)
                    .isNotNull()
                    .hasNoNullFieldsOrProperties();
            });
    }

    @Test
    void searchAHN() {
        final SearchAccountsRequestDto request =
            (SearchAccountsRequestDto) new SearchAccountsRequestDto().withAccountHolderNames("9");
        final ResponseEntity<List<SearchAccountsResponseDto>> response = this.controller.searchAccounts(request);
        final List<SearchAccountsResponseDto> body = response.getBody();

        assertThat(response)
            .extracting(ResponseEntity::getHeaders)
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
    void lastPage() {
        final SearchAccountsRequestDto request =
            (SearchAccountsRequestDto) new SearchAccountsRequestDto()
                .withFrom(2);
        final ResponseEntity<List<SearchAccountsResponseDto>> response = this.controller.searchAccounts(request);
        final List<SearchAccountsResponseDto> body = response.getBody();

        assertThat(response)
            .extracting(ResponseEntity::getHeaders)
            .extracting(h -> h.getFirst("x-total-count"))
            .isEqualTo("23");

        assertThat(body)
            .isNotNull()
            .hasSize(3);
    }

    @Test
    void searchWithoutTenant() {
        final SearchAccountsRequestDto request = new SearchAccountsRequestDto();
        final List<SearchAccountsResponseDto> body = this.controller.searchAccounts(request).getBody();
        final SearchAccountsResponseDto dto = body.get(5);

        assertThat(dto)
            .extracting("externalId", "name", "BBAN", "BIC")
            .allMatch(v -> ((String) v).contains("47"));
    }

    @Test
    void searchWithTenant() {
        this.configuration.setTenantId(2);
        this.controller.postConstruct();

        final SearchAccountsRequestDto request = new SearchAccountsRequestDto();
        final List<SearchAccountsResponseDto> body = this.controller.searchAccounts(request).getBody();
        final SearchAccountsResponseDto dto = body.get(7);

        assertThat(dto)
            .extracting("externalId", "name", "BBAN", "BIC")
            .allMatch(v -> ((String) v).contains("677"));
    }

}


