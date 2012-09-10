/**
 * Created with IntelliJ IDEA.
 * User: Keyston
 * Date: 6/21/12
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
define(["backbone", "app/common"], function (Backbone) {


    var Common = require("app/common");
    var View = Backbone.View.extend({


        initialize:function () {

            var hints = {"6":"", "11":"", "22":"e.g., roots, dub, dancehall, ska", "7":"", "12":"", "23":"e.g., emo, glam, hard rock, surf", "8":"", "13":"e.g., go-go, funk rock", "24":"", "9":"", "14":"e.g., underground, instrumental, freestyle, nerdcore", "25":"", "15":"e.g., fusion, big band, swing", "26":"", "16":"", "1":"", "17":"e.g., salsa, rock en espa\u00f1ol, reggaet\u00f3n, cumbia", "2":"e.g., math rock, grunge", "18":"e.g., black metal, death metal, grindcore", "3":"e.g., dark ambient, drone, shoegaze", "19":"e.g., power pop, electropop", "20":"e.g., garage, glam, hardcore", "4":"e.g., delta blues, boogie-woogie", "10":"e.g., house, techno, dubstep, ambient", "21":"e.g., contemporary R&B, neo soul, nu-jazz", "5":""};
            $("#genre").change(function () {
                var val = $(this).val();

                var hint = hints[val];

                $("#tags").attr("placeholder", hint).attr("title", hint);


            })
            Common.TagSelector("#tags");


        }
    })


    return {
        View:View

    }
})