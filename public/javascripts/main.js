require.config({
    baseUrl:"/assets/javascripts",
    paths:{
        "backbone":"libs/backbone/backbone",
        "underscore":"libs/underscore/underscore",
        "jquery":"libs/jquery/jquery",
        "swfupload":"libs/swfupload/main",
        "binder":"libs/backbone/Backbone.ModelBinder",
        "dropdown":"libs/bootstrap-dropdown",
        "typeahead":"libs/bootstrap-typeahead",
        "modal":"libs/bootstrap-modal"
    },
    shim:{

        'backbone':{
            //These script dependencies should be loaded before loading
            //backbone.js
            deps:['underscore', 'jquery'],
            //Once loaded, use the global 'Backbone' as the
            //module value.
            exports:'Backbone'
        },
        "binder":{
            deps:["backbone"],
            exports:"Backbone.ModelBinder"
        },
        "dropdown":{
            deps:["jquery"]

        },
        "swfupload":{
            exports:"SWFUpload"
        }


    }
});


require([

    "dropdown",
    // Load our app module and pass it to our definition function
    'app'


], function (App)
{
    // The "app" dependency is passed in as "App"
    // Again, the other dependencies passed in are not "AMD" therefore don't pass a parameter to this function

    require("app").initialize();
});