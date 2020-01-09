package timeboard.core.api;

import timeboard.core.model.Account;
import timeboard.core.model.VacationRequest;

public interface VacationService {


    public VacationRequest createVacationRequest(Account actor, VacationRequest request);

}
