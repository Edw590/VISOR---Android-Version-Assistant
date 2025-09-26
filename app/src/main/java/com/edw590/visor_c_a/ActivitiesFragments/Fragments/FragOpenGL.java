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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.AccessibilityService.AccessibilityService;
import com.edw590.visor_c_a.AugmentedReality.GyroRotationCorrection;
import com.edw590.visor_c_a.AugmentedReality.NotificationView;
import com.edw590.visor_c_a.AugmentedReality.OpenCV.OpenCV;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects.Object;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects.Parallelepiped;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects.Rectangle;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.Vector;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsLogging;
import com.edw590.visor_c_a.R;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.Collection;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class FragOpenGL extends Fragment implements GLSurfaceView.Renderer {

	/** Hold a reference to our GLSurfaceView. */
	private GLSurfaceView gl_surface_view = null;

	FrameLayout frame_layout = null;

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

	public FragOpenGL() {
		/*objects.add(new Parallelepiped(
				new Vector(0.0f, 0.5f, -3.0f),
				1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));*/
		objects.add(new Parallelepiped(
				new Vector(0.0f, 0.0f, -3.0f),
				1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f
		));
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

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.L) { // Keep this here - this is called on all versions
			AppCompatTextView text_view = new AppCompatTextView(requireContext());
			text_view.setText("Only available on Android Lollipop 5.0 and above");
			text_view.setTextSize(20);
			text_view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
			text_view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));
			text_view.setGravity(Gravity.CENTER);
			text_view.setLayoutParams(new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
			));
			requireActivity().setContentView(text_view);

			return;
		}

		if (!UtilsCheckHardwareFeatures.isCameraSupported()) {
			AppCompatTextView text_view = new AppCompatTextView(requireContext());
			text_view.setText("Only available on devices with a camera");
			text_view.setTextSize(20);
			text_view.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
			text_view.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));
			text_view.setGravity(Gravity.CENTER);
			text_view.setLayoutParams(new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
			));
			requireActivity().setContentView(text_view);

			return;
		}

		hideSystemUI(requireActivity().getWindow().getDecorView());

		requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Create a FrameLayout to hold the GLSurfaceView and TextView
		frame_layout = new FrameLayout(requireContext());
		frame_layout.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));

		JavaCameraView camera_view = new JavaCameraView(requireContext(), 0);
		camera_view.setVisibility(View.VISIBLE);
		camera_view.setCvCameraViewListener(open_cv);
		camera_view.enableView();
		camera_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
			camera_view.setCompositionOrder(98);
		} else {
			camera_view.setZOrderOnTop(true);
		}
		int camera_width = (int) UtilsRegistry.getData(RegistryKeys.K_AR_CAM_MAX_WIDTH, true);
		int camera_height = (int) UtilsRegistry.getData(RegistryKeys.K_AR_CAM_MAX_HEIGHT, true);
		camera_view.setMaxFrameSize(camera_width, camera_height);
		frame_layout.addView(camera_view);

		// Initialize GLSurfaceView and add to the FrameLayout
		gl_surface_view = new GLSurfaceView(requireContext());
		gl_surface_view.setEGLContextClientVersion(2);
		gl_surface_view.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		gl_surface_view.setRenderer(this);
		gl_surface_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
			gl_surface_view.setCompositionOrder(99);
		} else {
			gl_surface_view.setZOrderOnTop(true);
		}
		frame_layout.addView(gl_surface_view);

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
		frame_layout.addView(fps_text_view);

		// Set the FrameLayout as the content view
		requireActivity().setContentView(frame_layout);

		// /////////////////////////////////////////////////////////////////////

		//prepareSensors();

		Matrix.setIdentityM(view_matrix, 0);

		web_view_youtube = new WebView(requireContext());
		web_view_youtube.setLayoutParams(new FrameLayout.LayoutParams(
				500,
				200
		));
		web_view_youtube.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
		web_view_youtube.getSettings().setJavaScriptEnabled(true);
		web_view_youtube.loadData(
				"<iframe width=\"100%\" height=\"100%\"\n" +
					"src=\"https://www.youtube.com/embed/tgbNymZ7vqY?autoplay=1&mute=0\" frameborder=\"0\" allowfullscreen\n" +
				"</iframe>",
				"text/html",
				"utf-8"
		);
		//frame_layout.addView(web_view_youtube);

		try {
			requireContext().registerReceiver(broadcastReceiver,
					new IntentFilter(AccessibilityService.ACTION_NEW_NOTIFICATION));
		} catch (final IllegalArgumentException ignored) {
		}
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
				0.1f, 100.0f);
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

		/*UtilsOpenGL.setViewMatrix(view_matrix);

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
		}*/

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
				//object.translateM(0.0f, 0.0f, -0.01f);
				object.rotateM(0.3f, 1.0f, 0.6f);
				//object.rotateM(0.0f, 0.0f, 0.6f);
				//object.scaleM(1.0f, 1.0f, 0.999f);

				object.draw();
			}
		}
	}

	private void prepareSensors() {
		sensor_manager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
		if (sensor_manager == null) {
			UtilsLogging.logLnWarning("SensorManager is null");

			return;
		}

		Sensor accelerometer = sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor magnetometer = sensor_manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor gyroscope = sensor_manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (accelerometer == null || magnetometer == null) {
			UtilsLogging.logLnWarning("Accelerometer and/or Magnetometer not available");

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

	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			UtilsLogging.logLnInfo("PPPPPPPPPPPPPPPPPP-FragOpenGL - " + intent.getAction());

			if (!intent.getAction().equals(AccessibilityService.ACTION_NEW_NOTIFICATION)) {
				return;
			}

			if (frame_layout == null) {
				return;
			}

			String title = intent.getStringExtra("title");
			String txt = intent.getStringExtra("txt");
			String txt_big = intent.getStringExtra("txt_big");
			NotificationView notification_view = new NotificationView(requireContext(), title,
					txt.isEmpty() ? txt_big : txt);
			notification_view.showIn(frame_layout, 7500);
		}
	};

	private void hideSystemUI(@NonNull final View decor_view) {
		// Set the IMMERSIVE_STICKY flag.
		// Set the content to appear under the system bars so that the content
		// doesn't resize when the system bars hide and show.
		decor_view.setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	private void showSystemUI(@NonNull final View decor_view) {
		decor_view.setSystemUiVisibility(0);
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
		requireContext().unregisterReceiver(broadcastReceiver);

		showSystemUI(requireActivity().getWindow().getDecorView());
	}
}
