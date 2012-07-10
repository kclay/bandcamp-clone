define(["backbone", "swfupload", "underscore"], function (Backbone, SWFUpload, _)
{


    var UploadView = Backbone.View.extend({


        _file:null,
        _currentFile:null,
        events:{
            'click .cancel':"cancelUpload",
            'click .remove':"removeFile"
        },
        initialize:function (options)
        {


            var button = this.$el.find(".upload-button").click(function ()
            {
                return false;
            });
            button.html("<span class='trigger'>" + button.html() + "</span>");
            var hit = $("<span class='uploader'></span>").appendTo(button);
            this.swf = new SWFUpload({
                upload_url:options.uri + "/" + app_config.token,
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
                debug_handler:function ()
                {
                    console.log(arguments);
                },
                button_placeholder:hit[0]



            });
            this.$progress = $(this.options.progressSelector ? this.options.progressSelector : ".upload-progress");
            this.$percent = this.$progress.find(".percent");
            this.$duration = this.$progress.find(".duration");


            this.bindTo(this)

        },

        bindTo:function (view)
        {
            this.setElement(view.el, true);
            this.$wrapper = this.$(".progress-wrapper");
            this.$bar = this.$(".bar");


            this.$status = this.$(".status");
            this.$cancel = this.$(".cancel");
            this.$remove = this.$(".remove");
            this.$file = this.$(".file");
            this.$hit = this.$(".hit");

        },
        _onDialogComplete:function (numFilesSelected)
        {
            if (numFilesSelected == 1) {
                this.trigger("beforeStarted")
                this.render();
                this.$hit.hide();
                this.$progress.show();
                this.swf.startUpload();
                this.trigger("started");
            }

        },
        cancelUpload:function ()
        {
            if (this._file) {
                this.swf.stopUpload();
                this.swf.cancelUpload();
            }
            this.$progress.delay(200).fadeOut("slow");

        },
        removeFile:function ()
        {


        },
        _onUploadError:function (file, errorCode, message)
        {
            switch (errorCode) {
                case -290:
                    this.trigger("stopped");

                    break;
                case -280:
                    if (!this._currentFile) {
                        this.$hit.show("slow");
                        //this.$progress.hide("");
                        this.$wrapper.hide("slow");
                    }
                    this.trigger("canceled");
                    break;
            }
            this._file = null;
        },
        _onUploadStarted:function (file)
        {
            this.$wrapper.show();
            this._file = file;

            this.$file.html(file.name);
            this.$status.addClass("uploading");


            this.$progress.delay(500).fadeIn("slow");
            this.$percent.html("0%");
            this.$duration.html("--:--");

            this.render()
        },
        _onUploadProgress:function (file, bytes, total)
        {

            this._file = file;
            this.render()
        },
        _onUploadSuccess:function (file, serverData, receivedResponse)
        {
            this._file = file;
            this.$progress.hide();
            this.$cancel.hide();
            this.$remove.show();
            if (serverData) {
                var info = serverData.split("|");
                var created = info.shift()
                if (created) {

                    this.trigger.apply(this, ["uploaded"].concat(info));
                }

            }
            this._currentFile = file;

        },
        _onUploadComplete:function (file)
        {
            this._file = file;

        },
        render:function ()
        {
            var percent = this._file ? this._file.percentUploaded.toFixed(2) : 0;
            var remaining = this._file ? SWFUpload.speed.formatTime(this._file.timeRemaining) : "--:--";


            this.$bar.css({width:percent + "%"})
            this.$percent.html(percent);

            this.$duration.html(remaining);


        }

    })

    return{
        View:UploadView
    }
});

