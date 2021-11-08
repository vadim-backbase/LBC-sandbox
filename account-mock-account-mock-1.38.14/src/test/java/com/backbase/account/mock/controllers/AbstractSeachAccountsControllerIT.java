package com.backbase.account.mock.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.backbase.account.mock.MockApplication;
import com.backbase.mock.outbound.account.search.model.SearchAccountsRequestDto;
import com.backbase.mock.outbound.account.search.model.SearchAccountsResponseDto;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;



@ActiveProfiles("it")
@SpringBootTest(classes = {MockApplication.class}, webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
public abstract class AbstractSeachAccountsControllerIT {

    private static final String SEARCH_URI = "/service-api/v1/search-accounts";
    private static final ParameterizedTypeReference<List<SearchAccountsResponseDto>> RESPONSE_TYPE =
        new ParameterizedTypeReference<>() {};

    @Autowired
    private WebTestClient webClient;

    protected EntityExchangeResult<List<SearchAccountsResponseDto>> exchange(final SearchAccountsRequestDto request) {
        return this.webClient
            .post().uri(SEARCH_URI).bodyValue(request)
            .exchange().expectBody(RESPONSE_TYPE)
            .returnResult();
    }

    @Test
    void searchAny() {
        final SearchAccountsRequestDto request = new SearchAccountsRequestDto();
        final EntityExchangeResult<List<SearchAccountsResponseDto>> response = exchange(request);
        final List<SearchAccountsResponseDto> body = response.getResponseBody();

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
    void lastPage() {
        final SearchAccountsRequestDto request =
            (SearchAccountsRequestDto) new SearchAccountsRequestDto()
                .withFrom(2);
        final EntityExchangeResult<List<SearchAccountsResponseDto>> response = exchange(request);
        final List<SearchAccountsResponseDto> body = response.getResponseBody();


        assertThat(response)
            .extracting(EntityExchangeResult::getResponseHeaders)
            .extracting(h -> h.getFirst("x-total-count"))
            .isEqualTo("23");

        assertThat(body)
            .isNotNull()
            .hasSize(3);
    }

}

