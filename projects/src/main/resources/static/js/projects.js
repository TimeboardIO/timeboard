$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#projects-app',
        data: {
            table: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Project Name"
                    },
                    {
                        "slot": "actions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
        },
        mounted: function () {
            var self = this;
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
