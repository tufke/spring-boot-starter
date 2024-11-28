package nl.kabisa.spring.boot.starter.service.exception;

/**
 * Exception thrown when an identity has insufficient access rights to a resource method in the rest controller.
 * This will result in a response status 403 (Forbidden).
 *
 * @author Mark Spreksel
 */
public class ForbiddenException extends RuntimeException {
    @java.io.Serial
    private static final long serialVersionUID = 4991911151717615920L;

    public ForbiddenException(String message) {
        super(message);
    }
}
