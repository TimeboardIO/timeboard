const currentProjectID = $("meta[property='tasks']").attr('project');

// TASK EDIT/CREATE MODAL VUEJS COMPONENT
Vue.component('task-modal', {
    template: '#task-modal-template',
    props: {
        task: Object,
        formError: String,
        modalTitle: String
    }
});

// GRAPH MODAL VUEJS COMPONENT
Vue.component('graph-modal', {
    template: '#graph-modal-template',
    props: {
        task: Object,
        formError: String,
        modalTitle: String
    }
});

// Form validations rules
const formValidationRules = {
    fields: {
        projectID: {
            identifier: 'projectID',
            rules: [ { type   : 'empty', prompt : 'Please select project'  } ]
        },
        taskName: {
            identifier: 'taskName',
            rules: [ { type   : 'empty', prompt : 'Please enter task name'  } ]
        },
        taskStartDate: {
            identifier: 'taskStartDate',
            rules: [
            { type: "empty", prompt : 'Please enter task start date'  } ]
        },
        taskEndDate: {
            identifier: 'taskEndDate',
            rules: [
            { type: "empty", prompt : 'Please enter task end date'  } ]
        },
        taskOriginalEstimate: {
            identifier: 'taskOriginalEstimate',
            rules: [ { type   : 'empty', prompt : 'Please enter task original estimate in days'  },
            { type   : 'number', prompt : 'Please enter task a number original estimate in days'  } ]
        }
    }
};

// Empty task initalisation
const emptyTask =  {
    taskID: 0,
    projectID: currentProjectID,
    taskName: "",
    taskComments: "",
    startDate: "",
    endDate:"",
    originalEstimate: 0,
    typeID: 0,
    typeName: '',
    assignee: "",
    assigneeID: 0,
    status: "PENDING",
    statusName: '',
    milestoneID: '',
    milestoneName: '',
};

