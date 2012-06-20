define(function ()
{
    var AppRouter = {

        init:function ()
        {

            var path = location.pathname;
            if (path in this) {
                this[path]();
            }
        },
        "/add_track":function ()
        {
            console.log("hello");
        },
        defaultAction:function (actions)
        {
            // We have no matching route, lets just log what the URL was
            console.log('No route:', actions);
        }
    };


    var initialize = function ()
    {
        (new AppRouter).init();


    };
    return {
        initialize:initialize
    };
});