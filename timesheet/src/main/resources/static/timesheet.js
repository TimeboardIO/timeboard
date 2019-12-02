/**
* type = imputation | effortLeft
*/

const updateTask = function(date, task, type, val){

    return $.post("/timesheet", {
        'type':type,
        'day':date,
        'task':task,
        'imputation':val
    });
}

const timesheetModel = {
    newTask:  {taskID:0, projectID:0, taskName:"", taskComments:"", startDate:"", endDate:"", originalEstimate:0, typeID:0 },
    formError:"",
    week:0,
    year:0,
    sum:0,
    validated:false,
    days:[],
    projects: {},
    imputations: {},
    getImputationSum: function(date){
        var sum = 0;
        if(this.imputations[date]){
            Object.keys(this.imputations[date])
            .forEach(function(i){
                sum += this.imputations[date][i];
            }.bind(this));
        }
         this.sum = sum;
        return sum;
    },
    getImputation: function(date, taskID){
        return this.imputations[date][taskID];
    },
    enableValidateButton: function(week){
        var result = true;

        //check last week
        const lastWeekValidated = $("meta[property='timesheet']").attr('lastWeekValidated');
        result = result && (lastWeekValidated == 'true');

        //check all days imputations == 1
        this.days.forEach(function(day) {
            if(day.day != 'Sunday' && day.day != 'Saturday'){
                var sum = timesheetModel.getImputationSum(day.date);
                result = result && (sum == 1);
             }
        });

        return result;
    },
    rollWeek: function(year, week, x){
        var day = (1 + (week - 1) * 7); // 1st of January + 7 days for each week
        var date = new Date(year, 0, day);
        date.setDate(date.getDate() + 7 * x); //Add x week(s)
        return date;
    },
    getWeekNumber: function(date){
        var d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
        var dayNum = d.getUTCDay() || 7;
        d.setUTCDate(d.getUTCDate() + 4 - dayNum);
        var yearStart = new Date(Date.UTC(d.getUTCFullYear(),0,1));
        return Math.ceil((((d - yearStart) / 86400000) + 1)/7)
    },
    nextWeek: function(year, week){
        var date = timesheetModel.rollWeek(year, week, 1);
        return timesheetModel.getWeekNumber(date);
    },
    lastWeek: function(year, week){
        var date = timesheetModel.rollWeek(year, week, -1);
        return timesheetModel.getWeekNumber(date);
    },
    nextWeekYear: function(year, week){
        var date = timesheetModel.rollWeek(year, week, 1);
        var weekNum = timesheetModel.getWeekNumber(date);
        if(week > weekNum){ year ++; }
        return year;
    },
    lastWeekYear: function(year, week){
        var date = timesheetModel.rollWeek(year, week, -1);
        var weekNum = timesheetModel.getWeekNumber(date);
        if(week < weekNum) { year --; }
        return year;
    }
}

$(document).ready(function(){

    const week = $("meta[property='timesheet']").attr('week');
    const year = $("meta[property='timesheet']").attr('year');
    const lastWeekValidated = $("meta[property='timesheet']").attr('lastWeekValidated');

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



    var app = new Vue({
      el: '#timesheet',
      data: timesheetModel,
      methods: {
        validateMyWeek: function(event){
            $.post('/timesheet/validate', {
                'week': app.week,
                'year': app.year
            }).then(function(){
                app.validated=true;
            });
        },
        triggerUpdateEffortLeft: function(event){

            $(event.target).parent().addClass('left icon loading').removeClass('error');
            const taskID = $(event.target).attr('data-task-effortLeft');
            const val = $(event.target).val();
            updateTask(null, taskID, 'effortLeft', val)
            .then(function(updateTask){
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].realEffort = updateTask.realEffort;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].originalEstimate = updateTask.originalEstimate;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortLeft = updateTask.effortLeft;
                    $(event.target).parent().removeClass('left icon loading');
                });
        },
        triggerUpdateTask: function (event) {

           $(event.target).parent().addClass('left icon loading').removeClass('error');
            const date = $(event.target).attr('data-date');
            const taskID = $(event.target).attr('data-task');

            const currentSum = app.getImputationSum(date);
            var newval = parseFloat($(event.target).val());
            var oldVal = app.imputations[date][taskID];

            if(newval > 1){
                newval = 1;
            }
            if(newval < 0){
                newval = 0;
            }
            if(currentSum + (newval - oldVal) <= 1.0){
                $(event.target).val(newval);
                updateTask(date, taskID, 'imputation', newval)
                .then(function(updateTask){
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].realEffort = updateTask.realEffort;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].originalEstimate = updateTask.originalEstimate;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].effortLeft = updateTask.effortLeft;
                        app.imputations[date][taskID] = newval;
                        $(event.target).parent().removeClass('left icon loading');
                    });
            }else{
                $(event.target).val(oldVal);
                $(event.target).parent().removeClass('left icon loading').addClass('error');
            }
        },
        showCreateTaskModal: function(projectID, task, event){
        event.preventDefault()
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
            var keepThis = this;
            $('.create-task.modal').modal({
                onApprove : function($element){
                    var validated = $('.create-task .ui.form').form(formValidationRules).form('validate form');
                    if(validated){
                    $.post('/timesheet/api/task', app.object)
                    .then(function (response) {
                        // Success
                        console.log(response.data)
                    },function (response) {
                        // Error
                        console.log(response.data)
                    });
                        /*$.ajax({
                            method: "POST",
                            url: "/timesheet/api/task",
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

                          });*/
                    }
                    return false;
                },
                detachable : false
            }).modal('show');
        }
      }
    })

    var updateTimesheet = function(){
        $('.ui.dimmer').addClass('active');
        $.get("/timesheet/api?week="+week+"&year="+year)
        .then(function(data){
            app.week = data.week;
            app.year = data.year;
            app.validated = data.validated;
            app.days = data.days;
            app.imputations = data.imputations;
            app.projects = data.projects;

        })
        .then(function(){
            $('.ui.dimmer').removeClass('active');
        }).then(function(){
             var list = document.getElementsByClassName("day-badge");
             for (var i = 0; i < list.length; i++ ){
                var badge = list[i];
                 if(badge.innerText == "1.0"){
                      badge.classList.add("green");
                      badge.classList.remove("red");
                 }
            }
        });
    }
    updateTimesheet();

});






