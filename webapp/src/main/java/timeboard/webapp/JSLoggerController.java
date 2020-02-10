package timeboard.webapp;

/*-
 * #%L
 * webapp
 * %%
 * Copyright (C) 2019 - 2020 Timeboard
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import timeboard.core.security.TimeboardAuthentication;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/js-log")
public class JSLoggerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSLoggerController.class);

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
