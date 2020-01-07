$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    let app = new Vue({
        el: '#batches-app',
        data: {
            batch:{
                "id":undefined,
                "name":"New Batch",
                "type":"GROUP",
                "date":new Date()
            },
            table: {
                cols: [
                    {
                        "slot": "name",
                        "label": "Batch Name"
                    },
                    {
                        "slot": "type",
                        "label": "Batch Type"
                    },
                    {
                        "slot": "date",
                        "label": "Batch Date"
                    },
                    {
                        "slot": "tasks",
                        "label": "Tasks"
                    },
                    {
                        "slot": "actions",
                        "label": "Actions"
                    }],
                data: [],
                name: 'tableBatch'

            }
        },
        methods: {
            saveBatch : function(){
                let self = this;
                this.batch.date = new Date(this.batch.date);
                $.post("projects/" + projectID + "/batches",
                self.batch, function (d) {
                    self.listBatch();
                    $('#editMilestone').modal('hide');
                });
            },
            deleteBatch: function(row){
                let self = this;
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "projects/" + projectID + "/batches/" + row.id,
                    success: function (d) {
                        self.listBatch();
                    }
                });
            },
            editBatch: function(row){
                this.batch = row;
                $('#editMilestone').modal('show');
            },
            listBatch : function(){
                let self = this;
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "projects/" + projectID + "/batches/list",
                    success: function (d) {
                        self.table.data = d;
                    }
                });
            }
        },
        mounted: function () {
            this.listBatch();
        }
    });
});
