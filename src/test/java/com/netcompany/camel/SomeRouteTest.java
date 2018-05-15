package com.netcompany.camel;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * TODO: Javadoc
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CamelConfig.class, MockConfig.class })
@DirtiesContext
@MockEndpoints
public class SomeRouteTest {

    @Produce(uri = "direct:some.queue")
    private ProducerTemplate producer;

    @EndpointInject(uri = "mock:DLQ")
    private MockEndpoint dlq;

    @EndpointInject(uri = "mock:other.queue")
    private MockEndpoint exitQueue;

    @Autowired
    private SomeService service;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldRetryOnIllegalArgumentException() throws InterruptedException {
        doThrow(new IllegalArgumentException("hei")).doNothing().when(service).doSomething();
        producer.sendBody("hei");

        dlq.expectedMessageCount(0);
        dlq.assertIsSatisfied();

        exitQueue.expectedMessageCount(1);
        exitQueue.assertIsSatisfied();

        verify(service, times(2)).doSomething();
    }

    @Test
    public void shouldFailImmediatlyOnRuntimeException() throws InterruptedException {
        doThrow(new RuntimeException("hei")).doNothing().when(service).doSomething();
        producer.sendBody("hei");

        dlq.expectedMessageCount(1);
        dlq.assertIsSatisfied();

        exitQueue.expectedMessageCount(0);
        exitQueue.assertIsSatisfied();

        verify(service, times(1)).doSomething();
    }

    @Test
    public void shouldRetryFiveBeforeGivingUpTimesOnIllegalArgumentException() throws InterruptedException {
        doThrow(new IllegalArgumentException("hei")).when(service).doSomething();
        producer.sendBody("hei");

        dlq.expectedMessageCount(1);
        dlq.assertIsSatisfied();

        exitQueue.expectedMessageCount(0);
        exitQueue.assertIsSatisfied();

        verify(service, times(6)).doSomething();
    }
}