package timeboard.projects.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.ProjectService;
import timeboard.core.api.exceptions.BusinessException;
import timeboard.core.model.Project;
import timeboard.core.security.TimeboardAuthentication;


@Component
public class LongToProjectConverter implements Converter<String, Project>{

    @Autowired
    private ProjectService service;

    @Override
    public Project convert(String aLong) {
        TimeboardAuthentication auth = (TimeboardAuthentication) SecurityContextHolder.getContext().getAuthentication();
        try {
            return this.service.getProjectByID(auth.getDetails(), auth.getCurrentOrganization(), Long.parseLong(aLong));
        } catch (BusinessException e) {
            return null;
        }
    }
}
