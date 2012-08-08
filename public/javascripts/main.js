(function () {
    var app_config = window['app_config'] || {};
    var config = {
        baseUrl:"/assets/javascripts",
        paths:{
            "backbone":"libs/backbone/backbone",
            "underscore":"libs/underscore/underscore",

            'jquery-ui':"libs/jquery/jquery-ui",
            "swfupload":"libs/swfupload/main",
            "binder":"libs/backbone/Backbone.ModelBinder",
            "dropdown":"libs/bootstrap-dropdown",
            "typeahead":"libs/bootstrap-typeahead",
            "modal":"libs/bootstrap-modal",
            "html5":"libs/jquery/jquery.html5_upload"
        },


        shim:{

            'backbone':{
                //These script dependencies should be loaded before loading
                //backbone.js
                deps:['underscore'],
                //Once loaded, use the global 'Backbone' as the
                //module value.
                exports:'Backbone'
            },
            "binder":{
                deps:["backbone"],
                exports:"Backbone.ModelBinder"
            },

            "swfupload":{
                exports:"SWFUpload"
            }


        }
    }
    config = $.extend(true, {}, config, app_config.config)
    require.config(config);


    var includes = [

        'jquery-ui',
        "dropdown",
        "html5",
        // Load our app module and pass it to our definition function
        'app'


    ]
    includes = (app_config.includes || []).concat(includes)
    require(includes, function (App) {
        // The "app" dependency is passed in as "App"
        // Again, the other dependencies passed in are not "AMD" therefore don't pass a parameter to this function

        require("app").initialize();
    });
})()
