var chart_config = {};

function showError(type) {
  $('#progress-'+type).hide();
  if (type=="detail") $('#table-detail').hide();
  else if (type=='feeds') $('#content-feeds').hide();
  else $('#chart-'+type).hide();
  $('#error-'+type).show();
}

function showProgress(type) {
  if (type=="detail") $('#table-detail').hide();
  else if (type=='feeds') $('#content-feeds').hide();
  else $('#chart-'+type).hide();
  $('#error-'+type).hide();
  $('#progress-'+type).show();
}

function showContent(type) {
  $('#error-'+type).hide();
  $('#progress-'+type).hide();
  if (type=="detail") $('#table-detail').show();
  else if (type=='feeds') $('#content-feeds').show();
  else $('#chart-'+type).show();
}

var indicators = ["Price", "SMA", "EMA", "STOCH", "RSI", "ADX", "CCI", "BBANDS", "MACD"];

function submitChart(symbol) {

  for (var i in indicators) {
    let ind = indicators[i];

    showProgress(ind);

    $.ajax({
      url: 'http://stocksearch-zwt.us-east-2.elasticbeanstalk.com/api-chart/' + ind,
      type: 'GET',
      dataType: 'json',
      data: 'symbol='+symbol,
      success: function(response) {
        if (!response || !("Meta Data" in response)) {
          console.log("Received an Error from Alphavantage");
          showError(ind);
          if (ind=='Price') showError('his');
          return;
        }

        var type = ind;
        for (var key in response) {
          if (key != "Meta Data") {
            var g = response[key];
          }
        }
        var ct = 0, categories = [], data = [];
        for (var date in g) {
          ct++;
          var tmp=new Date(date);
          categories.push((('0' + (tmp.getMonth()+1)).slice(-2))+'/'+(('0' + (tmp.getDate()+1)).slice(-2)));
          if (type=="Price") {
            if (data["Price"] === undefined) {
              data["Price"] = [];
              data["Volume"] = [];
            }
            data["Price"].push(parseFloat(g[date]["4. close"]));
            data["Volume"].push(parseFloat(g[date]["5. volume"]));
          }
          else {
            for (var series in g[date]) {
              if (data[series] === undefined) data[series] = [];
              data[series].push(parseFloat(g[date][series]));
            }
          }
          if (ct==25*5+1) break;
        }
        categories.reverse();
        var series = [];
        if (type!="Price") {
          for (var name in data) {
            data[name].reverse();
            series.push({
              "name":type in {"STOCH":1, "BBANDS":1, "MACD":1, "Price":1}?symbol+" "+name: symbol,
              "data":data[name],
            });
          }
        }
        else {
          data["Price"].reverse();
          data["Volume"].reverse();
          series.push({
            "tooltip": {"valueDecimals":2},
            "name":"Price",
            "type":"area",
            "data":data["Price"],
            "yAxis":0
          });
          series.push({
            "name":"Volume",
            "type":"column",
            "data":data["Volume"],
            "yAxis":1
          });
        }

        var ind_full_name = {
          "Price":symbol.toUpperCase() +" Stock Price and Volume",
          "SMA":"Simple Moving Average (SMA)",
          "EMA":"Exponential Moving Average (EMA)",
          "STOCH":"Stochastic Oscillator (STOCH)",
          "RSI":"Relative Strength Index (RSI)",
          "ADX":"Average Directional movement indeX (ADX)",
          "CCI":"Commodity Channel Index (CCI)",
          "BBANDS":"Bollinger Bands (BBANDS)",
          "MACD":"Moving Average Convergence/Divergence (MACD)"
        }

        var tmp =
        {
            title: {
                text: ind_full_name[type],
            },
            chart: {
              zoomType: 'x'
            },
            subtitle: {
                text: '<a href="https://www.alphavantage.co/" target="_blank"><div id="chart_subtitle">Source: Alpha Vantage</div></a>',
                useHTML: true
            },
            xAxis: {
                categories: categories,
            },
            yAxis:
            type=="Price"? [
              {title: {text: "Stock Price"}},
              {title: {text: "Volume"}, opposite:true}
            ]:
            {
                title: {
                    text: type,
                },

            },
            series: series
        };

        Highcharts.chart('chart-' + ind, tmp);
        chart_config['chart-' + ind] = tmp;

        showContent(ind);
      },
      error: function(xhr, status, error) {
        console.log('Query for chart error');
        showError(ind);
      }
    });
  }
}

function callJS(symbol) {
    submitChart(symbol);
}

var current = null;
function show(ind) {
    if (current) $('#pn-'+current).hide();
    $('#pn-'+ind).show();
    current = ind;
}

function fetchChart(ind) {
    var ans = chart_config['chart-' + ind];
    if (ans) return chart_config['chart-' + ind];
    return undefined;
}

$(document).ready(function() {
  for (var i in indicators) {
    var tmp=$("<div></div>").attr('id', 'chart-' + indicators[i]).css('width', '100%').css('height', '100%');
    var tmp2 = `<div class="progress" id="progress-` + indicators[i] + `" style="display:none"><div class="progress-bar progress-bar-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0", style="width: 50%"></div></div><div class="alert alert-danger" id="error-` + indicators[i] + `"><strong>Error! Failed to get ` + indicators[i] +` data.</strong></div>`;
    var tmp3=$("<div></div>").attr('id', 'pn-' + indicators[i]).css('display','none').css('width', '95%').css('margin', 'auto').append(tmp, tmp2);
    $('#chart-set').append(tmp3);
  }
//  callJS('AAPL');
//  show('Price');
});
