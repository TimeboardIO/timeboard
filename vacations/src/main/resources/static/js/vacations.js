

let app = new Vue({
    el: '#vacationApp',
    data : {
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
    methods :  {
        openModal: function(){
            $('#newVacation').modal('show');
        },
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






