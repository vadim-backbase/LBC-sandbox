package com.backbase.accesscontrol.service.facades;

import static com.backbase.presentation.legalentity.rest.spec.v2.legalentities.enumeration.LegalEntityType.CUSTOMER;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.business.flows.legalentity.CreateLegalEntityFlow;
import com.backbase.accesscontrol.business.flows.legalentity.ExternalLegalEntitySearchFlow;
import com.backbase.accesscontrol.business.flows.legalentity.GetSubLegalEntitlesFlow;
import com.backbase.accesscontrol.business.flows.legalentity.SegmentationLegalEntitySearchFlow;
import com.backbase.accesscontrol.dto.ExternalLegalEntitySearchParameters;
import com.backbase.accesscontrol.dto.GetLegalEntitiesRequestDto;
import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.SegmentationLegalEntitiesSearchParameters;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.LegalEntitiesPostResponseBody;
import com.backbase.presentation.legalentity.rest.spec.v2.legalentities.PresentationCreateLegalEntityItemPostRequestBody;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegalEntityFlowServiceTest {

    @Mock
    private CreateLegalEntityFlow createLegalEntityFlow;

    @Mock
    private ExternalLegalEntitySearchFlow externalLegalEntitySearchFlow;

    @Mock
    private GetSubLegalEntitlesFlow getSubLegalEntitlesFlow;

    @Mock
    private SegmentationLegalEntitySearchFlow segmentationLegalEntitySearchFlow;

    @InjectMocks
    private LegalEntityFlowService testy;

    @Test
    public void shouldCallCreateLegalEntityFlow() {

        LegalEntitiesPostResponseBody responseBody = new LegalEntitiesPostResponseBody().withId("new_le_id");

        when(createLegalEntityFlow.start(ArgumentMatchers.any(PresentationCreateLegalEntityItemPostRequestBody.class)))
            .thenReturn(responseBody);

        PresentationCreateLegalEntityItemPostRequestBody requestBody =
            new PresentationCreateLegalEntityItemPostRequestBody()
                .withActivateSingleServiceAgreement(true)
                .withExternalId("ext_id")
                .withName("name")
                .withType(CUSTOMER)
                .withParentInternalId("p_id");

        testy.createLegalEntityWithInternalParentIdFlow(requestBody);

        verify(createLegalEntityFlow).start(eq(requestBody));
    }

    @Test
    public void shouldCallExternalLegalEntitySearchFlow() {
        when(externalLegalEntitySearchFlow.start(any(ExternalLegalEntitySearchParameters.class)))
            .thenReturn(new RecordsDto<>(0L, new ArrayList<>()));

        testy.getExternalLegalEntityData(new ExternalLegalEntitySearchParameters("f", "t", 0, null, 10));

        verify(externalLegalEntitySearchFlow).start(eq(new ExternalLegalEntitySearchParameters("f", "t", 0, null, 10)));
    }

    @Test
    public void shouldCallSegmentationLegalEntitySearchFlow() {
        SegmentationLegalEntitiesSearchParameters searchParameters = new SegmentationLegalEntitiesSearchParameters();
        searchParameters.setServiceAgreementId("saId");
        searchParameters.setLegalEntityId("leId");
        searchParameters.setUserId("userId");
        searchParameters.setBusinessFunction("bf");
        searchParameters.setPrivilege("privilege");
        searchParameters.setCursor("cursor");
        searchParameters.setFrom(100);
        searchParameters.setSize(10);
        searchParameters.setQuery("searchQuery");

        testy.getSegmentationLegalEntity(searchParameters);

        verify(segmentationLegalEntitySearchFlow).start(eq(searchParameters));
    }

    @Test
    public void shouldCallGetLegalEntitiesFlow() {
        String parentEntityId = "20";
        String cursor = "cursor";
        Integer from = 100;
        Integer size = 10;
        String searchQuery = "searchQuery";
        HashSet<String> excludeIds = newHashSet("1", "2");

        testy.getSubLegalEntities(
            new GetLegalEntitiesRequestDto(parentEntityId, excludeIds, cursor, from, size, searchQuery));

        ArgumentCaptor<GetLegalEntitiesRequestDto> captor = ArgumentCaptor.forClass(GetLegalEntitiesRequestDto.class);
        verify(getSubLegalEntitlesFlow).start(captor.capture());

        GetLegalEntitiesRequestDto param = captor.getValue();
        assertEquals(cursor, param.getCursor());
        assertEquals(from, param.getFrom());
        assertEquals(size, param.getSize());
        assertEquals(searchQuery, param.getQuery());
        assertEquals(parentEntityId, param.getParentEntityId());
        assertThat(param.getExcludeIds(), containsInAnyOrder("1", "2"));
    }
}
