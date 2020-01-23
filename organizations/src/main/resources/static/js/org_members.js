const currentOrgID = $("meta[property='organization']").attr('orgID');
const baseURL = $("meta[property='organization']").attr('baseURL');


let app = new Vue({

    el: '#members',
    data: {
        members: [],
    },
    filters:{
        formatDate: function(value) {
            if (value) {
                return new Date(value).toDateString();
            }
        }
    },
    methods: {
        removeMember: function(e, member){
            $.get("/org/members/remove?orgID="+currentOrgID+"&memberID="+member.id)
            .then(function(data){
                let copy = [];
                for (let i = 0; i < app.members.length; i++) {
                    if(app.members[i].id !== member.id){
                        copy.push(app.members[i]);
                    }
                }
                app.members = copy;
            });
        },
        addMember: function(memberID){
            $.get("/org/members/add?orgID="+currentOrgID+"&memberID="+memberID)
            .then(function(data){
                app.members.push(data);
            });
        },
        updateRole: function(e, member){

            $.ajax({
                type: "patch",
                url: "/org/members/"+member.id,
                data: JSON.stringify(member),
                dataType: "json",
                contentType: 'application/json; charset=utf-8'
            }).then(function(role){
                              member.role = role;
                          });
        }
    }
});


//Initialization
$(document).ready(function(){
    //initial data loading
        $.get("/org/members/list?orgID="+currentOrgID)
        .then(function(data){
            for (let i = 0; i < data.length; i++) {
                app.members.push(data[i]);
            }
            $('.ui.dimmer').removeClass('active');
        });
    //init search fields
    $('.ui.search')
    .search({
        apiSettings: {
            url: '/api/search?q={query}'
        },
        fields: {
            results : 'items',
            title   : 'screenName',
            description : 'email'
        },
        onSelect: function(result, response) {
            app.addMember(result.id);
        },
        minCharacters : 3
    });
});