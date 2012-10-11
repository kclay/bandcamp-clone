define(["binder", "backbone", "app/upload", "app/common", "app/track"], function (binder, Backbone, Upload) {


    var _ = require("underscore");
    var Common = require("app/common");


    var Routes = require("app").Routes
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
        defaults:function () {
            return {
                id:null,
                artist_id:0,
                name:"",
                download:true,
                price:"1.00",
                donateMore:true,
                about:"",

                credits:"",
                artistName:"",
                art:"",
                artURL:"",
                releaseDate:"",
                tags:"",
                session:app_config.session()


            }
        },

        capture:function () {
            this._tags = this.get("tags").split(",")
            this.tracks.capture();

        },
        saveTags:function () {

            var tags = [
                {slug:this.get("slug"), "tags":this._tags}
            ];
            var trackTags = _(this.tracks.models).map(function (track) {

                return {slug:track.get("slug"), tags:track._tags}
            });
            tags = [].concat(tags, trackTags);
            Routes.Ajax.saveTags().ajax({
                contentType:"application/json",
                data:JSON.stringify({
                    kind:"album",
                    items:tags

                })
            })
        },
        refresh:function () {
            this.trigger("refresh");
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
            var track = new Track.SingleTrack()
            _(resp.tracks).each(function (attrs, index) {
                if (edit) {
                    this.tracks.add(track.parse(attrs))
                } else {
                    this.tracks.at(index).set(track.parse(attrs), {slient:true})
                }
            }, this)

            var album = resp.album
            if (this._tags) {
                album.tags = this._tags.join(',');
            } else {
                var tags = _.map(album.tags, function (t) {
                    return t.name
                });

                album.tags = tags.join(',');
            }

            //this.tracks.reset(resp.tracks, {slient:true});
            return album
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
            artistName:"[name='album.artistName']",
            releaseDate:"[name='album.releaseDate']",
            tags:"[name='album.tags']"


        },
        events:{
            "click .album-art .close":"removeArt"
        },

        initialize:function () {
            _.bindAll(this);
            this._binder = new Backbone.ModelBinder();

            this.artUploadView = new Upload.View(
                {   el:this.$(".art-upload"),
                    uri:"/upload/art",
                    limit:"4MB",
                    types:"*.jpg;*.gif;*.png"
                }
            );

            this.artUploder = new Common.ArtUploader(this.$el, this.artUploadView, this.model);
            this.model.on("error", this._onAttributeError, this)
            this.model.on("change:releaseDate", this._onModelAttributeChanged, this)
            Common.TagSelector(this.$(".tag-selector"));
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