define(["underscore", "app/track", "app/upload", "app/album", "app/common", "modal"], function (_) {

    var Track = require("app/track");
    var Upload = require("app/upload");
    var Album = require("app/album");
    var Common = require("app/common");

    var Backbone = require("backbone");


    var CollectionView = UpdatingCollectionView.extend({


        viewByModel:function (model) {
            return _(this._childViews).find(function (view) {
                return view.model == model
            })
        },
        appendHtml:function (html) {

            this.$("#upload").before(html);
        },
        empty:function () {
            // this.$("li:not(#upload)").remove();
        }
    })

    var ConfirmDeleteView = Backbone.View.extend({
        tagName:"div",
        className:"modal hide",
        events:{
            "click .btn-accept":"accept",
            "click .btn-cancel":"cancel"
        },
        initialize:function (options) {
            this._callback = options.callback;
            this.render();
        },
        cancel:function () {
            this._callback(false);
        },
        accept:function () {
            this._callback(true);
            this.$el.modal("hide");
        },

        render:function () {

            // Load the compiled HTML into the Backbone "el"
            this.$el.html($("#tpl-delete").html());
            this.$el.on("hide", function () {
                $(this).remove();
            })
            this.$el.modal();
            return;
        }
    })


    var TrackOverviewView = Track.OverviewView.extend({
        tagName:"div",
        className:"track-overview span6",
        createUploadView:false,
        initialize:function () {

            $(this.el).html($("#tpl-track-overview").html());

            this._super("initialize");
        }
    })
    var trackBindings = {};
    _(Track.Bindings).map(function (v, k) {
        trackBindings[k] = v.replace("track.", "tracks.");
    })
    var TrackEditView = Backbone.View.extend({

        tagName:"li",
        className:"track",
        events:{
            "click .delete":"deleteView"
        },


        deleteView:function () {
            var self = this;
            new ConfirmDeleteView({
                callback:function (answer) {
                    if (answer) {
                        self.model.collection.remove(self.model);
                    }
                }
            })

        },
        initialize:function () {


            $(this.el).html($("#tpl-track").html());

            this.overviewView = new Track.OverviewView({
                createUploadView:false,
                el:this.$(".track-overview"),
                model:this.model});

            this.editView = new Track.EditView({bindings:trackBindings, el:this.$(".input-block"), model:this.model});


        },
        remove:function () {
            this.editView.remove();
            this.overviewView.remove();
            return this;
        },
        render:function () {

            return this;
        }


    })


    var View = Backbone.View.extend({
        el:".content-wrap",

        events:{
            "click #save-button":"save",
            "click #cancel":"cancel",
            "click #track-overviews .track-overview":"changeIndex"
        },

        initialize:function () {
            // _.bindAll(this);

            this._canSave = false;
            this.album = new Album.Model().on("change", this._onAttributeChanged, this);

            this.tracks = this.album.tracks.on("add", this._onAttributeChanged, this);


            this.albumView = new Album.View({el:"#album", model:this.album});

            this.albumOverviewView = new Common.OverviewView({el:"#album .track-overview", model:this.album})

            this.trackUploadView = new Upload.View(
                {el:"#track-upload",
                    uri:"/artist/upload/audio",
                    limit:"291MB",
                    types:"*.wav;*.aif;*.flac",
                    progressSelector:"#main-upload-progress"

                })

            this.trackUploadView.on("started", this._onUploadStarted, this)
                .on("beforeStarted", this._onBeforeUploadStarted, this);


            this.trackCollectionView = new CollectionView({
                collection:this.tracks,
                childViewConstructor:TrackEditView,

                childViewTagName:'li',
                onRemoveCallback:this._onRemoveCollectionViewCallback.bind(this),

                el:$('.tracks')[0]
            })


            this.trackCollectionView.render();


            this.tracks.bind("select", this.select);


            this.$saveButton = this.$("#save-button").addClass("disabled");
            this.tracks.on("remove", this._onTrackRemoved, this);

        },
        _onAttributeChanged:function (model) {


            this.album.off("change", this._onAttributeChanged);
            this.tracks.off("change", this._onAttributeChanged);


            $(window).bind('beforeunload', function (e) {
                return 'Are you sure you want to leave?';
            })
            this.album.on("change", this._onModelChanged, this);
            if (model == this.album) {
                this._onModelChanged(model);
            }


        },
        /**
         *
         * @param view {TrackEditView}
         * @param next
         * @private
         */
        _onRemoveCollectionViewCallback:function (view, next) {
            view.editView.$el.hide();
            view.overviewView.$el.slideUp(next)

        },
        _onTrackRemoved:function () {
            if (!this.tracks.length) {
                this.albumView.$(".right-panel").show();
            }
        },
        _activeModel:null,
        activeView:function () {
            return this.trackCollectionView.viewByModel(this._activeModel);


        },
        _onBeforeUploadStarted:function () {
            var prevActiveView = this.activeView();
            if (prevActiveView) {
                prevActiveView.overviewView.removeUploadListeners(this.trackUploadView);
            }
            this.albumView.$(".right-panel").hide();
            var model = this._activeModel = new Track.Model();
            this.tracks.add(model);

            var view = this.activeView();
            view.overviewView.attachUploadListeners(this.trackUploadView);
            this.trackUploadView.bindTo(view);
        },
        _onUploadStarted:function () {


        },
        changeIndex:function () {
            console.log(arguments);
        },
        _onModelChanged:function (model) {
            if (model == this.album) {
                if ("name" in this.album.changed) {

                    var name = this.album.get("name");
                    if (_.isEmpty(name)) {
                        this.$saveButton.addClass("disabled")
                        this._canSave = true;
                    } else {
                        this.$saveButton.removeClass("disabled");
                        this._canSave = false;
                    }
                }
            }

        },
        save:function () {
            if (!this._canSave)return;
            this.album.save()
        },
        cancel:function () {

        }
    })

    return {
        View:View
    }


})