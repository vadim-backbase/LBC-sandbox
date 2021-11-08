package com.backbase.accesscontrol.routes;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import com.backbase.buildingblocks.backend.communication.extension.annotations.BehaviorExtension;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.springframework.context.ApplicationContext;

/**
 * Abstract class that will be used for all unit tests for Routes Does not contain any tests, only configuration. All
 * tests in routes package should extend this class, and implement abstract method.
 */
@Ignore
public abstract class BaseTestRoutesBuilder<T extends SimpleExtensibleRouteBuilder> extends CamelTestSupport {

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

    /**
     * Method that sets mock Aplication context to a route and returns the setup route
     */
    private T setUpRoute(T route) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBeansWithAnnotation(eq(BehaviorExtension.class)))
            .thenReturn(Collections.emptyMap());
        route.setApplicationContext(applicationContext);
        route.init();

        return route;
    }

    @Override
    public T createRouteBuilder() {
        try {
            Type routeType = getClass().getGenericSuperclass();
            Type t = ((ParameterizedType) routeType).getActualTypeArguments()[0];

            T builder = (T) (Class.forName(t.getTypeName()).newInstance());
            return setUpRoute(builder);
        } catch (Exception e) {
            return null;
        }
    }

}
