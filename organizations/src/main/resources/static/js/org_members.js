const currentOrgID = $("meta[property='organization']").attr('orgID');

var app = new Vue({

    el: '#members',
    data: {
        members: [],
    },
    methods: {

    }
});

class MemberWrapper {
    constructor() {
        this.orgID = 0;
        this.screenName = screenName;
        this.isOrganization = false;
        this.role = "MEMBER";
    }
}
//Initialization
$(document).ready(function(){
    //initial data loading
        $.get("/api/org/members?orgID="+currentOrgID)
        .then(function(data){
            app.members = data;
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

        },
        minCharacters : 3
    });
});