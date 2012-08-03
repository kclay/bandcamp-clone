define(["underscore", "backbone", "jwplayer", "app/common"], function (_, Backbone) {

    var Common = require("app/common");
    var Routes = require("app").Routes
    var View = Backbone.View.extend({
        el:".display",
        events:{
            "click .track-title-column a":"play",
            "click .track-download-column a":"download"
        },

        initialize:function () {

            this.items = window.app_config.playlist;
            var player = this.player = new Player({
                files:window.app_config.playlist
            });
            $("table tr").each(function () {
                new TrackView({el:this, player:player})
            })
        },
        play:function (event) {


            this.player.item(this.index(event));
            return false;
        },
        index:function (event) {
            return parseInt($(event.currentTarget).parents("tr").attr("data-track"), 10)
        },
        item:function (event) {
            return this.items[this.index(event)];
        },
        download:function (event) {
            var model = this.item(event)
            new DownloadView({model:model, type:"track", data:{
                price:model.price

            }});
            return false;
        }


    })

    var TrackView = Backbone.View.extend({

        initialize:function (options) {
            this.index = parseInt(this.$el.attr("data-track"), 10);
            this.player = options.player;
        }


    })

    var DownloadView = Common.FeedbackView.extend({
        template:"#tpl-purchase",
        events:{
            "click #btn-purchase":"purchase"
        },
        purchase:function () {
            var user_price = parseFloat(this.$("#price").val())
            var price = this.options.price;
            if (price && user_price < price) {

                return;
            }
            var options = {
                data:{
                    price:user_price,
                    artist_id:this.model.artist_id
                },
                success:function (token) {
                    if (token == "error") {
                        alert("error");
                    } else {
                        var url = Routes.Purchase.checkout(token).url;
                        window.location = url;
                    }
                },
                error:function () {

                }
            }


            Routes.Purchase[this.options.type](this.model.slug).ajax(options)


        }




    })

    var State = {
        BUSY:"busy",
        PLAYING:"playing",
        PLAY:"play"
    }
    var Player = Backbone.View.extend({
        el:"#player",
        events:{
            "click a.play":"play",
            "click a.playing":"pause",
            "click a.next":"next",
            "click a.prev":"prev"

        },
        initialize:function (options) {
            this.player = jwplayer('player-proxy-inner');
            this.player.setup({
                'flashplayer':'/assets/player.swf',


                'controlbar':'bottom',
                'width':'470',
                'height':'24'
            });
            this.items = options.files;
            this.player
                .onTime(_.bind(this._onTime, this))
                .onPause(_.bind(this._onPause, this))
                .onReady(_.bind(this._onReady, this))
                .onPlay(_.bind(this._onPlay, this))
                .onMeta(_.bind(this._onMeta, this))
                .onBufferChange(_.bind(this._onBufferChange, this))


            this.$action = this.$(".action");
            this.$slider = this.$(".slider .bar").slider({
                slide:_.bind(this._onSliding, this),
                stop:_.bind(this._onSlideStop, this),
                start:_.bind(this._onSlideStart, this)
            });
            this._currentIndex = 0;
            this.$title = this.$(".title");
            this.$time = this.$(".time");
            this.$fill = this.$(".fill");
            this.$next = this.$(".next");
            this.$prev = this.$(".prev")
            this.render(0)


        },
        _onReady:function () {

            this.player.load(this.items);
        },
        _onSliding:function (event, ui) {
            this.render(ui.value);
        },
        _onSlideStop:function (event, ui) {
            this.seek(ui.value);
            this.play();
        },
        _onSlideStart:function (e) {
            this.pause();
        },
        _state:function (state) {
            this.$action.removeClass().addClass("action " + state);
            this.currentState = state;
        },
        _onPause:function (event) {
            this._state(State.PLAY)
        },
        _onMeta:function () {

        },
        _onPlay:function (event) {

            if (event.oldstate == "PAUSED" || event.oldstate == "BUFFERING") {
                //   this._state(State.PLAYING)
            }

        },
        _onBufferChange:function (event) {
            this.$fill.animate({width:event.bufferPercent + "%"});
        },
        _onTime:function (event) {
            if (this.currentState == State.BUSY && event.position > 1) {

                this._state(State.PLAYING);
                this.s("option", {
                    min:0,
                    max:event.duration
                })

            }
            this.$slider.slider("value", event.position);
            this.render();
        },
        item:function (index) {
            this.player.playlistItem(index);
            this._currentIndex = index;
            this._state(State.BUSY);
            this.render();
        },
        play:function () {
            this._state(State.BUSY);
            this.player.play(true);
        },
        pause:function () {
            this.player.pause(true);
        },
        next:function () {
            this.player.playlistNext();
            this.render();

        },
        prev:function () {
            this.player.playlistPrev()
            this.render();

        },

        seek:function (position) {
            this.player.seek(position)
        },

        _format:function (seconds) {
            var m = Math.floor(seconds / 60)
            var s = Math.floor(seconds % 60);

            return this.zero(m) + ":" + this.zero(s);
        },
        zero:function (nbr) {
            if (nbr < 10) {
                return '0' + nbr;
            } else {
                return '' + nbr;
            }
        },
        render:function (position) {
            position = typeof position == "undefined" ? this.$slider.slider("value") : position;
            var item = this.items[this._currentIndex];
            this.$title.text(item.title)
            this.$time.text(this._format(position) + "/" + this._format(item.duration));

            var length = this.items.length;
            this.$next[length > 1 && this._currentIndex <= length - 1 ? "show" : "hide"]();

            this.$prev[this._currentIndex > 0 ? "show" : "hide"]();
        }

    })
    return {
        View:View
    }
})