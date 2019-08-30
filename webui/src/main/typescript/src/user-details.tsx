import React, { MouseEvent } from "react";  
import { RPCUserServiceBP } from "./api";
 
export default class UserDetails extends React.Component {

    constructor(props: any) {
        super(props);
        this.state = {
            user: null
        };
    }

    componentDidMount() {

        RPCUserServiceBP.getCurrentUser().then(data => {
             this.setState({
                user: data
            });
        });
          
 


    }

    logout(e : MouseEvent){
        window.location.href = "http://localhost:3000/logout";
    }

    render() {
        if ((this.state as any).user) {
            return (
            <div className="user-details">
                <i className="far fa-user"></i>
                <div className="user-name">{(this.state as any).user.name}</div>
                <div className="logout" onClick={this.logout}><i className="fas fa-sign-out-alt"></i></div>
            </div>
            );
        } else {
            return (<div>...</div>);
        }
    }

}