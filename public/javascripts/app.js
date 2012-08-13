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

    return {
        initialize:initialize,
        Routes:(jsRoutes || {}).controllers

    };
});