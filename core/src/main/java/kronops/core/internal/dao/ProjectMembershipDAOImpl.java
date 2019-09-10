package kronops.core.internal.dao;

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

import kronops.core.api.dao.ProjectMembershipDAO;
import kronops.core.api.exceptions.BusinessException;
import kronops.core.model.ProjectMembership;
import org.apache.aries.jpa.template.JpaTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.List;

@Component(
        service = ProjectMembershipDAO.class,
        immediate = true,
        scope = ServiceScope.SINGLETON
)
public class ProjectMembershipDAOImpl implements ProjectMembershipDAO {


    @Reference(target = "(osgi.unit.name=kronops-pu)", scope = ReferenceScope.BUNDLE)
    JpaTemplate jpa;

    @Override
    public ProjectMembership save(ProjectMembership object) throws BusinessException {
        try {
            jpa.tx(em -> {
                em.persist(object);
                em.flush();
            });
        } catch (Exception e) {
            throw new BusinessException(e);
        }

        return object;
    }

    @Override
    public ProjectMembership update(ProjectMembership object) throws BusinessException {
        return null;
    }

    @Override
    public ProjectMembership delete(ProjectMembership object) {
        return null;
    }

    @Override
    public ProjectMembership findById(Long objectPkey) {
        return null;
    }

    @Override
    public List<ProjectMembership> findAll() {
        return null;
    }
}
