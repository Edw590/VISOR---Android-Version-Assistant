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

public final class Rectangle extends Object {
	private Triangle[] triangles = null;

	public Rectangle(@NonNull final Vector center, final float width, final float height,
					 final float x_angle, final float y_angle, final float z_angle) {
		triangles = new Triangle[2];
		triangles[0] = new Triangle(new Vector(-width / 6.0f, -height / 6.0f, 0.0f), width, height, 90, 0, 0, 0);
		triangles[1] = new Triangle(new Vector(width / 6.0f, height / 6.0f, 0.0f), -width, -height, 90, 0, 0, 0);

		rotateM(x_angle, y_angle, z_angle);
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
