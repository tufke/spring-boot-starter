package nl.kabisa.spring.boot.starter.service.problem;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Custom <code>ErrorAttributes</code> implementation to conform to Problem type definition.
 */
@Component
@ConditionalOnProperty(prefix = "service.starter.problem", name = "enabled", havingValue = "true", matchIfMissing = true)
class ProblemErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> defaultAttributes = super.getErrorAttributes(webRequest, options);
        defaultAttributes.put("title", defaultAttributes.get("error"));
        return defaultAttributes;
    }
}
