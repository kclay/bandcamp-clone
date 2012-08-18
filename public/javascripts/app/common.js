define(["underscore", "backbone", "modal"], function (_) {


    var FeedbackView = Backbone.View.extend({
        tagName:"div",
        className:"modal hide",
        template:"#tpl-feedback",
        module_options:{},

        initialize:function (options) {
            options = options || {};
            this.template = _.template($(this.template).html());
            if (options.error) {
                this.$el.addClass("error").find(".btn-cancel").addClass("btn-danger");
            }
            this.render();

        },


        appendHtml:function () {
            this.$el.html(this.template(this.options.data || {}));
        },
        render:function () {

            // Load the compiled HTML into the Backbone "el"
            this.appendHtml();
            this.$el.on("hide", function () {
                $(this).remove();
            })
            this.$el.modal(this.module_options);
            return this;
        }
    })


    var LoadingView = FeedbackView.extend({
        module_options:{
            keyboard:false
        },
        template:"#tpl-loading",
        initialize:function (options) {
            options = options || {};
            options.data = options.data || {
                title:"Please Wait",
                message:"Page Loading"
            }
            this._super("initialize", options);
        },
        destroy:function (callback) {
            var self = this;
            setTimeout(function () {
                self.$el.modal("hide")
                if (callback)callback();
            }, 1 * 1000)


            return;
        }

    })
    var ConfirmView = FeedbackView.extend({
        events:{
            "click .btn-accept":"accept",
            "click .btn-cancel":"cancel"
        },
        template:"#tpl-confirm",

        initialize:function (options) {
            this._super("initialize", [options]);
            this._callback = this._onCallback || options.callback;
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

            var refresh = !this._loaded;
            if (refresh || "name" in model.changed) {
                var title = this.model.get("name") || "Untitled Track";
                this.$title.html(title);
            }
            if (refresh || "artURL" in model.changed) {

                var url = this.model.get("artURL");
                this.setArtURL(url);

            }


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


            if (!this._loaded || "artist" in model.changed) {
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
    var STATES = {
        PROCESSING:"processing",
        DATA_CHANGED:"data_changed",
        UPLOADING:"uploading",
        PROCESSED:"processed"
    }
    var StatesManager = function () {


        $(window).bind('beforeunload', _.bind(this._onBeforeWindowUnload, this))
        this.reset();

    }
    $.extend(StatesManager.prototype, Backbone.Events, {
        reset:function () {
            this._states = {};
            this._states[STATES.PROCESSING] = {
                msg:"You Currently have (<%= models %>) tracks(s) being processed.\nAre you sure you want to close, all current data will be lost?",
                models:[]
            };
            this._states[STATES.PROCESSED] = {
                msg:"You Currently have (<%= models %>) tracks(s) that have been uploaded.\nAre you sure you want to close, all current data will be lost?",
                models:[]
            };
            this._states[STATES.DATA_CHANGED] = {
                msg:"You Currently have (<%= models %>) item(s) that have been changed since last save.\nAre you sure you want to close, all current data will be lost?",
                models:[]
            };
            this._states[STATES.UPLOADING] = {
                msg:"You Currently have (<%= models %>) track(s) being uploading.\nAre you sure you want to close, all current data will be lost?",
                models:[]
            };
        },
        _onBeforeWindowUnload:function () {
            var activeState = _.chain(this._states)
                .filter(function (info) {
                    return info.models.length
                })
                .first().value();


            if (activeState)return _.template(activeState.msg, {models:activeState.models.length});
            this.trigger("beforeUnload");


        },
        update:function (model, state, add) {
            var models = this._states[state].models;
            if (add) {
                if (_.indexOf(models, model) == -1)  models.push(model)
            } else {
                this._states[state].models = _.without(models, model);
            }
        }
    })

    var ArtUploader = function ($el, artUploadView, model) {
        this.artUploadView = artUploadView;
        this.$el = $el;
        this.model = model;
        this.artUploadView.on("uploaded", this._onArtUploaded, this);
        this.model.on("refresh", this._onModelRefresh, this);

    }
    $.extend(ArtUploader.prototype, {
        _onModelRefresh:function () {
            var model = this.model;
            if (!_.isEmpty(model.get("art")) && !_.isEmpty(model.get("artURL"))) {
                this._onArtUploaded({url:model.get("artURL"), id:model.get("art")});
            }
        },
        _onArtUploaded:function (info) {
            if (info.error) {
                new FeedbackView({title:"Upload Error", message:info.error, error:true});
            } else {
                var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.$el.find(".art-upload"));
                wrapper.find("img").attr("src", info.url);
                if (!this._artID) {
                    this._artID = info.id;
                    this.artUploadView.setPostParam("id", info.id)
                }
                this.model.set({art:info.id, artURL:info.url});
                this.artUploadView.reset();
            }


        }
    })
    return {
        OverviewView:OverviewView,
        FeedbackView:FeedbackView,
        ConfirmView:ConfirmView,
        ArtUploader:ArtUploader,
        Validate:Validators,
        STATES:STATES,
        LoadingView:LoadingView,
        StateManager:StatesManager
    }
})
