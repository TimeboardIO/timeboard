$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#components-demo',
        data: {
            table: {
                cols: [
                    {
                        "slot": "snapshotdate",
                        "label": "Date"
                    },
                    {
                        "slot": "snapshotglobalquotation",
                        "label": "Quotation"
                    },
                    {
                        "slot": "snapshotglobaloriginalestimate",
                        "label": "Original Estimate"
                    },
                    {
                        "slot": "snapshotglobalreeleffot",
                        "label": "Reel Effort"
                    },
                    {
                        "slot": "snapshotglobaleffortspent",
                        "label": "Effort Spent"
                    },
                    {
                        "slot": "snapshotglobaleffortleft",
                        "label": "Effort Left"
                    },
                    {
                        "slot": "snapshotactions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
            addSnapshot: function () {
                var self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots",
                    data: {

                    },
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            },
            removeSnapshot: function (row) {
                var self = this;
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots/" + row.id,
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            }
        },
        mounted: function () {
            var self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/" + projectID + "/snapshots/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
