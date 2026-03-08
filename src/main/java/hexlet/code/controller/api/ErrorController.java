package hexlet.code.controller.api;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class ErrorController {

    @GetMapping("/error")
    public String error() {
        throw new RuntimeException("This is a test exception for Sentry");
    }

    @GetMapping("/error2")
    public String error2() {
        try {
            throw new Exception("This is a test.");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
        return "This is a test.";
    }

}
