define([
// Load the original jQuery source file
    'libs/swfupload/swfupload', 'libs/swfupload/swfupload.speed'
], function ()
{
    new Speed(SWFUpload);


    SWFUpload.prototype.initSettings = function (oldInitSettings)
    {
        return function ()
        {
            if (typeof(oldInitSettings) === "function") {
                oldInitSettings.call(this);
            }

            this.refreshCookies(false);     // The false parameter must be sent since SWFUpload has not initialzed at this point
        };
    }(SWFUpload.prototype.initSettings);

    // refreshes the post_params and updates SWFUpload.  The sendToFlash parameters is optional and defaults to True
    SWFUpload.prototype.refreshCookies = function (sendToFlash)
    {
        if (sendToFlash === undefined) {
            sendToFlash = true;
        }
        sendToFlash = !!sendToFlash;

        // Get the post_params object
        var postParams = this.settings.post_params;

        // Get the cookies
        var i, cookieArray = document.cookie.split(';'), caLength = cookieArray.length, c, eqIndex, name, value;
        for (i = 0; i < caLength; i++) {
            c = cookieArray[i];

            // Left Trim spaces
            while (c.charAt(0) === " ") {
                c = c.substring(1, c.length);
            }
            eqIndex = c.indexOf("=");
            if (eqIndex > 0) {
                name = c.substring(0, eqIndex);
                value = c.substring(eqIndex + 1);
                postParams[name] = value;
            }
        }

        if (sendToFlash) {
            this.setPostParams(postParams);
        }
    };


    return SWFUpload
});