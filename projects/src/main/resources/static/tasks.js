Vue.component('demo-grid', {
   template: '#grid-template',
   props: {
     tasks: Array,
     columns: Array,
     filterKey: Array,
   },
   data: function () {
     var sortOrders = {}
     this.columns.forEach(function (key) {
       sortOrders[key] = 1
     })
     return {
       sortKey: '',
       sortOrders: sortOrders
     }
   },
   computed: {
     filteredTasks: function () {
       var sortKey = this.sortKey
       var filterKey = this.filterKey.filter(function (f) { return f.value != '' });
       var order = this.sortOrders[sortKey] || 1
       var tasks = this.tasks
       var keepThis = this;
       if (filterKey.length > 0) {
         tasks = tasks.filter(function (row) {
            return filterKey.some(function(f, i, array){
               return String(row[f.key]).toLowerCase().indexOf(String(f.value).toLowerCase()) > -1
            });
         })
       }
       if (sortKey) {
         tasks = tasks.slice().sort(function (a, b) {
           a = a[sortKey]
           b = b[sortKey]
           return (a === b ? 0 : a > b ? 1 : -1) * order
         })
       }
       return tasks
     }
   },
   filters: {
     capitalize: function (str) {
       return str.charAt(0).toUpperCase() + str.slice(1)
     }
   },
   methods: {
     sortBy: function (key) {
       this.sortKey = key
       this.sortOrders[key] = this.sortOrders[key] * -1
     }
   }
 })

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
          },
          taskTypeID: {
            identifier: 'taskTypeID',
            rules: [ { type   : 'empty', prompt : 'Please enter task type '  } ]
          }
        }
    };

 // bootstrap the demo
var app = new Vue({

    el: '#tasksList',
    data: {
        searchQuery: '',
        searchQueries: [{key : 'taskName', value: ''}, {key : 'taskComment', value: ''}, {key : 'startDate', value: ''},
            {key : 'endDate', value: ''}, {key : 'oE', value: ''}, {key : 'assignee', value: ''}, {key : 'status', value: ''}],
        gridColumns: ['taskName', 'taskComment', 'startDate', 'endDate', 'oE', 'assignee', 'status'],
        gridData: [ ],
        newTask:  {taskID:0, projectID:0, taskName:"", taskComments:"", startDate:"", endDate:"", originalEstimate:0, typeID:0 },
        formError:"",
    },
    methods: {
        showCreateTaskModal: function(projectID, task, event){
            event.preventDefault();
            if(task){
                 this.newTask.projectID = projectID;
                 this.newTask.taskID = task.taskID
                 this.newTask.taskName = task.taskName;
                 this.newTask.taskComments = task.taskComments;
                 this.newTask.startDate = task.startDate;
                 this.newTask.endDate = task.endDate;
                 this.newTask.originalEstimate = task.originalEstimate;
                 this.newTask.typeID = task.typeID;
            }else{
                 this.newTask =  {taskID:0, projectID:0, taskName:"", taskComments:"", startDate:"", endDate:"", originalEstimate:0, typeID:0 };
            }
            $('.create-task.modal').modal({
                onApprove : function($element){
                    var validated = $('.create-task .ui.form').form(formValidationRules).form('validate form');
                    if(validated){
                        $.ajax({
                            method: "GET",
                            url: "/timesheet/api/create_task",
                            data: app.newTask,
                          }).then(function(data) {
                              if(data == "DONE"){
                                 updateTimesheet();
                                 $('.create-task .ui.form').form('reset');
                                 $('.create-task.modal').modal('hide');
                              }else{
                                $('.ui.error.message').text(data);
                                $('.ui.error.message').show();
                              }

                          });
                    }
                    return false;
                },
                detachable : false, centered: true
            }).modal('show');
        }
    }
});

$(document).ready(function(){
    const project = $("meta[property='tasks']").attr('project');


    $.get("/projects/tasks/api?action=getTasks&project="+project)
    .then(function(data){
         app.gridData = data;
    });
});