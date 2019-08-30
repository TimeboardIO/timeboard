/* eslint-disable */



export class Project{

     public comments?:string | undefined;
     public name?:string | undefined;
     public startDate?:Date | undefined;
     public id?:number | undefined;
     public members?:Array<any> | undefined;

}


export class User{

     public password?:string | undefined;
     public beginWorkDate?:Date | undefined;
     public projectManager?:boolean | undefined;
     public validateOwnImputation?:boolean | undefined;
     public mantisUserName?:string | undefined;
     public mailTokenEndDate?:Date | undefined;
     public accountCreationTime?:Date | undefined;
     public imputationFutur?:boolean | undefined;
     public name?:string | undefined;
     public email?:string | undefined;
     public mailToken?:string | undefined;
     public jiraUserName?:string | undefined;
     public login?:string | undefined;
     public matriculeID?:string | undefined;
     public apiToken?:string | undefined;
     public id?:number | undefined;
     public firstName?:string | undefined;

}


export class RPCProjectServiceBP{


    public static getProjects():Promise<Array<Project>>{
        return new Promise((resolve: any, reject: any) => {
            fetch("/rpc", {
                            method: "POST",
                            headers: {
                                  'Accept': 'application/json',
                                  'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                "jsonrpc": "2.0",
                                "method": "kronops.core.service.ProjectServiceBPImpl#getProjects",
                                "params": [
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
    public static getProject(projectId:number,):Promise<Project>{
        return new Promise((resolve: any, reject: any) => {
            fetch("/rpc", {
                            method: "POST",
                            headers: {
                                  'Accept': 'application/json',
                                  'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                "jsonrpc": "2.0",
                                "method": "kronops.core.service.ProjectServiceBPImpl#getProject",
                                "params": [
                                    projectId,
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
    public static saveProject(project:Project,):Promise<Project>{
        return new Promise((resolve: any, reject: any) => {
            fetch("/rpc", {
                            method: "POST",
                            headers: {
                                  'Accept': 'application/json',
                                  'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                "jsonrpc": "2.0",
                                "method": "kronops.core.service.ProjectServiceBPImpl#saveProject",
                                "params": [
                                    project,
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

}
export class RPCUserServiceBP{


    public static getCurrentUser():Promise<User>{
        return new Promise((resolve: any, reject: any) => {
            fetch("/rpc", {
                            method: "POST",
                            headers: {
                                  'Accept': 'application/json',
                                  'Content-Type': 'application/json'
                            },
                            body: JSON.stringify({
                                "jsonrpc": "2.0",
                                "method": "kronops.core.service.UserServiceBP#getCurrentUser",
                                "params": [
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

}


