package com.backbase.accesscontrol.routes;

import com.backbase.buildingblocks.backend.communication.extension.ExtensibleRouteBuilder;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;

/**
 * Abstract class that will be used for all unit tests for Routes Does not contain any tests, only configuration. All
 * tests in routes package should extend this class, and implement abstract method.
 */
@Ignore
public abstract class BaseExtensibleTestRoutesBuilder<T extends ExtensibleRouteBuilder> extends CamelTestSupport {

    /**
     * Creates RouteBuilder that intercepts exchange sent to endpoint, and redirects it to mock  endpoint
     *
     * @param endpoint The endpoint where exchange will be intercepted
     */
    protected void setupAdvice(final String endpoint) throws Exception {
        RoutesTestUtil.setupAdvice(context, endpoint);
    }


    /**
     * Manually start/stop the camel context in case the CamelTestSupport starts routes before the mocks
     */
    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    public T createRouteBuilder() {
        try {
            Type routeType = getClass().getGenericSuperclass();
            Type t = ((ParameterizedType) routeType).getActualTypeArguments()[0];

            return (T) (Class.forName(t.getTypeName()).newInstance());
        } catch (Exception e) {
            return null;
        }
    }

}
