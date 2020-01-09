

let app = new Vue({
    el: '#vacationApp',
    data: {
        vacationRequest: {
            start : '',
            end : '',
            halfStart : true,
            halfEnd : true,
            label : '',
            assigneeName : '',
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
                data: vacationRequest,
                url: "vacation",
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
});






