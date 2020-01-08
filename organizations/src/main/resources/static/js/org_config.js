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
            configurable : false
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
            configurable : false
        }
    },
    methods: {
        addType: function () {
            let self = this;
            $.ajax({
                type: "POST",
                dataType: "json",
                url: "org/setup/task-type/",
                data: {
                    "name": "New Type",
                    "id": 0
                },
                success: function (d) {
                    d.forEach(r => r.edition = false);
                    self.types.data = d;
                }
            });
        },
        updateType: function (row) {
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: row,
                url: "org/setup/task-type/"+row.id,
                success: function (d) {
                    d.forEach(r => r.edition = false);
                    self.types.data = d;
                }
            });
        },
        removeType: function (row) {
            let self = this;
            this.$refs.confirmModal.confirm("Are you sure you want to delete "+ row.name + "?", function() {
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "org/setup/task-type/"+row.id,
                    success: function (d) {
                        d.forEach(r => r.edition = false);
                        self.types.data = d;
                    }
                });
            });
        },
        addTask: function () {
            let self = this;
            $.ajax({
                type: "POST",
                dataType: "json",
                url: "org/setup/default-task/",
                data: {
                    "name": "New Task",
                    "id": 0
                },
                success: function (d) {
                    d.forEach(r => r.edition = false);
                    self.tasks.data = d;
                }
            });
        },
        updateTask: function (row) {
            let self = this;
            $.ajax({
                type: "PATCH",
                dataType: "json",
                data: row,
                url: "org/setup/default-task/"+row.id,
                success: function (d) {
                    d.forEach(r => r.edition = false);
                    self.tasks.data = d;
                }
            });
        },
        removeTask: function (row) {
            let self = this;
            this.$refs.confirmModal.confirm("Are you sure you want to delete "+ row.tagKey + "?", function() {
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "org/setup/default-task/"+row.id,
                    success: function (d) {
                        d.forEach(r => r.edition = false);
                        self.tasks.data = d;
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
                d.forEach(r => r.edition = false);
                self.tasks.data = d;
            }
        });
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "org/setup/task-type/list",
            success: function (d) {
                d.forEach(r => r.edition = false);
                self.types.data = d;


            }
        });
    }

});

