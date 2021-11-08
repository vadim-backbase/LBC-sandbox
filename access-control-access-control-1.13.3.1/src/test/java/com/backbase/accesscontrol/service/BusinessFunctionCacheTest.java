package com.backbase.accesscontrol.service;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.backbase.accesscontrol.domain.ApplicableFunctionPrivilege;
import com.backbase.accesscontrol.domain.BusinessFunction;
import com.backbase.accesscontrol.domain.Privilege;
import com.backbase.accesscontrol.repository.ApplicableFunctionPrivilegeJpaRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BusinessFunctionCacheTest {

    @Mock
    private ApplicableFunctionPrivilegeJpaRepository applicableFunctionPrivilegeJpaRepository;

    private BusinessFunctionCache businessFunctionCache;

    @Before
    public void setup() {
        // Build list of AFP
        List<ApplicableFunctionPrivilege> mockedAFPs = mockAFP("1", "FN-1", "RN-1", "view", "create");
        mockedAFPs.addAll(mockAFP("2", "FN-2", "RN-2", "execute"));
        mockedAFPs.addAll(mockAFP("3", "FN-3", "RN-3", "create", "execute"));

        when(applicableFunctionPrivilegeJpaRepository.findAll())
            .thenReturn(mockedAFPs);

        businessFunctionCache = new BusinessFunctionCache(applicableFunctionPrivilegeJpaRepository);
    }

    private List<ApplicableFunctionPrivilege> mockAFP(String id, String fnName, String rName,
        String... privilegeNames) {

        List<ApplicableFunctionPrivilege> result = new ArrayList<>();

        for (String privilegeName : privilegeNames) {

            ApplicableFunctionPrivilege afp = new ApplicableFunctionPrivilege();
            afp.setId(privilegeName + id);

            afp.setBusinessFunction(mockBF(id, fnName, rName));

            Privilege p = new Privilege();
            p.setId(id);
            p.setName(privilegeName);
            afp.setPrivilege(p);

            result.add(afp);
        }

        return result;
    }

    private BusinessFunction mockBF(String id, String fnName, String rn) {

        BusinessFunction bf = new BusinessFunction();
        bf.setId(id);
        bf.setFunctionName(fnName);
        bf.setResourceName(rn);

        return bf;
    }

    @Test
    public void shouldReturnAllAfpIdsForAllParametersEmptyOrNull() {

        // Given
        String functionName = null;
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.containsAll(Arrays.asList("view1", "create1", "execute2", "create3", "execute3")));
    }

    @Test
    public void shouldFilterByFunctionNameSingleResult() {

        // Given
        String functionName = "FN-2";
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains("execute2"));
    }

    @Test
    public void shouldFilterAllByEmptyFunctionName() {

        // Given
        String functionName = "";
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByFunctionName() {

        // Given
        String functionName = "FN-1";
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("view1", "create1")));
    }

    @Test
    public void shouldFilterByResourceName() {

        // Given
        String functionName = null;
        String resourceName = "RN-3";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("execute3", "create3")));
    }

    @Test
    public void shouldFilterByEmptyResourceName() {

        // Given
        String functionName = null;
        String resourceName = "";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByPrivilegeName() {

        // Given
        String functionName = null;
        String resourceName = null;
        List<String> privileges = singletonList("create");

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("create1", "create3")));
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceNameAndReturnEmptyResult() {

        // Given
        String functionName = "FN-1";
        String resourceName = "RN-2";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceName() {

        // Given
        String functionName = "FN-3";
        String resourceName = "RN-3";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("create3", "execute3")));
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceNameAndPrivilegeName() {

        // Given
        String functionName = "FN-3";
        String resourceName = "RN-3";
        List<String> privileges = singletonList("execute");

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNameOrResourceNameOrPrivilegesOptional(functionName, resourceName, privileges);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains("execute3"));
    }

    @Test
    public void shouldReturnAllAfpIdsOnGetByFunctionNamesOrResourceNameOrPrivilegesForAllParametersEmptyOrNull() {

        // Given
        List<String> functionNames = Collections.emptyList();
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(5, result.size());
        assertTrue(result.containsAll(Arrays.asList("view1", "create1", "execute2", "create3", "execute3")));
    }

    @Test
    public void shouldFilterByFunctionNameSingleResultOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames =  singletonList("FN-2");
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains("execute2"));
    }

    @Test
    public void shouldFilterAllByEmptyFunctionNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = singletonList("");
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByFunctionNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = singletonList("FN-1");
        String resourceName = null;
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("view1", "create1")));
    }

    @Test
    public void shouldFilterByResourceNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = null;
        String resourceName = "RN-3";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("execute3", "create3")));
    }

    @Test
    public void shouldFilterByEmptyResourceNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = null;
        String resourceName = "";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByPrivilegeNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = null;
        String resourceName = null;
        List<String> privileges = singletonList("create");

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("create1", "create3")));
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceNameAndReturnEmptyResultOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = singletonList("FN-1");
        String resourceName = "RN-2";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(0, result.size());
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = singletonList("FN-3");
        String resourceName = "RN-3";
        List<String> privileges = Collections.emptyList();

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("create3", "execute3")));
    }

    @Test
    public void shouldFilterByFunctionNameAndResourceNameAndPrivilegeNameOnGetByFunctionNamesOrResourceNameOrPrivileges() {

        // Given
        List<String> functionNames = singletonList("FN-3");
        String resourceName = "RN-3";
        List<String> privileges = singletonList("execute");

        // When
        Set<String> result = businessFunctionCache
            .getByFunctionNamesOrResourceNameOrPrivileges(functionNames, resourceName, privileges);

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains("execute3"));
    }
}
