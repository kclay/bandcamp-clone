define(["binder", "backbone", "app/upload"], function (binder, Backbone, Upload) {
    var _ = require("underscore");
    var Track = Backbone.Model.extend({
        validation:{
            name:{
                required:true,
                msg:'Please enter a track name'
            }
        },
        defaults:{
            name:"",
            download:true,
            price:"1.00",
            donateMore:true,
            about:"",
            lyrics:"",
            credits:"",
            artist:"",
            art:"",
            artURL:"",
            license:"all_rights"

        },
        urlRoot:"/ajax/tracks"

    });

    var TrackEditView = Backbone.View.extend({


        _upload:null,

        bindings:{
            name:"[name='track.name']",
            download:"[name='track.download']",
            price:"[name='track.price']",
            donateMore:"[name='track.donateMore']",
            about:"[name='track.about']",
            lyrics:"[name='track.lyrics']",
            credits:"[name='track.credits']",
            artist:"[name='track.artist']",
            license:"[name='track.license']"

        },
        events:{
            "click .album-art .close":"removeArt"
        },

        initialize:function () {
            _.bindAll(this);
            this._binder = new Backbone.ModelBinder();

            this.artUploadView = new Upload.View(
                {   el:".album-art",
                    uri:"/artist/upload/art",
                    limit:"4MB",
                    types:"*.jpg;*.gif;*.png"
                }
            );

            this.artUploadView.on("uploaded", this._onArtUploaded);
            if (!this._rendered) {
                this.render();
            }

        },

        _onArtUploaded:function (url, id) {
            var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.$el.find(".album-art"));
            wrapper.find("img").attr("src", url);

            this.model.set({art:id, artURL:url});


        },
        removeArt:function () {
            this.$(".image").remove();
            this.model.set({art:"", artURL:""});
        },
        render:function () {

            this._binder.bind(this.model, this.$el, this.bindings);
//            Backbone.ModelBinding.bind(this,mo);
            //          Backbone.Validation.bind(this, {forceUpdate: true});
            return this;
        }
    })

    var TrackOverviewView = Backbone.View.extend({
        createUploadView:true,
        initialize:function () {
            _.bindAll(this);

            if (this.createUploadView) {
                this.trackUploadView = new Upload.View(
                    {el:"#track-upload",
                        uri:"/artist/upload/audio",
                        limit:"291MB",
                        types:"*.wav;*.aif;*.flac"

                    })
                this.attachUploadListeners(this.trackUploadView);

            }

            this.$title = this.$el.find(".title");
            this.$details = this.$el.find(".details");
            this.$art = this.$(".album-art");
            this.model.on("change:name change:download change:price change:donateMore change:artURL", this._onModelChange)
            this._onModelChange(this.model);

        },

        attachUploadListeners:function (view) {

            view
                .on("uploaded", this._onTrackUploaded, this)
                .on("canceled", this._onTrackCanceled, this)
                .on("stopped", this._onTrackStopped, this);
        },
        removeUploadListeners:function (view) {

            view
                .off("uploaded", this._onTrackUploaded, this)
                .off("canceled", this._onTrackCanceled, this)
                .off("stopped", this._onTrackStopped, this);
        },
        _onTrackCanceled:function () {
            this.is("canceled", true);


            this.delay(function () {
                this.is("canceled", false)
            }, this);
        },
        _onTrackStopped:function (fromDelay) {

            if (!fromDelay) {
                var self = this;
                setTimeout(function () {
                    self._onTrackStopped(true);
                }, 100);
            }
            if (this.is("canceled")) return;


        },
        _onTrackUploaded:function (name) {

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


        }
    })
    var Tracks = Backbone.Collection.extend({
        model:Track
    })


    return{
        EditView:TrackEditView,
        OverviewView:TrackOverviewView,
        Model:Track,
        Collection:Tracks
    }

})




