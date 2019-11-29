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

 const emptyTask =  {
        taskID: 0, projectID: 0, taskName: "", taskComments: "",
        startDate:"", endDate:"",
        originalEstimate: 0, typeID: 0,
        assignee : "", assigneeID:0
}


var app = new Vue({

    el: '#tasksList',
    data: {
        searchQuery: '',
        searchQueries: [{key : 'taskName', value: ''}, {key : 'taskComments', value: ''}, {key : 'startDate', value: ''},
            {key : 'endDate', value: ''}, {key : 'originalEstimate', value: ''}, {key : 'assignee', value: ''}, {key : 'status', value: ''}],
        gridColumns: ['taskName', 'taskComments', 'startDate', 'endDate', 'originalEstimate', 'assignee', 'status'],
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
                 this.newTask.originalEstimate = task.originalEstimate;
                 this.newTask.typeID = task.typeID;
                 this.newTask.assignee = task.assignee;
            }else{
                 this.modalTitle = "Create task";
                 Object.assign(this.newTask , emptyTask);
            }
            $('.create-task.modal').modal({
                onApprove : function($element){
                    var validated = $('.create-task .ui.form').form(formValidationRules).form('validate form');
                    if(validated){
                        $.ajax({
                            method: "POST",
                            url: "/api/tasks",
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
    }
});

$(document).ready(function(){
    const project = $("meta[property='tasks']").attr('project');

    $.get("/api/tasks?project="+project)
    .then(function(data){
         app.gridData = data;
         $('.ui.dimmer').removeClass('active');
    });


     $('select.dropdown') .dropdown() ;

     $('.ui.search')
        .search({
            apiSettings: {
                    url: '/search?q={query}&projectID='+project+''
                },
                fields: {
                    results : 'items',
                    title   : 'screenName',
                    description : 'email'
                },
                onSelect: function(result, response) {
                    $('.assigned').val(result.screenName);
                    $('.taskAssigned').val(result.id);
                 },
            minCharacters : 3
        });

});