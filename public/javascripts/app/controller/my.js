define(["underscore", "backbone", "app/common", "app"], function (_, Backbone) {

    var Common = require("app/common");
    var Routes = require("app").Routes;
    var View = Backbone.View.extend({
        el:"#content",
        events:{
            "click a.delete":"confirmDelete"
        },
        initialize:function (options) {
            this.album = (options || {}).album;
        },
        confirmDelete:function (e) {
            var $li = $(e.currentTarget).parents("li");
            var slug = $li.attr("data-slug")
            var deleteAlbum = this.album;
            new Common.ConfirmView({
                data:{
                    title:deleteAlbum ? "Delete Album" : "Delete Track",
                    message:"Are you sure you want to delete this " + (deleteAlbum ? "album" : "track") + "? This action cannot be undone."
                },
                callback:function (ok) {
                    if (ok) {
                        Routes.Ajax[deleteAlbum ? "deleteAlbum" : "deleteTrack"](slug).ajax({

                                success:function (resp) {
                                    if (resp.ok) {
                                        $li.fadeOut("slow", function () {
                                            $li.remove();
                                        })
                                    }
                                }
                            }
                        )


                    }
                }
            })
            return false;
        }


    })


    return {
        View:View
    }


});