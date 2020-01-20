$(document).ready(function () {

    const projectID = $("meta[name='projectID']").attr('content');

    var app = new Vue({
        el: '#projectSnapshots',
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
            polynomialRegression: function(dates, data){
                let polyData = [];
                for (let i = 0; i<dates.length; i++){
                    polyData.push([i, data[i]]);
                }
                const result = regression('polynomial', polyData, 3);
                let polyDataForGraph = [];
                for (let i = 0; i<result.points.length; i++){
                    polyDataForGraph.push(result.points[i][1]);
                }
                return polyDataForGraph;
            },

            showGraph: function() {
                var self = this;
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "projects/" + projectID + "/snapshots/chart",
                    success: function (d) {

                        let polyQuotationData = self.polynomialRegression(d.listOfProjectSnapshotDates, d.quotationData);
                        let polyOriginalEstimateData = self.polynomialRegression(d.listOfProjectSnapshotDates, d.originalEstimateData);
                        let polyRealEffortData = self.polynomialRegression(d.listOfProjectSnapshotDates, d.realEffortData);
                        let polyEffortLeftData = self.polynomialRegression(d.listOfProjectSnapshotDates, d.effortLeftData);
                        let polyEffortSpentData = self.polynomialRegression(d.listOfProjectSnapshotDates, d.effortSpentData);

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
                                    data: polyQuotationData,
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
                                    data: polyOriginalEstimateData,
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
                                     data: polyRealEffortData,
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
                                     data: polyEffortLeftData,
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
                                    data: polyEffortSpentData,
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
                    self.table.data = d;
                    self.showGraph();
                }
            });
        }
    });
});
