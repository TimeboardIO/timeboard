import React from "react";
import ReactDOM from "react-dom";

class TaostProps{
    constructor(public message:string){

    }
}

export default class ToastFactory{

    public static   toast(message: string):void{
        const errorToast = React.createElement(Toast, {
            message : JSON.stringify(message)
        });

        ReactDOM.render(errorToast, document.getElementById('toast'));
    }

}

class Toast extends React.Component<TaostProps, any>{


    render(){
        setTimeout(()=>{
            if((document.querySelector("#toast > *"))){ 
                ReactDOM.unmountComponentAtNode((document.querySelector("#toast") as Element));
            }
        }, 2000);
        return (<div>{this.props.message}</div>)
    }

}