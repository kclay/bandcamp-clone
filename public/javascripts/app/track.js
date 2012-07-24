define(["binder", "backbone", "app/upload", "app/common"], function (binder, Backbone, Upload) {
        var _ = require("underscore");
        var Common = require("app/common");
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
            urlRoot:"/ajax/tracks",
            toJSON:function () {
                var t = _.clone(this.attributes);
                delete t["artURL"];
                return t;
            }

        });

        var EditBindings = {
            name:"[name='track.name']",
            download:"[name='track.download']",
            price:"[name='track.price']",
            donateMore:"[name='track.donateMore']",
            about:"[name='track.about']",
            lyrics:"[name='track.lyrics']",
            credits:"[name='track.credits']",
            artist:"[name='track.artist']",
            license:"[name='track.license']"

        }

        var TrackErrorView = Backbone.View.extend({
            tagName:"div",
            className:"track-upload-error",
            events:{
                "click a":"showError"
            },
            initialize:function (options) {

                $("<s></s><a href='#'>upload error</a>").appendTo(this.el)
                this.update(options.fileName, options.reasons);

            },
            showError:function () {
                new Common.FeedbackView({
                    data:{
                        title:"Encoding Error",
                        message:this.reasons.join("<br/>")
                    }
                })
            },
            update:function (name, reasons) {
                this.fileName = name;
                this.reasons = reasons;
                this.render();
            },
            render:function () {
                this.$("s").html(this.fileName)

            }

        })
        var TrackEditView = Backbone.View.extend({


            _upload:null,


            events:{
                "click .album-art .close":"removeArt"
            },

            initialize:function () {
                _.bindAll(this);
                this._binder = new Backbone.ModelBinder();


                if (!this._rendered) {
                    this.render();
                }

            },
            remove:function () {
                this.artUploadView.remove();
                return this;
            },
            init:function () {
                this.artUploadView = new Upload.View(
                    {   el:this.$(".track-art"),
                        uri:"/artist/upload/art",
                        limit:"4MB",
                        types:"*.jpg;*.gif;*.png"
                    }
                );

                this.artUploadView.on("uploaded", this._onArtUploaded);
            },

            _onArtUploaded:function (info) {
                var wrapper = $("<div class='image'><img/><i class='close icon-remove'></i></div>").prependTo(this.$el.find(".album-art"));
                wrapper.find("img").attr("src", info.url);

                this.model.set({art:info.id, artURL:info.url});


            },
            removeArt:function () {
                this.$(".image").remove();
                this.model.set({art:"", artURL:""});
            },
            render:function () {

                this._binder.bind(this.model, this.$el, this.options.bindings || EditBindings);
//            Backbone.ModelBinding.bind(this,mo);
                //          Backbone.Validation.bind(this, {forceUpdate: true});
                return this;
            }
        })

        var Routes = jsRoutes.controllers.Upload;
        var TrackOverviewView = Common.OverviewView.extend({

            initialize:function () {


                if (this.options.createUploadView) {
                    this.trackUploadView = new Upload.View(
                        {el:"#track-upload",
                            uri:"/artist/upload/audio",
                            limit:"291MB",
                            types:"*.wav;*.aif;*.flac"

                        })
                    this.attachUploadListeners(this.trackUploadView);

                }
                this._super("initialize");


            },

            attachUploadListeners:function (view) {
                this.trackUploadView = view;
                view
                    .on("uploaded", this._onTrackUploaded, this)
                    .on("canceled", this._onTrackCanceled, this)
                    .on("stopped", this._onTrackStopped, this);
            },
            removeUploadListeners:function (view) {
                this.trackUploadView = null;
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
            _onTrackUploaded:function (info) {

                if (info.error) {

                } else {
                    this._encodingTrack = info;
                    this.trackUploadView.$(".progress").hide();
                    Routes.audioUploaded().ajax({
                        data:{
                            id:info.id
                        }
                    }).done(this._onAudioUploaded).error(this._onAudioUploaded)

                }
            },
            encodingName:function () {
                return this._encodingTrack.name;
            },
            _onAudioUploaded:function (res) {

                if (!res) {

                } else if (res.id) {      // passed verify
                    this._status = null;
                    this._currentStatusId = res.id;
                    this.trackEncodingStatus();
                } else if (res.error) {
                    if (!this.trackUploadErrorView) {
                        this.trackUploadErrorView = new TrackErrorView({
                            fileName:this.encodingName(),
                            reasons:res.error
                        })
                        this.trackUploadView.$(".progress-wrapper").after(this.trackUploadErrorView.el);
                    } else {
                        this.trackUploadErrorView.update(this.encodingName(), res.error)
                    }


                }


            },
            trackEncodingStatus:function (delay) {
                var self = this;
                setTimeout(function () {
                    Routes.status().ajax({
                        data:{
                            ids:{0:self._currentStatusId }
                        }
                    }).done(self._onEncodingStatus).error(self._onEncodingStatus)
                }, delay ? 2 : 0);
            },
            _onEncodingStatus:function (status) {
                if (!status)return;
                var enc = status.encodings;

                var status = enc[this._currentStatusId];
                switch (status) {
                    case "new":
                    case "processing":
                        if (!this._status) {
                            this.trackUploadView.$(".status")
                                .removeClass("uploading")
                                .addClass("processing")
                                .html("processing")
                            this._status = status;
                        }
                        this.trackEncodingStatus(true)
                        break;


                    case "error":
                        break;
                    case "completed":
                        break;
                }

            },

            render:function () {
                return this;
            }
        })
        var Tracks = Backbone.Collection.extend({
            model:Track,
            toJSON:function () {
                var json = {};
                _(this.models).each(function (model, i) {
                    json["tracks[" + i + "]"] = model.toJSON()
                })
                return json;
            }
        })


        return{
            EditView:TrackEditView,
            OverviewView:TrackOverviewView,
            Model:Track,
            Collection:Tracks,
            Bindings:EditBindings
        }

    }
)




