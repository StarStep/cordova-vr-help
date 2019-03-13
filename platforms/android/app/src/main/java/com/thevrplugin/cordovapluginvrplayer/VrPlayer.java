package com.thevrplugin.cordovapluginvrplayer;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.media.MediaPlayer;
import android.os.Build;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;

public class VrPlayer extends CordovaPlugin {

    private static final String TAG = "VrPlayer";
    private static final String[] inputFormats = {"MEDIA_MONOSCOPIC", "MEDIA_STEREO_LEFT_RIGHT", "MEDIA_STEREO_TOP_BOTTOM"};

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        alert("Action " + action  + " - Args: " + args.toString());
        Log.d(TAG, "Action " + action  + " - Args: " + args.toString());
        String result = "";
        try {
            Context context = this.cordova.getActivity().getApplicationContext();
            if (action.equals("play")) {
                result = play(context, args);
            } else if(action.equals("pause")) {
                result = pause(context, args);
            } else if(action.equals("seek")) {
                result = seek(context, args);
            } else if(action.equals("resume")) {
                result = resume(context, args);
            } else if(action.equals("rotate")) {
                result = rotate(context, args);
            } else if(action.equals("stop")) {
                result = stop(context, args);
            } else if(action.equals("hide")) {
                result = hide(context, args);
            } else if(action.equals("status")) {
                result = status(context, args);
            } else if(action.equals("isDeviceSupported")) {
                result = getDeviceSupportLevel();
            } else {
                result = "false";
            }
        } catch(Exception e) {
            Log.d(TAG, "Error initializing Vr View", e);
            callbackContext.error(result);
            return false;
        }
        Log.d(TAG, "Action " + action  + " - Result: " + result);
        if(result != "false") {
            callbackContext.success(result);
        } else {
            callbackContext.error(result);
        }
        return true;
    }

    private String isDeviceNotSupported() {
        return getDeviceSupportLevel() == "0" ? "true" : "false";
    }

    private String getDeviceSupportLevel() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            return (checkGyroExists() ? "1" : "2");
        } else {
            return "0";
        }
    }

    private boolean checkGyroExists() {
        try {
            Context context = this.cordova.getActivity().getApplicationContext();
            boolean gyroExists = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);
            return gyroExists;
        } catch(Exception e) {
            return true;
        }
    }

    private void onDeviceNotSupported() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Context context = VrPlayer.this.cordova.getActivity().getApplicationContext();
                Toast toast = Toast.makeText(context, "Device not supported", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private String play(Context context, JSONArray args) throws JSONException {
        if(isDeviceNotSupported() == "true") {
            onDeviceNotSupported();
            return "false";
        }
        Intent intent = new Intent(context, VrVideoActivity.class);
        intent.setAction(Intent.ACTION_VIEW);

        JSONObject options = args.getJSONObject(0);
        intent.putExtra("src", options.getString("src"));
        intent.putExtra("displayMode", 0);
        intent.putExtra("inputFormat", 0);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "Starting VR activity");
        context.startActivity(intent);
        return "Ok!";
    }

    protected void alert(String msg) {
        Toast.makeText(cordova.getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private String pause(Context context, JSONArray args) throws JSONException {
        (new VrVideoActivity()).myPause();
        return "true";
    }

    private String resume(Context context, JSONArray args) throws JSONException {
        // K // (new VrVideoActivity()).resumeVideo();
        return "";
    }

    private String seek(Context context, JSONArray args) throws JSONException {
        long new_time = args.getLong(0);
        // K // (new VrVideoActivity()).seekVideo(new_time);
        return "";
    }

    private String rotate(Context context, JSONArray args) throws JSONException {
        return "";
    }

    private String stop(Context context, JSONArray args) throws JSONException {
        // K // (new VrVideoActivity()).pauseVideo();
        // K // (new VrVideoActivity()).seekVideo(0);
        return "";
    }

    private String hide(Context context, JSONArray args) throws JSONException {
        // K // (new VrVideoActivity()).finish();
        return "";
    }

    private String status(Context context, JSONArray args) throws JSONException {
        // K //
        return "";
    }

}
