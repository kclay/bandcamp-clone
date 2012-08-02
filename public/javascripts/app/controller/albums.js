define(["underscore", "backbone", "app/common", "app"], function (_, Backbone) {

    var Common = require("app/common");
    var Routes = require("app").Routes;
    var View = Backbone.View.extend({
        el:"#content",
        events:{
            "click a.delete":"confirmDelete"
        },
        confirmDelete:function (e) {
            var $li = $(e.currentTarget).parents("li");
            var album = $li.attr("data-slug")
            new Common.ConfirmView({
                data:{
                    title:"Delete Album",
                    message:"Are you sure you want to delete this album? This action cannot be undone."
                },
                callback:function (ok) {
                    if (ok) {
                        Routes.Ajax.deleteAlbum(album).ajax({

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