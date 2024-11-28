package nl.kabisa.spring.boot.starter.service.problem;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * An <code>ErrorController</code> that overrides default Spring behaviour and returns error
 * JSON messages compliant with the Problem type definitions.
 * <p>
 * This controller is called when the client sends a not existing url (not leading to a resource controller) in the request. this error
 * controller will send a Problem result back in that case.
 */
@Controller
@ConditionalOnProperty(prefix = "service.starter.problem", name = "enabled", havingValue = "true", matchIfMissing = true)
class ProblemErrorController extends AbstractErrorController {

    /**
     * Path of the problem controller.
     */
    @Getter
    @Value("${server.error.path:/error}")
    private String errorPath = "/error";

    public ProblemErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @ResponseBody
    @RequestMapping(path = "${server.error.path:/error}", produces = Problem.PROBLEM_JSON)
    public ResponseEntity<Problem> error(HttpServletRequest req) {
        Map<String, Object> attr = getErrorAttributes(req, ErrorAttributeOptions.defaults());
        HttpStatus status = getStatus(req);
        String title = (String) attr.getOrDefault("title", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        String detail = (String) req.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String instance = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Problem body = Problem.builder()
                .status(status.value())
                .title(title)
                .detail(StringUtils.hasText(detail) ? detail : null)
                .instance(StringUtils.hasText(instance) ? instance : null)
                .build();
        return new ResponseEntity<>(body, status);
    }

}
