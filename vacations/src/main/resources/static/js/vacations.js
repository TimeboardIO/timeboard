const currentActorID = $("meta[property='vacations']").attr('actorID');

const BaseCalendar = Vue.options.components["calendar"];

const CustomCalendar = BaseCalendar.extend({
    methods : {
        selectColor: function(event) {
            let color = "";
            if (event.value > 0) {
                color = "#FBBD08";
            }
            if (event.value >= 1) {
                color = "#5BCA7E";
            }
            return color;
        }
    }
});

Vue.component("calendar", CustomCalendar);


// Form validations rules
const formValidationRules = {
    fields: {
        endDate: {
            identifier: 'end-date',
            rules: [ { type   : 'empty', prompt : 'Please enter a start date'  } ]
        },
        startDate: {
            identifier: 'start-date',
            rules: [
                { type: "empty", prompt : 'Please enter an end date'  } ]
        }
        ,
        assignee: {
            identifier: 'assignee',
            rules: [
                { type: "empty", prompt : 'Please enter user to notify'  } ]
        }
    }
};

const emptyVacationRequest = {
    recursive : false,
    start : "",
    end : "",
    halfStart : false,
    halfEnd : false,
    recurrenceDay : 1,
    recurrenceType : "FULL",
    status : "",
    label : "",
    assigneeName : "",
    assigneeID : 0,
    applicantName : "",
    applicantID : 0,
};

let app = new Vue({
    el: '#vacationApp',
    data: {
        formError : '',
        vacationRequest: Object.assign({}, emptyVacationRequest),
        calendarData : [],
        calendarYear : 2020,
        activeTab: 'Requests',
        myRequestsListConfig: {
            cols: [
                {
                    "slot": "label",
                    "label": "Label",
                    "sortKey": "label",
                    "primary" : false

                },

                {
                    "slot": "start",
                    "label": "From",
                    "sortKey": "start",
                    "primary" : true

                },
                {
                    "slot": "end",
                    "label": "To",
                    "sortKey": "start",
                    "primary" : true
                },
                {
                    "slot": "assignee",
                    "label": "Validation",
                    "sortKey": "assignee",
                    "primary" : false
                },
                {
                    "slot": "recursive",
                    "label": "R",
                    "sortKey": "recurrenceDay",
                    "primary" : true

                },
                {
                    "slot": "status",
                    "label": "Status",
                    "sortKey": "status",
                    "primary" : true
                },
                {
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            name: 'tableVacation',
            configurable : true
        },
        myRequestsListData: [],
        toValidateRequestsListConfig: {
            cols: [
                {
                    "slot": "label",
                    "label": "Label",
                    "sortKey": "label",
                    "primary" : false

                },
                {
                    "slot": "start",
                    "label": "From",
                    "sortKey": "start",
                    "primary" : true

                },
                {
                    "slot": "end",
                    "label": "To",
                    "sortKey": "start",
                    "primary" : true
                },
                {
                    "slot": "applicant",
                    "label": "Applicant",
                    "sortKey": "applicantName",
                    "primary" : true
                },
                {
                    "slot": "recursive",
                    "label": "R",
                    "sortKey": "recurrenceDay",
                    "primary" : true

                },
                {
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            name: 'tableVacationValidation',
            configurable : true
        },
        toValidateRequestsListData: []
    },
    methods:  {
        activateTab: function(tabName) {
            this.activeTab = tabName;
        },
        openModal: function() {
            this.vacationRequest = Object.assign({}, emptyVacationRequest);
            this.vacationRequest.recursive = false;
            $('#newVacation').modal('show');

        },
        openRecursiveModal: function() {
            this.vacationRequest = Object.assign({}, emptyVacationRequest);
            this.vacationRequest.recursive = true;
            $('#newRecursiveVacation').modal('show');
        },
        addVacationRequest: function () {
            let self = this;
            let validated = $('.ui.form').form(formValidationRules).form('validate form');
            let assignedToMyself = this.vacationRequest.assigneeID == currentActorID;

            if(validated) {
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data: self.vacationRequest,
                    url: "/vacation",
                    success: function (d) {
                        $('#newVacation').modal('hide');
                        $('#newRecursiveVacation').modal('hide');
                        // do something
                        self.myRequestsListData = d;
                        self.loadCalendar();
                        if(assignedToMyself){
                            self.listToValidateRequests();
                        }
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        $('.ui.error.message').text(jqXHR.responseText);
                        $('.ui.error.message').show();
                    }
                });
            }
        },
        approveRequest : function(request) {
            let assignedToMyself = request.assigneeID == currentActorID;
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: self.vacationRequest,
                url: "/vacation/approve/"+request.id,
                success: function (d) {
                    self.toValidateRequestsListData = d;
                    if(assignedToMyself){
                        self.listMyRequests();
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    self.formError = jqXHR.responseText;
                    $('.ui.message').show();
                }
            });
        },
        rejectRequest : function(request) {
            let assignedToMyself = request.assigneeID == currentActorID;
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: self.vacationRequest,
                url: "/vacation/reject/"+request.id,
                success: function (d) {
                    self.toValidateRequestsListData = d;
                    if(assignedToMyself){
                        self.listMyRequests();
                    }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    self.formError = jqXHR.responseText;
                    $('.ui.message').show();
                }
            });
        },
        cancelRequest : function(request) {
            let assignedToMyself = request.assigneeID == currentActorID;
            let self = this;
            let text =  "Are you sure you want to delete vacation request "
            + request.label !== 'null' ? request.label : '' + "? This action is definitive.";
            if(request.recursive && request.status === "ACCEPTED" && new Date(request.start).getTime() < new Date().getTime()) {
                text = "This recursive vacation request has started."
                +" Canceling it will set the recurrence end date to now and will delete future occurrences. Do you want to continue??"
            }
            this.$refs.confirmModal.confirm(text,
                function() {
                    $.ajax({
                        type: "DELETE",
                        dataType: "json",
                        data: self.vacationRequest,
                        url: "/vacation/"+request.id,
                        success: function (d) {
                            self.myRequestsListData = d;
                            self.loadCalendar();
                            if(assignedToMyself){
                                self.listToValidateRequests();
                            }
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            self.formError = jqXHR.responseText;
                            $('.ui.message').show();
                        }
                    });
                });

        },
        listToValidateRequests: function() {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "vacation/toValidate/list",
                success: function (d) {
                    self.toValidateRequestsListData = d;
                }
            });
        },
        listMyRequests: function() {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "vacation/list",
                success: function (d) {
                    self.myRequestsListData = d;
                }
            });
            self.loadCalendar();
        },
        loadCalendar: function() {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "vacation/calendar/2020",
                success: function (d) {
                    self.calendarData = d;
                }
            });
        }
    },
    mounted: function () {
        let self = this;
        self.listMyRequests();
        self.listToValidateRequests();
        self.loadCalendar();
    }
});

$(document).ready(function(){

    $('.message .close')
      .on('click', function() {
        $(this)
          .closest('.message')
          .transition('fade')
        ;
      })
    ;

    $('.ui.radio.checkbox')
        .checkbox()
    ;


    $('.ui.search')
        .search({
            apiSettings: {
                url: '/api/search/byRole?role=OWNER&q={query}&orgID='+$("meta[name=orgID]").attr("content")
            },
            fields: {
                results : 'items',
                title   : 'screenName',
                description :'email'
            },
            onSelect: function(result, response) {
                app.vacationRequest.assigneeID = result.id;
                app.vacationRequest.assigneeName = result.screenName;

            },
            minCharacters : 3
        });

});






