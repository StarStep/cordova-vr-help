/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thevrplugin.cordovapluginvrplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.util.Log;
import android.view.Surface;
import com.thevrplugin.cordovapluginvrplayer.rendering.Mesh;
import com.thevrplugin.cordovapluginvrplayer.rendering.SceneRenderer;

import org.apache.cordova.LOG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.security.InvalidParameterException;

/**
 * MediaLoader takes an Intent from the user and loads the specified media file.
 *
 * <p>Example intents compatible with adb are:
 *   <ul>
 *     <li>
 *       A top-bottom stereo image in the VR Activity.
 *       <b>adb shell am start -a android.intent.action.VIEW  \
 *          -n com.thevrplugin.cordovapluginvrplayer/.VrVideoActivity \
 *          -d "file:///sdcard/IMAGE.JPG" \
 *          --ei stereoFormat 2
 *       </b>
 *     </li>
 *     <li>
 *       A monoscopic video in the 2D Activity.
 *       <b>adb shell am start -a android.intent.action.VIEW  \
 *          -n com.thevrplugin.cordovapluginvrplayer/.VideoActivity \
 *          -d "file:///sdcard/VIDEO.MP4" \
 *          --ei stereoFormat 0
 *       </b>
 *     </li>
 *   </ul>
 *
 * <p>This sample does not validiate that a given file is readable by the Android media decoders.
 * You should validate that the file plays on your target devices via
 * <b>adb shell am start -a android.intent.action.VIEW -t video/mpeg -d "file:///VIDEO.MP4"</b>
 */
public class MediaLoader {

  private static final String TAG = "VrMediaLoader";
  private static final int DEFAULT_SURFACE_HEIGHT_PX = 2048;
  private static final int SPHERE_RADIUS_METERS = 40;
  private static final int DEFAULT_SPHERE_VERTICAL_DEGREES = 180;
  private static final int DEFAULT_SPHERE_HORIZONTAL_DEGREES = 360;
  private static final int DEFAULT_SPHERE_ROWS = 12;
  private static final int DEFAULT_SPHERE_COLUMNS = 24;

  private final Context context;
  // This can be replaced by any media player that renders to a Surface. In a real app, this
  // media player would be separated from the rendering code. It is left in this class for
  // simplicity. This should be set or cleared in a synchronized manner.
  MediaPlayer mediaPlayer;
  // This sample also supports loading images.
  Bitmap mediaImage;
  // If the video or image fails to load, a placeholder panorama is rendered with error text.
  String errorText;

  // Due to the slow loading media times, it's possible to tear down the app before mediaPlayer is
  // ready. In that case, abandon all the pending work.
  // This should be set or cleared in a synchronized manner.
  private boolean isDestroyed = false;

  // The type of mesh created depends on the type of media.
  Mesh mesh;

  // The sceneRenderer is set after GL initialization is complete.
  private SceneRenderer sceneRenderer;

  // The displaySurface is configured after both GL initialization and media loading.
  private Surface displaySurface;

  // The actual work of loading media happens on a background thread.
  private MediaLoaderTask mediaLoaderTask;

  public MediaLoader(Context context) {
    this.context = context;
  }

  /* Loads custom videos based on the Intent or load the default video. See the Javadoc for this
   * class for information on generating a custom intent via adb. */
  public void handleIntent(Intent intent, VideoUiView uiView) {
    // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
    // take 100s of milliseconds.
    // Note that this sample doesn't cancel any pending mediaLoaderTasks since it assumes only one
    // Intent will ever be fired for a single Activity lifecycle.
    mediaLoaderTask = new MediaLoaderTask(uiView);
    mediaLoaderTask.execute(intent);
  }

  /* Notifies MediaLoader that GL components have initialized. */
  public void onGlSceneReady(SceneRenderer sceneRenderer) {
    this.sceneRenderer = sceneRenderer;
    displayWhenReady();
  }

