package com.backbase.accesscontrol.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async tasks configurator. With this configuration class, we are enabling asynchronous
 * task execution. In the future, we can fine-tune the executor's type, pool size and queue capacity by making this
 * class implement {@link org.springframework.scheduling.annotation.AsyncConfigurer}.
 */
@EnableAsync
@Configuration
public class AsyncConfigurator {
}
