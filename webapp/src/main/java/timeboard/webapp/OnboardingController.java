package timeboard.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class OnboardingController {

    @GetMapping("/")
    public String onboarding(Principal p){
        if(p == null){
            return "onboarding";
        }else{
            return "redirect:/home";
        }

    }

}
