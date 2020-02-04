package timeboard.webapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/js-log")
public class JSLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSLogger.class);

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logError(final HttpServletRequest request,
                         final TimeboardAuthentication authentication,
                         @RequestParam(required = true) final String message,
                         @RequestParam(required = true) final String level) {
        if(authentication.isAuthenticated()) {
            final String ipAddress = request.getRemoteAddr();
            final String hostname = request.getRemoteHost();

            final String logMessage = "Received client-side message (" + ipAddress + "/" + hostname + "): " + message;
            switch (level) {
                case "LOG":
                case "INFO":
                    LOGGER.info(logMessage);
                    break;
                case "WARN":
                    LOGGER.warn(logMessage);
                    break;
                case "DEBUG":
                    LOGGER.debug(logMessage);
                    break;
                case "ERROR":
                    LOGGER.error(logMessage);
                    break;
                case "TRACE":
                    LOGGER.trace(logMessage);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + level);
            }


        }
    }

}
