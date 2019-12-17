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
                reportSelectProject: "",
                selectedProjects: [],
                sizeSelectedProjects: 0,
            },
            methods: {
                refreshProjectSelection: function (reportSelectProject) {
                    var self = this;
                    $.ajax({
                        type: "GET",
                        dataType: "json",
                        url: "/api/reports/refreshProjectSelection?filter=" + self.reportSelectProject,
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
});
