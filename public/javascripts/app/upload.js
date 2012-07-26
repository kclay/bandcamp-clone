define(["backbone", "swfupload", "underscore"], function (Backbone, SWFUpload, _) {

    var html5 = false;
    try {
        var xhr = new XMLHttpRequest();
        html5 = !!(xhr && ('upload' in xhr) && ('onprogress' in xhr.upload));
    } catch (e) {

    }
    var UploadView = Backbone.View.extend({


        _file:null,
        _currentFile:null,
        events:{
            'click .cancel':"cancelUpload",
            'click .remove':"removeFile"
        },
        initialize:function (options) {


            options = options || {};
            var button = this.$el.find(options.replace ? ".remove" : ".upload-button").click(function () {
                return false;
            });

            button.html("<span class='trigger'>" + button.html() + "</span>");

            var hit = $("<span class='uploader'></span>").appendTo(button);
            if (button.width() == 0) {
                var self = this;
                // allow for css redraw
                setTimeout(function () {
                    self.finalize(options, button, hit);
                }, 2);
            } else {
                this.finalize(options, button, hit);
            }


        },
        _createButton:function () {

            return button;
        },
        finalize:function (options, button, hit) {
            console.log(button.width());

            this.swf = new SWFUpload({
                upload_url:options.uri,
                post_params:{
                    token:app_config.token

                },
                flash_url:"/assets/swfupload.swf",
                file_size_limit:options.limit || "4 MB",
                file_types:options.types || "*",
                button_window_mode:SWFUpload.WINDOW_MODE.TRANSPARENT,
                button_cursor:SWFUpload.CURSOR,

                upload_start_handler:_.bind(this._onUploadStarted, this),
                upload_progress_handler:_.bind(this._onUploadProgress, this),
                upload_success_handler:_.bind(this._onUploadSuccess, this),
                upload_complete_handler:_.bind(this._onUploadComplete, this),
                upload_error_handler:_.bind(this._onUploadError, this),
                file_dialog_complete_handler:_.bind(this._onDialogComplete, this),


                button_width:button.width(),
                button_height:button.height(),
                button_text:"",
                debug:true,
                debug_handler:function () {
                    console.log(arguments);
                },
                button_placeholder:hit[0]



            });
            this.attachElements();


            this.bindTo(this)
        },
        attachElements:function () {
            this.$progress = this.options.progressSelector ? $(this.options.progressSelector) : this.$(".upload-progress");
            this.$percent = this.$progress.find(".percent");
            this.$duration = this.$progress.find(".duration");
        },

        remove:function () {

            this.swf.destroy();
            return this;
        },
        bindTo:function (view) {
            this.setElement(view.el, true);
            console.log(view.$el);
            this.$wrapper = this.$(".progress-wrapper");
            this.$bar = this.$(".bar");


            this.$status = this.$(".status");
            this.$cancel = this.$(".cancel");
            this.$remove = this.$(".remove");
            this.$file = this.$(".file");
            this.$hit = this.$(".hit");

        },
        _onDialogComplete:function (numFilesSelected) {

            if (numFilesSelected == 1) {
                this.trigger("beforeStarted", []);
                this._file = null;
                this.render();
                this.$hit.hide();

                this.$progress.show();
                this.swf.startUpload();

            }

        },
        cancelUpload:function () {
            if (this._file) {
                this.swf.stopUpload();
                this.swf.cancelUpload();
            }
            this.$progress.delay(200).fadeOut("slow");

        },
        removeFile:function () {


        },
        _onUploadError:function (file, errorCode) {
            switch (errorCode) {
                case -290:
                    this.trigger("stopped");

                    break;
                case -280:
                    if (!this._currentFile) {
                        this.$hit.show("slow");
                        //this.$progress.hide("");
                        this.$wrapper.hide("slow");
                    } else {
                        this._onStatusChange("finished", this._currentFile);
                    }
                    this.trigger("canceled");
                    break;
            }
            this._file = null;
        },
        reset:function () {
            //this._file = null;
            this._render();
            this.$wrapper.hide();
            this.$progress.hide();
        },
        _onUploadStarted:function (file) {

            this._onStatusChange("uploading")


            this._file = file;

            this.$file.html(file.name);


            this.$progress.delay(500).fadeIn("slow");
            this.$percent.html("0%");
            this.$duration.html("--:--");

            this.render()
            this.trigger("started");
        },
        _onUploadProgress:function (file, bytes, total) {

            this._file = file;
            this.render()
        },
        _onUploadSuccess:function (file, serverData, receivedResponse) {
            this._file = file;
            this.$progress.fadeOut();
            this._onStatusChange('uploaded', file);

            //this.$remove.show();


            if (serverData) {
                var info = $.parseJSON(serverData);


                this.trigger("uploaded", info);


            }


        },
        _onStatusChange:function (status, file) {
            if (file) {
                this._currentFile = file;
                this.$file.html(this._currentFile.name);
            }
            this.$wrapper.removeClass().addClass("progress-wrapper active " + status);
            this.$wrapper.find(".status").text(status);
            return this;
        },
        _onUploadDone:function (file) {


        },

        _onUploadComplete:function (file) {
            this._file = file;

        },
        render:function () {

            this._render(this._file);

        },
        _render:function (file) {
            var percent = file ? file.percentUploaded.toFixed(2) : 0;
            var remaining = file ? SWFUpload.speed.formatTime(file.timeRemaining) : "--:--";


            this.$bar.css({width:percent + "%"})
            this.$percent.html(percent);

            this.$duration.html(remaining);
        }

    })

    if (html5) {
        UploadView = UploadView.extend({
            cancelUpload:function () {
                this.$input.trigger("html5_upload.cancelOne");
                this._onUploadError(this._file, SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED)
                this._onUploadError(this._file, SWFUpload.UPLOAD_ERROR.FILE_CANCELLED);
            },
            finalize:function (options, button, hit) {
                var self = this;
                SWFUpload.prototype.initSWFUpload = function () {
                }
                SWFUpload.prototype.startUpload = function () {
                };
                var swf = this.swf = new SWFUpload();
                var input = this.$input = $("<input type='file'/>").appendTo(this.el).css({position:"absolute", "left":"-999999px"})
                swf.fileSpeedStats = {};
                swf.speedSettings = {};
                swf.settings = {moving_average_history_size:10}
                button.click(function () {
                    input.click();
                    return false;
                })
                options.types = _((options.types || "").split(";")).map(function (value, index) {
                    return value.replace("*.", "");
                })
                var _file = {};

                function extend() {
                    _file = SWFUpload.speed.extendFile(_file, swf.fileSpeedStats);
                }


                input.html5_upload({
                    fieldName:"Filedata",
                    url:options.uri,
                    extraFields:{
                        token:app_config.token

                    },

                    sendBoundary:window.FormData || $.browser.mozilla,
                    onStartOne:function (event, file, name, number, total) {
                        _file = self._file = {
                            name:name,
                            id:(new Date().getTime()),
                            size:file.size || file.fileSize
                        }
                        if (options.types.length) {
                            if (!options.types.indexOf(name.split(".").pop()) == -1) {
                                self._onUploadError(_file, SWFUpload.QUEUE_ERROR.INVALID_FILETYPE);
                                return false;
                            }

                        }

                        self._onDialogComplete(total);

                        setTimeout(function () {
                            extend();

                            self._onUploadStarted(_file);
                        }, 2);
                        return true;

                    },

                    onProgress:function (event, progress, name, number, total, res) {

                        swf.updateTracking(_file, res.loaded);
                        extend();
                        self._onUploadProgress(_file);


                    },
                    setName:function (text) {
                        // $("#progress_report_name").text(text);
                    },
                    setStatus:function (text) {
                        //$("#progress_report_status").text(text);
                    },
                    setProgress:function (val) {
                        //$("#progress_report_bar").css('width', Math.ceil(val * 100) + "%");
                    },
                    onFinishOne:function (event, response, name, number, total) {
                        self._onUploadSuccess(_file, response);
                    },
                    onError:function (event, name, error) {
                        //alert('error while uploading file ' + name);
                        self._onUploadError();
                    }
                });
                this.attachElements();


                this.bindTo(this)
            }
        })
    }


    var ReplaceUploadView = UploadView.extend({
        events:{

        },
        initialize:function (options, uploadView, mainView) {
            this.mainView = mainView;
            this.uploadView = uploadView;
            options = $.extend({}, options, {replace:true});
            this.proxy();
            this._super("initialize", [options]);


        },
        proxy:function () {
            var methods = ['reset', 'render', '_onUploadProgress',
                '_onUploadSuccess', '_onUploadStarted', '_onUploadError', '_onDialogComplete'];
            var self = this;
            var uploadView = this.uploadView;
            var mainView = this.mainView;
            var swf = self.swf;

            function proxy(method) {
                return function () {
                    mainView._active = true;
                    uploadView.swf = self.swf;
                    var results = method.apply(uploadView, arguments);
                    uploadView.swf = swf;
                    mainView._active = false;
                    return results;
                }
            }

            // remove events
            uploadView.undelegateEvents();
            _(methods).each(function (value, key) {


                self[value] = proxy(self[value])
            });
            uploadView.cancelUpload = proxy(uploadView.cancelUpload);
            // reapply events
            uploadView.delegateEvents();
        }
    })

    return{
        View:UploadView,
        ReplaceView:ReplaceUploadView

    }
})
;