// VUEJS MAIN APP
let app = new Vue({

    el: '#tasksList',
    data: {
        newTask: Object.assign({}, emptyTask),
        formError: "",
        modalTitle: "Create task",
        table: {
            cols: [
                {
                    "slot": "name",
                    "label": "Task",
                    "sortKey": "taskName",
                    "primary" : true
                },
                {
                    "slot": "start",
                    "label": "Start date",
                    "sortKey": "taskStartDate"

                },
                {
                    "slot": "end",
                    "label": "End date",
                    "sortKey": "taskEndDate"

                },
                {
                    "slot": "oe",
                    "label": "OE",
                    "sortKey": "originalEstimate"

                },
                {
                    "slot": "assignee",
                    "label": "Assignee",
                    "sortKey": "assignee"

                },
                {
                    "slot": "status",
                    "label": "Status",
                    "sortKey": "status"

                },
                {
                    "slot": "milestone",
                    "label": "Milestone",
                    "sortKey": "milestoneID"

                },
                {
                    "slot": "type",
                    "label": "Type",
                    "sortKey": "typeID"

                },
                {
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            filters: {
               name:      { filterKey: 'taskName',         filterValue: '', filterFunction: (filter, row) => row.toLowerCase().indexOf(filter.toLowerCase()) > -1 },
               start:     { filterKey: 'startDate',        filterValue: '', filterFunction: (filter, row) => new Date(row).getTime() > new Date(filter).getTime() },
               end:       { filterKey: 'endDate' ,         filterValue: '', filterFunction: (filter, row) => new Date(row).getTime() < new Date(filter).getTime() },
               oeMin:     { filterKey: 'originalEstimate', filterValue: '', filterFunction: (filter, row) => parseFloat(row) >= parseFloat(filter) },
               oeMax:     { filterKey: 'originalEstimate', filterValue: '', filterFunction: (filter, row) => parseFloat(row) <= parseFloat(filter) },
               assignee:  { filterKey: 'assignee',         filterValue: '', filterFunction: (filter, row) => row.toLowerCase().indexOf(filter.toLowerCase()) > -1 },
               status:    { filterKey: 'status',           filterValue: '', filterFunction: (filter, row) => row.toLowerCase().indexOf(filter[0].toLowerCase()) > -1 },
               milestone: { filterKey: 'milestoneID',      filterValue: '', filterFunction: (filter, row) => row.toLowerCase().indexOf(filter[0].toLowerCase()) > -1 },
               type:      { filterKey: 'typeID',           filterValue: '',  filterFunction: (filter, row) => row.toLowerCase().indexOf(filter[0].toLowerCase()) > -1 },
            },
            data: [],
            name: 'tableTask',
            configurable : true
        }
    },
    methods: {
        showGraphModal: function(projectID, task, event){
            $('.graph.modal').modal({ detachable : true, centered: true }).modal('show');
            $.ajax({
                method: "GET",
                url: "/api/tasks/chart?task="+task.taskID,
                success : function(data, textStatus, jqXHR) {

                    let listOfTaskDates = data.listOfTaskDates;
                    let effortSpentDataForChart = data.effortSpentData;
                    let realEffortDataForChart = data.realEffortData;

                    //chart config
                    let chart = new Chart($("#lineChart"), {
                        type: 'line',
                        data: {
                            labels: listOfTaskDates,
                            datasets: [{
                                data: effortSpentDataForChart,
                                label: "Effort spent for " + task.taskName,
                                borderColor: "#3e95cd",
                                fill: true,
                                steppedLine: true
                            } , {
                                data: realEffortDataForChart,
                                label: "Real effort for " + task.taskName,
                                borderColor: "#ff6384",
                                fill: true,
                                steppedLine: true
                            }]
                        },
                        options: {
                            title: { display: true, text: 'Task - Real Effort and Effort Spent graph' },
                            scales: {
                                yAxes: [{
                                    ticks: {
                                        min: 0
                                    },
                                    scaleLabel: { display: true, labelString: 'Number of days' }
                                }],
                                xAxes: [{
                                    scaleLabel: { display: true, labelString: 'Dates' }
                                }],
                            }
                        }
                    });

                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.log(data);
                }
            });
        },
        showCreateTaskModal: function(projectID, task, event){
            event.preventDefault();
            if(task){
                 this.modalTitle = "Edit task";
                 this.newTask.projectID = projectID;
                 this.newTask.projectID = currentProjectID;
                 this.newTask.taskID = task.taskID;

                 this.newTask.taskName = task.taskName;
                 this.newTask.taskComments = task.taskComments;

                 this.newTask.endDate = task.endDate;
                 this.newTask.startDate = task.startDate;

                 this.newTask.originalEstimate = task.originalEstimate;
                 this.newTask.typeID = task.typeID;

                 this.newTask.assignee = task.assignee;
                 this.newTask.assigneeID = task.assigneeID;

                 this.newTask.status = task.status;
                 this.newTask.milestoneID = task.milestoneID;

                 this.newTask.milestoneName = task.milestoneName;
                 this.newTask.typeName = task.typeName;
                 this.newTask.statusName = task.statusName;

            }else{
                 this.modalTitle = "Create task";
                 Object.assign(this.newTask , emptyTask);
            }
            keepThis = this;
            $('.create-task.modal').modal({
                onApprove : function($element){
                    var validated = $('.create-task .ui.form').form(formValidationRules).form('validate form');
                    var object = {};
                    if(validated){
                        $('.ui.error.message').hide();
                        $.ajax({
                            method: "POST",
                            url: "/api/tasks",
                            data: JSON.stringify(app.newTask),
                            contentType: "application/json",
                            dataType: "json",
                            success : function(data, textStatus, jqXHR) {
                                keepThis.gridData = keepThis.gridData.filter(function(el){
                                    return el.taskID != data.taskID;
                                });
                                keepThis.gridData.push(data);
                                $('.create-task .ui.form').form('reset');
                                $('.create-task.modal').modal('hide');
                            },
                            error: function(jqXHR, textStatus, errorThrown) {
                                $('.ui.error.message').text(jqXHR.responseText);
                                $('.ui.error.message').show();
                            }
                        });
                    }
                    return false;
                },
                detachable : true, centered: true
            }).modal('show');
        },
    },
    mounted: function () {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "/api/tasks?project="+currentProjectID,
            success: function (d) {
                self.table.data = d;
                $('.ui.dimmer').removeClass('active');
            }
        });
    }
});

//Initialization
$(document).ready(function(){
    //init dropdown fields
     $('.ui.multiple.dropdown').dropdown();

    //init search fields
    $('.ui.search')
    .search({
        apiSettings: {
            url: '/api/search?q={query}&projectID='+currentProjectID+''
        },
        fields: {
            results : 'items',
            title   : 'screenName',
            description : 'email'
        },
        onSelect: function(result, response) {
            $('.assigned').val(result.screenName);
            $('.taskAssigned').val(result.id);
            app.newTask.assignee = result.screenName;
            app.newTask.assigneeID = result.id;
        },
        minCharacters : 3
    });
});


