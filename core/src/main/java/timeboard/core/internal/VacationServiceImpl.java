package timeboard.core.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import timeboard.core.api.VacationService;
import timeboard.core.model.Account;
import timeboard.core.model.VacationRequest;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Component
@Transactional
public class VacationServiceImpl implements VacationService {

    @Autowired
    private EntityManager em;

    @Override
    public VacationRequest createVacationRequest(Account actor, VacationRequest request) {


        em.persist(request);

        em.flush();

        return request;
    }

}
