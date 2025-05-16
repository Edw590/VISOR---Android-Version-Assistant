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
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.OpenGL.GyroRotationCorrection;
import com.edw590.visor_c_a.OpenGL.Objects.Object;
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
	private GLSurfaceView mGLSurfaceView = null;

	private final Collection<Object> objects = new ArrayList<>(50);

	float[] view_matrix = new float[16];
	SensorManager sensor_manager = null;
	GyroRotationCorrection gyro_rotation_correction = new GyroRotationCorrection();

	private final OpenCV open_cv = new OpenCV();

	private long last_mov_check = 0;
	private long last_clear = 0;

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

	private int frame_count = 0;
	private long start_time = System.currentTimeMillis();

	private AppCompatTextView textView = null;

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

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Create a FrameLayout to hold the GLSurfaceView and TextView
		FrameLayout frameLayout = new FrameLayout(requireContext());

		JavaCameraView cameraView = new JavaCameraView(requireContext(), 0);
		cameraView.setVisibility(View.VISIBLE);
		cameraView.setCvCameraViewListener(open_cv);
		cameraView.enableView();
		frameLayout.addView(cameraView);

		// Initialize GLSurfaceView and add to the FrameLayout
		mGLSurfaceView = new GLSurfaceView(requireContext());
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLSurfaceView.setRenderer(this);
		mGLSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
		mGLSurfaceView.setZOrderOnTop(true);
		frameLayout.addView(mGLSurfaceView);

		// Create a TextView
		textView = new AppCompatTextView(requireContext());
		textView.setText("FPS: ERROR");
		textView.setTextSize(20);
		textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
		textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));

		// Set layout parameters for the TextView to position it at the top right corner
		FrameLayout.LayoutParams textViewParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT
		);
		textViewParams.gravity = Gravity.TOP | Gravity.END;
		textView.setLayoutParams(textViewParams);

		// Add TextView to the FrameLayout
		frameLayout.addView(textView);

		// Set the FrameLayout as the content view
		requireActivity().setContentView(frameLayout);

		// /////////////////////////////////////////////////////////////////////

		prepareSensors();

		Matrix.setIdentityM(view_matrix, 0);
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
				textView.setText("FPS: " + fps);
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
			object.draw();
		}
	}

	private void prepareSensors() {
		sensor_manager = (SensorManager) requireContext().
				getSystemService(Context.SENSOR_SERVICE);
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
	public void onDestroyView() {
		super.onDestroyView();
		if (mGLSurfaceView != null) {
			mGLSurfaceView.onPause();
		}
		sensor_manager.unregisterListener(sensor_listener);
		UtilsOpenGL.deleteProgram();
	}
}
