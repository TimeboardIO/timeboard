import React, { ChangeEvent } from "react";
import { Project, RPCProjectServiceBP } from "../api";
import ToastFactory from "../common/toast";

class State {
    project: Project = new Project();
    error: number = 0;

}

class Props {
    constructor(public project: Project, public title: String = "Project") { }
}

export default class ProjectDetails extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = new State();
    }

    componentWillReceiveProps(props: Props) {
        this.setState({
            project: props.project
        })
    }
    componentDidMount() {
        this.updateProjectsList();

    }


    private updateProjectsList() {
        if ((this.props as any).match) {

            const projectUUID = (this.props as any).match.params.uuid;
            RPCProjectServiceBP.getProject((projectUUID))
                .then(project => {
                    this.setState({
                        project: project as Project
                    });
                }).catch((error: any) => {
                    this.setState({
                        error: error.data[0].targetException.localizedMessage
                    });
                });
        }
    }

    handleChange(event: ChangeEvent) {

        var updatedProject = this.state.project;

        if (updatedProject) {
            updatedProject.name = (event.target as any).value;
        }

        this.setState({
            project: updatedProject
        });
    }


    createProject() {
        RPCProjectServiceBP.saveProject(this.state.project)
            .then((d) => { 
                this.updateProjectsList();
            })
            .catch(err => {
                console.log(err);
                ToastFactory.toast((err as any).data[0].targetException.localizedMessage);
            });
    }

    printMembers(s: any): any {
        if (s.project.members) {
            var acc: any[] = [];
            s.project.members.map((m: any) => {
                acc.push(<div key={m} className="list-group-item">{m}</div>);
            })
            return acc;
        }
        return (<div></div>);
    }

    render() {
        if (this.state.project && this.state.error < 300) {
            return (
                <React.Fragment>
                    <h3>{this.props.title} {this.state.project.name}</h3>

                    <div className="form-group">
                        <label>Project Name : </label>
                        <input className="form-control" type="text" name="name" value={this.state.project.name} onChange={this.handleChange.bind(this)} />
                    </div>
                    <h4>Members</h4>
                    <div className="list-group">
                        {this.printMembers(this.state)}
                    </div>
                    <div className="btn btn-success" onClick={this.createProject.bind(this)} >Create</div>
                </React.Fragment>
            );
        } else {
            return (<div>Oups ! -> {this.state.error}</div>)
        }
    }

}