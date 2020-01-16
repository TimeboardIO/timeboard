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

let app = new Vue({
    el: '#vacationApp',
    data: {
        formError : '',
        vacationRequest: {
            start : "",
            end : "",
            halfStart : false,
            halfEnd : false,
            status : "",
            label : "",
            assigneeName : "",
            assigneeID : 0,
            applicantName : "",
            applicantID : 0,
        },
        myRequests: {
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
            data: [],
            name: 'tableVacation',
            configurable : true
        },
        toValidateRequests: {
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
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            data: [],
            name: 'tableVacationValidation',
            configurable : true
        }
    },
    methods:  {
        openModal: function() {
            $('#newVacation').modal('show');
        },
        addVacationRequest: function () {
            let validated = $('.ui.form').form(formValidationRules).form('validate form');

            let self = this;
            if(validated) {
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data: self.vacationRequest,
                    url: "/vacation",
                    success: function (d) {
                        // do something
                        self.myRequests.data = d;
                        $('#newVacation').modal('hide');
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        $('.ui.error.message').text(jqXHR.responseText);
                        $('.ui.error.message').show();
                    }
                });
            }
        },
        approveRequest : function(request) {
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: self.vacationRequest,
                url: "/vacation/approve/"+request.id,
                success: function (d) {
                    self.toValidateRequests.data = d;
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    self.formError = jqXHR.responseText;
                    $('.ui.message').show();
                }
            });
        },
        rejectRequest : function(request) {
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: self.vacationRequest,
                url: "/vacation/reject/"+request.id,
                success: function (d) {
                    self.toValidateRequests.data = d;
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    self.formError = jqXHR.responseText;
                    $('.ui.message').show();
                }
            });
        },
        cancelRequest : function(request) {
            let self = this;
            this.$refs.confirmModal.confirm("Are you sure you want to delete vacation request "
                + request.label !== 'null' ? request.label : '' + "? This action is definitive.",
                function() {
                    $.ajax({
                        type: "DELETE",
                        dataType: "json",
                        data: self.vacationRequest,
                        url: "/vacation/"+request.id,
                        success: function (d) {
                            self.myRequests.data = d;
                        },
                        error: function(jqXHR, textStatus, errorThrown) {
                            self.formError = jqXHR.responseText;
                            $('.ui.message').show();
                        }
                    });
                });

        },
        dayCount : function(request) {
            let t1 = new Date(request.start).getTime();
            let t2 = new Date(request.end).getTime();

            let intResult = parseInt((t2-t1)/(24*3600*1000));
            let result = intResult + 1.0;
            if (request.halfStart) result = result - 0.5;
            if (request.halfEnd) result = result - 0.5;

            return result;
        }
    },
    mounted: function () {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "vacation/list",
            success: function (d) {
                self.myRequests.data = d;
            }
        });
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "vacation/toValidate/list",
            success: function (d) {
                self.toValidateRequests.data = d;
            }
        });
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
                url: '/api/search/myManagers?q={query}&orgID='+$("meta[name=orgID]").attr("content")
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