  /* Helper class to media loading. This accesses the disk and decodes images so it needs to run in
   * the background. */
  private class MediaLoaderTask extends AsyncTask<Intent, Void, Void> {
    private final VideoUiView uiView;

    public MediaLoaderTask(VideoUiView uiView) {
      this.uiView = uiView;
    }

    @Override
    protected Void doInBackground(Intent... intent) {

      // Extract the stereoFormat from the Intent's extras.
      int stereoFormat = intent[0].getIntExtra("stereoFormat", Mesh.MEDIA_MONOSCOPIC);
      if (stereoFormat != Mesh.MEDIA_STEREO_LEFT_RIGHT && stereoFormat != Mesh.MEDIA_STEREO_TOP_BOTTOM) {
        stereoFormat = Mesh.MEDIA_MONOSCOPIC;
      }

      // Create video sphere
      mesh = Mesh.createUvSphere(SPHERE_RADIUS_METERS, DEFAULT_SPHERE_ROWS, DEFAULT_SPHERE_COLUMNS, DEFAULT_SPHERE_VERTICAL_DEGREES, DEFAULT_SPHERE_HORIZONTAL_DEGREES, stereoFormat);

      // Based on the Intent's data, load the appropriate media from disk.
      Uri uri = Uri.parse(intent[0].getStringExtra("src"));
      try {

        File file = new File(uri.getPath());
        if (!file.exists()) {
          throw new FileNotFoundException();
        }

        String type = URLConnection.guessContentTypeFromName(uri.getPath());
        if (type == null) {
          throw new InvalidParameterException("Unknown file type: " + uri);
        } else if (type.startsWith("image")) {
          mediaImage = BitmapFactory.decodeFile(uri.getPath());
        } else if (type.startsWith("video")) {
          MediaPlayer mp = MediaPlayer.create(context, uri);
          synchronized (MediaLoader.this) {
            mediaPlayer = mp;
          }
        } else {
          throw new InvalidParameterException("Unsupported MIME type: " + type);
        }

      } catch (IOException | InvalidParameterException e) {
        errorText = String.format("Error loading file [%s]: %s", uri.getPath(), e);
        Log.e(TAG, errorText);
      }

      displayWhenReady();
      return null;
    }

    @Override
    public void onPostExecute(Void unused) {
      // Set or clear the UI's mediaPlayer on the UI thread.
      if (uiView != null) {
        uiView.setMediaPlayer(mediaPlayer);
      }
    }
  }

  /**
   * Creates the 3D scene and load the media after sceneRenderer & mediaPlayer are ready. This can run on the GL Thread or a background thread.
   */
  @AnyThread
  private synchronized void displayWhenReady() {

    if (isDestroyed) {
      if (mediaPlayer != null) {
        mediaPlayer.release();
        mediaPlayer = null;
      }
      return;
    }

    if (displaySurface != null) {
      return;
    }

    if ((errorText == null && mediaImage == null && mediaPlayer == null) || sceneRenderer == null) {
      return;
    }


    if (mediaPlayer != null) {

      displaySurface = sceneRenderer.createDisplay(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight(), mesh);
      mediaPlayer.setSurface(displaySurface);
      mediaPlayer.setLooping(false);
      mediaPlayer.start();

    } else if (mediaImage != null) {

      displaySurface = sceneRenderer.createDisplay(mediaImage.getWidth(), mediaImage.getHeight(), mesh);
      Canvas c = displaySurface.lockCanvas(null);
      c.drawBitmap(mediaImage, 0, 0, null);
      displaySurface.unlockCanvasAndPost(c);

    } else {

      Log.e(TAG,"VR Media not valid");

    }
  }

  @MainThread
  public synchronized void pause() {
    if (mediaPlayer != null) {
      mediaPlayer.pause();
    }
  }

  @MainThread
  public synchronized void resume() {
    if (mediaPlayer != null) {
      mediaPlayer.start();
    }
  }

  @MainThread
  public synchronized void destroy() {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.release();
      mediaPlayer = null;
    }
    isDestroyed = true;
  }
}
