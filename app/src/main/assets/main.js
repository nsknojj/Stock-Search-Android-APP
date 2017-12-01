function show(response) {

    var symbol = response["Meta Data"]["2. Symbol"];

    for (var key in response) {
        if (key != "Meta Data") {
            var g = response[key];
        }
    }
    var data = [];
    var ct = 0;
    for (var date in g) {
        ct++;
        var date_number=(new Date(date)).getTime();
        data.push([date_number, parseFloat(g[date]["4. close"])]);
        if(ct>=10000) break;
    }

    data.reverse();

    var his =
        {
            xAxis: {
                type: "datetime"
            },
            yAxis: {
                title: {text: 'Stock Value'}
            },
            chart: {
            type: 'area'
            },
            rangeSelector: {
                allButtonsEnabled: false,
                selected: 0,
                buttons: [ {
                    type: 'month',
                    count: 1,
                    text: '1m'
                }, {
                    type: 'month',
                    count: 3,
                    text: '3m'
                }, {
                    type: 'month',
                    count: 6,
                    text: '6m'
                }, {
                    type: 'year',
                    count: 1,
                    text: '1y'
                }, {
                    type: 'all',
                    text: 'All'
                }]
            },
            title: {
                text: symbol.toUpperCase()+" Stock Value"
            },
            tooltip: {valueDecimals:2},
            subtitle: {
                text: '<a href="https://www.alphavantage.co/" target="_blank"><div id="chart_subtitle">Source: Alpha Vantage</div></a>',
                useHTML: true
            },
            series: [{
              name: symbol.toUpperCase(),
              data: data
            }]
        };
    Highcharts.stockChart('chart-his', his);

}