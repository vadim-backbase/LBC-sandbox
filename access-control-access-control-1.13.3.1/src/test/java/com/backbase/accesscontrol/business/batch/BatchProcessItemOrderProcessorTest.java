package com.backbase.accesscontrol.business.batch;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BatchProcessItemOrderProcessorTest {

    @InjectMocks
    private BatchProcessItemOrderProcessor batchProcessItemOrderProcessor;

    @Test
    public void shouldCreateListWithItems() {
        List<String> batchList = Lists.newArrayList("one", "two", "three");
        List<ProcessableBatchBody<String>> procesableBatchBodies = batchProcessItemOrderProcessor
            .transformProcessableBody(batchList);
        assertThat(
            procesableBatchBodies,
            contains(
                allOf(
                    hasProperty("order", is(1)),
                    hasProperty("item", is("one")),
                    hasProperty("response", is(nullValue()))
                ),
                allOf(
                    hasProperty("order", is(2)),
                    hasProperty("item", is("two")),
                    hasProperty("response", is(nullValue()))
                ),
                allOf(
                    hasProperty("order", is(3)),
                    hasProperty("item", is("three")),
                    hasProperty("response", is(nullValue()))
                )
            )
        );
    }
}