package kronops.core.dao;

/*-
 * #%L
 * core
 * %%
 * Copyright (C) 2019 Kronops
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
