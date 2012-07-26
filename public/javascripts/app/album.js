define(["binder", "backbone", "app/upload", "app/common", "app/track"], function (binder, Backbone, Upload) {


    var _ = require("underscore");
    var Common = require("app/common");


    var Track = require("app/track");
    var Album = Backbone.Model.extend({
        initialize:function () {
            this.tracks = new Track.Collection()
        },
        validation:{
            name:{
                required:true,
                msg:'Please enter a track name'
            }
        },
        defaults:{
            id:0,
            artist_id:0,
            name:"",
            download:true,
            price:"1.00",
            donateMore:true,
            about:"",

            credits:"",
            artist:"",
            art:"",
            artURL:"",
            session:app_config.session


        }, isNew:function () {
            return this.id == 0;
        },
        urlRoot:"/ajax/albums",
        parse:function (resp, xhr) {
            _(this.tracks).each(function (model, index) {
                model.set(resp.tracks[index], {slient:true})
            })
            //this.tracks.reset(resp.tracks, {slient:true});
            return resp.album
        },
        toJSON:function () {
            var o = _.clone(this.attributes);

            delete o["artURL"];
            return{
                album:o,
                tracks:this.tracks.toJSON()
            }

        }


    });

    var AlbumOverviewView = Common.OverviewView.extend({

    })
    var AlbumEditView = Backbone.View.extend({


        _upload:null,

        bindings:{
            name:"[name='album.name']",
            download:"[name='album.download']",
            price:"[name='album.price']",
            donateMore:"[name='album.donateMore']",
            about:"[name='album.about']",

            credits:"[name='album.credits']",
            artist:"[name='album.artist']"


        },
        events:{
            "click .album-art .close":"removeArt"
        },

        initialize:function () {
            _.bindAll(this);
            this._binder = new Backbone.ModelBinder();

            this.artUploadView = new Upload.View(
                {   el:this.$(".art-upload"),
                    uri:"/artist/upload/art",
                    limit:"4MB",
                    types:"*.jpg;*.gif;*.png"
                }
            );

            this.artUploadView.on("uploaded", this._onArtUploaded);

            this.render();

        },

        _onArtUploaded:function (info) {
            var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.artUploadView.el);
            wrapper.find("img").attr("src", info.url);

            this.model.set({art:info.id, artURL:info.url});
            this.artUploadView.reset();


        },
        removeArt:function () {
            this.$(".image").remove();
            this.model.set({art:"", artURL:""});
        },
        render:function () {

            this._binder.bind(this.model, this.$el, this.bindings);

            return this;
        }
    })


    return{
        View:AlbumEditView,
        Model:Album,
        OverviewView:AlbumOverviewView
    }
})