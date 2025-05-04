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

public class Icosahidral extends Object {
	private Triangle[] triangles = null;

	public Icosahidral(@NonNull final Vector center, final float radius) {
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
					new Vector(first_vertex[0], first_vertex[1], first_vertex[2]),
					new Vector(second_vertex[0], second_vertex[1], second_vertex[2]),
					new Vector(third_vertex[0], third_vertex[1], third_vertex[2])
			);
		}

		translateM(center.x, center.y, center.z);
	}

	@Override
	public void draw(@Nullable final float[] parent_model_matrix) {
		float[] model_matrix = getModelMatrix();

		if (parent_model_matrix != null) {
			Matrix.multiplyMM(model_matrix, 0, parent_model_matrix, 0, model_matrix, 0);
		}

		float[] final_model_matrix = new float[16];
		for (final Triangle triangle : triangles) {
			Matrix.multiplyMM(final_model_matrix, 0, model_matrix, 0, triangle.getModelMatrix(), 0);

			UtilsOpenGL.drawTriangle(triangle, final_model_matrix);
		}
	}
}
