define(["backbone", "app/upload"], function (Backbone, Upload)
{

    var LeftPanel = Backbone.View.extend({
        id:""
    })


    var AudioUploadView = Upload.View.extend({
        el:"#track-upload",
        initialize:function ()
        {
            this.constructor.__super__.initialize.call(this, "/artist/upload/audio", {
                limit:"291MB",
                types:"*.wav;*.aif;*.flac"

            });
            this.$percent = this.$el.find(".percent");
            this.$duration = this.$el.find(".duration");
        },
        _onDialogComplete:function (file)
        {
            this._super("_onDialogComplete");
            this.$el.find(".hit").hide();
            this.$el.find(".upload-progress").show();
        },
        render:function ()
        {
            this._super("render");
            this.$percent.html(SWFUpload.speed.formatPercent(this._file.percentUploaded));
            this.$duration.html(SWFUpload.speed.formatTime(this._file.timeRemaining));

        }

    })

    return{
        AudioUpload:AudioUploadView
    }


})