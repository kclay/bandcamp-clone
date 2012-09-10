define(["underscore", "app/track", "app/upload", "app/album", "app/common", "modal"], function (_) {

    var Track = require("app/track");
    var Upload = require("app/upload");
    var Album = require("app/album");
    var Common = require("app/common");

    var Backbone = require("backbone");

    var Ajax = jsRoutes.controllers.Ajax


    var CollectionView = UpdatingCollectionView.extend({
        _createChildView:function (model, index) {
            return  new this._childViewConstructor({
                tagName:this._childViewTagName,
                model:model,
                id:"track_" + index
            });

        },
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
        initialize:function (options) {

            if (this.$el.is("li")) {
                $(this.el).html($("#tpl-track").html());
            }

            this.overviewView = new Track.OverviewView({
                createUploadView:false,
                el:this.$(".track-overview"),
                model:this.model}, this);

            this.editView = new Track.EditView({bindings:options.trackBindings || trackBindings, el:this.$(".input-block"), model:this.model});


        },


        init:function () {

            if (this.overviewView.init)this.overviewView.init()
            if (this.editView.init)this.editView.init()
            if (this.model.get("id")) {
                this.editView.render();
                this.overviewView.render();
            }

            return this;

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


    var STATES = Common.STATES;
    var View = Backbone.View.extend({
        el:".content-wrap",

        events:{
            "click #save-button":"save",
            "click #cancel":"cancel",
            "click #publish-button":"publish",
            "click #view-model":"view"

        },

        initialize:function (options) {
            // _.bindAll(this);
            this.publishTemplate = _.template($("#tpl-published").html());
            this.stateManager = new Common.StateManager();

            this._uploaders = {};

            this._canSave = false;

            this.uploadDefaults = {
                uri:"/upload/audio",
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
            if (options.album) {
                this._initAlbum();

            } else {
                this._initTrack();
            }


            this.$saveButton = this.$("#save-button").addClass("disabled");


            var path = window.location.pathname.split("/");
            var slug = _.last(path)
            if (!_.isEmpty(slug) &&
                slug.indexOf("_album") == -1 && slug.indexOf("_track") == -1) {
                var self = this;
                this.is("reloading", true);
                var album = this.options.album;
                var model = album ? this.album : this.track;
                var loading = new Common.LoadingView();
                model.fetch({
                    url:(album ? Ajax.fetchAlbum(slug) : Ajax.fetchTrack(slug)).url,
                    success:function () {
                        model.refresh();
                        if (!album) {
                            self._onTrackAdded(self.track);
                        }
                        self.stateManager.reset();
                        self.is("reloading", false)
                        loading.destroy();
                    },
                    error:function () {
                        loading.destroy(function () {
                            new Common.FeedbackView(
                                {
                                    error:true,
                                    data:{
                                        title:"Error Restoring",
                                        message:"Unable to restore your " + ((album) ? "Album" : "Track")
                                    }
                                })
                        })
                    }
                });


            }


        },
        _initAlbum:function () {
            this.album = new Album.Model()
                .on("change", this._onAlbumChanged, this)
                .on("change", this._onAttributeChanged, this);

            this.tracks = this.album.tracks;
            this.albumView = new Album.View({el:"#album", model:this.album});

            this.albumOverviewView = new Common.OverviewView({el:"#album .track-overview", model:this.album})

            this._watchOverView(this.albumOverviewView);

            this.$sort = $("ol.tracks").sortable({items:".track", handle:".drag", axis:"y",
                start:this._onDragSortStart.bind(this),
                update:this._onDragSortStop.bind(this)
            });

            this.trackCollectionView = new CollectionView({
                collection:this.tracks,
                childViewConstructor:TrackEditView,

                childViewTagName:'li',
                onRemoveCallback:this._onRemoveCollectionViewCallback.bind(this),

                el:$('.tracks')[0]
            })


            this.trackCollectionView.render();

            this.tracks.on("add", this._onTrackAdded, this);
            this.tracks.on("remove", this._onTrackRemoved, this);
        },
        _initTrack:function () {
            var track = this.track = new Track.SingleTrack();
            this.trackView = new TrackEditView({el:".new-track", model:track, trackBindings:Track.Bindings}).init();
            this._activeModel = track;
            this._onTrackAdded(track);
            this.trackView.overviewView.attachUploadListeners(this.trackUploadView);
            //this.trackUploadView.bindTo(overviewView);


        },
        _onDragSortStart:function (event, ui) {
            this._currentTrackOrder = this.$sort.sortable("toArray")
            ui.item.height(ui.item.find(".right-panel").height());
        },
        _onDragSortStop:function (event, ui) {
            var trackOrder = this.$sort.sortable("toArray");
            if (!_.isEqual(this._currentTrackOrder, trackOrder)) {
                var models = this.tracks.models;
                // get id of moved track
                var id = ui.item[0].id;
                // find were it was moved to
                var at = trackOrder.indexOf(id)
                // parse out the index
                var index = parseInt(id.split("_")[1], 10)

                // use _.find rather then model[index] since
                // the indices will change after the first move
                var model = _.find(models, function (m) {
                    return m.id == index
                })

                // remove
                models.splice(index, 1)

                // add
                models.splice(at, 0, model)


                model.trigger("refresh")
            }
        },
        /**
         * Applies a listener to an {Common.OverviewView} to listen for the "switch" event
         * @param overviewView
         * @return {*}
         * @private
         */
        _watchOverView:function (overviewView) {
            if (this.options.album)
                overviewView.on("switch", this._onOverViewSwitchRequest, this);
            return overviewView;
        },
        _trackView:function (model) {
            return this.trackCollectionView ?
                this.trackCollectionView.viewByModel(model)
                : this.trackView
        },
        /**
         * Removes a listener to the {Common.OverviewView}
         * @param overviewView
         * @return {*}
         * @private
         */
        _unwatchOverView:function (overviewView) {
            overviewView.off("switch", this._onOverViewSwitchRequest, this);
            return overviewView;
        },
        _onTrackAdded:function (model) {
            var modelView = this._trackView(model);
            var overviewView = modelView.overviewView;
            model.on(Track.Status.EVENT_CHANGED, this._onTrackStatusChanged, this);
            model.on("change:artURL", this._onTrackArtChanged, this);
            model.on("change", this._onAttributeChanged, this);
            if (this.options.album) {
                if (overviewView)this._watchOverView(overviewView) && this._onOverViewSwitchRequest(overviewView);
                this._updateHeight();
            }

            if (model.get("id")) {
                this._addReplaceUploader(modelView);
                this._attachOverviewToUploader(overviewView);
            }

        },
        _onTrackArtChanged:function (model) {
            if (!this.options.album)return;
            var artURL = model.get("artURL")

            if (!this.album.get("artURL")) {
                this._setAlbumArt(model.get("art"), artURL);
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
            if (this.options.album && !this.tracks.length) {
                this._onOverViewSwitchRequest(this.albumOverviewView);
            }
            model.off(Track.Status.EVENT_CHANGED, this._onTrackStatusChanged, this);
            model.off("change:artURL", this._onTrackArtChanged, this);
            if (this.options.album) {
                this._updateHeight(!this.tracks.length);
            }
            this.enable(true);


        },

        /**
         * Updates the state manager upon track status change
         * @see Track.STATUS
         * @see Common.STATES
         * @param model {Track.Model|Album.Model}
         * @param status {Track.STATUS.*}
         * @private
         */
        _onTrackStatusChanged:function (model, status) {

            switch (status) {
                case Track.Status.UPLOADED:
                    this.stateManager.update(model, STATES.PROCESSING, true)
                    break;
                case Track.Status.COMPLETED:
                    this.stateManager.update(model, STATES.PROCESSED, true);
                case Track.Status.ERROR:

                    this.stateManager.update(model, STATES.PROCESSING, false);
                    break;
            }
        },
        /**
         *
         * @param auto Optional
         * @private
         */
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


            if (!this.is("reloading")) {
                this.stateManager.update(model, STATES.DATA_CHANGED, true);


                this.enable(!_.isEmpty(model.get("name")));
            }


        },


        _activeModel:null,
        activeView:function () {
            return this.options.album ?
                this.trackCollectionView.viewByModel(this._activeModel) : this.trackView;


        },
        activeOverview:function () {
            return (this.activeView() || {}).overviewView;
        },
        _onBeforeUploadStarted:function () {
            if (!this.options.album)return;
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
            this._attachOverviewToUploader(overviewView);

        },
        _attachOverviewToUploader:function (overviewView) {

            overviewView.attachUploadListeners(this.trackUploadView);
            this.trackUploadView.bindTo(overviewView);
        },
        _onUploadStarted:function () {
            var view = this.activeView();

            this.stateManager.update(this._activeModel, STATES.UPLOADING, true)
            this._addReplaceUploader(view);
        },

        _addReplaceUploader:function (view) {
            if (!this._uploaders[view.cid]) {
                var el = $(".new-track #track-upload")[0] || view.overviewView.el;
                this._uploaders[view.cid] = new Upload.ReplaceView($.extend({},
                    this.uploadDefaults, {el:el, model:view.model}),
                    this.trackUploadView, view.overviewView);
            }

        },
        _onUploaded:function () {

        },

        _setAlbumArt:function (id, url) {
            if (id instanceof Backbone.Model) {
                url = id.get("artURL")
                id = id.get("art")
            }
            this.album.set({art:id, artURL:url});
            //this.albumOverviewView.setArtURL(id, url);
        },
        enable:function (yes) {


            this._canSave = yes;
            if (yes) {
                this.$saveButton.removeClass("disabled");
            } else {
                this.$saveButton.addClass("disabled")

            }
        },
        _onAlbumChanged:function (album) {

            if (!this.is("reloading")) {
                if ("name" in album.changed) {

                    var name = album.get("name");
                    this.enable(!_.isEmpty(name));

                }
            }
            if ("art" in album.changed) {
                if (_.isEmpty(album.get("art"))) {

                    var model = _.chain(this.tracks)
                        .filter(function (model) {
                            return !_.isEmpty(model.get("art"))
                        }).first().value()

                    if (model)this._setAlbumArt(model)
                }
            }


        },
        save:function () {
            if (!this._canSave)return;
            $("#saving").slideDown()
            var finish = function () {
                $("#saving").delay(500).slideUp()
            }
            var self = this;
            var model = (this.album || this.track);
            model.capture();
            model.save(null, {
                success:function () {
                    finish()
                    $("#publish-button").fadeIn();
                    self.stateManager.reset();
                    model.saveTags();
                },
                error:function () {
                    finish()
                }
            })
        },

        view:function () {
            var uri = this.album ? "/album/" + this.album.get("slug") : "/track/" + this.track.get("slug")
            window.location.href = uri;
        },
        publish:function () {
            $("#saving").slideDown()
            var finish = function () {
                $("#saving").delay(500).slideUp()
            }
            var self = this;
            var album = this.options.album;
            var kind = album ? "album" : "track";
            var model = album ? this.album : this.track;
            Ajax.publish(kind, model.get("slug")).ajax({

                success:function () {

                    $("#published").html(self.publishTemplate({title:model.get("name"), view:album ? "Album" : "Track"}))
                    $("#published").slideDown(function () {
                        $(this).css({display:"inline-block"})
                    })


                    finish();
                },
                error:function () {
                    alert("an error happened")
                }
            })
        },
        cancel:function () {

        }
    })

    return {
        View:View
    }


})