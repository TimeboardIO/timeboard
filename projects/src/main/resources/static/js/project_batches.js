$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    let app = new Vue({
        el: '#batches-app',
        data: {
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
                    }],
                data: [],
                name: 'tableBatch'

            }
        },
        methods: {
        },
        mounted: function () {
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
    });
});
