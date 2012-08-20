define(["underscore", "dropdown"], function () {
    var initialize = function () {
        // Pass in our Router module and call it's initialize function

        controllers.init();

    }

    var controllers = {

        pages:{
            "edit_track":{
                ctr:"app/controller/track"
            },
            "new_album":{
                ctr:"app/controller/editor",
                config:{
                    album:true
                }
            },
            "edit_album":{
                ctr:"app/controller/editor",
                config:{
                    album:true
                }
            },
            "new_track":{
                ctr:"app/controller/editor"

            },
            "edit_track":{
                ctr:"app/controller/editor"

            },
            "pick_tags":{
                ctr:"app/controller/pickTags"
            },
            "album":{
                ctr:"app/controller/display"
            },
            "track":{
                ctr:"app/controller/display"
            },
            "my_albums":{
                ctr:"app/controller/albums"
            },
            "stats":{
                ctr:"app/controller/stats"
            }

        },
        init:function () {

            $('.dropdown-toggle').dropdown();
            app_config.session = function () {
                return $("input[name='session']:first").val();
            }
            var path = location.pathname.split("/")[1];
            if (path in this.pages) {
                var c = this.pages[path];
                require([c.ctr], function (ctr) {
                    window.view = new ctr.View(c.config || {});
                });
            }
            $.each(app_config.after, function (index, callback) {
                callback();
            })

        }

    }
    var Routes = (jsRoutes || {}).controllers
    var fetch = function (object, deletePrevious) {
    }
    var Stats = {
        Events:"Play,Skip,Partial,Complete".split(","),
        fetch:{
            Play:fetch,
            Skip:fetch,
            Partial:fetch,
            Complete:fetch
        },
        track:{},
        Metrics:{
            Plays:function (range, sucess, error) {
            },
            Sales:function (range, sucess, error) {
            }
        },
        Ranges:{
            AllTime:"alltime",
            TwoMonths:"twomonths",
            Month:"month",
            Week:"week",
            Today:"today"




        },
        RangeText:{
            Today:"today",
            Week:"7 Days",
            Month:"30 days",
            TwoMonths:"60 days",
            AllTime:"all-time"
        }
    };

    _.each("Plays,Sales".split(","), function (name) {

        var Type = name;
        var method = Type.toLowerCase();
        Stats.fetch[Type] = function (Metric, Range, success, error) {

            Routes.Stats[method](Metric, Range).ajax({
                success:success,
                error:error
            })

        }
    })

    _.each(Stats.Events, function (name) {
        var Event = name;
        var Metric = Event.toLowerCase()
        Stats.Metrics[Event] = Metric;
        var self = Stats.track[Event] = function (object, deletePrevious) {
            var index = Stats.Events.indexOf(Event);
            var tracked = self.tracked;
            if (deletePrevious && !tracked.previous[object]) {

                for (var i = index - 1; i > 0; i--) {
                    var PreviousEvent = Stats.Events[i];
                    if (Stats.track[PreviousEvent].tracked.now[object]) {
                        tracked.previous[object] = true
                        var PreviousMetric = PreviousEvent.toLowerCase();
                        Routes.Stats.track(PreviousMetric, object, 1).ajax({

                            error:function () {
                                delete tracked.previous[object];
                            }
                        });
                    }
                }

            }
            if (!tracked.now[object]) {
                tracked.now[object] = true;


                Routes.Stats.track(Metric, object, 0).ajax({
                    error:function () {
                        delete tracked.now[object];
                    }
                });

            }

        }
        Stats.track[Event].tracked = {previous:{}, now:{}};
    })


    return {
        initialize:initialize,
        Routes:Routes,
        Stats:Stats



    };
});