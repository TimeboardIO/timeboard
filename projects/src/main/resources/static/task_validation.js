

const taskValidationModel = {
    userTasks: {}
}

$(document).ready(function(){
    const project = $("meta[property='taskValidation']").attr('project');
    var app = new Vue({
        el: '#taskValidation',
        data: taskValidationModel,
        methods: {
             updateModel: function(){
                    $('.ui.dimmer').addClass('active');
                    $.get("/projects/tasks/api?action=getPendingTasks&project="+project)
                    .then(function(data){
                        app.userTasks = data;
                    })
                    .then(function(){
                        $('.ui.dimmer').removeClass('active');
                    });
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
             }
        }
    });
    app.updateModel();
});