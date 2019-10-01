const updateBadge = function(date){
    const reducer = (accumulator, currentValue) => accumulator + currentValue;
    var data = $('[data-date="'+date+'"]').map(function(){return parseFloat($(this).val())}).get();


    var sum = 0;

    if(data.length > 0){
        sum = data.reduce(reducer);
    }

    if(sum === 1.00){
        $('[day-badge="'+date+'"]').addClass('green');
    }else{
        $('[day-badge="'+date+'"]').removeClass('green');
    }

    if(sum === 0.00){
        $('[day-badge="'+date+'"]').addClass('red');
    }else{
        $('[day-badge="'+date+'"]').removeClass('red');
    }

    $('[day-badge="'+date+'"]').text(sum.toFixed(2));
}

/**
* type = imputation | rtbd
*/
const updateTask = function(date, task, type, val){

    $(".ui.dimmer").addClass("active");

    $.post("/timesheet", {
        'type':type,
        'day':date,
        'task':task,
        'imputation':val
    }).then(function(updateTask){
        $('[data-task-rew='+updateTask.taskID+']').text(updateTask.reEstimateWork);
        $('[data-task-es='+updateTask.taskID+']').text(updateTask.effortSpent);
        $('[data-task-ew='+updateTask.taskID+']').text(updateTask.estimateWork);
        $('[data-task-rtbd='+updateTask.taskID+']').val(parseFloat(updateTask.remainToBeDone.replace(',','.')));
        updateBadge(date);
        $(".ui.dimmer").removeClass("active");
    });
}


const timesheetModel = {
    week:0,
    year:0,
    validated:false,
    days:[],
    projects: {},
    imputations: {},
    getImputation: function(date, taskID){
        return this.imputations[date][taskID];
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
        triggerUpdateRTBD: function(event){
            const taskID = $(event.target).attr('data-task-rtbd');
            const val = $(event.target).val();
            updateTask(null, taskID, 'rtbd', val);
        },
        triggerUpdateTask: function (event) {
            const date = $(event.target).attr('data-date');
            const taskID = $(event.target).attr('data-task');
            var val = $(event.target).val();
            if(val > 1){
                val = 1;
                $(event.target).val(val);
            }
            updateTask(date, taskID, 'imputation', val);
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
    }).then(function(){
        $('.ui.dimmer').removeClass('active');
    });
});






