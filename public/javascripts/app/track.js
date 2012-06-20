define(["binder", "backbone", "app/upload"], function (binder, Backbone, Upload)
{
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
            license:"all_rights"

        }
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
            var wrapper = $("<div class='image'><img/><span class='close'></span>").prependTo(this.$el.find(".album-art"));
            wrapper.find("img").attr("src", url);

            this.$el.find("[name='track.art']").val(id);
        },
        render:function ()
        {

            this._binder.bind(this.model, this.$el, this.bindings);
            Backbone.ModelBinding.bind(this,mo);
            Backbone.Validation.bind(this, {forceUpdate: true});
        }
    })

    var TrackView = Backbone.View.extend({

        initialize:function ()
        {
            _.bindAll(this);

            this.$title = this.$el.find(".title");
            this.$details = this.$el.find(".details");
            this.model.on("change:name change:download change:price change:donateMore", this._onModelChange)
            this._onModelChange();
        },
        _onModelChange:function ()
        {

            var title = this.model.get("name") || "Untitled Track";
            this.$title.html(title);


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




