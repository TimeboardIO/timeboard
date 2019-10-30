
$(document).ready(function(){

    $('.message .close')
      .on('click', function() {
        $(this)
          .closest('.message')
          .transition('fade')
        ;
      })
    ;
/*
    $('.remove[data-key]').click(function(){
        $('tr[data-key="'+$(this).attr('data-key')+'"]').remove();
        $('.ui.dropdown select').append('<option value="'+$(this).attr('data-key')+'">'+$(this).attr('data-key')+'</option>');
    });

     $('tr[data-key]').each(function(){
        $('option:contains(' + $(this).attr('data-key') + ')').remove();
     });

     $('.add').click(function(){

        var type = $('.ui.dropdown').dropdown('get text');
        var value = $('.newAttrValue').val();

        $('.dropdown > select > option[value="' + $('.ui.dropdown').dropdown('get value') + '"]').remove();
        $('.externalTools').prepend(''
            +'<tr data-key="'+type+'" >'
                +'<td>'
                    +type
                +'</td>'
                +'<td>'
                    +'<input type="text" value="'+value+'" />'
                +'</td>'
                +'<td>'
                    +'<div class="ui negative basic button remove"  data-key="'+type+'">'
                        +'<i class="remove alternate icon"></i>Remove'
                    +'</div>'
                +'<td>'
            +'</tr>');

        $('.remove[data-key]').click(function(){
            $('tr[data-key="'+$(this).attr('data-key')+'"]').remove();
            $('.ui.dropdown select').append('<option value="'+$(this).attr('data-key')+'">'+$(this).attr('data-key')+'</option>');
        });

        $('.ui.dropdown').dropdown('clear');
     });



    $('.field .dropdown')
          .dropdown();

    new Vue({
      el: '#externalTools',
      data: {
        selected: '',
        availableTypes: ['Jira' ,  'GitHub' , 'GitLab' ]
      },
      methods: {
        addField : function(){


        }
      }
    })
*/


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






