package com.backbase.accesscontrol.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.repository.IdProjection;
import com.backbase.accesscontrol.repository.LegalEntityJpaRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidateLegalEntityHierarchyServiceTest {

    @Mock
    private LegalEntityJpaRepository legalEntityJpaRepository;

    @InjectMocks
    private ValidateLegalEntityHierarchyService validateLegalEntityHierarchyService;

    @Test
    public void shouldListOfAllLegalEntitiesThatExistInTheListOfParticipants() {
        String leId = "le1";

        List<IdProjection> answer1 = IntStream
            .range(0, 1000)
            .mapToObj(i -> (IdProjection) () -> "data" + i).collect(Collectors.toList());
        List<IdProjection> answer2 = IntStream
            .range(0, 100)
            .mapToObj(i -> (IdProjection) () -> "data" + i).collect(Collectors.toList());

        when(legalEntityJpaRepository.findByLegalEntityAncestorsIdAndIdIn(eq(leId), anyList()))
            .thenReturn(answer1, answer2);

        List<String> input = IntStream.range(0, 1100).mapToObj(i -> "data" + i).collect(Collectors.toList());

        List<String> result = validateLegalEntityHierarchyService.getLegalEntityHierarchy(leId, input);

        assertEquals(1101, result.size());
    }

}