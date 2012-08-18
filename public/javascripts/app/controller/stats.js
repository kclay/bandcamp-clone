/**
 * Created with IntelliJ IDEA.
 * User: Keyston
 * Date: 8/18/12
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
define("underscore", "backbone", "app/common", function (_, Backbone, Common) {

    var Routes = require("app").Routes;

    var View = Backbone.View.extend({

        initialize:function () {

            new Common.LoadingView()
            this.load()
        },
        load:function (stats) {
            var view = new Common.LoadingView();


        },
        play:function (range) {
            Stats.fetch.Play(range)
        }

    })
})