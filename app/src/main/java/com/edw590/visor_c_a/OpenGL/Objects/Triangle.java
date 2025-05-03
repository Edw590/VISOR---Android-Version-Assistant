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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.OpenGL.Vector;
import com.edw590.visor_c_a.OpenGL.Vertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class Triangle extends Object {
	private final Vertex[] vertexes = new Vertex[3];
	private FloatBuffer vertex_buffer = null;
	private FloatBuffer color_buffer = null;

	public Triangle(@NonNull final Vector first_vertex, final float width, final float height, final float main_angle,
					final float x_angle, final float y_angle, final float z_angles) {
		// First vertex is the main vertex
		vertexes[0] = new Vertex(first_vertex.x, first_vertex.y, first_vertex.z);

		// Calculate the second vertex
		Vector second_vertex = new Vector(
				first_vertex.x + width,
				first_vertex.y,
				first_vertex.z
		);
		vertexes[1] = new Vertex(second_vertex.x, second_vertex.y, second_vertex.z);

		double main_angle_rad = Math.toRadians(main_angle);
		float dx = (float) (height * StrictMath.cos(main_angle_rad));
		float dy = (float) (height * StrictMath.sin(main_angle_rad));

		// Still relative to A (first_vertex), but at angle
		Vector third_vertex = new Vector(
				first_vertex.x + dx,
				first_vertex.y + dy,
				first_vertex.z
		);
		vertexes[2] = new Vertex(third_vertex.x, third_vertex.y, third_vertex.z);

		// Set the center of the triangle
		center = new Vector(
				(first_vertex.x + second_vertex.x + third_vertex.x) / 3,
				(first_vertex.y + second_vertex.y + third_vertex.y) / 3,
				(first_vertex.z + second_vertex.z + third_vertex.z) / 3
		);


		// Set the vertices float array
		float[] vertices = {
				vertexes[0].position.x, vertexes[0].position.y, vertexes[0].position.z,
				vertexes[1].position.x, vertexes[1].position.y, vertexes[1].position.z,
				vertexes[2].position.x, vertexes[2].position.y, vertexes[2].position.z
		};

		// Set the colors float array
		float[] colors = {
				vertexes[0].color.x, vertexes[0].color.y, vertexes[0].color.z, vertexes[0].color.w,
				vertexes[1].color.x, vertexes[1].color.y, vertexes[1].color.z, vertexes[1].color.w,
				vertexes[2].color.x, vertexes[2].color.y, vertexes[2].color.z , vertexes[2].color.w
		};
		colors = new float[]{
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f
		};

		vertex_buffer = ByteBuffer.allocateDirect(vertices.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(vertices);
		vertex_buffer.position(0);

		color_buffer = ByteBuffer.allocateDirect(colors.length * UtilsOpenGL.FLOAT_BYTES)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer()
				.put(colors);
		color_buffer.position(0);

		// Rotate the triangle
		rotate(null, x_angle, y_angle, z_angles);
	}

	public void setColor(final float r, final float g, final float b, final float a) {
		for (final Vertex vertex : vertexes) {
			vertex.color.x = r;
			vertex.color.y = g;
			vertex.color.z = b;
			vertex.color.w = a;
		}
	}

	public void rotate(@Nullable final Vector center, final float x_angle, final float y_angle, final float z_angle) {
		Vector used_center = center;
		if (center == null) {
			used_center = this.center;
		}

		for (int i = 0; i < vertex_buffer.capacity() / 3; i++) {
			Vector vertex = new Vector(
					vertex_buffer.get(i * 3),
					vertex_buffer.get(i * 3 + 1),
					vertex_buffer.get(i * 3 + 2)
			);
			Utils.applyRotation(vertex, used_center, x_angle, y_angle, z_angle);
			vertex_buffer.put(i * 3, vertex.x);
			vertex_buffer.put(i * 3 + 1, vertex.y);
			vertex_buffer.put(i * 3 + 2, vertex.z);
		}
	}

	public void translate(final float x_offset, final float y_offset, final float z_offset) {
		for (int i = 0; i < vertex_buffer.capacity() / 3; i++) {
			float x = vertex_buffer.get(i * 3) + x_offset;
			float y = vertex_buffer.get(i * 3 + 1) + y_offset;
			float z = vertex_buffer.get(i * 3 + 2) + z_offset;
			vertex_buffer.put(i * 3, x);
			vertex_buffer.put(i * 3 + 1, y);
			vertex_buffer.put(i * 3 + 2, z);
		}
	}

	public void scale(final float x_scale, final float y_scale, final float z_scale) {
		for (int i = 0; i < vertex_buffer.capacity() / 3; i++) {
			float x = vertex_buffer.get(i * 3) * x_scale;
			float y = vertex_buffer.get(i * 3 + 1) * y_scale;
			float z = vertex_buffer.get(i * 3 + 2) * z_scale;
			vertex_buffer.put(i * 3, x);
			vertex_buffer.put(i * 3 + 1, y);
			vertex_buffer.put(i * 3 + 2, z);
		}
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
	public void draw() {
		UtilsOpenGL.drawTriangle(this);
	}
}
