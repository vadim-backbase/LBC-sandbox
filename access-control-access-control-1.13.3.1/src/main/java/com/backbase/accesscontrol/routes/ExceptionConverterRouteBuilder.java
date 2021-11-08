package com.backbase.accesscontrol.routes;

import com.backbase.buildingblocks.backend.communication.extension.SimpleExtensibleRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;

public abstract class ExceptionConverterRouteBuilder extends SimpleExtensibleRouteBuilder {

    public ExceptionConverterRouteBuilder(String routeId, String fromEndpoint, String toEndpoint) {
        super(routeId, fromEndpoint, toEndpoint);
    }

    @Override
    protected void configureTo(RouteDefinition rd) throws Exception {
        rd.onException(RuntimeException.class)
            .handled(true)
            .process(exchange -> {
                throw exchange.getProperty(Exchange.EXCEPTION_CAUGHT, RuntimeException.class);
            });
        super.configureTo(rd);
    }
}
