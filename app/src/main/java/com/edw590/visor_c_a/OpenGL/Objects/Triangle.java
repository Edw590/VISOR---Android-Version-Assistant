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

package com.edw590.visor_c_a.OpenGL.Objects;

import android.opengl.Matrix;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.OpenGL.Vector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class Triangle extends Object {
	private FloatBuffer vertex_buffer = null;
	private FloatBuffer color_buffer = null;

	public Triangle(@NonNull final Vector first_vertex, @NonNull final Vector second_vertex,
					@NonNull final Vector third_vertex) {
		Vector[] vertices = new Vector[3];

		// Store final vertices
		vertices[0] = new Vector(first_vertex.x, first_vertex.y, first_vertex.z);
		vertices[1] = new Vector(second_vertex.x, second_vertex.y, second_vertex.z);
		vertices[2] = new Vector(third_vertex.x, third_vertex.y, third_vertex.z);

		restOfConstructor(vertices);
	}

	public Triangle(@NonNull final Vector center, final float width, final float height, final float main_angle,
					final float x_angle, final float y_angle, final float z_angle) {
		Vector[] vertices = new Vector[3];

		// Local triangle: vertex A at origin
		Vector A = new Vector(0.0f, 0.0f, 0.0f);  // Main vertex

		// Vertex B goes horizontally by width
		Vector B = new Vector(width, 0.0f, 0.0f);

		// Vertex C is height units away from A, at 'main_angle' from AB
		double radians = Math.toRadians(main_angle);
		float dx = (float) (height * StrictMath.cos(radians));
		float dy = (float) (height * StrictMath.sin(radians));
		Vector C = new Vector(dx, dy, 0.0f);

		// Compute centroid to center the triangle in local space
		float cx = (A.x + B.x + C.x) / 3.0f;
		float cy = (A.y + B.y + C.y) / 3.0f;
		float cz = (A.z + B.z + C.z) / 3.0f;

		// Shift all vertices to center the triangle
		A = A.subtract(cx, cy, cz);
		B = B.subtract(cx, cy, cz);
		C = C.subtract(cx, cy, cz);

		// Store final vertices
		vertices[0] = new Vector(A.x, A.y, A.z);
		vertices[1] = new Vector(B.x, B.y, B.z);
		vertices[2] = new Vector(C.x, C.y, C.z);

		restOfConstructor(vertices);

		// Apply rotation in 3D
		rotateM(x_angle, y_angle, z_angle);

		// Move to final position
		translateM(center.x, center.y, center.z);
	}

	private void restOfConstructor(@NonNull final Vector[] vertices_vectors) {
		float[] vertices = {
				vertices_vectors[0].x, vertices_vectors[0].y, vertices_vectors[0].z,
				vertices_vectors[1].x, vertices_vectors[1].y, vertices_vectors[1].z,
				vertices_vectors[2].x, vertices_vectors[2].y, vertices_vectors[2].z
		};

		vertex_buffer = ByteBuffer.allocateDirect(vertices.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		vertex_buffer.position(0);

		setColor(0.0f, 0.0f, 0.0f, 0.0f);
	}

	public void setColor(final float r, final float g, final float b, final float a) {
		float[] colors = {
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f
		};

		color_buffer = ByteBuffer.allocateDirect(colors.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(colors);
		color_buffer.position(0);
	}

	@NonNull
	public FloatBuffer getVertexBuffer() {
		return vertex_buffer;
	}

	@NonNull
	public FloatBuffer getColorBuffer() {
		return color_buffer;
	}

	@Override
	public void draw(@Nullable final float[] parent_model_matrix) {
		float[] model_matrix = getModelMatrix();

		if (parent_model_matrix != null) {
			Matrix.multiplyMM(model_matrix, 0, parent_model_matrix, 0, model_matrix, 0);
		}

		UtilsOpenGL.drawTriangle(this, model_matrix);
	}
}
