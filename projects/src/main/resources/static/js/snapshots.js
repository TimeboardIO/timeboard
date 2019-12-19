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
                        "label": "QT"
                    },
                    {
                        "slot": "snapshotglobaloriginalestimate",
                        "label": "OE"
                    },
                    {
                        "slot": "snapshotglobalreeleffot",
                        "label": "RE"
                    },
                    {
                        "slot": "snapshotglobaleffortspent",
                        "label": "ES"
                    },
                    {
                        "slot": "snapshotglobaleffortleft",
                        "label": "EL"
                    },
                    {
                        "slot": "snapshotactions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
            formatDate: function(d) {
                for (var  i = 0; i<d.length; i++){
                    d[i].projectSnapshotDate = new Date(d[i].projectSnapshotDate).toLocaleString();
                }
            },
            addSnapshot: function () {
                var self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots",
                    data: {

                    },
                    success: function (d) {
                        self.formatDate(d);
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
                        self.formatDate(d);
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
                    self.formatDate(d);
                    self.table.data = d;
                }
            });
        }
    });
});
