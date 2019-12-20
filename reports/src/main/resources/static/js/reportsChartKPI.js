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
                    url: "/data-chart/report-kpi/"+reportID,
                    success : function(dataKPI, textStatus, jqXHR) {

                        let chart = new Chart($("#chartKPI"), {
                            type: 'bar',
                            data: {
                                labels: ['QT', 'OE', 'RE', 'ES', "EL"],
                                datasets: [
                                    {
                                        label: 'Quotation',
                                        data: [dataKPI.quotation, 0, 0, 0, 0],
                                        backgroundColor: 'royalblue',
                                        borderWidth: 3
                                    },
                                    {
                                        label: 'Original Estimate',
                                        data: [0, dataKPI.originalEstimate, 0, 0, 0],
                                        backgroundColor: '#ff6384',
                                        borderWidth: 3
                                    },
                                    {
                                       label: 'Real Effort',
                                       data: [0, 0, dataKPI.realEffort, 0, 0],
                                        backgroundColor: '#36a2eb',
                                        borderWidth: 3
                                    },
                                    {
                                        label: 'Effort Spent',
                                        data: [0, 0, 0, dataKPI.effortSpent, 0],
                                        backgroundColor: '#cc65fe',
                                        borderWidth: 3
                                    },
                                    {
                                        label: 'Effort Left',
                                        data: [0, 0, 0, 0, dataKPI.effortLeft],
                                        backgroundColor: '#ffce56',
                                        borderWidth: 3
                                    }
                                ]
                            },
                            options: {
                                responsive: true,
                                scales: {
                                    xAxes: [{
                                        stacked: true
                                    }],
                                    yAxes: [{
                                        stacked: true,
                                        ticks: {
                                            beginAtZero: true
                                        }
                                    }]
                                }
                            }
                        });

                    },
                    error: function(dataKPI, textStatus, jqXHR) {
                        $("#app-view-report").empty();
                        if(dataKPI.status == 500){
                           $("#app-view-report").append("Your filter is not supported by the Spring Expression Language (SpEL), please modify your filter.")
                        }else{
                           $("#app-view-report").append(dataKPI.responseText)
                        }
                    }
                });
            }
        });
});
