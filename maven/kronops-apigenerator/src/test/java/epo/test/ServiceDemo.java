package kronops.test;

/*-
 * #%L
 * kronops-apigenerator-maven-plugin
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
