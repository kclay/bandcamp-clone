/*(function ($)
 {
 // Behind the scenes method deals with browser
 // idiosyncrasies and such
 $.caretTo = function (el, index)
 {
 if (el.createTextRange) {
 var range = el.createTextRange();
 range.move("character", index);
 range.select();
 } else if (el.selectionStart != null) {
 el.focus();
 el.setSelectionRange(index, index);
 }
 };

 // The following methods are queued under fx for more
 // flexibility when combining with $.fn.delay() and
 // jQuery effects.

 // Set caret to a particular index
 $.fn.caretTo = function (index, offset)
 {
 return this.queue(function (next)
 {
 if (isNaN(index)) {
 var i = $(this).val().indexOf(index);

 if (offset === true) {
 i += index.length;
 } else if (offset) {
 i += offset;
 }

 $.caretTo(this, i);
 } else {
 $.caretTo(this, index);
 }

 next();
 });
 };

 // Set caret to beginning of an element
 $.fn.caretToStart = function ()
 {
 return this.caretTo(0);
 };

 // Set caret to the end of an element
 $.fn.caretToEnd = function ()
 {
 return this.queue(function (next)
 {
 $.caretTo(this, $(this).val().length);
 next();
 });
 };
 }(jQuery));*/
require.config({
    baseUrl:"/assets/javascripts",
    paths:{
        "backbone":"libs/backbone/backbone",
        "underscore":"libs/underscore/underscore",
        "jquery":"libs/jquery/jquery",
        "swfupload":"libs/swfupload/main",
        "binder":"libs/backbone/Backbone.ModelBinder",
        "dropdown":"libs/bootstrap-dropdown",
        "typeahead":"libs/bootstrap-typeahead"
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