import React from "react";
import { RPCProjectServiceBP, Project } from "../api";
import '../App.scss';

class ProjectTimesheetLineProps {
    constructor(public project: Project, public days: Array<String>) {

    }
}

export class ProjectTimesheetLine extends React.Component<ProjectTimesheetLineProps, any>{

    constructor(props: ProjectTimesheetLineProps) {
        super(props);
    }

    getClassName(load: number) {
        if (load < 0.2) {
            return "load empty"
        }
        if (load < 0.8) {
            return "load todo"
        }
        if (load < 1) {
            return "load good"
        }
        return "load";
    }

    render() {
        return (<div className="project">
            <div className="projectName">{this.props.project.name}</div>
            <div className="projectDays">
                {
                    this.props.days.map(d => {
                        const load = (Math.round(Math.random() * 100) / 100);

                        return (<input
                            className={this.getClassName(load)}
                            type="number"
                            value={load}></input>
                        )
                    })
                }
            </div>
        </div>)
    }


}

class TimeSheetState {
    constructor(public projects: Array<Project>, public days: Array<String>) {

    }
}

export default class Timesheet extends React.Component<any, TimeSheetState> {


    constructor(props: any) {
        super(props);


        this.state = {
            projects: [],
            days: [
                "Lundi",
                "Mardi",
                "Mercredi",
                "Jeudi",
                "Vendredi",
                "Samedi",
                "Dimanche"
            ]
        };

    }


    componentDidMount() {


        RPCProjectServiceBP.getProjects().then(pjts => {
            this.setState({
                projects: pjts,
            });
        });


    }

    render() {
        return (<div className="timesheet">
            <h2>Timesheet</h2>
            <div className="days">
                <div></div>
                {
                    this.state.days.map(d => {
                        return (
                            <div>{d}</div>
                        )
                    })
                }
            </div>
            <div className="projects">
                {
                    this.state.projects.map(p => {
                        return (<ProjectTimesheetLine project={p} days={this.state.days} />);
                    })
                }
            </div>
        </div>);
    }

}



