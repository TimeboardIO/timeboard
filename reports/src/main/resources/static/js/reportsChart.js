$(document).ready(function () {

    const reportID = $("meta[name='reportID']").attr('content');
    const reportType = $("meta[name='reportType']").attr('content');

    var appListReports = new Vue({
            el: '#app-view-report',
            data: {
            },
            methods: {
            },
            mounted: function () {
                $.ajax({
                    method: "GET",
                    url: "/data-chart/"+"project_kpi"/*JSON.stringify(reportType)*/+"/"+reportID,
                    success : function(data, textStatus, jqXHR) {
                        console.log(data);
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        console.log(data);
                    }
                });
            }
        });
});

