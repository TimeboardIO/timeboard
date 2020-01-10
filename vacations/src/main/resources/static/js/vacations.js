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
        vacationRequest: {
            start : "",
            end : "",
            halfStart : false,
            halfEnd : false,
            status : false,
            label : "",
            assigneeName : "",
            assigneeID : 0,
        },
        table: {
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
                    "sortKey": "start",
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
            data: [],
            name: 'tableVacation',
            configurable : true
        }
    },
    methods:  {
        openModal: function(){
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
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        $('.ui.error.message').text(jqXHR.responseText);
                        $('.ui.error.message').show();
                    }
                });
            }
        }
    },
    mounted: function () {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "vacation/list",
            success: function (d) {
                self.table.data = d;
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
                url: '/api/search?q={query}&orgID='+$("meta[name=orgID]").attr("content")
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






