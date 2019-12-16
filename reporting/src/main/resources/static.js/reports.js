$(document).ready(function () {

    const projectID = $("meta[name='reportID']").attr('content');

    var app = new Vue({
        el: '#reports-app',
        data: {
            table: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Report Name"
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
                url: "reports/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
