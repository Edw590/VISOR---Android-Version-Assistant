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

package com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects;

import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.AugmentedReality.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.Vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class Object {
	@NonNull float vertices[] = null;
	@NonNull float colors[] = null;
	@Nullable short indexes[] = null;

	FloatBuffer vertex_buffer = null;
	FloatBuffer color_buffer = null;
	ByteBuffer index_buffer = null;

	@NonNull private final float[] scale_matrix = new float[16];
	@NonNull private final float[] translation_matrix = new float[16];
	@NonNull private final float[] rotation_matrix = new float[16];

	Vector center = new Vector(0.0f, 0.0f, 0.0f);

	public Object(@NonNull final Vector center) {
		this.center = center;

		Matrix.setIdentityM(scale_matrix, 0);
		Matrix.setIdentityM(translation_matrix, 0);
		Matrix.setIdentityM(rotation_matrix, 0);
	}

	public final void rotateM(final float x_angle, final float y_angle, final float z_angle) {
		Matrix.rotateM(rotation_matrix, 0, x_angle, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(rotation_matrix, 0, y_angle, 0.0f, 1.0f, 0.0f);
		Matrix.rotateM(rotation_matrix, 0, z_angle, 0.0f, 0.0f, 1.0f);
	}

	public final void translateM(final float x_offset, final float y_offset, final float z_offset) {
		Matrix.translateM(translation_matrix, 0, x_offset, y_offset, z_offset);
	}

	public final void scaleM(final float x_scale, final float y_scale, final float z_scale) {
		Matrix.scaleM(scale_matrix, 0, x_scale, y_scale, z_scale);
	}

	@NonNull
	public final FloatBuffer getVertexBuffer() {
		if (vertex_buffer == null) {
			vertex_buffer = ByteBuffer.allocateDirect(vertices.length * UtilsOpenGL.FLOAT_BYTES)
					.order(ByteOrder.nativeOrder())
					.asFloatBuffer()
					.put(vertices);
			vertex_buffer.position(0);
		}

		return vertex_buffer;
	}

	public final int getVertexFloatsCount() {
		return vertices.length;
	}

	@NonNull
	public final FloatBuffer getColorBuffer() {
		if (color_buffer == null) {
			color_buffer = ByteBuffer.allocateDirect(colors.length * UtilsOpenGL.FLOAT_BYTES)
					.order(ByteOrder.nativeOrder())
					.asFloatBuffer()
					.put(colors);
			color_buffer.position(0);
		}

		return color_buffer;
	}

	@Nullable
	public final ByteBuffer getIndexBuffer() {
		if (index_buffer == null && indexes != null) {
			index_buffer = ByteBuffer.allocateDirect(indexes.length * UtilsOpenGL.SHORT_BYTES)
					.order(ByteOrder.nativeOrder());
			for (final short index : indexes) {
				index_buffer.putShort(index);
			}
			index_buffer.position(0);
		}

		return index_buffer;
	}

	public final int getIndexCount() {
		return (indexes == null) ? 0 : indexes.length;
	}

	@NonNull
	public final float[] getModelMatrix() {
		float[] model_matrix = new float[16];
		Matrix.multiplyMM(model_matrix, 0, rotation_matrix, 0, scale_matrix, 0);
		Matrix.multiplyMM(model_matrix, 0, translation_matrix, 0, model_matrix, 0);

		return model_matrix;
	}

	@NonNull
	public Vector getCenter() {
		return center;
	}

	public void draw() {
		UtilsOpenGL.draw(this);
	}
}
