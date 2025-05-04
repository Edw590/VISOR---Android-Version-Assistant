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

import java.util.ArrayList;
import java.util.List;

public final class Sphere extends Object {
	private final List<Triangle> triangles = new ArrayList<>(30);

	/**
	 * Creates a sphere using spherical coordinates.
	 *
	 * @param center center of the sphere
	 * @param radius radius of the sphere
	 * @param stacks number of vertical segments (latitude)
	 * @param slices number of horizontal segments (longitude)
	 */
	public Sphere(@NonNull final Vector center, final float radius, final int stacks, final int slices) {
		for (int i = 0; i < stacks; i++) {
			float phi1 = (float) Math.PI * i / stacks;
			float phi2 = (float) Math.PI * (i + 1) / stacks;

			for (int j = 0; j < slices; j++) {
				float theta1 = (float) (2 * Math.PI) * j / slices;
				float theta2 = (float) (2 * Math.PI) * (j + 1) / slices;

				// Calculate points
				Vector p1 = sphericalToCartesian(radius, phi1, theta1);
				Vector p2 = sphericalToCartesian(radius, phi2, theta1);
				Vector p3 = sphericalToCartesian(radius, phi2, theta2);
				Vector p4 = sphericalToCartesian(radius, phi1, theta2);

				// Two triangles per quad
				triangles.add(new Triangle(p1, p2, p3));
				triangles.add(new Triangle(p1, p3, p4));
			}
		}

		translateM(center.x, center.y, center.z);
	}

	private static Vector sphericalToCartesian(final float radius, final float phi, final float theta) {
		float x = (float) (radius * StrictMath.sin(phi) * StrictMath.cos(theta));
		float y = (float) (radius * StrictMath.cos(phi));
		float z = (float) (radius * StrictMath.sin(phi) * StrictMath.sin(theta));

		return new Vector(x, y, z);
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
