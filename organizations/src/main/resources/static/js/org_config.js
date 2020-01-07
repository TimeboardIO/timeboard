const currentOrgID = $("meta[property='organization']").attr('orgID');
const baseURL = $("meta[property='organization']").attr('baseURL');

let app = new Vue({
    el: '#orgConfig',
    data: {
        types: {
            cols: [
                {
                    "slot": "name",
                    "label": "Task Type",
                    "sortKey": "name",
                    "primary" : true

                },
                {
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            data: [],
            name: 'tableTag',
            configurable : true
        },
        tasks: {
            cols: [
                {
                    "slot": "name",
                    "label": "Default Task",
                    "sortKey": "name",
                    "primary" : true

                },
                {
                    "slot": "actions",
                    "label": "Actions",
                    "primary" : true
                }],
            data: [],
            name: 'tableTag',
            configurable : true
        }
    },
    methods: {
        addType: function () {
            let self = this;
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
        updateType: function (row) {
            let self = this;
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
        removeType: function (row) {
            let self = this;
            this.$refs.confirmModal.confirm("Are you sure you want to delete "+ row.tagKey + "?", function() {
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "projects/" + projectID + "/tags/" + row.id,
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            });
        }
    },
    mounted: function () {
        let self = this;
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "org/setup/default-task/list",
            success: function (d) {
                self.tasks.data = d;
            }
        });
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "org/setup/task-type/list",
            success: function (d) {
                self.types.data = d;
            }
        });
    }

});

