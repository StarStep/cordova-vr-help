var app = {

    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function() {
        this.receivedEvent('deviceready');
    },

    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);

        cordova.exec(console.log, console.error, "VrPlayer", "play", [{
              src: "file:///sdcard/test.mp4", /* http://example.com/video.mp4 | file://storage/path/video.mp4 */
              inputType: "MEDIA_MONOSCOPIC",  /* MEDIA_MONOSCOPIC | MEDIA_STEREO_LEFT_RIGHT | MEDIA_STEREO_TOP_BOTTOM */
              displayMode : "DISPLAY_STEREO"  /* DISPLAY_STEREO | DISPLAY_FULLSCREEN*/
        }]);

        setTimeout(function() {
             cordova.exec(console.log, console.error, "VrPlayer", "pause", []);
        }, 4000);

    }
};

app.initialize();