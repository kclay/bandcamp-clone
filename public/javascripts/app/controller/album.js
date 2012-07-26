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
        initialize:function (options, parent) {

            $(this.el).html($("#tpl-track-overview").html());

            this._super("initialize", [options, parent]);
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

        hide:function () {
            this.$el.addClass("inactive");
        },
        show:function () {
            this.$el.removeClass("inactive");
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
                model:this.model}, this);

            this.editView = new Track.EditView({bindings:trackBindings, el:this.$(".input-block"), model:this.model});


        },
        init:function () {
            if (this.editView.init)this.editView.init()
            if (this.overviewView.init)this.overviewView.init()
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


            this._uploaders = {};

            this._canSave = false;
            this.album = new Album.Model().on("change", this._onAttributeChanged, this);

            this.tracks = this.album.tracks.on("add", this._onAttributeChanged, this);


            this.albumView = new Album.View({el:"#album", model:this.album});

            this.albumOverviewView = new Common.OverviewView({el:"#album .track-overview", model:this.album})

            this._watchOverView(this.albumOverviewView);
            this.uploadDefaults = {
                uri:"/artist/upload/audio",
                limit:"291MB",
                types:"*.wav;*.aif;*.flac"
            }
            this.trackUploadView = new Upload.View(
                $.extend({}, this.uploadDefaults, {
                    el:"#track-upload",

                    progressSelector:"#main-upload-progress"

                }));

            this.trackUploadView.on("started", this._onUploadStarted, this)
                .on("beforeStarted", this._onBeforeUploadStarted, this)
                .on('uploaded', this._onUploaded, this);


            this.trackCollectionView = new CollectionView({
                collection:this.tracks,
                childViewConstructor:TrackEditView,

                childViewTagName:'li',
                onRemoveCallback:this._onRemoveCollectionViewCallback.bind(this),

                el:$('.tracks')[0]
            })


            this.trackCollectionView.render();


            this.$saveButton = this.$("#save-button").addClass("disabled");
            this.tracks.bind("add", this._onTrackAdded, this);
            this.tracks.on("remove", this._onTrackRemoved, this);
            $("ol.tracks").sortable({items:".track", handle:".drag", axis:"y",
                start:function (event, ui) {
                    ui.item.height(ui.item.find(".right-panel").height());
                }});

        },

        _watchOverView:function (view) {
            view.on("switch", this._onOverViewSwitchRequest, this);
            return view;
        },
        _unwatchOverView:function (view) {
            view.off("switch", this._onOverViewSwitchRequest, this);
            return view;
        },
        _onTrackAdded:function (model) {
            var overviewView = this.trackCollectionView.viewByModel(model).overviewView;
            model.on(Track.Status.EVENT_CHANGED, this._onTrackStatusChanged, this);
            model.on("change:artURL", this._onTrackArtChanged, this);
            if (overviewView) this._watchOverView(overviewView) && this._onOverViewSwitchRequest(overviewView);
            this._updateHeight();

        },
        _onTrackArtChanged:function (model) {
            var art = model.get("artURL")
            if (!this.album.get("artURL")) {
                this._setAlbumArt(model.get("art"), art);
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
            this._unwatchOverView(view.overviewView);

        },
        _onTrackRemoved:function (model) {
            if (!this.tracks.length) {
                this._onOverViewSwitchRequest(this.albumOverviewView);
            }
            model.off(Track.Status.EVENT_CHANGED, this._onTrackStatusChanged, this);
            model.off("change:artURL", this._onTrackArtChanged, this);
            this._updateHeight(!this.tracks.length);


        },
        _onTrackStatusChanged:function (status) {

        },
        _updateHeight:function (auto) {
            this.$el.height(auto ? "auto" : this.$el.find(".album-group").height());
            $("ol.tracks").sortable("refresh");

        },
        _onOverViewSwitchRequest:function (newOverviewView) {
            var activeOverviewView = this.activeOverview();
            if (newOverviewView == this.albumOverviewView) {
                this.albumView.$(".right-panel").show();

            } else {
                newOverviewView.parent().show();
                this._activeModel = newOverviewView.model;

                this.albumView.$(".right-panel").hide();
            }
            if (activeOverviewView && newOverviewView != activeOverviewView) {
                activeOverviewView.$el.height()
                activeOverviewView.parent().hide();
            }


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


        _activeModel:null,
        activeView:function () {
            return this.trackCollectionView.viewByModel(this._activeModel);


        },
        activeOverview:function () {
            return (this.activeView() || {}).overviewView;
        },
        _onBeforeUploadStarted:function () {
            var prevOverviewView = this.activeOverview();
            if (prevOverviewView) {
                // if upload is from 'replace' link then exit
                if (prevOverviewView._active) return;
                prevOverviewView.removeUploadListeners(this.trackUploadView);
            }

            var model = new Track.Model();
            this.tracks.add(model);
            this._activeModel = model;

            var overviewView = this.activeOverview();
            overviewView.attachUploadListeners(this.trackUploadView);
            this.trackUploadView.bindTo(overviewView);
        },
        _onUploadStarted:function () {
            var view = this.activeView();
            if (!this._uploaders[view]) {
                this._uploaders[view] = new Upload.ReplaceView($.extend({},
                    this.uploadDefaults, {el:view.overviewView.el}),
                    this.trackUploadView, view.overviewView);
            }

        },
        _onUploaded:function () {

        },
        changeIndex:function () {
            console.log(arguments);
        },
        _setAlbumArt:function (id, url) {
            this.albumOverviewView.setArtURL(url);
        },
        _onModelChanged:function (model) {
            if (model == this.album) {
                if ("name" in this.album.changed) {

                    var name = this.album.get("name");
                    if (_.isEmpty(name)) {
                        this.$saveButton.addClass("disabled")
                        this._canSave = false;
                    } else {
                        this.$saveButton.removeClass("disabled");
                        this._canSave = true;
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