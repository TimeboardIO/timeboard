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
                selectedProjectFilter: "",
                selectedProjects: [],
                sizeSelectedProjects: 0
            },
            methods: {
                refreshProjectSelection: function (selectedProjectFilter) {
                    var self = this;
                    $.ajax({
                        type: "POST",
                        dataType: "json",
                        data: JSON.stringify(self.selectedProjectFilter),
                        contentType: "application/json",
                        url: "/api/reports/refreshProjectSelection",
                        success: function (d) {
                            self.selectedProjects = d;
                            self.sizeSelectedProjects = d.length;
                        },
                        errors: function (d) {
                            alert(d);
                        }
                    });
                }
            }
        });


    $( "#refreshSelectedProjects" ).click(function() {
        var filter = $( "input[name='selectedProjectFilter']").val();
         $.ajax({
             type: "POST",
             dataType: "json",
             data: JSON.stringify(filter),
             contentType: "application/json",
             url: "/api/reports/refreshProjectSelection",
             success: function (d) {
                 selectedProjects = d;
                 sizeSelectedProjects = d.length;
             },
             errors: function (d) {
                 alert(d);
             }
         });
    });
});
