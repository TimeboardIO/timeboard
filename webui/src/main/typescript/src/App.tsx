import React from 'react';
import '../node_modules/@fortawesome/fontawesome-free/css/all.css';
import './App.scss';
import { BrowserRouter as Router, Route, NavLink } from "react-router-dom";

import Timesheet from './timesheet/timesheet';
import UserDetails from './user-details';
import Projects from './projects/projects';
import ProjectDetails from './projects/project-details';

function Index() {
  return <h2>Home</h2>;
}



const App: React.FC = () => {
  return (
    <Router>
      <div className="layout">
        <nav className="main-nav">
          <div className="logo">ePO</div>
          <ul className="nav-bar">
            <li>
              <NavLink  exact={true} className="nav-link" to="/"  activeClassName='active'><i className="fas fa-chart-line"></i> Home</NavLink>
            </li>
            <li>
              <NavLink className="nav-link" to="/timesheet/"  activeClassName='active' ><i className="far fa-clock"></i> Timesheet</NavLink>
            </li>
            <li>
              <NavLink className="nav-link" to="/projects/"  activeClassName='active' ><i className="fas fa-project-diagram"></i> Projects</NavLink>
            </li>
          </ul>
          <UserDetails/>
        </nav>

        <div className="main-placeholder">
          <div className="container-fluid">
          <Route path="/" exact component={Index}   />
          <Route path="/timesheet/" exact component={Timesheet}/>
          <Route path="/projects/" exact component={Projects}/>
          <Route path="/projects/:uuid" exact component={ProjectDetails}/>
          </div>
        </div>
      </div>
    </Router>
  );
}

export default App;
