const currentOrgID = $("meta[property='organization']").attr('orgID');
const baseURL = $("meta[property='organization']").attr('baseURL');

let app = new Vue({

    el: '#members',
    data: {
        members: [],
    },
    methods: {
        removeMember: function(e, member){
            $.get("/org/members/remove?orgID="+currentOrgID+"&memberID="+member.id)
            .done(function(data){
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
            .done(function(data){
                app.members.push(new MemberWrapper(data));
            });
        },
        updateRole: function(e, member){
            $.get("/org/members/updateRole?orgID="+currentOrgID+"&memberID="+member.id+"&role="+member.role)
            .done(function(data){
                member.role = data.role;
            });
        }
    }
});

class MemberWrapper {
    constructor(data) {
        this.id = data.orgID;
        this.screenName = data.screenName;
        this.isOrganization = data.isOrganization;
        this.role = data.role;
    }
}
//Initialization
$(document).ready(function(){
    //initial data loading
        $.get("/org/members/list?orgID="+currentOrgID)
        .then(function(data){
            for (var item in data) {
                app.members.push(new MemberWrapper(data[item]));
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