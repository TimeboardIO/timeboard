
const properties = $("meta[property='dashboard']");
let qt =  properties.attr('qt');
let oe = properties.attr('oe');
let re = properties.attr('re');
let es = properties.attr('es');
let el = properties.attr('el');


const data = {
    labels: ['QT', 'OE', 'RE', 'ES + EL'],
    datasets: [
        {
            label: 'Quotation',
            data: [qt, 0, 0, 0],
            backgroundColor: 'rgba(255, 99, 132, 0.2)',
            borderColor: 'rgba(255, 99, 132, 1)',
            borderWidth: 3
        },
        {
            label: 'Original Estimate',
            data: [0, oe, 0, 0],
            backgroundColor: 'rgba(54, 162, 235, 0.2)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 3
        },
        {
            label: 'Real Effort',
            data: [0, 0, re, 0],
            backgroundColor: 'rgba(153, 102, 255, 0.2)',
            borderColor: 'rgba(153, 102, 255, 1)',
            borderWidth: 3
        },
        {
            label: 'Effort Spent',
            data: [0, 0, 0, es],
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            borderColor: 'rgba(75, 192, 192, 1)',
            borderWidth: 3
        },
        {
            label: 'Effort Left',
            data: [0, 0, 0, el],
            backgroundColor: 'rgba(255, 206, 86, 0.2)',
            borderColor: 'rgba(255, 206, 86, 1)',
            borderWidth: 3
        }
    ]
};

const options = {
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
};


const ctx = document.querySelector("#dashboardBarChart");
let kpiChart = new Chart(ctx, {
    type: 'bar',
    data: data,
    options: options
});
