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

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.AugmentedReality.OpenGL.Vector;

import java.nio.FloatBuffer;

public final class Triangle extends Object {
	private FloatBuffer vertex_buffer = null;
	private FloatBuffer color_buffer = null;

	public Triangle(@NonNull final Vector center, final float width, final float height, final float main_angle,
					final float x_angle, final float y_angle, final float z_angle) {
		super(center);

		Vector[] vertices_vectors = new Vector[3];

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
		vertices_vectors[0] = new Vector(A.x, A.y, A.z);
		vertices_vectors[1] = new Vector(B.x, B.y, B.z);
		vertices_vectors[2] = new Vector(C.x, C.y, C.z);

		vertices = new float[]{
				vertices_vectors[0].x, vertices_vectors[0].y, vertices_vectors[0].z,
				vertices_vectors[1].x, vertices_vectors[1].y, vertices_vectors[1].z,
				vertices_vectors[2].x, vertices_vectors[2].y, vertices_vectors[2].z
		};

		colors = new float[]{
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f
		};

		// Apply rotation in 3D
		rotateM(x_angle, y_angle, z_angle);

		// Move to final position
		translateM(center.x, center.y, center.z);
	}
}
