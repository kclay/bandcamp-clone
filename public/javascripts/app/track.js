define(["binder", "backbone", "app/upload", "app/common"], function (binder, Backbone, Upload) {
        var _ = require("underscore");
        var Common = require("app/common");
        var V = Common.Validate

        var Routes = require("app").Routes
        var Track = Backbone.Model.extend({
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
                    lyrics:"",
                    credits:"",
                    artist:"",
                    art:"",
                    artURL:"",
                    license:"all_rights",
                    status:"",
                    order:0,
                    tags:"",
                    genre_id:0,

                    session:app_config.session()


                }
            },

            validate:function (attrs, options) {
                if (!_.isEmpty(attrs.releaseDate) && !V.date(attrs.releaseDate)) {
                    return "releaseDate"
                }


            },
            capture:function () {
                this._tags = this.get("tags").split(",");
            },

            saveTags:function () {

                Routes.Ajax.saveTags("track").ajax({
                    contentType:"application/json",
                    data:JSON.stringify({
                        kind:"track",
                        items:[
                            {
                                slug:this.get("slug"),
                                tags:this._tags
                            }
                        ]

                    })
                })

            },
            urlRoot:"/ajax/tracks",
            toJSON:function () {
                var t = _.clone(this.attributes);

                if (this.isNew())t.id = 0
                return t;
            },

            refresh:function () {
                this.trigger("refresh");
                return this;
            }

        });
        var SingleTrack = Track.extend({
            toJSON:function () {
                var track = this._super("toJSON")
                return {track:track}
            },
            parse:function (resp, xhr) {
                var track = "track" in resp ? resp.track : resp;
                if (this._tags) {
                    track.tags = this._tags.join(',');
                } else {
                    var tags = _.map(track.tags, function (t) {
                        return t.name
                    });

                    track.tags = tags.join(',');
                }
                return track;
            }
        })

        var Status = {
            EVENT_CHANGED:"statusChanged",
            NEW:"new",
            UPLOADED:"uploaded",
            PROCESSING:"processing",
            COMPLETED:"processed",
            ERROR:"error"
        }
        var EditBindings = {
            name:"[name='track.name']",
            download:"[name='track.download']",
            price:"[name='track.price']",
            donateMore:"[name='track.donateMore']",
            about:"[name='track.about']",
            lyrics:"[name='track.lyrics']",
            credits:"[name='track.credits']",
            artist:"[name='track.artist']",
            license:"[name='track.license']",
            releaseDate:"[name='track.releaseDate']",
            tags:"[name='track.tags']",
            genre_id:"[name='track.genre']"

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
            hide:function () {
                this.$el.hide();
            },
            show:function () {
                this.$el.show();
            },
            update:function (name, reasons) {
                this.fileName = name;
                this.reasons = reasons;
                this.render();
            },
            render:function () {
                this.$("s").html(this.fileName)
                this.show();

            }

        })
        var TrackEditView = Backbone.View.extend({


            _upload:null,


            events:{
                "click .track-art .close":"removeArt"
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
                        uri:"/upload/art",
                        limit:"4MB",
                        types:"*.jpg;*.gif;*.png"
                    }
                );
                this.model.on("error", this._onAttributeError, this)
                this.model.on("change:releaseDate", this._onModelAttributeChanged, this)
                this.model.on("refresh", this.render, this);
                this.artUploder = new Common.ArtUploader(this.$el, this.artUploadView, this.model);

                Common.TagSelector(this.$(".tag-selector"));


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
                var binds = $.extend({}, this.options.bindings || EditBindings);
                delete binds['about'];
                this._binder.bind(this.model, this.$el, binds);
//            Backbone.ModelBinding.bind(this,mo);
                //          Backbone.Validation.bind(this, {forceUpdate: true});
                return this;
            }
        })

        var RoutesUpload = Routes.Upload;
        var TrackOverviewView = Common.OverviewView.extend({

            initialize:function (options, parent) {


                if (this.options.createUploadView) {
                    this.trackUploadView = new Upload.View(
                        {el:"#track-upload",
                            uri:"/upload/audio",
                            limit:"291MB",
                            types:"*.wav;*.aif;*.flac",
                            proxy:true

                        })
                    this.attachUploadListeners(this.trackUploadView);

                }
                this._super("initialize", [options, parent]);


            },
            init:function () {
                if (this.model.get("id")) {

                    this._encodingTrack = {
                        id:this.model.get("id")
                    }
                    if (this.model.get("file")) {
                        this.$(".file").html(this.model.get("fileName"));
                        this._onStatusChange(Status.COMPLETED);


                        this._updatePostParams(false);
                    }


                }
            },

            /**
             *
             * @param {Upload.View}
                */
            attachUploadListeners:function (view) {
                this.trackUploadView = view;
                this._updatePostParams();
                if (this._uploadCurrentFile)
                    view._currentFile = this._uploadCurrentFile;
                view
                    .on("uploaded", this._onTrackUploaded, this)
                    .on("canceled", this._onTrackCanceled, this)
                    .on("stopped", this._onTrackStopped, this)
                    .on("started", this._onTrackStarted, this)
                    .on("error", this._onTrackError, this)

            },
            /**
             *
             * @param {Upload.View}
                */
            removeUploadListeners:function (view) {
                this._updatePostParams(true);
                this.trackUploadView = null;
                if (this.trackUploadView == view) {
                    this._uploadCurrentFile = view._currentFile;
                }
                view
                    .off("uploaded", this._onTrackUploaded, this)
                    .off("canceled", this._onTrackCanceled, this)
                    .off("stopped", this._onTrackStopped, this)
                    .off("started", this._onTrackStarted, this)
                    .off("error", this._onTrackError, this)


            },
            _onTrackError:function () {
                this._onUploadError(["upload error"])
                this._onStatusChange(Status.ERROR);
            },

            _onTrackStarted:function () {
                if (this.trackUploadErrorView) {
                    this.trackUploadErrorView.hide();
                }
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
                    this._onStatusChange(Status.UPLOADED)
                    this._encodingTrack = info;


                    RoutesUpload.audioUploaded().ajax({
                        data:{
                            id:info.id,
                            session:app_config.session()
                        }
                    }).done(this._onAudioUploaded).error(this._onAudioUploaded)

                }
            },
            encodingName:function () {
                var upload = this.trackUploadView || {};
                return (this._encodingTrack || {}).name ||
                    (upload._file || upload._currentFile || {}).name

            },
            _onAudioUploaded:function (res) {

                if (!res) {

                } else if (res.id) {      // passed verify
                    this._status = null;
                    this._currentStatusId = res.id;
                    this.trackEncodingStatus(false);
                } else if (res.error) {

                    this._onUploadError(res.error)
                    // delete processing file name so the {encodingName} method can
                    // retrive the correct file name
                    delete this._encodingTrack['name'];
                }


            },

            _onUploadError:function (errors) {
                if (!this.trackUploadErrorView) {
                    this.trackUploadErrorView = new TrackErrorView({
                        fileName:this.encodingName(),
                        reasons:errors
                    })
                    this.trackUploadView.$(".progress-wrapper").after(this.trackUploadErrorView.el);
                } else {
                    this.trackUploadErrorView.update(this.encodingName(), errors)
                }
            },

            trackEncodingStatus:function (delay) {
                var self = this;
                setTimeout(function () {
                    RoutesUpload.status().ajax({
                        data:{
                            ids:{0:self._currentStatusId }
                        }
                    }).done(self._onEncodingStatus).error(self._onEncodingStatus)
                }, ( delay ? 5 : 0) * 1000);
            },
            statusId:function () {
                return (this._currentStatusId || "").split("-")[1];
            },
            _onEncodingStatus:function (status) {
                if (!status)return;
                var enc = status.encodings;

                var info = enc[this.statusId()];
                switch (info.status) {
                    case "new":
                    case "processing":
                        if (!this._status) {


                            this._onStatusChange(Status.PROCESSING);
                            this._status = status;
                        }
                        this.trackEncodingStatus(true)
                        break;


                    case "error":
                        this._onStatusChange(Status.ERROR);
                        break;
                    case "completed":
                        this._onStatusChange(Status.COMPLETED);
                        this.model.set({"file":this._encodingTrack.id, duration:info.duration, fileName:this.encodingName()})

                        this._updatePostParams(false);
                        break;
                    default:
                        // TODO : Notify of error

                        break;
                }

            },
            _updatePostParams:function (remove) {
                if (this.trackUploadView) {
                    var value = remove ? null : (this._encodingTrack ? this._encodingTrack.id : null);
                    this.trackUploadView.setPostParam("id", value)
                }
            },
            _onStatusChange:function (status) {
                if (this.trackUploadView) {
                    this.trackUploadView._onStatusChange(status)
                } else {
                    this.$(".progress-wrapper")
                        .removeClass()
                        .addClass("progress-wrapper active " + status)
                        .find(".status").text(status)


                }
                this.trigger(Status.EVENT_CHANGED, status)
            },

            render:function () {
                return this;
            }
        })
        var Tracks = Backbone.Collection.extend({
            model:Track,
            capture:function () {
                _(this.models).each(function (track) {

                    track.capture();
                })
            },
            toJSON:function () {
                var data = [];
                _(this.models).each(function (model, i) {

                    data.push(model.toJSON());
                })
                return data;
            }
        })


        return{
            EditView:TrackEditView,
            OverviewView:TrackOverviewView,
            Model:Track,
            SingleTrack:SingleTrack,
            Collection:Tracks,
            Bindings:EditBindings,
            Status:Status
        }

    }
)




