/**
* type = imputation | rtbd
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

    $('.ui.dimmer').addClass('active');

    const week = $("meta[property='timesheet']").attr('week');
    const year = $("meta[property='timesheet']").attr('year');
    const lastWeekValidated = $("meta[property='timesheet']").attr('lastWeekValidated');


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

            $(event.target).parent().addClass('left icon loading').removeClass('error');
            const taskID = $(event.target).attr('data-task-rtbd');
            const val = $(event.target).val();
            updateTask(null, taskID, 'rtbd', val)
            .then(function(updateTask){
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].reEstimateWork = updateTask.reEstimateWork;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].estimateWork = updateTask.estimateWork;
                    app.projects[updateTask.projectID].tasks[updateTask.taskID].remainToBeDone = updateTask.remainToBeDone;
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
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].reEstimateWork = updateTask.reEstimateWork;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].estimateWork = updateTask.estimateWork;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].remainToBeDone = updateTask.remainToBeDone;
                        app.imputations[date][taskID] = newval;
                        $(event.target).parent().removeClass('left icon loading');
                    });
            }else{
                $(event.target).val(oldVal);
                $(event.target).parent().removeClass('left icon loading').addClass('error');
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

});






