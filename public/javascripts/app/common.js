define(["underscore", "backbone"], function (_) {


    var FeedbackView = Backbone.View.extend({
        tagName:"div",
        className:"modal hide",
        template:_.template($("#tpl-feedback").html()),

        initialize:function (options) {

            this.render();
        },


        appendHtml:function () {
            this.$el.html(this.template(this.options.data));
        },
        render:function () {

            // Load the compiled HTML into the Backbone "el"
            this.appendHtml();
            this.$el.on("hide", function () {
                $(this).remove();
            })
            this.$el.modal();
            return this;
        }
    })
    var ConfirmView = FeedbackView.extend({
        events:{
            "click .btn-accept":"accept",
            "click .btn-cancel":"cancel"
        },
        initialize:function (options) {
            this._super("initialize", options);
            this._callback = options.callback;
        },
        cancel:function () {
            this._callback(false);
        },
        accept:function () {
            this._callback(true);
            this.$el.modal("hide");
        }
    })
    var OverviewView = Backbone.View.extend({
        events:{
            'click':'switchToView'
        },
        parent:function () {
            return this._parent;
        },
        setArtURL:function (url) {
            if (!_.isEmpty(url)) {
                // add cache buster for browser and hide/show for rendering flaw
                this.$art.find("img")
                    .attr("src", url + "?r=" + (new Date()).getTime())
                    .hide().show()
            } else {
                this.$art.find("img").hide();
            }
        },
        initialize:function (options, parent) {
            _.bindAll(this);
            this._parent = parent;


            this.$title = this.$(".title");
            this.$details = this.$(".details");
            this.$art = this.$(".album-art");
            this.$by = this.$(".by span");
            this.model.on("change:name change:download change:price change:donateMore change:artURL change:artist", this._onModelChange)


            this._onModelChange(this.model);
            this._loaded = true;


        },
        switchToView:function () {
            this.trigger("switch", this);
        },

        _onModelChange:function (model) {


            if ("name" in model.changed) {
                var title = this.model.get("name") || "Untitled Track";
                this.$title.html(title);
            } else if ("artURL" in model.changed) {

                var url = this.model.get("artURL");
                this.setArtURL(url);

            } else {


                var text = "";
                if (this.model.get("download")) {
                    text = "downloadable,";
                    var price = this.model.get("price");
                    var donateMore = this.model.get("donateMore");
                    if (parseInt(price, 10) == 0) {
                        this.model.set("price", 1);
                        return;
                    } else {
                        text += "$" + price;
                    }


                }

                this.$details.html(text);
            }

            if ("artist" in model.changed || !this._loaded) {
                var artist = this.model.get("artist") || app_config.band_name;
                this.$by.html(artist);
            }


        }
    });
    var Validators = {
        date:function (value) {
            var parts = _.filter((value || "").split("/"), function (value) {
                return value.length == 2
            })


            if (parts.length != 3)return false

            if (isNaN(Date.parse(value)))return false
            return true
        }
    }
    return {
        OverviewView:OverviewView,
        FeedbackView:FeedbackView,
        ConfirmView:ConfirmView,
        Validate:Validators
    }
})
