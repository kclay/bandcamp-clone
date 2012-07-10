define(["binder", "backbone", "app/upload"], function (binder, Backbone, Upload) {


    var _ = require("underscore");
    var Album = Backbone.Model.extend({
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

            credits:"",
            artist:"",
            art:"",
            artURL:""


        },
        urlRoot:"/ajax/albums"

    });

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
                {   el:".album-art",
                    uri:"/artist/upload/art",
                    limit:"4MB",
                    types:"*.jpg;*.gif;*.png"
                }
            );

            this.artUploadView.on("uploaded", this._onArtUploaded);

            this.render();

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

            return this;
        }
    })


    return{
        View:AlbumEditView,
        Model:Album
    }
})