define(["binder", "backbone", "app/upload"], function (binder, Backbone, Upload)
{
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

        initialize:function ()
        {
            _.bindAll(this);
            this._binder = new Backbone.ModelBinder();
            this.trackUploadView = new Upload.View(
                {el:"#track-upload",
                    uri:"/artist/upload/audio",
                    limit:"291MB",
                    types:"*.wav;*.aif;*.flac"

                })

            this.trackUploadView.on("uploaded", this._onTrackUploaded);
            this.artUploadView = new Upload.View(
                {   el:".album-art",
                    uri:"/artist/upload/art",
                    limit:"4MB",
                    types:"*.jpg;*.gif;*.png"
                }
            );

            this.artUploadView.on("uploaded", this._onArtUploaded);
            this.render();

        },
        _onTrackUploaded:function (name)
        {

        },
        _onArtUploaded:function (url, id)
        {
            var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.$el.find(".album-art"));
            wrapper.find("img").attr("src", url);

            this.model.set({art:id, artURL:url});


        },
        removeArt:function ()
        {
            this.$(".image").remove();
            this.model.set({art:"", artURL:""});
        },
        render:function ()
        {

            this._binder.bind(this.model, this.$el, this.bindings);
//            Backbone.ModelBinding.bind(this,mo);
            //          Backbone.Validation.bind(this, {forceUpdate: true});
        }
    })

    var TrackView = Backbone.View.extend({

        initialize:function ()
        {
            _.bindAll(this);

            this.$title = this.$el.find(".title");
            this.$details = this.$el.find(".details");
            this.$art = this.$(".album-art");
            this.model.on("change:name change:download change:price change:donateMore change:artURL", this._onModelChange)

        },
        _onModelChange:function (model, info)
        {

            if (!_.isObject(info))return;
            if ("name" in info.changes) {
                var title = this.model.get("name") || "Untitled Track";
                this.$title.html(title);
            } else if ("artURL" in info.changes) {

                this.$art.find("img").src(this.model.get("artURL"));
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
        TrackView:TrackView,
        Model:Track,
        Collection:Tracks
    }

})




