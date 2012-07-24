define(["backbone"], function () {


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
        initialize:function () {
            _.bindAll(this);


            this.$title = this.$(".title");
            this.$details = this.$(".details");
            this.$art = this.$(".album-art");
            this.$by = this.$(".by span");
            this.model.on("change:name change:download change:price change:donateMore change:artURL,change:artist", this._onModelChange)


            this._onModelChange(this.model);
            this._loaded = true;


        },

        _onModelChange:function (model) {


            if ("name" in model.changed) {
                var title = this.model.get("name") || "Untitled Track";
                this.$title.html(title);
            } else if ("artURL" in model.changed) {

                var url = this.model.get("artURL");
                if (!_.isEmpty(url)) {
                    this.$art.find("img").attr("src", url).show();
                } else {
                    this.$art.find("img").hide();
                }

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
    return {
        OverviewView:OverviewView,
        FeedbackView:FeedbackView,
        ConfirmView:ConfirmView
    }
})
