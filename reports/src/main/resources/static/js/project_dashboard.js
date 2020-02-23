
const properties = $("meta[property='dashboard']");
let qt =  properties.attr('qt');
let oe = properties.attr('oe');
let re = properties.attr('re');
let es = properties.attr('es');
let el = properties.attr('el');


const data = {
    labels: ['QT', 'OE', 'RE', 'ES'],
    datasets: [
        {
            label: 'Quotation',
            data: [qt,0,0,0],
            backgroundColor: 'royalblue',
            borderWidth: 3
        },
        {
            label: 'Original Estimate',
            data: [0, oe,0,0],
            backgroundColor: '#ff6384',
            borderWidth: 3
        },
        {
            label: 'Real Effort',
            data: [0, 0,re,0],
            backgroundColor: '#36a2eb',
            borderWidth: 3
        },
        {
            label: 'Effort Spent',
            data: [0, 0, 0,es],
            backgroundColor: '#cc65fe',
            borderWidth: 3
        },
        {
            label: 'Effort Left',
            data: [0, 0, 0, el],
            backgroundColor: '#ffce56',
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


const ctx = document.querySelector("#barchart");
let kpiChart = new Chart(ctx, {
    type: 'bar',
    data: data,
    options: options
});


/*let myChart = new Chart(ctx, {
    type: 'bar',
    data: {
        labels: ["M", "T", "W", "R", "F", "S", "S"],
        datasets: [{
            label: 'apples',
            data: [12, 19, 3, 17, 28, 24, 7]
        }, {
            label: 'oranges',
            data: [30, 29, 5, 5, 20, 3, 10]
        }]
    }
});*/
