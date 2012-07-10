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
    var TrackEditView = Track.EditView.extend({

        tagName:"div",
        className:"input-block track-block",
        delayRender:true,
        events:{
            "click":"select"
        },
        initialize:function ()
        {
            var self = this;
            _(this.bindings).map(function (v, k)
            {
                self.bindings[k] = v.replace("track.", "tracks.");
            })


            $(this.el).html($("#tpl-track-input").html());


            this._super("initialize");
        },
        select:function ()
        {
            var index = this.index();
            console.log(index);


        },
        index:function ()
        {
            return this.model.collection.indexOf(this.model);
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
                    types:"*.wav;*.aif;*.flac"

                })

            this.trackUploadView.on("started", this._onUploadStarted, this);
            this.trackEditCollectionView = new CollectionView({
                collection:this.tracks,
                childViewConstructor:TrackEditView,

                childViewTagName:'div',
                el:$('#track-inputs')[0]
            })


            this.trackOverviewCollectionView = new CollectionView({
                childViewConstructor:TrackOverviewView,
                childViewTagName:'div',
                collection:this.tracks,
                el:$("#track-overviews .inner")[0]
            })


            this.trackEditCollectionView.render();
            this.trackOverviewCollectionView.render();


            this.tracks.bind("select", this.select);


            /*  this.trackCollectionView = new UpdatingCollectionView({
             collection:this.tracks,
             childViewConstructor:UpdatingTrackView,
             childViewTagName:'div',
             el:$('#tracks')[0]
             })   */
            this.$saveButton = this.$("#save-button").addClass("disabled");
            /* var model = this.model = new Track.Model();
             this.model.on("change", this._onModelChanged)
             this.trackView = new Track.TrackView({el:".track-overview", model:model});
             this.editView = new Track.EditView({el:"#track", model:model});    */



        },
        _activeModel:null,
        activeViews:function ()
        {
            return {
                overview:this.trackOverviewCollectionView.viewByModel(this._activeModel),
                edit:this.trackEditCollectionView.viewByModel(this._activeModel)
            }

        },
        _onUploadStarted:function ()
        {

            this.albumView.$el.hide();
            var model = this._activeModel = new Track.Model();
            this.tracks.add(model);
            var overviewView = this.trackOverviewCollectionView.viewByModel(model);
            overviewView.attachUploadListeners(this.trackUploadView);



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