package nl.kabisa.spring.boot.starter.service.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * A constraint violation.
 *
 * @author Mark Spreksel
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintViolation {

    /**
     * The field that causes the violation.
     * <p>
     * Example: car.licenseplate
     */
    private String field;

    /**
     * The violation message.
     * <p>
     * Example: may not be empty
     */
    @NotNull
    private String message;
}
