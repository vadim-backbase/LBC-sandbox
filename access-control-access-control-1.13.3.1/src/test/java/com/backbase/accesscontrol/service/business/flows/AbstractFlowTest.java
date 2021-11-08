package com.backbase.accesscontrol.service.business.flows;

import static org.junit.Assert.assertEquals;

import com.backbase.accesscontrol.business.flows.AbstractFlow;
import org.junit.Test;

public class AbstractFlowTest {

    @Test
    public void shouldExecuteTheFlow() {
        TestFlow testy = new TestFlow();

        assertEquals("value", testy.start("value"));
        assertEquals(1, testy.executeIsCalled);
        assertEquals(1, testy.preHookisCalled);
        assertEquals(1, testy.postHookIsCalled);
    }

    private class TestFlow extends AbstractFlow<String, String> {

        public int executeIsCalled = 0;
        public int preHookisCalled = 0;
        public int postHookIsCalled = 0;

        @Override
        protected String execute(String data) {
            executeIsCalled++;
            return data;
        }

        @Override
        protected void preHook(String data) {
            preHookisCalled++;
            super.preHook(data);
        }

        @Override
        protected String postHook(String data, String result) {
            postHookIsCalled++;
            return super.postHook(data, result);
        }
    }
}
