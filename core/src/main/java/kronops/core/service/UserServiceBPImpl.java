package kronops.core.service;

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.core.api.UserServiceBP;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Component;

@RPCEndpoint
@Component(
        service = UserServiceBP.class,
        immediate = true
)
public class UserServiceBPImpl implements UserServiceBP {

    @Override
    @RPCMethod
    public User getCurrentUser(){
        User u = new User();
        u.setName("Hello");
        u.setFirstName("World");
        return u;
    }
}
