package nl.kabisa.spring.boot.starter.service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationListener;

/**
 * This {@link ApplicationListener} is used to log some application specific
 * version information.
 *
 * This information can be used on Grafana or Splunk like dashboards to monitor the LCM of services.
 *
 */
public class AppStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppStartupListener.class);

    /**
     * This event is executed as late as possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        try {
            final BuildProperties properties = event.getApplicationContext().getBean(BuildProperties.class);
            LOGGER.info("Application {} [version={}, java-version={}, spring-boot-version={}, spring-cloud-version={}] started.",
                    properties.getName(),
                    properties.getVersion(),
                    System.getProperty("java.version"),
                    properties.get("springBootVersion"),
                    properties.get("springCloudVersion"));
        } catch (final NoSuchBeanDefinitionException e) {
            // ignore, BuildProperties can only be created in case META-INF/build-info.properties file is present
            LOGGER.warn("BuildProperties are not logged, META-INF/build-info.properties file is not present");
        }
    }
}

