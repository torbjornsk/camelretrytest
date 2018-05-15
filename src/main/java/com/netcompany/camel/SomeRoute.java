package com.netcompany.camel;

import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TODO: Javadoc
 */
@Component
public class SomeRoute extends SpringRouteBuilder {

    @Autowired
    private SomeService someService;

    @Override
    public void configure() throws Exception {

        errorHandler(deadLetterChannel("mock:DLQ"));
        onException(IllegalArgumentException.class).redeliveryDelay(1000).maximumRedeliveries(5);

        from("direct:some.queue").startupOrder(100)
                                 .bean(someService)
                                 .to("mock:other.queue");
    }
}