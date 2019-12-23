$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#components-demo',
        data: {
            table: {
                cols: [
                    {
                        "slot": "snapshotdate",
                        "label": "Date"
                    },
                    {
                        "slot": "snapshotglobalquotation",
                        "label": "QT"
                    },
                    {
                        "slot": "snapshotglobaloriginalestimate",
                        "label": "OE"
                    },
                    {
                        "slot": "snapshotglobalreeleffort",
                        "label": "RE"
                    },
                    {
                        "slot": "snapshotglobaleffortspent",
                        "label": "ES"
                    },
                    {
                        "slot": "snapshotglobaleffortleft",
                        "label": "EL"
                    },
                    {
                        "slot": "snapshotactions",
                        "label": "Actions"
                    }],
                data: []
            }
        },
        methods: {
            formatDate: function(d) {
                for (var  i = 0; i<d.length; i++){
                    d[i].projectSnapshotDate = new Date(d[i].projectSnapshotDate).toLocaleString();
                }
            },
            addSnapshot: function () {
                var self = this;
                $.ajax({
                    type: "POST",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots",
                    data: {

                    },
                    success: function (d) {
                        self.formatDate(d);
                        self.table.data = d;
                        self.showGraph();
                    }
                });
            },
            removeSnapshot: function (row) {
                var self = this;
                $.ajax({
                    type: "DELETE",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots/" + row.id,
                    success: function (d) {
                        self.formatDate(d);
                        self.table.data = d;
                        self.showGraph();
                    }
                });
            },
            showGraph: function() {
                var self = this;
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots/chart",
                    success: function (d) {
                        let listOfProjectSnapshotDates = d.listOfProjectSnapshotDates;
                        let quotationDataForChart = d.quotationData;
                        let originalDataForChart = d.originalEstimateData;
                        let realEffortDataForChart = d.realEffortData;
                        let effortLeftDataForChart = d.effortLeftData;
                        let effortSpentDataForChart = d.effortSpentData;

                        //chart config
                        let chart = new Chart($("#lineChart"), {
                            type: 'line',
                            data: {
                                labels: listOfProjectSnapshotDates,
                                datasets: [{
                                    data: quotationDataForChart,
                                    label: "QT",
                                    borderColor: "#3e95cd",
                                    fill: true,
                                    steppedLine: true
                                } , {
                                    data: originalDataForChart,
                                    label: "OE",
                                    borderColor: "#ff6384",
                                    fill: true,
                                    steppedLine: true
                                 } , {
                                    data: realEffortDataForChart,
                                    label: "RE",
                                    borderColor: "#00CC00",
                                    fill: true,
                                    steppedLine: true
                                 } , {
                                     data: effortLeftDataForChart,
                                     label: "EL",
                                     borderColor: "#FF00CC",
                                     fill: true,
                                     steppedLine: true
                                 } , {
                                    data: effortSpentDataForChart,
                                    label: "ES",
                                    borderColor: "#FFFF00",
                                    fill: true,
                                    steppedLine: true
                                  }
                                 ]
                            },
                            options: {
                                title: { display: true, text: 'Project' },
                                scales: {
                                    yAxes: [{
                                        ticks: {
                                            min: 0
                                        },
                                        scaleLabel: { display: true, labelString: 'Number of point' }
                                    }],
                                    xAxes: [{
                                        scaleLabel: { display: true, labelString: 'Dates' }
                                    }],
                                }
                            }
                        });
                    }
                });
            }
        },
        mounted: function () {
            var self = this;
            $.ajax({
                type: "GET",
                dataType: "json",
                url: "projects/" + projectID + "/snapshots/list",
                success: function (d) {
                    self.formatDate(d);
                    self.table.data = d;
                    self.showGraph();
                }
            });
        }
    });
});
