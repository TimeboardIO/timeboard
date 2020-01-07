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
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    data: self.batch,
                    url: "projects/" + projectID + "/batches",
                    success: function (d) {
                        self.listBatch();
                        $('#editMilestone').modal('hide');
                    }
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
