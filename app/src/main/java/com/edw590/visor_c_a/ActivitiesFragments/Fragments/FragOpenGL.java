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
import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
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

	static class ObjectData {
		FloatBuffer vertex_buffer = null;
		FloatBuffer color_buffer = null;
		ByteBuffer index_buffer = null;
		int index_count = 0;
	}
	private final List<ObjectData> objects = new ArrayList<>(10);

	private int scale_handle = 0;

	private float scale = 0.0f;
	private float increment = 0.05f;

	private int position_handle = 0;
	private int color_handle = 0;

	public FragOpenGL() {
		addObject(new float[] {
				-1.0f, -1.0f,  0.5f,
				 1.0f, -1.0f,  0.5f,
				 0.0f,  1.0f, -0.5f,
		}, new float[] {
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
		}, new short[] {
				0, 1, 2,
		});
		addObject(new float[] {
				 1.0f,  1.0f,  0.5f,
				-1.0f,  1.0f,  0.5f,
				 0.0f, -1.0f, -0.5f,
		}, new float[] {
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
		}, new short[] {
				0, 1, 2,
		});
	}

	private int frame_count = 0;
	private long start_time = new Date().getTime();

	AppCompatTextView textView;

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

		int program_handle = UtilsOpenGL.createProgram();
		if (program_handle == 0) {
			throw new RuntimeException("Error creating OpenGL program");
		}
		GLES20.glUseProgram(program_handle);

		position_handle = GLES20.glGetAttribLocation(program_handle, "a_position");
		color_handle = GLES20.glGetAttribLocation(program_handle, "a_color");
		scale_handle = GLES20.glGetUniformLocation(program_handle, "u_scale");
	}

	@Override
	public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
		GLES20.glViewport(0, 0, width, height);
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

		for (final ObjectData object : objects) {
			GLES20.glEnableVertexAttribArray(position_handle);
			UtilsOpenGL.checkGLErrors("glEnableVertexAttribArray 1");
			GLES20.glVertexAttribPointer(position_handle, 3, GLES20.GL_FLOAT, false, 3 * UtilsOpenGL.FLOAT_BYTES,
					object.vertex_buffer);
			UtilsOpenGL.checkGLErrors("glVertexAttribPointer 1");

			GLES20.glEnableVertexAttribArray(color_handle);
			UtilsOpenGL.checkGLErrors("glEnableVertexAttribArray 2");
			GLES20.glVertexAttribPointer(color_handle, 4, GLES20.GL_FLOAT, false, 4 * UtilsOpenGL.FLOAT_BYTES,
					object.color_buffer);
			UtilsOpenGL.checkGLErrors("glVertexAttribPointer 2");

			GLES20.glDrawElements(GLES20.GL_TRIANGLES, object.index_count, GLES20.GL_UNSIGNED_SHORT,
					object.index_buffer);
			UtilsOpenGL.checkGLErrors("glDrawElements");
		}

		// Set scale
		if (scale > 1.0f) {
			increment = -0.05f;
		} else if (scale < 0.0f) {
			increment = 0.05f;
		}
		scale += increment;
		GLES20.glUniform1f(scale_handle, scale);
	}

	private void addObject(@NonNull final float[] vertices, @NonNull final float[] colors, @NonNull final short[] indices) {
		ObjectData object = new ObjectData();

		object.vertex_buffer = ByteBuffer.allocateDirect(vertices.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		object.vertex_buffer.position(0);

		object.color_buffer = ByteBuffer.allocateDirect(colors.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(colors);
		object.color_buffer.position(0);

		object.index_buffer = ByteBuffer.allocateDirect(indices.length * UtilsOpenGL.SHORT_BYTES)
				.order(ByteOrder.nativeOrder());
		for (final short index : indices) {
			object.index_buffer.putShort(index);
		}
		object.index_buffer.position(0);

		object.index_count = indices.length;

		objects.add(object);
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
