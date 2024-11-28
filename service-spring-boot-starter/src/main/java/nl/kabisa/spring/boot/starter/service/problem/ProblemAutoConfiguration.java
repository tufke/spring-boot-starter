package nl.kabisa.spring.boot.starter.service.problem;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This configuration class configures Spring to return Problem types in case of exceptions.
 * <p>
 * You can disable this configuration with:
 * <p>
 * service.starter.problem.enabled=false
 *
 * @author Mark Spreksel
 */

@Data
@AutoConfiguration
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class})
@ConfigurationProperties("service.starter.problem")
@ConditionalOnProperty(prefix = "service.starter.problem", name = "enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
class ProblemAutoConfiguration {

    @Bean
    @ConditionalOnClass(ResponseEntityExceptionHandler.class)
    public ProblemEntityExceptionHandler exceptionHandler() {
        log.info("Configuring ProblemEntityExceptionHandler");
        return new ProblemEntityExceptionHandler();
    }

    @Bean
    @ConditionalOnClass(DefaultErrorAttributes.class)
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public ProblemErrorAttributes errorAttributes() {
        log.info("Configuring ProblemErrorAttributes");
        return new ProblemErrorAttributes();
    }

    @Bean
    @ConditionalOnClass(AbstractErrorController.class)
    @ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
    public ProblemErrorController errorController(ErrorAttributes errorAttributes) {
        log.info("Configuring ProblemErrorController");
        return new ProblemErrorController(errorAttributes);
    }

}
