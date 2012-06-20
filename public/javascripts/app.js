define(["dropdown"], function ()
{
    var initialize = function ()
    {
        // Pass in our Router module and call it's initialize function

        controllers.init();

    }

    var controllers = {

        pages:{
            "/add_track":["app/controller/addTrack"]
        },
        init:function ()
        {

            $('.dropdown-toggle').dropdown();
            var path = location.pathname;
            if (path in this.pages) {
                require(this.pages[path], this[path]);
            }
        },
        "/add_track":function ()
        {

            var ctr = require("app/controller/addTrack");
            new ctr.View();

        }
    }

    return {
        initialize:initialize
    };
});