package com.backbase.accesscontrol.audit.descriptionprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.presentation.accessgroup.event.spec.v1.DataGroupBase;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.functiongroups.FunctionGroupBase;
import java.util.Arrays;
import java.util.Collections;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link AbstractDescriptionProvider}
 */
@RunWith(MockitoJUnitRunner.class)
public class DescriptionUtilsTest {

    private ProceedingJoinPoint joinPoint;

    @Before
    public void setup() {
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @Test
    public void shouldReturnSpecificArgumentByClassFromMapOfArguments() {
        DataGroupBase dataGroup = new DataGroupBase().withName("Data Group");
        FunctionGroupBase functionGroup = new FunctionGroupBase().withName("Function Group");
        String argument = "argument";
        when(joinPoint.getArgs()).thenReturn(Arrays.asList(dataGroup, functionGroup, argument).toArray());

        DataGroupBase returnedArgument = DescriptorUtils.getArgument(joinPoint, DataGroupBase.class);

        assertNotNull(returnedArgument);
        assertEquals("Data Group", returnedArgument.getName());
    }

    @Test
    public void shouldReturnSpecificPathParameterFromHttpServletRequest() {
        String dgId = "DG-01";

        when(joinPoint.getArgs())
            .thenReturn(Collections.singletonList(dgId).toArray());

        String pathParameter = DescriptorUtils.getPathParameter(joinPoint, "id");

        assertEquals(dgId, pathParameter);
    }
}
