package kronops.core.dao;

import kronops.core.JPAUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

import java.util.Map;

@Component(
        service = ProjectDAOImpl.class,
        immediate = true,
        scope = ServiceScope.SINGLETON
)
public class ProjectDAOImpl implements kronops.core.api.ProjectDAO {



}
