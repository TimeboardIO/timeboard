package kronops.test;

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.apigenerator.annotation.RPCParam;
import kronops.test.sub.ClassParam;

import java.util.List;

@RPCEndpoint
public class AnotherDemo {


    @RPCMethod(returnListOf = ClassParam.class)
    public List<ClassParam> doAnotherThing(
            @RPCParam(value = "a", listOf =  ClassParam.class) List<ClassParam> a,
            @RPCParam("b") Integer b,
            @RPCParam("c") int c,
            @RPCParam("d") Boolean d,
            @RPCParam("e") boolean e) {
        return null;
    }

}
