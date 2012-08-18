define(["dropdown"], function () {
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
    var Stats = {
        fetch:{},
        track:{}
    };
    var Events = "Play,Skip,Partial,Complete".split(",");
    _.each("Play,Sales".split(","), function (name) {

        var Event = name;
        var Metric = Event.toLowerCase();
        Stats.fetch[Event] = function (success, error) {
            Routes.Ajax.fetchStats(Metric).ajax({
                sucess:success,
                error:error
            })

        }
    })
    _.each(Events, function (name) {
        var Event = name;
        var Metric = Event.toLowerCase()
        Stats[Event] = Metric;
        Stats.track[Event] = function (object, deletePrevious) {
            var index = Events.indexOf(Event);
            var tracked = Stats[Event].tracked;
            if (deletePrevious && !tracked.previous[object]) {

                for (var i = index; i > 0; i--) {
                    var PreviousEvent = Events[i];
                    if (Stats[PreviousEvent].tracked.now[object]) {
                        tracked.previous[object] = true
                        Routes.Ajax.track(Events[i], object, 1).ajax({

                            error:function () {
                                delete tracked.previous[object];
                            }
                        });
                    }
                }

            }
            if (!tracked.now[object]) {
                tracked.now[object] = true;
                var canTrack = _.filter(Events, function (event) {
                    if (event == "Play")return true;
                    if (Events.indexOf(event) > index) {
                        if (Stats[Event].tracked.now[object])return false;
                        return true;
                    } else {
                        return true;
                    }
                })
                if (canTrack.length == 1) {
                    Routes.Ajax.track(Metric, object, 0).ajax({
                        error:function () {
                            delete tracked.now[object];
                        }
                    });
                }
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