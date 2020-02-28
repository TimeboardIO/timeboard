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
            let self = this;
            this.$refs.confirmModal.confirm("Are you sure you want to remove "+ member.screenName + "?", function() {
                $.ajax({
                    type: "DELETE",
                    url: "/org/members/"+member.id,
                }).then(function(role){
                    let copy = [];
                    for (let i = 0; i < self.members.length; i++) {
                        if(self.members[i].id !== member.id){
                            copy.push(self.members[i]);
                        }
                    }
                    self.members = copy;
                });
            });

        },
        addMember: function(memberID){
            let self = this;
            $.get("/org/members/add?orgID="+currentOrgID+"&memberID="+memberID)
            .then(function(data){
                data.edition = false;
                self.members.push(data);
            });
        },
        updateRole: function(e, member){
            $.ajax({
                type: "patch",
                url: "/org/members/"+member.id,
                data: JSON.stringify(member),
                dataType: "json",
                contentType: 'application/json; charset=utf-8',
                success: function (d) {
                    member.edition = false;
                }
            });
        },
        impersonateMember: function(e, member){
            $.ajax({
                type: "POST",
                url: "/org/impersonate/"+member.id,
            }).then(function(role){
                window.location.href = '../../'; //two level up
            });
        }
    }
});


//Initialization
$(document).ready(function(){
    //initial data loading
        $.get("/org/members/list")
        .then(function(data){
            for (let i = 0; i < data.length; i++) {
                data[i].edition = false;
                app.members.push(data[i]);
            }
            $('.ui.dimmer').removeClass('active');
        });
    //init search fields
    $('.ui.search')
    .search({
        apiSettings: {
            url: '/api/search/all?q={query}'
        },
        fields: {
            results : 'items',
            title   : 'screenName',
            description : 'email'
        },
        onSelect: function(result, response) {
            app.addMember(result.id);
        },
        error : {
            source      : 'Cannot search. No source used, and Semantic API module was not included',
            noResultsHeader : 'No Results',
            noResults   : 'Please enter the exact email of an existing account',
            logging     : 'Error in debug logging, exiting.',
            noTemplate  : 'A valid template name was not specified.',
            serverError : 'There was an issue with querying the server.',
            maxResults  : 'Results must be an array to use maxResults setting',
            method      : 'The method you called is not defined.'
        },
        minCharacters : 3
    });
});