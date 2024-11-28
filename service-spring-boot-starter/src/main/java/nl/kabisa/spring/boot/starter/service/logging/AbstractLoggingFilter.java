package nl.kabisa.spring.boot.starter.service.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

/**
 * Base logging filter for request/response logging.
 */
@Getter
@Setter
abstract class AbstractLoggingFilter extends OncePerRequestFilter {

    private static final AtomicLong ID = new AtomicLong(0);

    /**
     * Enable filtering of uri's with includes and excludes
     */
    protected boolean filterUri = false;

    /**
     * Set of URI patterns to exclude.
     */
    private final Set<String> excludes = new HashSet<>();

    /**
     * Set of URI patterns to include.
     */
    private final Set<String> includes = new HashSet<>();

    /**
     * Include HTTP Header information in request/response logging? (default is true)
     */
    protected boolean includeHeaders = false;

    /**
     * Include HTTP Client information in request logging? (default is true)
     */
    protected boolean includeClientInfo = false;

    /**
     * Include HTTP Query String information in request logging? (default is true)
     */
    protected boolean includeQueryString = false;

    /**
     * Include HTTP Payload in request/response logging? (default is true)
     */
    protected boolean includePayload = false;

    public AbstractLoggingFilter(Set<String> excludeUriPatterns, Set<String> includeUriPatterns) {
        if (excludeUriPatterns != null) {
            this.excludes.addAll(excludeUriPatterns);
        }
        if (includeUriPatterns != null) {
            this.includes.addAll(includeUriPatterns);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (includePayload && !(request instanceof CachedContentRequestWrapper)) {
            request = new CachedContentRequestWrapper(request);
        } else if (!includePayload && !(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }
        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        final Instant start = Instant.now();
        long id = ID.incrementAndGet();
        logRequest(id, request);
        try {
            chain.doFilter(request, response);
        } finally {
            final Instant end = Instant.now();
            final Duration duration = Duration.between(start, end);

            logResponse(id, duration, request, response);
            updateResponse(response);
        }
    }

    /**
     * Spring Boot sets up a default redirect for controller exceptions to /error, from the documentation :
     * Spring Boot provides an /error mapping by default that handles all errors in a sensible way, and it is
     * registered as a ‘global’ error page in the servlet container.
     * <p>
     * Because of this default behavior there will be no nested errors in the request but redirects to /error.
     * You can alter this behavior by telling the dispatcher servlet to throw an exception instead but to make that
     * work you will have to switch of the default static resource mapping.
     * it is very important to disable the automatic mapping of static resources since if you are using Spring Boot's
     * default configuration for handling static resources then the resource handler will be handling the request
     * (it's ordered last and mapped to /** which means that it picks up any requests that haven't been handled by
     * any other handler in the application) so the dispatcher servlet doesn't get a chance to throw an exception.
     * <p>
     * spring.mvc.throw-exception-if-no-handler-found=true
     * spring.resources.add-mappings=false
     */
    @Override
    protected void doFilterNestedErrorDispatch(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !shouldLog(request);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    protected abstract void logRequest(long id, HttpServletRequest request);

    protected abstract void logResponse(long id, Duration duration, HttpServletRequest request, HttpServletResponse response);

    protected boolean shouldLog(HttpServletRequest request) {
        return logger.isInfoEnabled() && isIncluded(request.getRequestURI()) && !isExcluded(request.getRequestURI());
    }

    protected boolean isExcluded(String requestURI) {
        if (filterUri) {
            return this.excludes.stream().anyMatch(p -> Pattern.matches(p, requestURI));
        }
        return false;
    }

    protected boolean isIncluded(String requestURI) {
        if (filterUri) {
            return this.includes.isEmpty() || this.includes.stream().anyMatch(p -> Pattern.matches(p, requestURI));
        }
        return true;
    }

    protected String getRequestPayload(HttpServletRequest request, int maxSize) {
        CachedContentRequestWrapper wrapper = WebUtils.getNativeRequest(request, CachedContentRequestWrapper.class);
        if (wrapper != null) {
            return getPayload(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding(), maxSize);
        }
        return "";
    }

    protected String getResponsePayload(HttpServletResponse response, int maxSize) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            return getPayload(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding(), maxSize);
        }
        return "";
    }

    private String getPayload(byte[] content, String charEncoding, int maxSize) {
        if (content.length > 0) {
            int length = Math.min(content.length, maxSize);
            try {
                return new String(content, 0, length, charEncoding != null ? charEncoding : Charset.defaultCharset().name());
            } catch (UnsupportedEncodingException ex) {
                return "[unknown]";
            }
        }
        return "";
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (responseWrapper != null) {
            responseWrapper.copyBodyToResponse();
        }
    }

}
