package kronops.core.dao;

import kronops.core.JPAUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(
        service = ProjectDAOImpl.class,
        immediate = true
)
public class ProjectDAOImpl implements kronops.core.api.ProjectDAO {

    @Activate
    public void init(){
        Map<String, Object> test = JPAUtil.getEntityManagerFactory().getProperties();
        System.out.println(test.size());
    }

}
