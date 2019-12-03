package timeboard.projects;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"timeboard.projects"})
@ServletComponentScan({"timeboard.projects"})
public class ProjectsConfiguration {
}
