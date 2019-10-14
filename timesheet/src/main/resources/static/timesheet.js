/**
* type = imputation | rtbd
*/
const updateTask = function(date, task, type, val){

    $(".ui.dimmer").addClass("active");

    return $.post("/timesheet", {
        'type':type,
        'day':date,
        'task':task,
        'imputation':val
    });
}



const timesheetModel = {
    week:0,
    year:0,
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
        return sum;
    },
    getImputation: function(date, taskID){
        return this.imputations[date][taskID];
    },
    getImputationColor: function(date){
        var num = getImputationSum(date);
           if(num == 1) return 'green';
           else return 'red';
     }
}

$(document).ready(function(){

    $('.ui.dimmer').addClass('active');

    const week = $("meta[property='timesheet']").attr('week');
    const year = $("meta[property='timesheet']").attr('year');

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
        triggerUpdateRTBD: function(event){
            const taskID = $(event.target).attr('data-task-rtbd');
            const val = $(event.target).val();
            updateTask(null, taskID, 'rtbd', val)
            .then(function(updateTask){
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].reEstimateWork = updateTask.reEstimateWork;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].estimateWork = updateTask.estimateWork;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].remainToBeDone = updateTask.remainToBeDone;
                    $(".ui.dimmer").removeClass("active");
                });
        },
        triggerUpdateTask: function (event) {
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
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].reEstimateWork = updateTask.reEstimateWork;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].estimateWork = updateTask.estimateWork;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].remainToBeDone = updateTask.remainToBeDone;
                        app.imputations[date][taskID] = newval;
                        $(".ui.dimmer").removeClass("active");
                    });
            }else{
                $(event.target).val(oldVal);
            }
        }
      }
    })


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
    });
});






