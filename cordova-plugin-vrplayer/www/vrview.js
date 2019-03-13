/*global cordova, module*/

vrview = {
    TYPE_MONO: 1,
    TYPE_STEREO_OVER_UNDER: 2,
    FORMAT_DEFAULT: 1,
    FORMAT_HLS: 2,
    FORMAT_DASH: 3,
    DISPLAY_MODE_FULLSCREEN: 2,
    DISPLAY_MODE_VR: 3,

    playVideo: function (videoUrl, userOptions) {
        var options = vrview.getOptions(userOptions);
        cordova.exec(null, null, "VrPlayer", "playVideo", [videoUrl, options]);
    },
    playVideoFromAppFolder: function (videoPath, userOptions) {
        var options = vrview.getOptions(userOptions);
        cordova.exec(null, null, "VrPlayer", "playVideoFromAppFolder", [videoPath, options]);
    },
    showPhoto: function(photoUrl, userOptions) {
        var options = vrview.getOptions(userOptions);
        cordova.exec(null, null, "VrPlayer", "showPhoto", [photoUrl, options]);
    },
    showPhotoFromAppFolder: function(photoPath, userOptions) {
        var options = vrview.getOptions(userOptions);
        cordova.exec(null, null, "VrPlayer", "showPhotoFromAppFolder", [photoPath, options]);
    },
    isDeviceSupported: function(callback) {
        cordova.exec(callback, null, "VrPlayer", "isDeviceSupported", []);
    },

    getOptions: function(userOptions) {
        var options = {
            inputType: vrview.TYPE_MONO,
            inputFormat: vrview.FORMAT_DEFAULT,
            startDisplayMode: vrview.DISPLAY_MODE_FULLSCREEN
        };

        if(userOptions !== undefined) {
            if(userOptions.inputType !== undefined) {
                var inputType = vrview.getTypeFromString(userOptions.inputType);
                if(inputType !== undefined) { options.inputType = inputType; }
            }
            if(userOptions.inputFormat !== undefined) {
                var inputFormat = vrview.getFormatFromString(userOptions.inputFormat);
                if(inputFormat !== undefined) { options.inputFormat = inputFormat; }
            }
            if(userOptions.startDisplayMode !== undefined) {
                var startDisplayMode = vrview.getDisplayModeFromString(userOptions.startDisplayMode);
                if(startDisplayMode !== undefined) { options.startDisplayMode = startDisplayMode; }
            }
        }
        return options;
    },

    getTypeFromString: function(typeString) {
        if(typeString == "TYPE_MONO") {
            return vrview.TYPE_MONO;
        } else if(typeString == "TYPE_STEREO_OVER_UNDER") {
            return vrview.TYPE_STEREO_OVER_UNDER;
        } else {
            return undefined;
        }
    },
    getFormatFromString: function(formatString) {
        if(formatString == "FORMAT_DEFAULT") {
            return vrview.FORMAT_DEFAULT;
        } else if(formatString == "FORMAT_HLS") {
            return vrview.FORMAT_HLS;
        } else if(formatString == "FORMAT_DASH") {
            return vrview.FORMAT_DASH;
        } else {
            return undefined;
        }
    },
    getDisplayModeFromString: function(displayMode) {
        if(displayMode == "DISPLAY_MODE_FULLSCREEN") {
            return vrview.DISPLAY_MODE_FULLSCREEN;
        } else if(displayMode == "DISPLAY_MODE_VR") {
            return vrview.DISPLAY_MODE_VR;
        } else {
            return undefined;
        }
    },

};
module.exports = vrview;
