let updateUser = function(membershipID, role){
    let path = '/projects/'+$("meta[name=projectID]").attr("content")+'/setup/memberships/'+membershipID+'/'+role;
    $.ajax({
        url: path,
        type: 'PATCH',
        success: function(result) {
            document.location.reload(true);
        }
    });
};

let removeUser = function(membershipID){
    let path = '/projects/'+$("meta[name=projectID]").attr("content")+'/setup/memberships/'+membershipID;

    $.ajax({
        url: path,
        type: 'DELETE',
        success: function(result) {
            document.location.reload(true);
        }
    });

};

let removeConfig = function(configKey){
    $("[data-key="+configKey+"]").remove();
};


$('.ui.checkbox')
    .checkbox();

$('.message .close')
    .on('click', function() {
        $(this)
            .closest('.message')
            .transition('fade')
        ;
    })
;


$('.delete[data-key]').click(function(){
    console.log("delete");
    $('tr[data-key="'+$(this).attr('data-key')+'"]').remove();
});

// User search
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
            $.post('/projects/'+$("meta[name=projectID]").attr("content")+'/setup/memberships', {
                memberID: result.id
            }).done(function(){
                document.location.reload(true);
            });
        },
        minCharacters : 3
    });