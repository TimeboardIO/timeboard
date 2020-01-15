package timeboard.vacations.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import timeboard.core.api.VacationService;
import timeboard.core.model.VacationRequest;
import timeboard.core.security.TimeboardAuthentication;

import java.util.Optional;

@Component
public class LongToVacationRequestConverter implements Converter<String, VacationRequest> {

    @Autowired
    private VacationService service;

    @Override
    public VacationRequest convert(String aLong) {
        TimeboardAuthentication auth = (TimeboardAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Optional<VacationRequest> request = this.service.getVacationRequestByID(auth.getDetails(), Long.parseLong(aLong));
        if (request.isPresent()) {
            return this.service.getVacationRequestByID(auth.getDetails(), Long.parseLong(aLong)).get();
        } else {
            return null;
        }
    }
}
