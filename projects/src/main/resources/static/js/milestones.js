$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#milestones-app',
        data: {
            table: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Milestone Name"
                    },
                    {
                        "slot": "type",
                        "label": "Milestone Type"
                    },
                    {
                        "slot": "date",
                        "label": "Milestone Date"
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
                url: "projects/" + projectID + "/milestones/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
