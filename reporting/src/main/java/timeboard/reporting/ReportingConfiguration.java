package timeboard.reporting;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "timeboard.reporting")
@ServletComponentScan(basePackages = "timeboard.reporting")
public class ReportingConfiguration {
}
