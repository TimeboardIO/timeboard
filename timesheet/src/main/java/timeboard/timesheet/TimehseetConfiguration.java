package timeboard.timesheet;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ServletComponentScan(basePackages = {"timeboard.timesheet"})
@ComponentScan(basePackages = {"timeboard.timesheet"})
public class TimehseetConfiguration {
}
