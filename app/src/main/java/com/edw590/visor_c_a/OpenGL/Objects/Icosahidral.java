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

import com.edw590.visor_c_a.OpenGL.Vector;

public class Icosahidral extends Object {
	private Triangle[] triangles = null;

	public Icosahidral(@NonNull final Vector center, final float radius) {
		this.center = center;

		triangles = new Triangle[20];

		// Golden ratio
		float phi = (1 + (float) Math.sqrt(5)) / 2;

		// Normalize to radius
		float a = radius / (float) Math.sqrt(1 + phi * phi);
		float b = a * phi;

		// Vertices of the icosahedron
		float[][] vertices = {
				{-a,  b,  0}, { a,  b,  0}, {-a, -b,  0}, { a, -b,  0},
				{ 0, -a,  b}, { 0,  a,  b}, { 0, -a, -b}, { 0,  a, -b},
				{ b,  0, -a}, { b,  0,  a}, {-b,  0, -a}, {-b,  0,  a}
		};

		// Faces of the icosahedron (20 triangles)
		int[][] faces = {
				{0, 11, 5}, {0, 5, 1}, {0, 1, 7}, {0, 7, 10}, {0, 10, 11},
				{1, 5, 9}, {5, 11, 4}, {11, 10, 2}, {10, 7, 6}, {7, 1, 8},
				{3, 9, 4}, {3, 4, 2}, {3, 2, 6}, {3, 6, 8}, {3, 8, 9},
				{4, 9, 5}, {2, 4, 11}, {6, 2, 10}, {8, 6, 7}, {9, 8, 1}
		};

		for (int i = 0; i < faces.length; i++) {
			float[] first_vertex = vertices[faces[i][0]];
			float[] second_vertex = vertices[faces[i][1]];
			float[] third_vertex = vertices[faces[i][2]];

			// Offset vertices by the center
			triangles[i] = new Triangle(
					new Vector(first_vertex[0] + center.x, first_vertex[1] + center.y, first_vertex[2] + center.z),
					new Vector(second_vertex[0] + center.x, second_vertex[1] + center.y, second_vertex[2] + center.z),
					new Vector(third_vertex[0] + center.x, third_vertex[1] + center.y, third_vertex[2] + center.z)
			);
		}
	}

	@Override
	public void translate(final float x_offset, final float y_offset, final float z_offset) {
		super.translate(x_offset, y_offset, z_offset);

		for (final Triangle triangle : triangles) {
			triangle.translate(x_offset, y_offset, z_offset);
		}
	}

	@Override
	public void rotate(@Nullable final Vector center, final float x_angle, final float y_angle, final float z_angle) {
		Vector used_center = center;
		if (center == null) {
			used_center = this.center;
		}

		for (final Triangle triangle : triangles) {
			triangle.rotate(used_center, x_angle, y_angle, z_angle);
		}
	}

	@Override
	public void scale(final float x_scale, final float y_scale, final float z_scale) {
		for (final Triangle triangle : triangles) {
			triangle.scale(x_scale, y_scale, z_scale);
		}
	}

	@Override
	public void draw() {
		for (final Triangle triangle : triangles) {
			triangle.draw();
		}
	}
}
