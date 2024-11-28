package nl.kabisa.spring.boot.starter.service.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * Auto configure listeners for application.
 */
@AutoConfiguration
@ConfigurationProperties("service.starter.application.listener")
@ConditionalOnProperty(prefix = "service.starter.application.listener", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApplicationListenerConfig {
    /**
     * Can be disabled through service.starter.application.listener set up as boolean true.
     *
     * @return {@link ApplicationListener}
     */
    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationListener() {
        return new AppStartupListener();
    }
}
