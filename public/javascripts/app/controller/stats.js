/**
 * Created with IntelliJ IDEA.
 * User: Keyston
 * Date: 8/18/12
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
define(["underscore", "backbone", "app/common", "highcharts"], function (_, Backbone, Common, Highcharts) {

    var chartOptions =
    {

        chart:{
            renderTo:'chart'
        },

        title:{
            text:'Daily visits at www.highcharts.com'
        },

        subtitle:{
            text:'Source: Google Analytics'
        },

        xAxis:{
            type:'datetime',
            tickInterval:24 * 3600 * 1000, // one day
            tickWidth:0,
            gridLineWidth:1,
            labels:{
                align:'left',
                x:3,
                y:-3
            }
        },

        yAxis:[
            { // left y axis
                title:{
                    text:null
                },
                labels:{
                    align:'left',
                    x:3,
                    y:16,
                    formatter:function () {
                        return Highcharts.numberFormat(this.value, 0);
                    }
                },
                showFirstLabel:false
            },
            { // right y axis
                linkedTo:0,
                gridLineWidth:0,
                opposite:true,
                title:{
                    text:null
                },
                labels:{
                    align:'right',
                    x:-3,
                    y:16,
                    formatter:function () {
                        return Highcharts.numberFormat(this.value, 0);
                    }
                },
                showFirstLabel:false
            }
        ],

        legend:{
            align:'left',
            verticalAlign:'top',
            y:20,
            floating:true,
            borderWidth:0
        },

        tooltip:{
            shared:true,
            crosshairs:true
        },

        plotOptions:{
            series:{
                cursor:'pointer',
                point:{
                    events:{
                        click:function () {
                            /* hs.htmlExpand(null, {
                             pageOrigin:{
                             x:this.pageX,
                             y:this.pageY
                             },
                             headingText:this.series.name,
                             maincontentText:Highcharts.dateFormat('%A, %b %e, %Y', this.x) + ':<br/> ' +
                             this.y + ' visits',
                             width:200
                             }); */
                        }
                    }
                },
                marker:{
                    lineWidth:1
                }
            }
        },

        series:[
            {
                name:'All visits',
                lineWidth:4,
                marker:{
                    radius:4
                }
            },
            {
                name:'New visitors'
            }
        ]
    };
    var Stats = require("app").Stats;

    var View = Backbone.View.extend({

        initialize:function () {

            _.bind(this._onSuccess, this)
            _.bind(this._onError, this);
            this.load(Stats.Metrics.Play, Stats.Ranges.AllTime)
        },
        load:function (stats, range) {
            this.loadingView = new Common.LoadingView();
            this[stats](range);

        },
        play:function (range) {
            this.fetch(Stats.fetch.Play, range)
        },
        sales:function (range) {
            this.fetch(Stats.fetch.Sales, range);
        },
        fetch:function (method, range) {
            method(range, _.bind(this._onSuccess, this), _.bind(this._onError, this))
        },
        _onSuccess:function (json) {
            this.loadingView.destroy();
            json = $.parseJSON(json);
            if (json)  this.update(json)
        },
        _onError:function () {

        },
        update:function (data) {
            this.currentData = data;


            var stats = _.map(data.stats, function (records, date) {
                date = new Date(date);

                date = Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())
                return  _.map(records, function (record) {
                    return [date, record.total]
                })


            })
            stats = _.flatten(stats, true);
            var dates = _.pluck(stats, "0")

            var minDate = new Date()
            minDate.setUTCMilliseconds(_.min(dates))

            minDate.setMonth(minDate.getMonth() - 1)


            chartOptions.series[0].data = stats;
            chartOptions.plotOptions.series.pointStart = minDate;
            this.chat = new Highcharts.Chart(chartOptions);


        }


    })
    return {
        View:View
    }
})