define(["underscore", "backbone", "jwplayer", "app/common"], function (_, Backbone) {

    var Common = require("app/common");
    var App = require("app")
    var Routes = App.Routes;
    var Stats = App.Stats


    var View = Backbone.View.extend({
        el:".display",
        events:{
            "click .track-title-column a":"play",
            "click .download":"download",

            "click tr.active .track-play-column a":"pause",
            "click tr:not(.active) .track-play-column a":"play"
        },

        initialize:function () {

            this.model = window.app_config.model;
            this.items = window.app_config.playlist;
            var player = this.player = new Player({
                files:window.app_config.playlist
            });
            player.on("change", this._onPlayerChange, this)
                .on("play", this._onFirstPlay, this)
                .on("pause", this._onPause, this);
            $("table tr").each(function () {
                new TrackView({el:this, player:player})
            })
            this._currentIndex = -1;
            this.$tracks = this.$("#track-list tr");
        },
        _onPause:function () {
            this._onPlayerChange(this._currentIndex, true);
        },
        _onFirstPlay:function (index) {
            this._init = true;
            this._currentIndex = index;
            this._onPlayerChange(index)
            this.player.off("play", this._onFirstPlay)
        },
        _onPlayerChange:function (index, pause) {

            if (!this._init)return;

            $(this.$tracks.get(this._currentIndex)).removeClass("active")
                .find(".track-play-column i")
                .removeClass().addClass("icon-play")

            if (!pause) {
                $(this.$tracks.get(index)).addClass("active")
                    .find(".track-play-column i")
                    .removeClass().addClass("icon-pause")
            }
            this._currentIndex = index;
        },
        pause:function (event) {
            this.player.pause();
        },
        play:function (event) {


            var index = this.index(event);
            if (index == this._currentIndex) {
                if (this.player.paused()) {
                    this.player.play();
                }
            } else {

                this.player.item(index);

            }

            return false;
        },
        index:function (event) {
            return parseInt($(event.currentTarget).parents("tr").attr("data-track"), 10)
        },
        item:function (event) {
            return this.items[this.index(event)];
        },
        download:function (event) {
            // var model = this.item(event)
            new DownloadView({model:this.model, type:this.model.kind, data:{
                price:this.model.price

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
            if (isNaN(price) || price && user_price < price) {
                alert("Invalid Price");
                return;
            }
            var options = {
                data:{
                    price:user_price,
                    artist_id:this.model.artist_id
                },
                dataType:"json",
                success:function (json) {
                    if (!json) {
                        alert("An unknown error has occured")
                    } else if (json.error) {
                        alert(json.error);
                    } else {
                        var url = Routes.Purchase.checkout(json.token).url;
                        window.location = url;
                    }
                },
                error:function () {
                    alert("An unknown error has occured")
                }
            }


            Routes.Purchase[this.options.type](this.model.slug).ajax(options)


        }




    })
    var Duration = 90;

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
                .onPlaylistItem(_.bind(this._onPlaylistItem, this))
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
            this._position = 0;
            this.render(0)


        },
        currentItem:function () {
            return this.items[this._currentIndex];
        },
        _onPlaylistItem:function (event) {
            this._currentIndex = event.index;
            this.trigger("change", this._currentIndex);
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
            this.trigger("pause");
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

            this._state(State.BUSY);
            this.trigger("play", index)

            this._onTrackChange();

        },
        _onTrackChange:function () {
            this._track(true);
            this.render(0);
        },
        play:function () {
            this._state(State.BUSY);
            this.player.play(true);
            this.trigger("play", this._currentIndex);
            this.trigger("change", this._currentIndex);

            this._onTrackChange();

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
        paused:function () {
            return this.player.getState() == "PAUSED";
        },

        _track:function (play) {

            var item = this.currentItem();
            var percent = (this._position * 100) / Duration;
            if (play && percent < 10) {
                Stats.track.Play(item.id, false);
                Stats.track.Skip(item.id, false);
            } else if (percent >= 90) {
                Stats.track.Complete(item.id, true);
            } else if (percent > 10) {
                Stats.track.Partial(item.id, true);
            }
        },
        render:function (position) {
            position = this._position = typeof position == "undefined" ? this.$slider.slider("value") : position;
            var item = this.currentItem();
            this.$title.text(item.title)
            this.$time.text(this._format(position) + "/" + this._format(Duration));

            var length = this.items.length;
            this.$next[length > 1 && this._currentIndex <= length - 1 ? "show" : "hide"]();

            this.$prev[this._currentIndex > 0 ? "show" : "hide"]();
            this._track();
        }

    })
    return {
        View:View
    }
})