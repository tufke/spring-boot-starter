package nl.kabisa.spring.boot.starter.service.bootstrap;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * This environment post processor loads the default-bootstrap.properties file. This is needed because
 * you can specify additional bootstrap-xxx.properties file for different profiles in Spring, but not
 * for the default profile.
 *
 * The bootstrap context is searching for a bootstrap.properties or a bootstrap.yaml file,
 * whereas the application context is searching for an application.properties or an application.yaml file.
 *
 * The configuration properties of the bootstrap context load before the configuration properties
 *  of the application context.
 *
 * @author Mark Spreksel
 */
@Slf4j
class BootstrapEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    PropertySourceLoader loader;

    BootstrapEnvironmentPostProcessor() {
        this.loader = new PropertiesPropertySourceLoader();
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication application) {
        //ensure that the bootstrap file is only loaded in the bootstrap context
        if (!env.getPropertySources().contains("bootstrap")) {
            try {
                val sources = loader.load("default-bootstrap", new ClassPathResource("/default-bootstrap.properties"));
                if (sources != null && !sources.isEmpty()) {
                    sources.forEach(s -> {
                        log.debug("Adding bootstrap properties: {}", s.getName());
                        env.getPropertySources().addLast(s);
                    });
                }
            } catch (IOException io) {
                throw new RuntimeException(io);
            }
        }
    }

    @Override
    public int getOrder() {
        //must go after ConfigFileApplicationListener
        return Ordered.HIGHEST_PRECEDENCE + 11;
    }

}
