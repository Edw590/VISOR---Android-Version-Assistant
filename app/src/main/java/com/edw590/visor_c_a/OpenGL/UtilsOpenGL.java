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

package com.edw590.visor_c_a.OpenGL;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.OpenGL.Objects.Triangle;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UtilsOpenGL {

	static final Logger LOGGER_STR = Logger.getLogger("OpenGL");

	public static final int FLOAT_BYTES = 4;
	public static final int SHORT_BYTES = 2;

	public static final int FLOATS_PER_VERTEX = 3;

	private static ByteBuffer index_buffer = null;

	private static int program_id = 0;
	private static float[] projection_matrix = new float[16];

	public static void setProgramID(final int program_id) {
		UtilsOpenGL.program_id = program_id;
	}

	public static void setProjectionMatrix(@NonNull final float[] projection_matrix) {
		UtilsOpenGL.projection_matrix = projection_matrix.clone();
	}

	public static void drawTriangle(@NonNull final Triangle triangle, @NonNull final float[] model_matrix) {
		clearGLErrors();

		if (index_buffer == null) {
			index_buffer = ByteBuffer.allocateDirect(3);
			index_buffer.order(ByteOrder.nativeOrder());
			index_buffer.put(new byte[] {0, 1, 2});
			index_buffer.position(0);
		}

		int position_id = GLES20.glGetAttribLocation(program_id, "a_position");
		UtilsOpenGL.checkGLErrors("glGetAttribLocation 1");
		if (position_id == -1) {
			LOGGER_STR.log(Level.SEVERE, "Error getting attribute location for a_position");

			return;
		}
		int color_id = GLES20.glGetAttribLocation(program_id, "a_color");
		UtilsOpenGL.checkGLErrors("glGetAttribLocation 2");
		if (color_id == -1) {
			LOGGER_STR.log(Level.SEVERE, "Error getting attribute location for a_color");

			return;
		}

		int model_matrix_id = GLES20.glGetUniformLocation(program_id, "u_model");
		UtilsOpenGL.checkGLErrors("glGetUniformLocation 1");
		if (model_matrix_id == -1) {
			LOGGER_STR.log(Level.SEVERE, "Error getting uniform location for u_model");

			return;
		}

		int projection_matrix_id = GLES20.glGetUniformLocation(program_id, "u_projection");
		UtilsOpenGL.checkGLErrors("glGetUniformLocation 2");
		if (projection_matrix_id == -1) {
			LOGGER_STR.log(Level.SEVERE, "Error getting uniform location for u_projection");

			return;
		}

		GLES20.glUniformMatrix4fv(model_matrix_id, 1, false, model_matrix, 0);
		UtilsOpenGL.checkGLErrors("glUniformMatrix4fv 1");
		GLES20.glUniformMatrix4fv(projection_matrix_id, 1, false, projection_matrix, 0);
		UtilsOpenGL.checkGLErrors("glUniformMatrix4fv 3");

		GLES20.glEnableVertexAttribArray(position_id);
		UtilsOpenGL.checkGLErrors("glEnableVertexAttribArray 1");
		GLES20.glVertexAttribPointer(position_id, 3, GLES20.GL_FLOAT, false, 3 * UtilsOpenGL.FLOAT_BYTES,
				triangle.getVertexBuffer());
		UtilsOpenGL.checkGLErrors("glVertexAttribPointer 1");

		GLES20.glEnableVertexAttribArray(color_id);
		UtilsOpenGL.checkGLErrors("glEnableVertexAttribArray 2");
		GLES20.glVertexAttribPointer(color_id, 4, GLES20.GL_FLOAT, false, 4 * UtilsOpenGL.FLOAT_BYTES,
				triangle.getColorBuffer());
		UtilsOpenGL.checkGLErrors("glVertexAttribPointer 2");

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 3, GLES20.GL_UNSIGNED_BYTE, index_buffer);
		UtilsOpenGL.checkGLErrors("glDrawElements");
	}

	public static int createProgram() {
		clearGLErrors();

		int vertex_shader_id = compileShader(GLES20.GL_VERTEX_SHADER, Shaders.VERTEX_SHADER_CODE);
		if (vertex_shader_id == 0) {
			LOGGER_STR.log(Level.SEVERE, "Error creating vertex shader");

			return 0;
		}
		int fragment_shader_id = compileShader(GLES20.GL_FRAGMENT_SHADER, Shaders.FRAGMENT_SHADER_CODE);
		if (fragment_shader_id == 0) {
			LOGGER_STR.log(Level.SEVERE, "Error creating fragment shader");
			GLES20.glDeleteShader(vertex_shader_id);
			checkGLErrors("glDeleteShader");

			return 0;
		}

		int program_id = GLES20.glCreateProgram();
		checkGLErrors("glCreateProgram");
		GLES20.glAttachShader(program_id, vertex_shader_id);
		checkGLErrors("glAttachShader 1");
		GLES20.glAttachShader(program_id, fragment_shader_id);
		checkGLErrors("glAttachShader 2");
		GLES20.glLinkProgram(program_id);
		checkGLErrors("glLinkProgram");

		GLES20.glDetachShader(program_id, vertex_shader_id);
		checkGLErrors("glDetachShader 1");
		GLES20.glDeleteShader(vertex_shader_id);
		checkGLErrors("glDeleteShader 1");
		GLES20.glDetachShader(program_id, fragment_shader_id);
		checkGLErrors("glDetachShader 2");
		GLES20.glDeleteShader(fragment_shader_id);
		checkGLErrors("glDeleteShader 2");

		return program_id;
	}

	/**
	 * <p>Compile a shader of the given type.</p>
	 *
	 * @param type the type of shader to load (vertex or fragment)
	 * @param shader_code the source code of the shader
	 *
	 * @return the shader ID
	 */
	public static int compileShader(final int type, @NonNull final String shader_code) {
		clearGLErrors();

		int shader_id = GLES20.glCreateShader(type);
		checkGLErrors("glCreateShader");
		GLES20.glShaderSource(shader_id, shader_code);
		checkGLErrors("glShaderSource");
		GLES20.glCompileShader(shader_id);
		checkGLErrors("glCompileShader");

		int[] compile_status = new int[1];
		GLES20.glGetShaderiv(shader_id, GLES20.GL_COMPILE_STATUS, compile_status, 0);
		checkGLErrors("glGetShaderiv");
		if (compile_status[0] == GLES20.GL_FALSE) {
			String error_message = GLES20.glGetShaderInfoLog(shader_id);
			checkGLErrors("glGetShaderInfoLog");
			LOGGER_STR.log(Level.SEVERE, "Error compiling shader: " + error_message);
			GLES20.glDeleteShader(shader_id);
			checkGLErrors("glDeleteShader");

			return 0;
		}

		return shader_id;
	}

	/**
	 * <p>Clear all OpenGL errors.</p>
	 */
	public static void clearGLErrors() {
		while (GLES20.glGetError() != GLES20.GL_NO_ERROR);
	}

	/**
	 * <p>Check for OpenGL errors.</p>
	 *
	 * @param id the identifier for the operation being checked
	 */
	public static void checkGLErrors(@NonNull final String id) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			String error_string;
			switch (error) {
				case GLES20.GL_INVALID_ENUM:
					error_string = "GL_INVALID_ENUM";
					break;
				case GLES20.GL_INVALID_VALUE:
					error_string = "GL_INVALID_VALUE";
					break;
				case GLES20.GL_INVALID_OPERATION:
					error_string = "GL_INVALID_OPERATION";
					break;
				case GLES20.GL_INVALID_FRAMEBUFFER_OPERATION:
					error_string = "GL_INVALID_FRAMEBUFFER_OPERATION";
					break;
				case GLES20.GL_OUT_OF_MEMORY:
					error_string = "GL_OUT_OF_MEMORY";
					break;
				default:
					error_string = "Unknown error";
					break;
			}

			LOGGER_STR.log(Level.SEVERE, "OpenGL error on \"" + id + "\": " + error_string);
		}
	}
}
