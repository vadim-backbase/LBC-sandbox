package com.backbase.accesscontrol.routes;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.model.ModelCamelContext;

public class RoutesTestUtil {

    public static void setupAdvice(ModelCamelContext context, final String endpoint) throws Exception {

        AdviceWithRouteBuilder.adviceWith(context, context.getRouteDefinitions().get(0).getRouteId(),
            a -> a.interceptSendToEndpoint(endpoint).skipSendToOriginalEndpoint().to("mock:" + endpoint));
    }

}
