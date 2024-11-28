package nl.kabisa.spring.boot.starter.service.logging;

import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Auto configure logging in this package for use outside this module.
 */
@Data
@AutoConfiguration
@ConfigurationProperties("service.starter.logging")
@ConditionalOnProperty(prefix = "service.starter.logging", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnClass(OncePerRequestFilter.class)
class LoggingAutoConfiguration {

    /**
     * Is the BSP logging enabled? (default is true)
     */
    private boolean enabled = true;

    /**
     * Include HTTP Header information in request/response logging? (default is true)
     */
    private boolean includeHeaders = true;

    /**
     * Include HTTP Client information in request logging? (default is false)
     */
    private boolean includeClientInfo = false;

    /**
     * Include HTTP Query String information in request logging? (default is true)
     */
    private boolean includeQueryString = true;

    /**
     * Include HTTP Payload in request/response logging? (default is true)
     */
    private boolean includePayload = true;

    /**
     * The maximum payload size (default is 2048)
     */
    private int maxPayloadSize = 2048;

    /**
     * The maxIndex of the RollingPolicy of the FileAppender to use (default is 1)
     */
    private int rollingMaxIndex = 1;

    /**
     * Enable filtering with uri patterns
     */
    private boolean filterUri = true;

    /**
     * Specify regex patterns for path Uri's to exclude from logging,
     * e.g. '/app/health' (default is no exclusions)
     */
    private Map<String, String> excludeUriPattern = new HashMap<>();

    /**
     * Specify regex patterns for path Uri's to include from logging,
     * e.g. '.*\/api\/.*' (default is include api only)
     */
    private Map<String, String> includeUriPattern = new HashMap<>();


    @Bean
    @SuppressWarnings("rawtypes")
    public FilterRegistrationBean registerRequestLogFilter() {
        RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter(
                new HashSet<>(excludeUriPattern.values()), new HashSet<>(includeUriPattern.values()));
        filter.setIncludeHeaders(includeHeaders);
        filter.setIncludeClientInfo(includeClientInfo);
        filter.setIncludeQueryString(includeQueryString);
        filter.setIncludePayload(includePayload);
        filter.setMaxPayloadSize(maxPayloadSize);
        filter.setFilterUri(filterUri);

        FilterRegistrationBean result = new FilterRegistrationBean<>(filter);
        result.setOrder(Ordered.HIGHEST_PRECEDENCE); // make sure it runs before the security filter chain
        return result;
    }

}
