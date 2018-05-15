package com.netcompany.camel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * TODO: Javadoc
 */
@Configuration
public class MockConfig {

    @Bean
    @Primary
    public SomeService someService() {
        return mock(SomeService.class);
    }
}
