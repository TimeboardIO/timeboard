import React from "react";
import { NavLink } from "react-router-dom";
import { Project, RPCProjectServiceBP } from "../api";
import ProjectDetails from "./project-details";

 

export default class Projects extends React.Component<any, any> {

    constructor(props: any) {
        super(props); 
        this.state = {
            projects: [],
            newProject: new Project()
        };
    }

    componentDidMount() {


        RPCProjectServiceBP.getProjects().then(pjts => { 
            this.setState({
                projects: pjts,
                newProject: new Project()
            });
        });


    }



    render() {
        return (
            <React.Fragment>
                <h2>Projects</h2>
                <div className="list-group">
                    {(this.state.projects as Project[]).map(p => {
                        return (<NavLink className="list-group-item"  key={p.id} to={`/projects/${p.id}`} activeClassName='active'  >{p.name}</NavLink>);
                    })}
                </div>
                <hr/>
                <div className="card">
                    <div className="card-body">
                        <ProjectDetails project={this.state.newProject} title="New Project"/>
                    </div>
                </div>
            </React.Fragment>
        );
    }

}