package timeboard.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.NoResultException;

@Controller
public class ExceptionHandlingController {

    @ExceptionHandler(Exception.class)
    public String missingRessource() {
        return "error";
    }

}
