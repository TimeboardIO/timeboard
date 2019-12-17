const currentProjectID = $("meta[property='tasks']").attr('project');

// TASKS TABLE VUEJS COMPONENT
Vue.component('task-list', {
    template: '#task-list-template',
    props: {
        tasks: Array,
        columns: Array,
        filterKey: Array,
    },
    data: function () {
        var sortOrders = {}
        this.columns.forEach(function (key) {
            sortOrders[key] = 1;
        })
        return {
            sortKey: '',
            sortOrders: sortOrders
        }
    },
    computed: {
        filteredTasks: function () {
            var sortKey = this.sortKey
            var filterKeys = this.filterKey.filter(function (f) { return f.value != '' || (f.key == 'originalEstimate' && (f.value.min != '' ||  f.value.max != ''))});
            var order = this.sortOrders[sortKey] || 1
            var tasks = this.tasks
            var keepThis = this;
            if (filterKeys.length > 0) {
                tasks = tasks.filter(function (row) {
                    return filterKeys.every(function(f, i, array){
                        var rowValue = String(row[f.key]);
                        var filterValue = String(f.value);
                        if (f.key == 'startDate') {
                            var rowDate = new Date(rowValue);
                            var filterDate = new Date(filterValue);
                            return rowDate.getTime() > filterDate.getTime();
                        } else if(f.key == 'endDate') {
                            var rowDate = new Date(rowValue);
                            var filterDate = new Date(filterValue);
                            return rowDate.getTime() < filterDate.getTime();
                        } else if(f.key == 'originalEstimate') {
                            var rowDouble = parseFloat(rowValue);
                            var filterMin =  parseFloat(f.value.min);
                            if(!filterMin) filterMin = 0;
                            var filterMax =  parseFloat(f.value.max);
                            if(!filterMax) filterMax = 9999999;
                            return rowDouble <= filterMax && rowDouble >= filterMin  ;
                        }
                        return rowValue.toLowerCase().indexOf(filterValue.toLowerCase()) > -1
                    });
                })
            }
            if (sortKey) {
                tasks = tasks.slice().sort(function (a, b) {
                    a = a[sortKey]
                    b = b[sortKey]
                    return (a === b ? 0 : a > b ? 1 : -1) * order;
                })
            }
            return tasks;
        }
   },
    filters: {
        capitalize: function (str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
        }
    },
    methods: {
        sortBy: function (key) {
            this.sortKey = key
            this.sortOrders[key] = this.sortOrders[key] * -1
        },
        showCreateTaskModal: function(projectID, task, event){ // proxy to vue app
            this.$parent.showCreateTaskModal(projectID, task, event);
        },
        showGraphModal: function(projectID, task, event){ // proxy to vue app
            this.$parent.showGraphModal(projectID, task, event);
        },
        deleteTask: function(event, task){
            keepThis = this;
            event.target.classList.toggle('loading');
            $.get("/api/tasks/delete?task="+task.taskID)
            .then(function(data){
                keepThis.tasks.splice( keepThis.tasks.indexOf(task), 1 );
                event.target.classList.toggle('loading');
            });
        }
    }
});

// PENDING TASKS TABLE VUEJS COMPONENT
Vue.component('pending-task-list', {
    template: '#pending-task-list-template',
    props: {
        tasks: Array,
        columns: Array,
        filterKey: Array,
    },
    data: function () {
        var sortOrders = {}
        this.columns.forEach(function (key) {
            sortOrders[key] = 1;
        })
        return {
            sortKey: '',
            sortOrders: sortOrders
        }
    },
    computed: {
        filteredTasks: function () {
            var sortKey = this.sortKey
            var filterKey = [];
            filterKey.push({ key: 'status', value: 'PENDING' });
            var order = this.sortOrders[sortKey] || 1 ;
            var tasks = this.tasks;
            var keepThis = this;
            if (filterKey.length > 0) {
                tasks = tasks.filter(function (row) {
                    return filterKey.every(function(f, i, array){
                        return (String(row[f.key]).toLowerCase().indexOf(String(f.value).toLowerCase()) > -1)
                    });
                });
            }
            if (sortKey) {
                tasks = tasks.slice().sort(function (a, b) {
                    a = a[sortKey];
                    b = b[sortKey];
                    return (a === b ? 0 : a > b ? 1 : -1) * order;
                });
            }
            return tasks;
        }
    },
    filters: {
        capitalize: function (str) {
            return str.charAt(0).toUpperCase() + str.slice(1);
        }
    },
    methods: {
        sortBy: function (key) {
            this.sortKey = key
            this.sortOrders[key] = this.sortOrders[key] * -1
        },
        approveTask: function(event, task){
            var keepThis = this;
            event.target.classList.toggle('loading');
            $.get("/api/tasks/approve?task="+task.taskID)
            .then(function(data){
                task.status = 'IN_PROGRESS';
                event.target.classList.toggle('loading');
            });
        },
        denyTask: function(event, task){
            event.target.classList.toggle('loading');
            $.get("/api/tasks/deny?task="+task.taskID)
            .then(function(data){
                task.status = 'REFUSED';
                event.target.classList.toggle('loading');
            });
        }
    }
});

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
    startDate:"", endDate:"",
    originalEstimate: 0,
    typeID: 0,
    typeName: '',
    assignee: "",
    assigneeID: 0,
    status:"PENDING",
    statusName: '',
    milestoneID: '',
    milestoneName: '',
}

const projectID = $("meta[name='projectID']").attr('value');


// VUEJS MAIN APP
var app = new Vue({

    el: '#tasksList',
    data: {
        searchQuery: '',
        searchQueries: [ { key : 'taskName', value: '' }, { key : 'taskComments', value: '' }, {key : 'startDate', value: '' },
                         { key : 'endDate', value: '' }, { key : 'originalEstimate', value: { min: '', max: '' } },
                         { key : 'assignee', value: '' }, { key : 'status', value: '' }, { key : 'milestoneID', value: '' }, { key : 'typeID', value: '' } ],
        gridColumns: ['taskName', 'taskComments', 'startDate', 'endDate', 'originalEstimate', 'assignee', 'status', 'milestoneID', 'typeID'],
        gridData: [],
        newTask: Object.assign({}, emptyTask),
        formError: "",
        modalTitle: "Create task",
        sync:{
            jira: {
                username:"",
                password:"",
                url:"https://...",
                project: "JIRA project name"
            }
        }
    },
    methods: {
        importFromJIRA: function(){
            var self = this;
            $.ajax({
                type: "POST",
                dataType: "json",
                data: this.sync.jira,
                url: "projects/" + projectID + "/tasks/sync/jira",
                success: function (d) {
                    $('.ui.modal.import.jira').modal('hide');
                    window.location.reload();
                }
            });
        },
        showGraphModal: function(projectID, task, event){
            $('.graph.modal').modal({ detachable : true, centered: true }).modal('show');
            $.ajax({
                method: "GET",
                url: "/api/tasks/chart?task="+task.taskID,
                success : function(data, textStatus, jqXHR) {

                    var listOfTaskDates = data.listOfTaskDates;
                    var effortSpentDataForChart = data.effortSpentData;
                    var realEffortDataForChart = data.realEffortData;

                    //chart config
                    var chart = new Chart($("#lineChart"), {
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
                 this.newTask.taskID = task.taskID

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
    }
});

//Initialization
$(document).ready(function(){
    //initial data loading
    $.get("/api/tasks?project="+currentProjectID)
    .then(function(data){
        app.gridData = data;
        $('.ui.dimmer').removeClass('active');
    });

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