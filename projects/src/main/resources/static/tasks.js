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
     },
     showCreateTaskModal: function(projectID, task, event){
        this.$parent.showCreateTaskModal(projectID, task, event);
     }
   }
 });

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
        filterKey.push({key: 'status', value: 'PENDING'});
        var order = this.sortOrders[sortKey] || 1
        var tasks = this.tasks
        var keepThis = this;
        if (filterKey.length > 0) {
          tasks = tasks.filter(function (row) {
             return filterKey.every(function(f, i, array){
                return (String(row[f.key]).toLowerCase().indexOf(String(f.value).toLowerCase()) > -1)
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
  });


Vue.component('task-modal', {
   template: '#task-modal-template',
   props: {
     task: Object,
     formError: String,
     modalTitle: String
   }
 });



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

 const emptyTask =  {taskID:0, projectID:0, taskName:"", taskComments:"", startDate:"", endDate:"", originalEstimate:0, typeID:0 }


var app = new Vue({

    el: '#tasksList',
    data: {
        searchQuery: '',
        searchQueries: [{key : 'taskName', value: ''}, {key : 'taskComments', value: ''}, {key : 'startDate', value: ''},
            {key : 'endDate', value: ''}, {key : 'oE', value: ''}, {key : 'assignee', value: ''}, {key : 'status', value: ''}],
        gridColumns: ['taskName', 'taskComments', 'startDate', 'endDate', 'oE', 'assignee', 'status'],
        gridData: [ ],
        newTask: Object.assign({}, emptyTask),
        formError:"",
        modalTitle:""
    },
    methods: {
        showCreateTaskModal: function(projectID, task, event){
            event.preventDefault();
            if(task){
                 this.modalTitle = "Edit task";
                 this.newTask.projectID = projectID;
                 this.newTask.taskID = task.taskID
                 this.newTask.taskName = task.taskName;
                 this.newTask.taskComments = task.taskComments;
                 this.newTask.startDate = task.startDate;
                 this.newTask.endDate = task.endDate;
                 this.newTask.originalEstimate = task.oE;
                 this.newTask.typeID = task.type;
            }else{
                 this.modalTitle = "Create task";
                 Object.assign(this.newTask , emptyTask);
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
                detachable : true, centered: true
            }).modal('show');
        },
        approveTask : function(user, task) {
            let app = this;
            $.get("/projects/tasks/api?action=approveTask&project="+project+"&taskId="+task.taskID)
            .then(function(data){
                if(data == "DONE"){
                    user.tasks = user.tasks.filter(function (item) {
                      return item != task;
                    });
                    if(user.tasks.length == 0){
                        app.userTasks = app.userTasks.filter(function (item) {
                           return item != user;
                        });
                     }
                }
            });
        },
        denyTask : function(user, task) {
            let app = this;
            $.get("/projects/tasks/api?action=denyTask&project="+project+"&taskId="+task.taskID)
            .then(function(data){
                if(data == "DONE"){
                    user.tasks = user.tasks.filter(function (item) {
                      return item != task;
                    });
                    if(user.tasks.length == 0){
                        app.userTasks = app.userTasks.filter(function (item) {
                           return item != user;
                        });
                     }
                }
            });
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