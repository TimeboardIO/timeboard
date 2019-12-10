package timeboard.core.api.exceptions;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(CommercialException.class)
    public ModelAndView handleCommercialException(CommercialException ex) {

        ModelAndView model = new ModelAndView("commercial_error.html");
        model.addObject("errCause", ex.getCause());
        model.addObject("errMsg", ex.getMessage());

        return model;

    }

}