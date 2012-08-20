/**
 * Created with IntelliJ IDEA.
 * User: Keyston
 * Date: 8/18/12
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
define(["underscore", "backbone", "app/common", "highcharts"], function (_, Backbone, Common, Highcharts) {

    var Stats = require("app").Stats;
    var ActiveView;
    window.changeRange = function (range) {
        ActiveView.range(range);
    }
    var chartOptions =
    {

        chart:{
            renderTo:'chart'
        },

        title:{
            text:""


        },

        subtitle:{
            text:_.map(Stats.Ranges,function (rel, range) {

                var tpl = '<a href="javascript:changeRange(\'' + rel + '\')" class="range">' + Stats.RangeText[range] + '</a>';
                console.log(tpl);
                return tpl;
            }).join('')
        },

        xAxis:{
            type:'datetime',
            tickInterval:24 * 3600 * 1000, // one day
            tickWidth:0,


            labels:{
                align:'left',
                x:3,
                y:-3,
                step:15
            }
        },

        yAxis:[
            { // left y axis
                title:{
                    text:null
                },
                gridLineWidth:0,
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

                            ActiveView.render(this.items);
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

            }
        ]
    };


    var MetricsLower = _.map(Stats.Events, function (s) {
        return s.toLowerCase();
    });
    var PlayMetricDefaults = _.reduce(MetricsLower, function (memo, metric) {

        memo[metric] = 0;
        memo[metric + "_ratio"] = 0
        return memo;
    }, {});
    var PlayRules = {
        title:"Play",
        method:Stats.fetch.Plays,
        template:function (item) {
            var t = this.t || (this.t = _.template($("#tpl-plays").html()))
            return t(item);
        },
        group:function (items) {
            return items;
        },
        compute:function (item) {
            return item.metric == "play" ? item.total : 0
        },
        prep:function (items) {

            var tracks = _.sortBy(
                _.map(
                    _.groupBy(items, "slug"), function (stats, key) {
                        var defaults = _.clone(PlayMetricDefaults);
                        var sum = _.reduce(stats, function (memo, stat) {
                            return memo + stat.total
                        }, 0);

                        var metrics = _.reduce(stats, function (memo, stat) {
                            memo[stat.metric] += stat.total

                            return memo;
                        }, defaults);

                        _.each(MetricsLower, function (metric) {
                            var total = metrics[metric]
                            metrics[metric + "_ratio"] = (total * 100) / sum;
                        })


                        return {
                            title:stats[0].title,
                            slug:stats[0].slug,
                            metrics:metrics,
                            total:sum
                        }

                    }
                ), function (track) {
                    return track.total;
                })
            var rank = 0;
            return _.map(tracks, function (track, key) {
                track.rank = ++rank;
                return track;
            })


        }
    }
    var SalesRules = {
        method:Stats.fetch.Sales,
        title:"Sales/Downloads",
        template:function (item) {
            return " ";
        },
        compute:function (item) {
            return item.total;
        },
        group:function (items) {

            return  _.reduce(items, function (memo, values) {
                _.each(values, function (i, date) {
                    if (!memo[date]) {
                        memo[date] = []
                    }
                    memo[date] = memo[date].concat(i)

                })
                return memo;
            }, {})

        },
        prep:function (items) {

            var rank = 0;
            items = _.sortBy(
                _.map(
                    _.groupBy(items, "slug"), function (stats, key) {

                        var sum = _.reduce(stats, function (memo, stat) {
                            return memo + stat.total
                        }, 0);

                        return {
                            title:stats[0].title,
                            slug:stats[0].slug,

                            total:sum
                        }
                    }
                ), function (item) {
                    return item.total;
                })
            return _.map(items, function (item, key) {
                item.rank = ++rank;
                return item;
            })

        },
        compute:function (item) {
            return item.total
        }
    }
    var View = Backbone.View.extend({

        initialize:function () {
            ActiveView = this;
            this._onSuccess = _.bind(this._onSuccess, this)
            this._onError = _.bind(this._onError, this);


            // this.load(Stats.Metrics.Play, Stats.Ranges.AllTime)
            this.load(Stats.Reports.Sales, Stats.Ranges.AllTime)
            this.changeView("#plays-stats");
        },
        changeView:function (id) {
            this.setElement(id);
            this.$body = this.$(".stats-body");
        },

        load:function (stats, range) {
            this.loadingView = new Common.LoadingView();
            this[stats](range);

        },
        range:function (range) {
            this.fetch(this._rules, range);
        },
        play:function (range) {
            this.fetch(PlayRules, range)
        },

        sales:function (range) {
            this.fetch(SalesRules, range);
        },
        fetch:function (rules, range) {

            this._rules = rules;


            rules.method(range, this._onSuccess, this._onError)

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

            var rules = this._rules;
            var stats = _.map(
                rules.group(data), function (items, date) {
                    date = new Date(date);

                    date = Date.UTC(date.getFullYear(), date.getMonth(), date.getDate())
                    var total = _.reduce(items, function (memo, item) {
                        return memo + rules.compute(item)
                    }, 0)
                    return {
                        x:date,
                        y:total,
                        items:items

                    }


                })
            stats = _.flatten(stats, true);
            var dates = _.pluck(stats, "x")
            var lastDate = _.min(dates)
            var date = new Date(lastDate)


            date.setMonth(date.getMonth() - 1)

            stats.unshift([Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()), 0]);
            chartOptions.title.text = rules.title;
            chartOptions.series[0].data = stats;
            chartOptions.series[0] = {
                name:rules.title,
                lineWidth:4,
                marker:{
                    radius:4
                },
                data:stats
            }
            //chartOptions.plotOptions.series.pointStart = minDate.getUTCMilliseconds();
            this.chat = new Highcharts.Chart(chartOptions);


        },
        render:function (items) {
            var rules = this._rules;
            items = rules.prep(items);
            this.debug(items);
            var rows = _.map(items, function (item) {
                return rules.template(item);
            })
            this.$body.html(rows.join(""));

        }


    })
    return {
        View:View
    }
})