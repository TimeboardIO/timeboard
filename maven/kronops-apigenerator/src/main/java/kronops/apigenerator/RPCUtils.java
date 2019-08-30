package kronops.apigenerator;



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
