$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#components-demo',
        data: {
            table: {
                cols: [
                    {
                        "slot": "tagkey",
                        "label": "Tag Key",
                        "sortKey": "tagKey"
                    },
                    {
                        "slot": "tagvalue",
                        "label": "Tag Value",
                        "sortKey": "tagValue",

                    },
                    {
                        "slot": "tagactions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
            addTag: function () {
                var self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "projects/" + projectID + "/tags",
                    data: {
                        "tagKey": "New Tag",
                        "tagValue": "New Value"
                    },
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            },
            updateTag: function (row) {
                var self = this;
                $.ajax({
                    type: "PATCH",
                    dataType: "json",
                    data: row,
                    url: "projects/" + projectID + "/tags/" + row.id,
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            },
            removeTag: function (row) {
                var self = this;
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "projects/" + projectID + "/tags/" + row.id,
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
                url: "projects/" + projectID + "/tags/list",
                success: function (d) {
                    self.table.data = d;
                }
            });
        }
    });
});
