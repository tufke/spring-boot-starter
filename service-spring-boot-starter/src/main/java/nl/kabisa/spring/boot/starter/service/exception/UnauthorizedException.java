package nl.kabisa.spring.boot.starter.service.exception;

/**
 * Exception thrown when an identity is not authenticated sufficiently to access a rest controller (service) at all.
 * This will result in a response status 401 (Unauthorized).
 *
 * @author Mark Spreksel
 */
public class UnauthorizedException extends RuntimeException {

    @java.io.Serial
    private static final long serialVersionUID = 4750443217387437838L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
