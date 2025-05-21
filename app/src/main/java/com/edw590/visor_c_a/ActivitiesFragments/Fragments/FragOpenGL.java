/*
 * Copyright 2021-2025 Edw590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.edw590.visor_c_a.ActivitiesFragments.Fragments;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.OpenGL.GyroRotationCorrection;
import com.edw590.visor_c_a.OpenGL.Objects.Object;
import com.edw590.visor_c_a.OpenGL.Objects.Rectangle;
import com.edw590.visor_c_a.OpenGL.OpenCV;
import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.R;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragOpenGL extends Fragment implements GLSurfaceView.Renderer {

	/** Hold a reference to our GLSurfaceView. */
	private GLSurfaceView gl_surface_view = null;

	private final Collection<Object> objects = new ArrayList<>(50);

	float[] view_matrix = new float[16];
	SensorManager sensor_manager = null;
	GyroRotationCorrection gyro_rotation_correction = new GyroRotationCorrection();

	private final OpenCV open_cv = new OpenCV();

	private long last_mov_check = 0;
	private long last_clear = 0;

	private AppCompatTextView fps_text_view = null;
	private int frame_count = 0;
	private long start_time = System.currentTimeMillis();

	private WebView web_view_youtube = null;
	private boolean player_ready = false;
	private long player_ready_time = 0;
	private long player_unmute_time = System.currentTimeMillis() + 1000000000;
	private long player_continue_time = System.currentTimeMillis() + 1000000000;

	public FragOpenGL() {
		/*objects.add(new Parallelepiped(
				new Vector(0.0f, 0.5f, -3.0f),
				1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));*/
		/*objects.add(new Parallelepiped(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));*/
		/*objects.add(new Parallelepiped(
				new Vector(0.3f, -0.5f, -3.0f),
				1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));*/
		/*objects.add(new Triangle(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f, 1.0f, 90.0f, 0.0f, 0.0f, 0.0f
		));*/
		/*objects.add(new Rectangle(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));*/
		/*objects.add(new Icosahidral(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f
		));*/
		/*objects.add(new Sphere(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f, 20, 20
		));*/
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		if (UtilsApp.isRunningOnWatch()) {
			return inflater.inflate(R.layout.frag_main_watch, container, false);
		} else {
			return inflater.inflate(R.layout.frag_main, container, false);
		}
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Create a FrameLayout to hold the GLSurfaceView and TextView
		FrameLayout frameLayout = new FrameLayout(requireContext());
		frameLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));

		JavaCameraView camera_view = new JavaCameraView(requireContext(), 0);
		camera_view.setVisibility(View.VISIBLE);
		camera_view.setCvCameraViewListener(open_cv);
		camera_view.enableView();
		camera_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
		camera_view.setZOrderOnTop(true);
		frameLayout.addView(camera_view);

		// Initialize GLSurfaceView and add to the FrameLayout
		gl_surface_view = new GLSurfaceView(requireContext());
		gl_surface_view.setEGLContextClientVersion(2);
		gl_surface_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		gl_surface_view.setRenderer(this);
		gl_surface_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
		gl_surface_view.setZOrderOnTop(true);
		frameLayout.addView(gl_surface_view);

		// Create a TextView
		fps_text_view = new AppCompatTextView(requireContext());
		fps_text_view.setText("FPS: ERROR");
		fps_text_view.setTextSize(20);
		fps_text_view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
		fps_text_view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));

		// Set layout parameters for the TextView to position it at the top right corner
		FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);
		textViewParams.gravity = Gravity.TOP | Gravity.END;
		fps_text_view.setLayoutParams(textViewParams);

		// Add TextView to the FrameLayout
		frameLayout.addView(fps_text_view);

		// Set the FrameLayout as the content view
		requireActivity().setContentView(frameLayout);

		// /////////////////////////////////////////////////////////////////////

		prepareSensors();

		Matrix.setIdentityM(view_matrix, 0);

		web_view_youtube = new WebView(requireContext());
		web_view_youtube.setLayoutParams(new FrameLayout.LayoutParams(
				500,
				200
		));
		web_view_youtube.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
		web_view_youtube.getSettings().setJavaScriptEnabled(true);
		web_view_youtube.getSettings().setDomStorageEnabled(true);
		web_view_youtube.setWebChromeClient(new WebChromeClient());
		// Inject the YouTubePlayerBridge
		web_view_youtube.addJavascriptInterface(new java.lang.Object() {
			@JavascriptInterface
			public void sendYouTubeIFrameAPIReady() {
				System.out.println("YouTube: Iframe API ready");
			}

			@JavascriptInterface
			public void sendReady() {
				System.out.println("YouTube: Player is ready");
				player_ready = true;
				player_ready_time = System.currentTimeMillis();
			}

			@JavascriptInterface
			public void sendStateChange(String state) {
				System.out.println("YouTube: State changed to: " + state);
			}

			@JavascriptInterface
			public void sendVideoDuration(double duration) {
				System.out.println("YouTube: Duration: " + duration);
			}

			@JavascriptInterface
			public void sendVideoCurrentTime(double time) {
				System.out.println("YouTube: Current time: " + time);
			}

			@JavascriptInterface
			public void sendVideoLoadedFraction(double fraction) {
				System.out.println("YouTube: Loaded fraction: " + fraction);
			}

			@JavascriptInterface
			public void sendPlaybackQualityChange(String quality) {
				System.out.println("YouTube: Quality changed to: " + quality);
			}

			@JavascriptInterface
			public void sendPlaybackRateChange(String rate) {
				System.out.println("YouTube: Playback rate changed to: " + rate);
			}

			@JavascriptInterface
			public void sendError(String error) {
				System.out.println("YouTube: Error: " + error);
			}

			@JavascriptInterface
			public void sendApiChange() {
				System.out.println("YouTube: API Changed");
			}

			@JavascriptInterface
			public void sendVideoId(String id) {
				System.out.println("YouTube: Video ID: " + id);
			}

		}, "YouTubePlayerBridge");
		web_view_youtube.loadData(
				"<!DOCTYPE html>\n" +
				"<html>\n" +
				"  <style type=\"text/css\">\n" +
				"        html, body {\n" +
				"            height: 100%;\n" +
				"            width: 100%;\n" +
				"            margin: 0;\n" +
				"            padding: 0;\n" +
				"            background-color: #000000;\n" +
				"            overflow: hidden;\n" +
				"            position: fixed;\n" +
				"        }\n" +
				"    </style>\n" +
				"\n" +
				"  <head>\n" +
				"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
				"    <!-- defer forces the library to execute after the html page is fully parsed. -->\n" +
				"    <!-- This is needed to avoid race conditions, where the library executes and calls `onYouTubeIframeAPIReady` before the page is fully parsed. -->\n" +
				"    <!-- See #873 on GitHub -->\n" +
				"    <script defer src=\"https://www.youtube.com/iframe_api\"></script>\n" +
				"  </head>\n" +
				"\n" +
				"  <body>\n" +
				"    <div id=\"youTubePlayerDOM\"></div>\n" +
				"    <button id=\"playBtn\" onclick=\"player.unMute(); player.playVideo();\">Play</button>\n" +
				"  </body>\n" +
				"\n" +
				"  <script type=\"text/javascript\">\n" +
				"    var UNSTARTED = \"UNSTARTED\";\n" +
				"    var ENDED = \"ENDED\";\n" +
				"    var PLAYING = \"PLAYING\";\n" +
				"    var PAUSED = \"PAUSED\";\n" +
				"    var BUFFERING = \"BUFFERING\";\n" +
				"    var CUED = \"CUED\";\n" +
				"\n" +
				"    var YouTubePlayerBridge = window.YouTubePlayerBridge;\n" +
				"    var YouTubePlayerCallbacks = window.YouTubePlayerCallbacks;\n" +
				"    var player;\n" +
				"\n" +
				"    var timerId;\n" +
				"\n" +
				"    function onYouTubeIframeAPIReady() {\n" +
				"\n" +
				"      YouTubePlayerBridge.sendYouTubeIFrameAPIReady();\n" +
				"            \n" +
				"    \tplayer = new YT.Player('youTubePlayerDOM', {\n" +
				"    \t\t\t\n" +
				"        height: '100%',\n" +
				"    \t  width: '100%',\n" +
				"    \t  videoId: 'tgbNymZ7vqY',\n" +
				"    \t\t\t\n" +
				"        events: {\n" +
				"    \t    onReady: function(event) { YouTubePlayerBridge.sendReady() },\n" +
				"    \t\t  onStateChange: function(event) { sendPlayerStateChange(event.data) },\n" +
				"    \t\t  onPlaybackQualityChange: function(event) { YouTubePlayerBridge.sendPlaybackQualityChange(event.data) },\n" +
				"    \t\t  onPlaybackRateChange: function(event) { YouTubePlayerBridge.sendPlaybackRateChange(event.data) },\n" +
				"    \t\t  onError: function(error) { YouTubePlayerBridge.sendError(error.data) },\n" +
				"    \t\t  onApiChange: function(event) { YouTubePlayerBridge.sendApiChange() }\n" +
				"    \t  },\n" +
				"\n" +
				"    \t  playerVars: {\n" +
						"  autoplay: 1,\n" +
						"  mute: 1,\n" +
						"  playsinline: 1,\n" +
						"  fs: 0,\n" +
						"  modestbranding: 1,\n" +
						"  rel: 0,\n" +
						"  enablejsapi: 1,\n" +
						"}\n" +
						"\n" +
						"      });\n" +
						"    }\n" +
						"\n" +
						"    function sendPlayerStateChange(playerState) {\n" +
						"      clearTimeout(timerId);\n" +
						"\n" +
						"      switch (playerState) {\n" +
						"        case YT.PlayerState.UNSTARTED:\n" +
						"          sendStateChange(UNSTARTED);\n" +
						"          sendVideoIdFromPlaylistIfAvailable(player);\n" +
						"          return;\n" +
						"\n" +
						"        case YT.PlayerState.ENDED:\n" +
						"          sendStateChange(ENDED);\n" +
						"          return;\n" +
						"\n" +
						"        case YT.PlayerState.PLAYING:\n" +
						"          sendStateChange(PLAYING);\n" +
						"\n" +
						"          startSendCurrentTimeInterval();\n" +
						"          sendVideoData(player);\n" +
						"          return;\n" +
						"\n" +
						"        case YT.PlayerState.PAUSED:\n" +
						"          sendStateChange(PAUSED);\n" +
						"          return;\n" +
						"\n" +
						"        case YT.PlayerState.BUFFERING:\n" +
						"          sendStateChange(BUFFERING);\n" +
						"          return;\n" +
						"\n" +
						"        case YT.PlayerState.CUED:\n" +
						"          sendStateChange(CUED);\n" +
						"          return;\n" +
						"      }\n" +
						"\n" +
						"      function sendVideoData(player) {\n" +
						"        var videoDuration = player.getDuration();\n" +
						"\n" +
						"        YouTubePlayerBridge.sendVideoDuration(videoDuration);\n" +
						"      }\n" +
						"\n" +
						"      // This method checks if the player is playing a playlist.\n" +
						"      // If yes, it sends out the video id of the video being played.\n" +
						"      function sendVideoIdFromPlaylistIfAvailable(player) {\n" +
						"        var playlist = player.getPlaylist();\n" +
						"        if ( typeof playlist !== 'undefined' && Array.isArray(playlist) && playlist.length > 0 ) {\n" +
						"          var index = player.getPlaylistIndex();\n" +
						"          var videoId = playlist[index];\n" +
						"          YouTubePlayerBridge.sendVideoId(videoId);\n" +
						"        }\n" +
						"      }\n" +
						"\n" +
						"      function sendStateChange(newState) {\n" +
						"        YouTubePlayerBridge.sendStateChange(newState)\n" +
						"      }\n" +
						"\n" +
						"      function startSendCurrentTimeInterval() {\n" +
						"        timerId = setInterval(function() {\n" +
						"          YouTubePlayerBridge.sendVideoCurrentTime( player.getCurrentTime() )\n" +
						"          YouTubePlayerBridge.sendVideoLoadedFraction( player.getVideoLoadedFraction() )\n" +
						"        }, 100 );\n" +
						"      }\n" +
						"    }\n" +
						"\n" +
						"    // JAVA to WEB functions\n" +
						"\n" +
						"    function seekTo(startSeconds) {\n" +
						"      player.seekTo(startSeconds, true);\n" +
						"    }\n" +
						"\n" +
						"    function pauseVideo() {\n" +
						"      player.pauseVideo();\n" +
						"    }\n" +
						"\n" +
						"    function playVideo() {\n" +
						"      player.playVideo();\n" +
						"    }\n" +
						"\n" +
						"    function loadVideo(videoId, startSeconds) {\n" +
						"      player.loadVideoById(videoId, startSeconds);\n" +
						"      YouTubePlayerBridge.sendVideoId(videoId);\n" +
						"    }\n" +
						"\n" +
						"    function cueVideo(videoId, startSeconds) {\n" +
						"      player.cueVideoById(videoId, startSeconds);\n" +
						"      YouTubePlayerBridge.sendVideoId(videoId);\n" +
						"    }\n" +
						"\n" +
						"    function mute() {\n" +
						"      player.mute();\n" +
						"    }\n" +
						"\n" +
						"    function unMute() {\n" +
						"      player.unMute();\n" +
						"    }\n" +
						"\n" +
						"    function setVolume(volumePercent) {\n" +
						"      player.setVolume(volumePercent);\n" +
						"    }\n" +
						"\n" +
						"    function setPlaybackRate(playbackRate) {\n" +
						"      player.setPlaybackRate(playbackRate);\n" +
						"    }\n" +
						"\n" +
						"    function toggleFullscreen() {\n" +
						"      player.toggleFullscreen();\n" +
						"    }\n" +
						"\n" +
						"    function nextVideo() {\n" +
						"      player.nextVideo();\n" +
						"    }\n" +
						"\n" +
						"    function previousVideo() {\n" +
						"      player.previousVideo();\n" +
						"    }\n" +
						"\n" +
						"    function playVideoAt(index) {\n" +
						"      player.playVideoAt(index);\n" +
						"    }\n" +
						"\n" +
						"    function setLoop(loop) {\n" +
						"      player.setLoop(loop);\n" +
						"    }\n" +
						"\n" +
						"    function setShuffle(shuffle) {\n" +
						"      player.setShuffle(shuffle);\n" +
						"    }\n" +
						"\n" +
						"    function getMuteValue(requestId) {\n" +
						"      var isMuted = player.isMuted();\n" +
						"      YouTubePlayerCallbacks.sendBooleanValue(requestId, isMuted);\n" +
						"    }\n" +
						"\n" +
						"  </script>\n" +
						"</html>\n",
				"text/html",
				"utf-8"
		);
		frameLayout.addView(web_view_youtube);
	}

	@Override
	public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);

		int program_id = UtilsOpenGL.createProgram();
		if (program_id == 0) {
			throw new RuntimeException("Error creating OpenGL program");
		}
		GLES20.glUseProgram(program_id);
		UtilsOpenGL.setProgramID(program_id);
	}

	@Override
	public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
		GLES20.glViewport(0, 0, width, height);

		float[] projection_matrix = new float[16];
		Matrix.perspectiveM(projection_matrix, 0, UtilsOpenGL.setFovY(60), UtilsOpenGL.setAspectRatio(width, height),
				0.1f, 10.0f);
		UtilsOpenGL.setProjectionMatrix(projection_matrix);
	}

	@Override
	public void onDrawFrame(final GL10 gl) {
		frame_count++;
		long curr_time = System.currentTimeMillis();
		double seconds = (curr_time - start_time) / 1000.0;
		if (seconds > 1.0) {
			// Update the TextView with the FPS
			int fps = (int) (frame_count / seconds);
			frame_count = 0;
			start_time = curr_time;
			requireActivity().runOnUiThread(() -> {
				fps_text_view.setText("FPS: " + fps);
			});
		}

		UtilsOpenGL.clearGLErrors();
		GLES20.glClearColor(0, 0, 0, 0); // Transparent
		UtilsOpenGL.checkGLErrors("glClearColor");
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		UtilsOpenGL.checkGLErrors("glClear");

		UtilsOpenGL.setViewMatrix(view_matrix);

		/*for (final Object object : objects) {
			//object.translateM(0.0f, 0.0f, -0.01f);
			object.rotateM(0.3f, 1.0f, 0.6f);
			//object.rotateM(0.0f, 0.0f, 0.6f);
			//object.scaleM(1.0f, 1.0f, 0.999f);

			object.draw();
		}*/

		if (System.currentTimeMillis() - last_mov_check > 33) { // 33 ms
			if (gyro_rotation_correction.getAccelDifference() > 0.75f) {
				gyro_rotation_correction.saveCurrentAccel();
				objects.clear();
				objects.addAll(Arrays.asList(open_cv.getDetectedRectangles()));
			}

			last_mov_check = System.currentTimeMillis();
		}
		if (System.currentTimeMillis() - last_clear > 1000) {
			last_clear = System.currentTimeMillis();
			objects.clear();
		}
		if (objects.isEmpty()) {
			objects.addAll(Arrays.asList(open_cv.getDetectedRectangles()));
		}

		for (final Object object : objects) {
			if (object instanceof Rectangle) {
				Rectangle rectangle = (Rectangle) object;
				float x = rectangle.getCenter().x;
				float y = rectangle.getCenter().y;
				float z = rectangle.getCenter().z;
				float width = rectangle.getWidth();
				float height = rectangle.getHeight();

				float max_x = UtilsOpenGL.getMaxX(z);
				float max_y = UtilsOpenGL.getMaxY(z);
				float view_width = max_x * 2;
				float view_height = max_y * 2;

				requireActivity().runOnUiThread(() -> {
					DisplayMetrics display_metrics = requireContext().getResources().getDisplayMetrics();
					FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(
							(int) (width / view_width * display_metrics.widthPixels),
							(int) (height / view_height * display_metrics.heightPixels)
					);
					layout_params.leftMargin = (int) ((x + max_x - width / 2) / view_width * display_metrics.widthPixels);
					layout_params.topMargin = (int) ((-y + max_y - height / 2) / view_height * display_metrics.heightPixels);
					web_view_youtube.setLayoutParams(layout_params);
				});
			} else {
				object.draw();
			}
		}
	}

	private void prepareSensors() {
		sensor_manager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
		if (sensor_manager == null) {
			System.out.println("SensorManager is null");

			return;
		}

		Sensor accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor gyroscope = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (accelerometer == null || magnetometer == null) {
			System.out.println("Accelerometer and/or Magnetometer not available");

			return;
		}

		sensor_manager.registerListener(sensor_listener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		sensor_manager.registerListener(sensor_listener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		if (gyroscope != null) {
			sensor_manager.registerListener(sensor_listener, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	private final SensorEventListener sensor_listener = new SensorEventListener() {
		@Override
		public void onSensorChanged(final SensorEvent event) {
			float[] matrix = gyro_rotation_correction.onSensorChanged(event);
			if (matrix != null) {
				//view_matrix = matrix;
			}
		}

		@Override
		public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
			// No need to implement
		}
	};

	@Override
	public void onPause() {
		super.onPause();

		System.out.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
	}

	@Override
	public void onResume() {
		super.onResume();

		System.out.println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (gl_surface_view != null) {
			gl_surface_view.onPause();
		}
		if (web_view_youtube != null) {
			web_view_youtube.destroy();
		}
		if (sensor_manager != null) {
			sensor_manager.unregisterListener(sensor_listener);
		}
		UtilsOpenGL.deleteProgram();
	}
}
