package nl.kabisa.spring.boot.starter.service.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

/**
 * A simple request/response logging filter inspired by the Jersey logging interceptor.
 */
@Slf4j
@Getter
@Setter
class RequestResponseLoggingFilter extends AbstractLoggingFilter implements Ordered {

    /**
     * The maximum payload size to use (default is 2048)
     */
    private int maxPayloadSize = 4096;

    public RequestResponseLoggingFilter(Set<String> excludeUriPatterns, Set<String> includeUriPatterns) {
        super(excludeUriPatterns, includeUriPatterns);
    }

    @Override
    protected void logRequest(long id, HttpServletRequest request) {
        StringBuilder msg = new StringBuilder();

        msg.append(id).append(" > REQUEST ");
        appendRequestURIAndQueryString(msg, request, includeQueryString).append(" ");
        appendContentType(msg, request.getContentType());
        msg.append("\n");

        if (includeHeaders || includePayload) {
            msg.append(id).append(" > thread:");
            appendThread(msg);
            msg.append("\n");
        }

        if (includeClientInfo) {
            msg.append(id).append(" > ClientInfo:");
            if (isNotBlank(request.getRemoteAddr())) {
                msg.append(" remoteAddr=").append(request.getRemoteAddr());
            }
            if (isNotBlank(request.getRemoteUser())) {
                msg.append(" remoteUser=").append(request.getRemoteUser());
            }
            HttpSession session = request.getSession(false);
            if (session != null) {
                msg.append(" sessionId=").append(session.getId());
            }
            msg.append("\n");
        }

        if (includeHeaders && request.getHeaderNames().hasMoreElements()) {
            Collections.list(request.getHeaderNames()).stream()
                    .filter(h -> !h.equalsIgnoreCase("Content-Type"))
                    .sorted()
                    .forEach(h -> msg.append(id).append(" > ").append(h).append(": ").append(
                            String.join(", ", Collections.list(request.getHeaders(h)))).append("\n"));
        }

        if (includePayload) {
            String payload = getRequestPayload(request, maxPayloadSize);
            if (isNotBlank(payload)) {
                msg.append(id).append(" > ");
                appendContentType(msg, request.getContentType());
                msg.append("\n");
                msg.append(payload).append("\n");
            }
        }

        log.info(msg.toString());
    }

    @Override
    protected void logResponse(long id, Duration duration, HttpServletRequest request, HttpServletResponse response) {
        StringBuilder msg = new StringBuilder();

        msg.append(id).append(" < RESPONSE ");
        appendResponseHttpStatus(msg, response).append(" ");
        appendTime(msg, duration).append(" ");
        appendRequestURIAndQueryString(msg, request, includeQueryString).append(" ");
        appendContentType(msg, response.getContentType());
        msg.append("\n");

        if (includeHeaders || includePayload) {
            msg.append(id).append(" < thread:");
            appendThread(msg);
            msg.append("\n");
        }

        if (includeHeaders && !response.getHeaderNames().isEmpty()) {
            response.getHeaderNames().stream()
                    .filter(h -> !h.equalsIgnoreCase("Content-Type"))
                    .sorted()
                    .forEach(h -> msg.append(id).append(" < ").append(h).append(": ").append(
                            String.join(", ", response.getHeaders(h))).append("\n"));
        }

        if (includePayload) {
            String payload = getResponsePayload(response, maxPayloadSize);
            if (isNotBlank(payload)) {
                msg.append(id).append(" < ");
                appendContentType(msg, request.getContentType());
                msg.append("\n");
                msg.append(payload).append("\n");
            }
        }
        log.info(msg.toString());
    }

    private boolean isNotBlank(String s) {
        return s != null && s.trim().length() > 0;
    }

    private StringBuilder appendTime(StringBuilder msg, Duration duration) {
        long millis = duration.toMillis();
        long HH = millis / 3600000;
        long MM = (millis % 3600000) / 60000;
        long SS = (millis % 60000) / 1000;
        long MS = millis % 1000;

        String time = String.format("%d ms", MS);
        if (HH > 0) {
            time = String.format("%d hrs %d min %d.%03d sec", HH, MM, SS, MS);
        } else if (MM > 0) {
            time = String.format("%d min %d.%03d sec", MM, SS, MS);
        } else if (SS > 0) {
            time = String.format("%d.%03d sec", SS, MS);
        }

        msg.append("[" + time + "]");

        return msg;
    }

    private StringBuilder appendThread(StringBuilder msg) {
        msg.append("[").append(Thread.currentThread().getName()).append("]");

        return msg;
    }

    private StringBuilder appendContentType(StringBuilder msg, String contentType) {
        contentType = contentType == null ? "none (no body)" : contentType;
        msg.append("content-type=" + contentType);

        return msg;
    }

    private StringBuilder appendResponseHttpStatus(StringBuilder msg, HttpServletResponse response) {
        if (msg != null) {
            HttpStatus status = HttpStatus.valueOf(response.getStatus());
            if (status == null) {
                msg.append(response.getStatus());
            } else {
                msg.append(status.value()).append(" (").append(status.getReasonPhrase()).append(")");
            }
        }

        return msg;
    }

    private StringBuilder appendRequestURLAndQueryString(StringBuilder msg, HttpServletRequest request, boolean includeQueryString) {
        appendRequestPathAndQueryString(msg, request, includeQueryString, true, true);

        return msg;
    }

    private StringBuilder appendRequestURIAndQueryString(StringBuilder msg, HttpServletRequest request, boolean includeQueryString) {
        appendRequestPathAndQueryString(msg, request, includeQueryString, false, false);

        return msg;
    }

    private StringBuilder appendRequestPathAndQueryString(StringBuilder msg, HttpServletRequest request, boolean includeQueryString, boolean logFullURL, boolean addLineBreak) {
        if (msg != null) {
            msg.append(request.getMethod()).append(" ");
            if (logFullURL) {
                msg.append(request.getRequestURL().toString());
            } else {
                msg.append(request.getRequestURI().toString());
            }

            if (includeQueryString && StringUtils.isNotBlank(request.getQueryString())) {
                msg.append("?").append(request.getQueryString());
            }

            if (addLineBreak) {
                msg.append("\n");
            }
        }

        return msg;
    }

    @Override
    public int getOrder() {
        // run before standard Spring Boot customizer
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
