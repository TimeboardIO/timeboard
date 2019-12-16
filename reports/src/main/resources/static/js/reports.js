$(document).ready(function () {

    const reportID = $("meta[name='reportID']").attr('content');

    var appListReports = new Vue({
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


    var appCreateReport = new Vue({
            el: '#create-report',
            data: {
            },
            methods: {
                refreshProjectSelection: function(event){
                    event.target.classList.toggle('loading');
                    $.get("/api/reports/refreshProjectSelection")
                    .then(function(data){
                        event.target.classList.toggle('loading');
                    });
                }
            }
        });
});
