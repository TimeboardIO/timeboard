$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#projectsApp',
        data: {
            projectListConfig: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Project Name",
                        "primary" : true,
                        "sortKey" : "name",
                    },
                    {
                        "slot": "actions",
                        "label": "Actions",
                        "primary" : true,
                        "class":"right aligned collapsing"
                    }],
                name : "tableProjects",
            },
            projectListData: []
        },
        methods: {
            archive : function (row){
                this.$refs.confirmModal.confirm("Are you sure you want to archive project "+ row.name + "? This action is irreversible.",
                    function(){
                        document.location.replace('/projects/'+row.id+'/delete');
                    });
            }
        },
        mounted: function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/list",
                success: function (d) {
                    self.projectListData = d;
                    $('.ui.loading').toggle();
                }
            });
        }
    });
});
