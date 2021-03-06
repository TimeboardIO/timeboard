/**
 * type = imputation | effortLeft
 */

const properties = $("meta[property='timesheet']");
const _USER_ID = properties.attr('userID');
const _ACTOR_ID = properties.attr('actorID');
const _YEAR = properties.attr('year');
const _WEEK = properties.attr('week');
const _LOCALE = properties.attr('locale');

const ValidationStatus = {
    PENDING_VALIDATION: "PENDING_VALIDATION",
    VALIDATED: "VALIDATED",
    REJECTED: "REJECTED"
};

const emptyTask = {
    taskID: 0,
    projectID: 0,
    taskName: "",
    taskComments: "",
    startDate: "", endDate: "",
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

const timesheetModel = {
    windowWidth: window.innerWidth,
    week: 0,
    year: 0,
    sum: 0,
    days: [],
    projects: {},
    imputations: {},
    successMessages: [],
    errorMessages: [],
    newTask: Object.assign({}, emptyTask),
    disableNext: false,
    disablePrev: false,
    formError: "",
    modalTitle: "",
    canValidate: false,
    currentWeekValidationStatus: {},
    previousWeekValidationStatus: {},
    userID: _USER_ID,
    userIsActor: _USER_ID === _ACTOR_ID,
    columns: [
        {
            "slot": "es",
            "visible" : true
        },
        {
            "slot": "esw",
            "visible" : true

        },
        {
            "slot": "re",
            "visible" : true

        },
        {
            "slot": "oe",
            "visible" : true

        }
    ],
    getImputationSum: function (date) {
        let sum = 0;
        if (this.imputations[date]) {
            Object.keys(this.imputations[date])
                .forEach(function (i) {
                    sum += this.imputations[date][i];
                }.bind(this));
        }
        this.sum = sum;
        return sum;
    },
    getTaskESW: function (task) {
        return Object.values(this.imputations).reduce((accumulator, currentValue ) => accumulator + currentValue[task.taskID], 0);
    },
    getImputation: function (date, taskID) {
        return this.imputations[date][taskID];
    },
    rollWeek: function (year, week, x) {
        let day = (1 + (week - 1) * 7); // 1st of January + 7 days for each week
        let date = new Date(year, 0, day);
        date.setDate(date.getDate() + 7 * x); //Add x week(s)
        return date;
    },
    getWeekNumber: function (date) {
        let d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
        let dayNum = d.getUTCDay() || 7;
        d.setUTCDate(d.getUTCDate() + 4 - dayNum);
        let yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
        return Math.ceil((((d - yearStart) / 86400000) + 1) / 7)
    },
    nextWeek: function (year, week) {
        let date = timesheetModel.rollWeek(year, week, 1);
        return timesheetModel.getWeekNumber(date);
    },
    lastWeek: function (year, week) {
        let date = timesheetModel.rollWeek(year, week, -1);
        return timesheetModel.getWeekNumber(date);
    },
    nextWeekYear: function (year, week) {
        let date = timesheetModel.rollWeek(year, week, 1);
        let weekNum = timesheetModel.getWeekNumber(date);
        if (week > weekNum) {
            year++;
        }
        return year;
    },
    lastWeekYear: function (year, week) {
        let date = timesheetModel.rollWeek(year, week, -1);
        let weekNum = timesheetModel.getWeekNumber(date);
        if (week < weekNum) {
            year--;
        }
        return year;
    },
    updateTask: function (date, task, type, val) {
        return $.ajax({
            method: "POST",
            url: "timesheet",
            data: JSON.stringify({
                'type': type,
                'day': date,
                'task': task,
                'imputation': val
            }),
            contentType: "application/json",
        });
    },
    getFirstDayOfWeekDateString: function () {
        let d1 = new Date('' + this.year + '');
        d1.setDate(d1.getDate() - d1.getDay() + 1 + (7 * (this.week - 1)));
        return d1.toLocaleDateString(_LOCALE, { year: 'numeric', month: 'short', day: 'numeric' });
    },
    getLastDayOfWeekDateString: function () {
        let d1 = new Date('' + this.year + '');
        d1.setDate(d1.getDate() - d1.getDay() + 7 + (7 * (this.week - 1)));
        return d1.toLocaleDateString(_LOCALE, { year: 'numeric', month: 'short', day: 'numeric' });
    }
};


$(document).ready(function () {

    const formValidationRules = {
        fields: {
            projectID: {
                identifier: 'projectID',
                rules: [{ type: 'empty', prompt: 'Please select project' }]
            },
            taskName: {
                identifier: 'taskName',
                rules: [{ type: 'empty', prompt: 'Please enter task name' }]
            },
            taskStartDate: {
                identifier: 'taskStartDate',
                rules: [{ type: "empty", prompt: 'Please enter task start date' }]
            },
            taskEndDate: {
                identifier: 'taskEndDate',
                rules: [{ type: "empty", prompt: 'Please enter task end date' }]
            },
            taskOriginalEstimate: {
                identifier: 'taskOriginalEstimate',
                rules: [{ type: 'empty', prompt: 'Please enter task original estimate in days' },
                { type: 'number', prompt: 'Please enter task a number original estimate in days' }]
            },
        }
    };

    Vue.component('task-modal', {
        template: '#task-modal-template',
        props: {
            task: Object,
            formError: String,
            modalTitle: String
        }
    });

    let app = new Vue({
        el: '#timesheet',
        data: timesheetModel,
        methods: {
            enableSubmitButton: function (week) {
                let result = true;

                result &= [ValidationStatus.PENDING_VALIDATION, ValidationStatus.VALIDATED].includes(this.previousWeekValidationStatus);
                //check all days imputations == 1
                app.days.forEach(function (day) {
                    if (day.dayNum !== 1 && day.dayNum !== 7) {
                        let sum = timesheetModel.getImputationSum(day.date);
                        result = result && (sum === 1);
                    }
                });

                return result;
            },
            enableValidationButtons: function (week) {
                return [ValidationStatus.VALIDATED].includes(this.previousWeekValidationStatus);
            },
            updateTimesheet: function () {
                $('.ui.dimmer').addClass('active');
                $.get("/timesheet/data/" + _YEAR + "/" + _WEEK)
                    .then(function (data) {
                        app.canValidate = data.canValidate;
                        app.week = data.week;
                        app.year = data.year;
                        app.days = data.days;
                        app.imputations = data.imputations;
                        app.projects = data.projects;
                        app.disableNext = data.disableNext;
                        app.disablePrev = data.disablePrev;
                        app.currentWeekValidationStatus = data.currentWeekValidationStatus;
                        app.previousWeekValidationStatus = data.previousWeekValidationStatus;
                    }).then(function () {
                        $('.ui.dimmer').removeClass('active');
                        $(' #timesheet').removeClass('hidden');
                    }).then(function () {
                        let list = document.getElementsByClassName("day-badge");
                        for (let i = 0; i < list.length; i++) {
                            let badge = list[i];
                            if (badge.innerText === "1.0") {
                                badge.classList.add("green");
                                badge.classList.remove("red");
                            }
                        }
                    })
            },
            isTimesheetHasState: function (statesArray) {
                return statesArray.includes(this.currentWeekValidationStatus);
            },
            displayErrorMessage: function (message) {
                this.errorMessages.push({ message: message, visible: true });
            },
            displaySuccessMessage: function (message) {
                this.successMessages.push({ message: message, visible: true });
            },
            submitMyWeek: function (event) {
                $.ajax({
                    method: "GET",
                    url: "timesheet/submit/" + app.year + "/" + app.week,
                    success: function (weekValidationStatus, textStatus, jqXHR) {
                        app.submitted = true;
                        app.displaySuccessMessage("Your timesheet have been submitted successfully.");
                        app.updateTimesheet();
                        app.currentWeekValidationStatus = ValidationStatus.PENDING_VALIDATION;

                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        app.displayErrorMessage("Error can not submit your timesheet. " + textStatus);
                    }
                });
            },

            validateWeek: function (event) {
                $.ajax({
                    method: "POST",
                    url: "timesheet/validate/" + _USER_ID + "/" + app.year + "/" + app.week,
                    success: function (weekValidationStatus, textStatus, jqXHR) {
                        app.submitted = true;
                        app.displaySuccessMessage("This timesheet have been validated successfully.");
                        app.updateTimesheet();
                        app.currentWeekValidationStatus = ValidationStatus.VALIDATED;

                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        app.displayErrorMessage("Error can not validate this timesheet. " + textStatus);
                    }
                });
            },
            rejectWeek: function (event) {
                $.ajax({
                    method: "POST",
                    url: "timesheet/reject/" + _USER_ID + "/" + app.year + "/" + app.week,
                    success: function (weekValidationStatus, textStatus, jqXHR) {
                        app.submitted = false;
                        app.displaySuccessMessage("This timesheet have been rejected successfully.");
                        app.updateTimesheet();
                        app.currentWeekValidationStatus = ValidationStatus.REJECTED;

                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        app.displayErrorMessage("Error can not validate this timesheet. " + textStatus);
                    }
                });
            },
            sendReminder: function (event) {
                event.stopPropagation();
                let self = this;
                $.ajax({
                    type: "POST",
                    contentType: "application/json",
                    url: "timesheet/sendReminderMail/" + _USER_ID,
                    success: function (data, textStatus, jqXHR) {
                        event.target.classList.add('disabled');
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        event.target.classList.add('red');
                    }
                });
            },
            triggerUpdateEffortLeft: function (event) {
                $(event.target).parent().addClass('left icon loading').removeClass('error');
                const taskID = $(event.target).attr('effortLeft');
                const val = $(event.target).val();
                const self = this;
                this.updateTask(null, taskID, 'effortLeft', val)
                    .done(function (updateTask) {
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].realEffort = updateTask.realEffort;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].originalEstimate = updateTask.originalEstimate;
                        app.projects[updateTask.projectID].tasks[updateTask.taskID].effortLeft = updateTask.effortLeft;
                        $(event.target).parent().removeClass('left icon loading');
                    })
                    .fail(function (errorMessage) {
                        self.displayErrorMessage(errorMessage.responseText);
                        $(event.target).parent().removeClass('left icon loading');
                    });
            },
            triggerUpdateTask: function (event) {

                const date = $(event.target).attr('date');
                const taskID = $(event.target).attr('task');
                const currentSum = app.getImputationSum(date);
                let newVal = parseFloat($(event.target).val());
                let oldVal = app.imputations[date][taskID];
                if (newVal !== oldVal) {

                    if (newVal > 1) {
                        newVal = 1;
                    }
                    if (newVal < 0) {
                        newVal = 0;
                    }
                    if (currentSum + (newVal - oldVal) <= 1.0) {
                        $(event.target).val(newVal);
                        const self = this;
                        $(event.target).parent().addClass('left icon loading').removeClass('error');
                        this.updateTask(date, taskID, 'imputation', newVal)
                            .done(function (updateTask) {
                                app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                                app.projects[updateTask.projectID].tasks[updateTask.taskID].realEffort = updateTask.realEffort;
                                app.projects[updateTask.projectID].tasks[updateTask.taskID].originalEstimate = updateTask.originalEstimate;
                                app.projects[updateTask.projectID].tasks[updateTask.taskID].effortLeft = updateTask.effortLeft;
                                app.imputations[date][taskID] = newVal;
                                $(event.target).parent().removeClass('left icon loading');
                            })
                            .fail(function (errorMessage) {
                                self.displayErrorMessage(errorMessage.responseText);
                                $(event.target).parent().removeClass('left icon loading');
                            });
                    } else {
                        $(event.target).val(oldVal);
                        $(event.target).parent().removeClass('left icon loading').addClass('error');
                        this.displayErrorMessage("you cannot charge more than one day a day.");


                        if (newval > 1) {
                            newval = 1;
                        }
                        if (newval < 0) {
                            newval = 0;
                        }
                        if (currentSum + (newval - oldVal) <= 1.0) {
                            $(event.target).val(newval);
                            this.updateTask(date, taskID, 'imputation', newval)
                                .then(function (updateTask) {
                                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortSpent = updateTask.effortSpent;
                                    app.projects[updateTask.projectID].tasks[updateTask.taskID].realEffort = updateTask.realEffort;
                                    app.projects[updateTask.projectID].tasks[updateTask.taskID].originalEstimate = updateTask.originalEstimate;
                                    app.projects[updateTask.projectID].tasks[updateTask.taskID].effortLeft = updateTask.effortLeft;
                                    app.imputations[date][taskID] = newval;
                                    $(event.target).parent().removeClass('left icon loading');
                                });
                        } else {
                            $(event.target).val(oldVal);
                            $(event.target).parent().removeClass('left icon loading').addClass('error');
                            this.displayErrorMessage("you cannot charge more than one day a day.");

                        }
                    }
                }
            },
            showConfigModal: function() {
                $( '#timesheetConfigModal').modal({
                    onApprove : function($element) {

                    },
                    detachable : true, centered: true
                }).modal('show');
            },
            changeDataTableConfig: function(event) {
                let self = this;
                event.target.classList.toggle('loading');

                let cols = [];
                this.columns.forEach(function (col) {
                    if(col.visible) cols.push(col.slot);
                });
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    contentType: "application/json",
                    data: JSON.stringify({
                        colNames : cols,
                        tableID : "timesheetConfigModal",
                        userID : 0
                    }),
                    url: "/api/datatable",
                    success: function (d) {
                        event.target.classList.toggle('loading');
                        $('#timesheetConfigModal').modal('hide');
                    }
                });
            },
            showCreateTaskModal: function (projectID, task, event) {
                event.preventDefault();
                if (task) {
                    this.newTask.projectID = projectID;
                    this.newTask.taskID = task.taskID;
                    this.newTask.taskName = task.taskName;
                    this.newTask.taskComments = task.taskComments;
                    this.newTask.startDate = task.startDate;
                    this.newTask.endDate = task.endDate;
                    this.newTask.originalEstimate = task.originalEstimate;
                    this.newTask.typeID = task.typeID;
                    this.newTask.assigneeID = task.assigneeID;
                    this.modalTitle = "Edit task";
                } else {
                    this.modalTitle = "Create task";
                    Object.assign(this.newTask, emptyTask);
                    this.newTask.assigneeID = _USER_ID;
                }
                let self = this;
                $('.create-task.modal').modal({
                    onApprove: function ($element) {
                        let formIsOK = $('.create-task .ui.form').form(formValidationRules).form('validate form');
                        if (formIsOK) {
                            $('.ui.error.message').hide();
                            $.ajax({
                                method: "POST",
                                url: "/api/tasks",
                                data: JSON.stringify(app.newTask),
                                contentType: "application/json",
                                dataType: "json",
                                success: function (data, textStatus, jqXHR) {
                                    self.updateTimesheet();
                                    $('.create-task .ui.form').form('reset');
                                    $('.create-task.modal').modal('hide');
                                },
                                error: function (jqXHR, textStatus, errorThrown) {
                                    $('.ui.error.message').text(jqXHR.responseText);
                                    $('.ui.error.message').show();
                                }
                            });
                        }
                        return false;
                    },
                    detachable: true, centered: true
                }).modal('show');
            },
            cancelTask: function (event, task) {
                event.preventDefault();
                event.stopPropagation();
                $.ajax({
                    method: "DELETE",
                    url: "timesheet/cancelTask/" + task.taskID,
                    success: function (weekValidationStatus, textStatus, jqXHR) {
                        app.displaySuccessMessage("Task successfully canceled. ");
                        app.updateTimesheet();

                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        app.displayErrorMessage("Error can not cancel task : " + textStatus);
                    }
                });
            },
            getWindowWidth(event) {
                this.windowWidth = document.documentElement.clientWidth;
            },
        },
        computed : {
            activeColumnsCount : function () {
                return this.columns.filter(col => col.visible).length + 1 ; //1 is for el (can not be disabled)
            }
        },
        mounted: function () {
            window.addEventListener('resize', this.getWindowWidth);
            this.updateTimesheet();
            let self = this;
            // columns config loading
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "/api/datatable?tableID=timesheetConfigModal",
                success: function (d) {
                    if (d.initialized) {
                        self.columns
                            .forEach(function (c) {
                                c.visible = d.colNames.includes(c.slot);
                            });
                    }
                }
            });
        }
    });

});
