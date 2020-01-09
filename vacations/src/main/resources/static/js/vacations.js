

let app = new Vue({
    el: '#vacationApp',
    data: {
        vacationRequest: {
            start : "",
            end : "",
            halfStart : false,
            halfEnd : false,
            label : "",
            assigneeName : "",
            assigneeID : 0,
        }
    },
    methods:  {
        openModal: function(){
            $('#newVacation').modal('show');
        },
        addVacationRequest: function () {
            let self = this;
            $.ajax({
                type: "POST",
                dataType: "json",
                data: self.vacationRequest,
                url: "/vacation",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
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






