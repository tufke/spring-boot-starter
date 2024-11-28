package nl.kabisa.spring.boot.starter.service.problem;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import nl.kabisa.spring.boot.starter.service.exception.ForbiddenException;
import nl.kabisa.spring.boot.starter.service.exception.ServiceException;
import nl.kabisa.spring.boot.starter.service.exception.UnauthorizedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * A common <code>ControllerAdvice</code> class to translate known Exceptions to standard Problem types.
 * Currently supports: {@link ServiceException}, {@link ForbiddenException}, {@link ValidationException}
 * and {@link RuntimeException}.
 *
 * @author Mark Spreksel
 * @see <a href="https://www.baeldung.com/exception-handling-for-rest-with-spring">Spring Exception Handling</a>
 */
@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = "service.starter.problem", name = "enabled", havingValue = "true", matchIfMissing = true)
class ProblemEntityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * The problem message if a validation failed.
     */
    static final String MSG_VIOLATIONS = "Validation failed, see violations property for more details";

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Problem> handleServiceException(ServiceException ex, HttpServletRequest request) {
        if (!ex.exposeDetails()) {
            logException("Unexposed exception details - UUID = " + ex.getUuid() + " --> {}", ex);
        }
        return problem(ex.getHttpStatus(), request, Problem.TYPE_TRACEABLE_PROBLEM,
                ex.exposeDetails() ? ex.getMessage() : null, ex.getUuid(), ex.getCode(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Problem> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        logException("*** ACCESS DENIED *** {}", ex);
        // NOTE: Don't expose the exception detail message in the response for security reasons!!!
        return problem(HttpStatus.UNAUTHORIZED, request, Problem.TYPE_PROBLEM, "Access Denied", null, null, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(code = HttpStatus.FORBIDDEN)
    public ResponseEntity<Problem> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        logException("*** ACCESS DENIED *** {}", ex);
        // NOTE: Don't expose the exception detail message in the response for security reasons!!!
        return problem(HttpStatus.FORBIDDEN, request, Problem.TYPE_PROBLEM, "Access Denied", null, null, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        logException("Illegal argument: {}", ex, true); // always log stacktrace for IllegalArgumentExceptions
        return problem(HttpStatus.BAD_REQUEST, request, Problem.TYPE_PROBLEM, null, null, null, null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Problem> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        if (ex.getStatusCode().isError()) {
            logException("Response status: " + ex.getStatusCode() + " ({})", ex);
        }

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return problem(status, request, Problem.TYPE_PROBLEM, null, null, null, null);
    }

    /*
     * A TypeMismatchException raised while resolving a controller method argument. Provides access to the target MethodParameter.
     * This exception is thrown when a validation in the openapi spec is failing for instance.
     * - Validating a Request Body (model object with annotated properties @NotNull, @Pattern etc)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        val message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return problem(HttpStatus.BAD_REQUEST, request, Problem.TYPE_PROBLEM, message, null, null, null);
    }

    /*
     * In contrast to request body validation a failed validation will trigger a ConstraintViolationException instead of a MethodArgumentNotValidException.
     * - Validating Path Variables and Request Parameters (Url parameter annotated with @Min, @Max etc) an id in the url for instance
     * - Validating Input to a Spring Service Method (@valid on objects as method parameters within a @Component or @Service class)
     * - Validating JPA Entities
     *
     * ValidationException is the base exception of all Jakarta Bean Validation "unexpected" problems.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ResponseEntity<Problem> handleValidationException(ValidationException ex, HttpServletRequest request) {
        return ex instanceof ConstraintViolationException ?
                problem(HttpStatus.BAD_REQUEST, request, Problem.TYPE_CONSTRAINT_VIOLATIONS,
                        MSG_VIOLATIONS, null, null, violations((ConstraintViolationException) ex))
                : problem(HttpStatus.BAD_REQUEST, request, Problem.TYPE_PROBLEM,
                ex.getMessage(), null, null, null);
    }

    /**
     * unexpose checked and unchecked runtime exceptions.
     * RuntimeException extends Exception.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Problem> handleRuntimeException(Exception ex, HttpServletRequest request) {
        log.warn("Uncaught exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, request, Problem.TYPE_PROBLEM,
                null, null, null, null);
    }

    /**
     * Called by all exceptions in the @ExceptionHandler annotation in ResponseEntityExceptionHandler from which this
     * class extends.
     *
     * {@inheritDoc}
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        logException("Handling internal exception: " + ex.getClass().getSimpleName() + " ({})", ex);

        final Problem problem;
        if (body instanceof Problem) {
            problem = (Problem) body;
        } else {
            problem = Problem.builder()
                    .type(Problem.TYPE_PROBLEM)
                    .status(status.value())
                    .title(HttpStatus.valueOf(status.value()).getReasonPhrase())
                    .instance(request instanceof ServletWebRequest ? ((ServletWebRequest) request).getRequest().getRequestURI() : null)
                    .build();
        }

        // Accept header without json will result in status 406: prevent this by removing Accept values..
        request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, SCOPE_REQUEST);

        return ResponseEntity.status(status)
                .contentType(Problem.MEDIA_TYPE_PROBLEM_JSON)
                .headers(headers)
                .body(problem);
    }

    private void logException(String message, Exception e) {
        logException(message, e, false);
    }

    private void logException(String message, Exception e, boolean alwaysLogStacktrace) {
        log.warn(message, e.getMessage());
        if (alwaysLogStacktrace) {
            log.warn("Exception details: ", e);
        }
        else if (log.isTraceEnabled()) {
            log.trace("Exception details:", e);
        }
    }

    private ResponseEntity<Problem> problem(HttpStatus status, HttpServletRequest req, String type, String message,
                                            UUID id, String code, List<ConstraintViolation> cv) {

        // Accept header without json will result in status 406: prevent this by removing Accept values..
        req.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

        return ResponseEntity.status(status)
                .contentType(Problem.MEDIA_TYPE_PROBLEM_JSON)
                .body(Problem.builder()
                        .type(type)
                        .status(status.value())
                        .title(status.getReasonPhrase())
                        .detail(message)
                        .instance(req.getRequestURI())
                        .id(id)
                        .code(code)
                        .violations(cv != null ? cv : Collections.emptyList())
                        .build()
                );
    }

    private List<ConstraintViolation> violations(ConstraintViolationException cve) {
        return cve.getConstraintViolations().stream().map(cv -> ConstraintViolation.builder()
                .field(cv.getPropertyPath().toString())
                .message(cv.getMessage())
                .build()
        ).collect(Collectors.toList());
    }

}
