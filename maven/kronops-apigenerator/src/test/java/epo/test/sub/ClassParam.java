package kronops.test.sub;

import kronops.apigenerator.annotation.RPCEntity;
import kronops.apigenerator.annotation.RPCParam;
import kronops.test.ServiceDemo;

import java.util.List;

@RPCEntity
public  class ClassParam {

    private TestEnum name;

    @RPCParam(value = "child", listOf = ServiceDemo.ChildParam.class)
    private List<ServiceDemo.ChildParam> child;


}
