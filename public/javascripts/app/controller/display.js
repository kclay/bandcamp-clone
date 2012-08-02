define(["underscore", "backbone", "jwplayer", "app/common"], function (_, Backbone) {


    var View = Backbone.View.extend({
        el:".display",
        events:{
            "click .track-title-column a":"play"
        },

        initialize:function () {


            this.player = new Player({
                files:window.app_config.playlist
            });
        },
        play:function (event) {
            var index = parseInt($(event.currentTarget).parents("tr").attr("data-track"), 10)

            this.player.item(index);
            return false;
        }

    })

    var Player = Backbone.View.extend({
        el:"#player",
        events:{
            "click a.play":"play",
            "click a.pause":"pause",
            "click a.next":"next",
            "click a.prev":"prev",
            "click .thumb":"startDrag"
        },
        initialize:function (options) {
            this.player = jwplayer('player-proxy-inner');
            this.player.setup({
                'flashplayer':'/assets/player.swf',


                'controlbar':'bottom',
                'width':'470',
                'height':'24'
            });
            this.player
                .onTime(_.bind(this._onTime, this))
                .onPause(_.bind(this._onPause, this))
                .onReady(_.bind(this._onReady, this))


            this.$action = this.$(".action");
            this.$slider = this.$(".slider").slider({
                slide:_.bind(this._onSliding, this),
                stop:_.bind(this._onSlideStop, this)
            });
            this.s = this.$slider.slider;
        },
        _onReady:function () {
            this.debug(this.options.files);
            this.player.load(this.options.files);
        },
        _onSliding:function (event, ui) {

        },
        _onSlideStop:function (event, ui) {

        },
        _onPause:function (event) {
            this.$action.removeClass("").addClass("action play");
        },

        _onPlay:function (event) {
            this.$action.removeClass("").addClass("action pause");

        },
        _onTime:function (event) {
            this.s("value", event.position);
        },
        item:function (index) {
            this.player.playlistItem(index);
        },
        play:function () {
            this.player.play();
        },
        pause:function () {
            this.player.pause();
        },
        next:function () {
            this.player.playlistNext();
        },
        prev:function () {
            this.player.playlistPrev()
        },
        seek:function () {

        },
        startDrag:function (e) {

        },
        _format:function (seconds) {
            var mins = Math.floor(seconds / 60)
        },
        render:function (position) {
            position = position || this.s("value");

            this.$time.text()
        }

    })
    return {
        View:View
    }
})