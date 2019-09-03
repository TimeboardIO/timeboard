package kronops.core.dao;

import kronops.core.api.ProjectDAO;
import kronops.core.model.Project;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;

@Component(
        service = ProjectDAO.class,
        immediate = true,
        scope = ServiceScope.SINGLETON
)
@Transactional
public class ProjectDAOImpl implements ProjectDAO {

    @Reference(target = "(osgi.unit.name=kronops-pu)")
    JpaTemplate jpa;


    @Override
    public Project save(Project object) {
        jpa.tx(em -> {
            em.persist(object);
            em.flush();
        });

        return object;
    }



    @Override
    public Project delete(Project object) {
        jpa.tx(em -> {
            em.remove(object);
            em.flush();
        });
        return object;
    }

    @Override
    public Project findById(Long objectPkey) {
        return jpa.txExpr(em -> {
            Project data = em.createQuery("select p from Project p where p.id = :id", Project.class)
                    .setParameter("id", objectPkey)
                    .getSingleResult();
            return data;
        });
    }

    @Override
    public List<Project> findAll() {
        return jpa.txExpr(em -> {
            List<Project> data = em.createQuery("select p from Project p", Project.class)
                    .getResultList();
            return data;
        });
    }
}
