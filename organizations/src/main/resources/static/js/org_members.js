const currentOrgID = $("meta[property='organization']").attr('orgID');
const baseURL = $("meta[property='organization']").attr('baseURL');

var app = new Vue({

    el: '#members',
    data: {
        members: [],
    },
    methods: {
        removeMember: function(e, member){
            $.get("/api/org/members/remove?orgID="+currentOrgID+"&memberID="+member.id)
            .done(function(data){
                delete app.members[app.members.indexOf(member)];
            });
        },
        addMember: function(memberID){
            $.get("/api/org/members/add?orgID="+currentOrgID+"&memberID="+memberID)
            .done(function(data){
                app.members.push(new MemberWrapper(data));
            });
        },
        updateRole: function(e, member){
            $.get("/api/org/members/updateRole?orgID="+currentOrgID+"&memberID="+member.id+"&role="+member.role)
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
        $.get("/api/org/members?orgID="+currentOrgID)
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
            url: '/search?q={query}'
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