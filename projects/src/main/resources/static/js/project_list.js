$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#projects-app',
        data: {
            table: {
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
                data: [],
                name : "tableProjects",
            }
        },
        methods: {
        },
        mounted: function () {
            let self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
