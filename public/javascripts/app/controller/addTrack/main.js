define(["app/track"], function ()
{

    var Track = require("app/track");

    var Backbone = require("backbone");
    var View = Backbone.View.extend({
        el:"#save",

        events:{
            "click #save-button":"save",
            "click #cancel":"cancel"
        },

        initialize:function ()
        {
            _.bindAll(this);
            this.$saveButton = this.$("#save-button");
            var model = this.model = new Track.Model();
            this.trackView = new Track.TrackView({el:".track-overview", model:model});
            this.editView = new Track.EditView({el:"#track", model:model});

        },
        _onModelChange:function ()
        {
            if ("name" in this.model.changed) {
                var name = this.model.get("name");
                if (_.isEmpty(name)) {
                    this.$saveButton.addClass("disabled")
                } else {
                    this.$saveButton.removeClass("disabled");
                }
            }

        },
        save:function ()
        {
            this.model.validate()
        },
        cancel:function ()
        {

        }
    })

    return {
        View:View
    }


})