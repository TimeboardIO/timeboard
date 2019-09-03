package kronops.apigenerator;

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



import kronops.apigenerator.annotation.RPCParam;

import java.lang.reflect.Parameter;

public class RPCUtils {

    public static String convert(String type) {

        String genericParam = "any";


        switch (type.toLowerCase()) {
            case "long":
                return "number";
            case "double":
                return "number";
            case "float":
                return "number";
            case "set":
                return "Array<"+genericParam+">";
            case "list":
                return "Array<"+genericParam+">";
            case "int":
                return "number";
            case "integer":
                return "number";
            case "class":
                return "any";
            case "uuid":
                return "string";
            case "string":
                return "string";
            case "boolean":
                return "boolean";
            default:
                return type;
        }
    }

    public static String getParamName(Parameter p) {
        if (p.getAnnotation(RPCParam.class) != null) {
            return p.getAnnotation(RPCParam.class).value();
        }
        return p.getName();
    }



    public static String convertList(String type) {
        return "Array<"+RPCUtils.convert(type)+">";
    }
}
