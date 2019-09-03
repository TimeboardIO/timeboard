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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientAPIModel {

    private final Set<ApiEntity> apiEntities = new HashSet<>();
    private final Set<ApiEndpoint> apiEndpoints = new HashSet<>();

    public Set<ApiEndpoint> getApiEndpoints() {
        return apiEndpoints;
    }

    public Set<ApiEntity> getApiEntities() {
        return apiEntities;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("===Entites===\n");
        this.getApiEntities().forEach(entity -> {
            builder.append(entity.toString());
        });

        builder.append("===Enpoints===\n");
        this.getApiEndpoints().forEach(endpoint -> {
            builder.append(endpoint.toString());
        });


        return builder.toString();
    }

    public static class ApiEntity {
        private boolean isEnum = false;
        private final Set<TypeAttribute> attributes = new HashSet<>();
        private String entityName;

        public boolean isEnum() {
            return isEnum;
        }

        public void setEnum(boolean anEnum) {
            isEnum = anEnum;
        }

        public Set<TypeAttribute> getAttributes() {
            return attributes;
        }


        public String getEntityName() {
            return entityName;
        }

        public void setEntityName(String entityName) {
            this.entityName = entityName;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if(this.isEnum()) {
                builder.append("Enum :" + this.getEntityName() + "\n");
            }else{
                builder.append("Class :" + this.getEntityName() + "\n");
            }
            this.getAttributes().forEach(a -> {
                builder.append("\t" + a.getAttributeName() + ":" + RPCUtils.convert(a.getAttributeType()) + "\n");
            });
            builder.append("\n");
            return builder.toString();
        }
    }

    public static class TypeAttribute {
        private String attributeName;
        private String attributeType;

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeType() {
            return attributeType;
        }

        public void setAttributeType(String attributeType) {
            this.attributeType = attributeType;
        }
    }

    public static class ApiEndpointParam {

        private String paramName;
        private String paramType;

        public ApiEndpointParam(String paramName, String paramType) {
            this.paramName = paramName;
            this.paramType = paramType;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }
    }

    public static class ApiEndpoint {

        private String endpointName;
        private String endpointClass;
        private Set<ApiEndpointMethod> endpointMethods = new HashSet<>();

        public String getEndpointName() {
            return endpointName;
        }

        public void setEndpointName(String endpointName) {
            this.endpointName = endpointName;
        }

        public String getEndpointClass() {
            return endpointClass;
        }

        public void setEndpointClass(String endpointClass) {
            this.endpointClass = endpointClass;
        }

        public Set<ApiEndpointMethod> getEndpointMethods() {
            return endpointMethods;
        }

        public void setEndpointMethods(Set<ApiEndpointMethod> endpointMethods) {
            this.endpointMethods = endpointMethods;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append(this.getEndpointName());
            builder.append("\n");
            this.getEndpointMethods().forEach(apiEndpointMethod -> {
                builder.append("\t");
                builder.append(apiEndpointMethod.toString());
                builder.append("\n");
            });
            return builder.toString();
        }
    }

    public static class ApiEndpointMethod {

        private String methodName;
        private String methodReturnType;
        private List<ApiEndpointParam> methodParams = new ArrayList<>();

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodReturnType() {
            return methodReturnType;
        }

        public void setMethodReturnType(String methodReturnType) {
            this.methodReturnType = methodReturnType;
        }

        public List<ApiEndpointParam> getMethodParams() {
            return methodParams;
        }

        public void setMethodParams(List<ApiEndpointParam> methodParams) {
            this.methodParams = methodParams;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("\t" + this.getMethodName());
            builder.append("(");
            this.getMethodParams().stream().forEach(p -> {
                builder.append(p.getParamName());
                builder.append(":");
                builder.append(p.getParamType());
                builder.append(",");
            });
            builder.append("):");
            builder.append(this.getMethodReturnType());
            builder.append(";\n");

            return builder.toString();
        }


    }
}
