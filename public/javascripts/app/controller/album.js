define(["underscore", "app/track", "app/upload", "app/album"], function (_)
{

    var Track = require("app/track");
    var Upload = require("app/upload");
    var Album = require("app/album");

    var Backbone = require("backbone");


    var CollectionView = UpdatingCollectionView.extend({

        viewByModel:function (model)
        {
            return _(this._childViews).find(function (view)
            {
                return view.model == model
            })
        }
    })


    var TrackOverviewView = Track.OverviewView.extend({
        tagName:"div",
        className:"track-overview span6",
        createUploadView:false,
        initialize:function ()
        {

            $(this.el).html($("#tpl-track-overview").html());
            this._super("initialize");
        }
    })
    var trackBindings = {};
    _(Track.Bindings).map(function (v, k)
    {
        trackBindings[k] = v.replace("track.", "tracks.");
    })
    var TrackEditView = Backbone.View.extend({

        tagName:"li",
        className:"track",
        events:{
            "click .delete":"deleteView"
        },


        deleteView:function ()
        {
            this.model.collection.remove(this.model);
        },
        initialize:function ()
        {


            $(this.el).html($("#tpl-track").html());

            this.overviewView = new Track.OverviewView({
                createUploadView:false,
                el:this.$(".track-overview"),
                model:this.model});
            this.editView = new Track.EditView({bindings:trackBindings, el:this.$(".input-block"), model:this.model});


        },

        render:function ()
        {

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

        initialize:function ()
        {
            // _.bindAll(this);
            this.tracks = new Track.Collection();
            this.model = new Album.Model()
            this.albumView = new Album.View({el:"#album", model:this.model});
            this.trackUploadView = new Upload.View(
                {el:"#track-upload",
                    uri:"/artist/upload/audio",
                    limit:"291MB",
                    types:"*.wav;*.aif;*.flac",
                    progressSelector:"#tracks-wrapper .upload-progress"

                })

            this.trackUploadView.on("started", this._onUploadStarted, this)
                .on("beforeStarted", this._onBeforeUploadStarted, this);
            this.trackCollectionView = new CollectionView({
                collection:this.tracks,
                childViewConstructor:TrackEditView,

                childViewTagName:'li',

                el:$('.tracks')[0]
            })


            this.trackCollectionView.render();


            this.tracks.bind("select", this.select);


            this.$saveButton = this.$("#save-button").addClass("disabled");
            this.tracks.on("remove", this._onTrackRemoved, this);

        },
        _onTrackRemoved:function ()
        {
            if (!this.tracks.length) {
                this.albumView.$(".right-panel").show();
            }
        },
        _activeModel:null,
        activeView:function ()
        {
            return this.trackCollectionView.viewByModel(this._activeModel);


        },
        _onBeforeUploadStarted:function ()
        {
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
        _onUploadStarted:function ()
        {


        },
        changeIndex:function ()
        {
            console.log(arguments);
        },
        _onModelChanged:function (target, info)
        {
            if ("name" in info.changes) {

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
            this.model.save()
        },
        cancel:function ()
        {

        }
    })

    return {
        View:View
    }


})