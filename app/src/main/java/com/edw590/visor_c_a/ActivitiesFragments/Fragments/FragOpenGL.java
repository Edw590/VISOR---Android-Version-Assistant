/*
 * Copyright 2021-2024 Edw590
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

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.OpenGL.Objects.Object;
import com.edw590.visor_c_a.OpenGL.Objects.Parallelepiped;
import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.OpenGL.Vector;
import com.edw590.visor_c_a.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <p>Fragment that shows the list of the Values Storage values.</p>
 */
public final class FragOpenGL extends Fragment implements GLSurfaceView.Renderer {

	/** Hold a reference to our GLSurfaceView. */
	private GLSurfaceView mGLSurfaceView;

	private final List<Object> objects = new ArrayList<>(50);

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

	int program_id = 0;

	private int frame_count = 0;
	private long start_time = new Date().getTime();

	private AppCompatTextView textView;

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

		// Initialize GLSurfaceView
		mGLSurfaceView = new GLSurfaceView(requireContext());
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		mGLSurfaceView.setRenderer(this);

		// Add GLSurfaceView to the FrameLayout
		frameLayout.addView(mGLSurfaceView);

		// Create a TextView
		textView = new AppCompatTextView(requireContext());
		textView.setText("FPS: ERROR");
		textView.setTextSize(20);
		textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
		textView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));

		// Set layout parameters for the TextView to position it at the top
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
	}

	@Override
	public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		program_id = UtilsOpenGL.createProgram();
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
		Matrix.perspectiveM(projection_matrix, 0, 60, (float) width / height, 0.1f, 10.0f);
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
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		UtilsOpenGL.checkGLErrors("glClear");

		int transformation_id = GLES20.glGetUniformLocation(program_id, "u_transformation");
		for (final Object object : objects) {
			//object.translateM(0.0f, 0.0f, -0.01f);
			object.rotateM(0.3f, 1.0f, 0.6f);
			//object.rotateM(0.0f, 0.0f, 0.6f);
			//object.scaleM(0.0f, 0.0f, 0.01f);

			object.draw(null);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mGLSurfaceView.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mGLSurfaceView.onResume();
	}
}
