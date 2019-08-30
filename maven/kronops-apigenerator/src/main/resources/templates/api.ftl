/* eslint-disable */

<#assign RPC=statics['kronops.apigenerator.RPCUtils']>

<#list model.apiEntities as entity>

<#if !entity.isEnum()>
export class ${entity.entityName}{

<#list entity.attributes as attribute>
     public ${attribute.attributeName}?:${attribute.attributeType} | undefined;
 </#list>

}
<#else>
export enum ${entity.entityName}{

<#list entity.attributes as attribute>
     ${attribute.attributeName},
 </#list>

}
</#if>

</#list>

<#list model.apiEndpoints as endpoint>
export class ${endpoint.endpointName}{


<#list endpoint.endpointMethods as method>
    public static ${method.methodName}(<#list method.methodParams as param>${param.paramName}:${param.paramType},</#list>):Promise<${method.methodReturnType}>{
        return new Promise((resolve: any, reject: any) => {
            fetch("/rpc", {
                            method: "POST",
                            headers: {
                                  'Accept': 'application/json',
                                  'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                "jsonrpc": "2.0",
                                "method": "${endpoint.endpointClass}#${method.methodName}",
                                "params": [<#list method.methodParams as param>
                                    ${param.paramName},</#list>
                                ],
                                "id": 1
                            })
                        })
                       .then(response => response.json())
                       .then(json => {
                        if(json.error){
                            reject(json.error);
                        }
                            resolve(json.result);
                        });
        });
    }
</#list>

}
</#list>


