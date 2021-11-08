package com.backbase.accesscontrol.business.serviceagreement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.LegalEntity;
import com.backbase.accesscontrol.domain.ServiceAgreement;
import com.backbase.accesscontrol.domain.ServiceAgreementState;
import com.backbase.accesscontrol.dto.PaginationDto;
import com.backbase.accesscontrol.dto.SearchAndPaginationParameters;
import com.backbase.accesscontrol.mappers.ServiceAgreementsListMapper;
import com.backbase.accesscontrol.service.impl.PersistenceServiceAgreementService;
import com.backbase.buildingblocks.backend.internalrequest.InternalRequest;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Status;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceAgreementsTest {

    @Mock
    private PersistenceServiceAgreementService persistenceServiceAgreementService;
    @Mock
    private ServiceAgreementsListMapper serviceAgreementsListMapper;

    @InjectMocks
    private ListServiceAgreements listServiceAgreements;

    @Test
    public void shouldPassIfGetServiceAgreementIsInvoked() {
        String query = "";
        String cursor = "";
        Integer from = 0;
        Integer size = 10;
        String creatorId = "LE-01";
        String serviceAgreementId = "SA-01";
        ArgumentCaptor<SearchAndPaginationParameters> parametersCaptor = ArgumentCaptor
            .forClass(SearchAndPaginationParameters.class);

        List<ServiceAgreement> serviceAgreements = Arrays.asList(
            new ServiceAgreement()
                .withId(serviceAgreementId)
                .withExternalId("extSa01")
                .withName("sa01")
                .withDescription("sa01-description")
                .withState(ServiceAgreementState.DISABLED)
                .withCreatorLegalEntity(new LegalEntity().withId(creatorId))
                .withStartDate(new Date())
                .withEndDate(new Date())
                .withMaster(true)
                .withAdditions(new HashMap<String, String>() {{
                    put("add-01", "add-01-01");
                    put("add-012", "add-01-02");
                }}),
            new ServiceAgreement()
                .withId("SA-02")
                .withExternalId("extSa02")
                .withName("sa02")
                .withDescription("sa02-description")
                .withState(ServiceAgreementState.ENABLED)
                .withCreatorLegalEntity(new LegalEntity().withId(creatorId))
                .withStartDate(new Date())
                .withEndDate(new Date())
                .withMaster(false)
                .withAdditions(new HashMap<String, String>() {{
                    put("add-02", "add-02-01");
                    put("add-022", "add-02-02");
                }})
        );

        ServiceAgreementGetResponseBody sa01 = new ServiceAgreementGetResponseBody()
            .withId(serviceAgreementId)
            .withExternalId("extSa01")
            .withName("sa01")
            .withDescription("sa01-description")
            .withStatus(Status.DISABLED)
            .withCreatorLegalEntity(creatorId)
            .withValidFromDate("2020-01-01")
            .withValidFromTime("12:15:55")
            .withValidUntilDate("2020-01-20")
            .withValidUntilTime("12:15:55")
            .withIsMaster(true);
        sa01.getAdditions().put("add-01", "add-01-01");
        sa01.getAdditions().put("add-012", "add-01-02");
        ServiceAgreementGetResponseBody sa02 = new ServiceAgreementGetResponseBody()
            .withId("SA-02")
            .withExternalId("extSa02")
            .withName("sa02")
            .withDescription("sa02-description")
            .withStatus(Status.ENABLED)
            .withCreatorLegalEntity(creatorId)
            .withValidFromDate("2020-01-01")
            .withValidFromTime("12:15:55")
            .withValidUntilDate("2020-01-20")
            .withValidUntilTime("12:15:55")
            .withIsMaster(false);
        sa02.getAdditions().put("add-02", "add-02-01");
        sa02.getAdditions().put("add-022", "add-02-02");
        List<ServiceAgreementGetResponseBody> saResponse = Arrays.asList(sa01, sa02);


        when(persistenceServiceAgreementService
            .getServiceAgreements(isNull(), eq(creatorId), any(SearchAndPaginationParameters.class)))
            .thenReturn(new PageImpl<>(serviceAgreements));
        when(serviceAgreementsListMapper.mapList(eq(serviceAgreements))).thenReturn(saResponse);

        InternalRequest<PaginationDto<ServiceAgreementGetResponseBody>> serviceAgreementsResponse = listServiceAgreements
            .getServiceAgreements(new InternalRequest<>(), creatorId, query, from, size, cursor);

        verify(persistenceServiceAgreementService)
            .getServiceAgreements(isNull(), eq(creatorId), parametersCaptor.capture());
        verify(serviceAgreementsListMapper).mapList(eq(serviceAgreements));

        SearchAndPaginationParameters parameters = parametersCaptor.getValue();
        assertEquals(query, parameters.getQuery());
        assertEquals(from, parameters.getFrom());
        assertEquals(size, parameters.getSize());
        assertEquals(cursor, parameters.getCursor());

        PaginationDto<ServiceAgreementGetResponseBody> responseBodies = serviceAgreementsResponse.getData();
        assertEquals(saResponse.size(), (long) responseBodies.getTotalNumberOfRecords());
        List<ServiceAgreementGetResponseBody> saResponseList = responseBodies.getRecords();

        ServiceAgreementGetResponseBody firstSa = saResponse.get(0);
        ServiceAgreementGetResponseBody secondSa = saResponse.get(1);
        assertThat(saResponseList, containsInAnyOrder(
            allOf(
                hasProperty("id", is(firstSa.getId())),
                hasProperty("externalId", is(firstSa.getExternalId())),
                hasProperty("name", is(firstSa.getName())),
                hasProperty("description", is(firstSa.getDescription())),
                hasProperty("isMaster", is(firstSa.getIsMaster())),
                hasProperty("status", is(firstSa.getStatus())),
                hasProperty("creatorLegalEntity", is(firstSa.getCreatorLegalEntity()))
            ),
            allOf(
                hasProperty("id", is(secondSa.getId())),
                hasProperty("externalId", is(secondSa.getExternalId())),
                hasProperty("name", is(secondSa.getName())),
                hasProperty("description", is(secondSa.getDescription())),
                hasProperty("isMaster", is(secondSa.getIsMaster())),
                hasProperty("status", is(secondSa.getStatus())),
                hasProperty("creatorLegalEntity", is(secondSa.getCreatorLegalEntity()))
            )
        ));
    }
}
