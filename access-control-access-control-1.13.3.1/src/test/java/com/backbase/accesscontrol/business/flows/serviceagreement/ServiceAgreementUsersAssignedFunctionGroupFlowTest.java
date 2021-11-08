package com.backbase.accesscontrol.business.flows.serviceagreement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.backbase.accesscontrol.dto.RecordsDto;
import com.backbase.accesscontrol.dto.UserAssignedFunctionGroupDto;
import com.backbase.accesscontrol.service.impl.UserContextService;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.UserAssignedFunctionGroupResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;

@RunWith(MockitoJUnitRunner.class)
public class ServiceAgreementUsersAssignedFunctionGroupFlowTest {

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private ServiceAgreementUsersAssignedFunctionGroupFlow serviceAgreementUsersAssignedFunctionGroupFlow;

    @Test
    public void shouldGetUserIdsWithAssignedFunctionGroup() {
        String serviceAgreementId = "sa_id";
        String functionGroupId = "fg_id";
        Integer from = 2;
        Integer size = 2;
        Long totalElements = 20L;
        String userId1 = "id_1";
        String userId2 = "id_2";

        Page<String> userIdsPageMock = createPageMock(totalElements, userId1, userId2);

        doReturn(userIdsPageMock).when(userContextService)
            .findUserIdsByServiceAgreementIdAndFunctionGroupId(serviceAgreementId, functionGroupId, from, size);

        RecordsDto<UserAssignedFunctionGroupResponse> recordsDto = serviceAgreementUsersAssignedFunctionGroupFlow
            .execute(new UserAssignedFunctionGroupDto(serviceAgreementId, functionGroupId, from, size));

        assertEquals(totalElements, recordsDto.getTotalNumberOfRecords());
        assertEquals(2, recordsDto.getRecords().size());

        assertEquals(createResponse(userId1, userId2), recordsDto.getRecords());
    }

    private List<UserAssignedFunctionGroupResponse> createResponse(String... ids) {
        return Arrays.stream(ids)
            .map(id -> new UserAssignedFunctionGroupResponse().withId(id))
            .collect(Collectors.toList());
    }

    private Page<String> createPageMock(Long totalElements, String... ids) {
        Page<String> page = mock(Page.class);
        doReturn(totalElements).when(page).getTotalElements();
        doReturn(Arrays.asList(ids)).when(page).getContent();
        return page;
    }
}