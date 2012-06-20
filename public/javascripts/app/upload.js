define(["backbone", "swfupload", "underscore"], function (Backbone, SWFUpload, _)
{


    var UploadView = Backbone.View.extend({


        _file:null,
        _currentFile:null,
        events:{
            'click .cancel':"cancel"
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
                upload_url:options.uri + "?token=" + $("#token").val(),
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
            this.$wrapper = this.$el.find(".progress-wrapper");
            this.$bar = this.$el.find(".bar");

            this.$percent = this.$el.find(".percent");
            this.$duration = this.$el.find(".duration");
            this.$progress = this.$el.find(".upload-progress");
            this.$status = this.$el.find(".status");


        },
        _onDialogComplete:function ()
        {
            this.$el.find(".hit").hide();
            this.$el.find(".upload-progress").show();
            this.swf.startUpload();

        },
        cancel:function ()
        {
            this.swf.stopUpload();
            this.swf.cancelUpload();
            this.$progress.delay(200).fadeOut("slow");
            return false;
        },
        _onUploadError:function ()
        {

        },
        _onUploadStarted:function (file)
        {
            this.$wrapper.show();
            this._file = file;

            this.$el.find('.file').html(file.name);
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
            var percent = this._file.percentUploaded.toFixed(2);


            this.$bar.css({width:percent + "%"})
            this.$percent.html(percent);
            this.log(this._file.timeRemaining);
            this.$duration.html(SWFUpload.speed.formatTime(this._file.timeRemaining));


        }

    })

    return{
        View:UploadView
    }
});

