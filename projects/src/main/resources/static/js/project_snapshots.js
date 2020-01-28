$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#projectSnapshots',
        data: {
            snapshotsListConfig: {
                name:"tableSnapshots",
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
                    }]
            },
            snapshotsListData: []
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
                        self.snapshotsListData = d;
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
                        self.snapshotsListData = d;
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

                        for(let i = 0; i < d.listOfProjectSnapshotDates.length; i++){
                            let date = new Date(d.listOfProjectSnapshotDates[i]);
                            d.listOfProjectSnapshotDates[i] = date.toLocaleString();
                        }

                        //chart config
                        let chart = new Chart($("#lineChart"), {
                            type: 'line',
                            data: {
                                labels: d.listOfProjectSnapshotDates,
                                datasets: [{
                                    data: d.quotationData,
                                    label: "QT",
                                    borderColor: "#3e95cd",
                                    fill: false,
                                    steppedLine: true
                                } , {
                                    data: d.quotationRegressionData,
                                    label: "Poly_QT",
                                    borderColor: "#3e95cd",
                                    fill: false,
                                    steppedLine: true,
                                    borderDash:[5, 15]
                                } , {
                                    data: d.originalEstimateData,
                                    label: "OE",
                                    borderColor: "#ff6384",
                                    fill: false,
                                    steppedLine: true
                                } , {
                                    data: d.originalEstimateRegressionData,
                                    label: "Poly_OE",
                                    borderColor: "#ff6384",
                                    fill: false,
                                    steppedLine: true,
                                    borderDash:[5, 15]
                                } , {
                                    data: d.realEffortData,
                                    label: "RE",
                                    borderColor: "#00CC00",
                                    fill: false,
                                    steppedLine: true
                                } , {
                                     data: d.realEffortRegressionData,
                                     label: "Poly_RE",
                                     borderColor: "#00CC00",
                                     fill: false,
                                     steppedLine: true,
                                     borderDash:[5, 15]
                                } , {
                                     data: d.effortLeftData,
                                     label: "EL",
                                     borderColor: "#FF00CC",
                                     fill: false,
                                     steppedLine: true
                                } , {
                                     data: d.effortLeftRegressionData,
                                     label: "Poly_EL",
                                     borderColor: "#FF00CC",
                                     fill: false,
                                     steppedLine: true,
                                     borderDash:[5, 15]
                                } , {
                                    data: d.effortSpentData,
                                    label: "ES",
                                    borderColor: "#FFFF00",
                                    fill: false,
                                    steppedLine: true
                                } , {
                                    data: d.effortSpentRegressionData,
                                    label: "Poly_ES",
                                    borderColor: "#FFFF00",
                                    fill: false,
                                    steppedLine: true,
                                    borderDash:[5, 15]
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
                    self.snapshotsListData = d;
                    if(d.length>0){
                        self.showGraph();
                    }

                }
            });
        }
    });
});
