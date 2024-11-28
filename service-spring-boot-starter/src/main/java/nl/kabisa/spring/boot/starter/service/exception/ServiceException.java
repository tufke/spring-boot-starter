package nl.kabisa.spring.boot.starter.service.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Exception to throw if a backend error occurs. When using REST this will
 * result in a status 500 (Internal Server Error) with a traceable problem object.
 * <p>
 * The traceable elements that you can use are 'code' and 'uuid' to specify an
 * application error.
 *
 * @author Mark Spreksel
 */
@Data
@Setter(AccessLevel.NONE)
@EqualsAndHashCode(callSuper = false)
public class ServiceException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 94758614386714373L;
    public static final Predicate<ServiceException> EXPOSE_DETAILS_ALWAYS = (e) -> true;
    public static final Predicate<ServiceException> EXPOSE_DETAILS_NEVER = (e) -> false;
    public static final Predicate<ServiceException> EXPOSE_DETAILS_CLIENT_ERROR = (e) -> e.getHttpStatus() != null && e.getHttpStatus().is4xxClientError();

    private final HttpStatus httpStatus;
    private final String code;
    private final UUID uuid;
    private final Predicate<ServiceException> exposeDetails;

    public ServiceException(String message) {
        this(null, null, null, null, message, null);
    }

    public ServiceException(String message, String code) {
        this(null, null, null, code, message, null);
    }

    public ServiceException(String message, String code, Predicate<ServiceException> exposeDetails) {
        this(null, exposeDetails, null, code, message, null);
    }

    public ServiceException(Throwable t) {
        this(null, null, null, null, null, t);
    }

    public ServiceException(String message, Throwable t) {
        this(null, null, null, null, message, t);
    }

    @Builder
    private ServiceException(HttpStatus httpStatus, Predicate<ServiceException> exposeDetails, UUID uuid, String code, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus;
        this.exposeDetails = exposeDetails == null ? EXPOSE_DETAILS_CLIENT_ERROR : exposeDetails;
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.code = code;
    }

    public boolean exposeDetails() {
        return exposeDetails.test(this);
    }
}
