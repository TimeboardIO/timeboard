package kronops.test;

import kronops.apigenerator.annotation.RPCEndpoint;
import kronops.apigenerator.annotation.RPCEntity;
import kronops.apigenerator.annotation.RPCMethod;
import kronops.apigenerator.annotation.RPCParam;
import kronops.test.sub.ClassParam;

import java.util.Collections;
import java.util.List;

@RPCEndpoint
public class ServiceDemo {

    @RPCMethod
    public String doStuff() {
        return "ok";
    }

    @RPCMethod
    public String doStuffWithPrimitiveParams(
            @RPCParam("a") String a,
            @RPCParam("b") Integer b,
            @RPCParam("c") int c,
            @RPCParam("d") Boolean d,
            @RPCParam("e") boolean e) {
        return "ok";
    }

    @RPCMethod(returnListOf = String.class)
    public List<String> doStuffWithClassParams(
            @RPCParam(value = "a", listOf = ClassParam.class) List<ClassParam> a,
            @RPCParam("b") Integer b,
            @RPCParam("c") int c,
            @RPCParam("d") Boolean d,
            @RPCParam("e") boolean e) {
        return (List<String>) Collections.singleton("ok");
    }

    @RPCEntity
    public static class ChildParam {

        private String name;
        private int date;
        private String test;
    }



}
