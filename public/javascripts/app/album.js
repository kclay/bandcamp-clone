define(["binder", "backbone", "app/upload", "app/common", "app/track"], function (binder, Backbone, Upload) {


    var _ = require("underscore");
    var Common = require("app/common");


    var V = Common.Validate
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
            id:null,
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
            releaseDate:"",
            session:app_config.session


        },
        refresh:function () {

        },
        validate:function (attrs, options) {
            if (!_.isEmpty(attrs.releaseDate) && !V.date(attrs.releaseDate)) {
                return "releaseDate"
            }


        },
        urlRoot:"/ajax/albums",
        parse:function (resp, xhr) {
            var edit = !this.tracks.models.length && !this._init;
            this._init = true;
            _(resp.tracks).each(function (attrs, index) {
                if (edit) {
                    this.tracks.add(attrs)
                } else {
                    this.tracks.at(index).set(attrs, {slient:true})
                }
            }, this)

            //this.tracks.reset(resp.tracks, {slient:true});
            return resp.album
        },
        toJSON:function () {
            var o = _.clone(this.attributes);
            if (this.isNew())o.id = 0
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
            artist:"[name='album.artist']",
            releaseDate:"[name='album.releaseDate']"


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
            this.model.on("error", this._onAttributeError, this)
            this.model.on("change:releaseDate", this._onModelAttributeChanged, this)

            this.render();

        },

        _onAttributeError:function (model, attr) {
            this._modelAttributeChanged(model, attr, true)
        },
        _onModelAttributeChanged:function (model) {
            _.each(model.changed, function (value, key) {
                this._modelAttributeChanged(model, key, false)
            }, this)
        },
        _modelAttributeChanged:function (model, attr, error) {
            var input = this.$('[name="album.' + attr + '"]')

            input.parents("div.control-group")[error ? 'addClass' : 'removeClass']("error")
        },

        _onArtUploaded:function (info) {
            var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.artUploadView.el);
            wrapper.find("img").attr("src", info.url);

            if (!this._artID) {
                this._artID = info.id;
                this.artUploadView.setPostParam("id", info.id)
            }
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