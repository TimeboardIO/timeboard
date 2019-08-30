package kronops.core.service;

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.core.model.User;
import org.osgi.service.component.annotations.Component;

@RPCEndpoint
@Component(
        service = UserServiceBP.class,
        immediate = true
)
public class UserServiceBP implements kronops.core.api.UserServiceBP {

    @Override
    @RPCMethod
    public User getCurrentUser(){
        return null;
    }
}
