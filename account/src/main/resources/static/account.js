
$(document).ready(function(){

    $('.message .close')
      .on('click', function() {
        $(this)
          .closest('.message')
          .transition('fade')
        ;
      })
    ;

    $('.ui.form.password')
      .form({
        fields: {
          oldPassword: {
            rules: [
              {
                type   : 'empty',
                prompt : 'You have to type your old password'
              }
            ]
          },
          password1: {
            rules: [
              {
                type   : 'empty',
                prompt : 'You have to specify a new password'
              }
            ]
          },
          password2: {
            rules: [
              {
                type   : 'match[password1]',
                prompt : 'Password are not the same.'

              }
            ]
          },
        }
      });


      $('.ui.form.account')
            .form({
              fields: {
                email: {
                  rules: [
                    {
                      type   : 'email',
                      prompt : 'Email format is not correct'
                    }
                  ]
                },
              }
            });

});






