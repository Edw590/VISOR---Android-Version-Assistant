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

import com.edw590.visor_c_a.OpenGL.UtilsOpenGL;
import com.edw590.visor_c_a.OpenGL.Vector;

import java.util.ArrayList;
import java.util.List;

public class Parallelepiped extends Object {
	private static final int NUM_TRIANGLES = 12;
	private static final int NUM_VERTICES = 36;

	public Parallelepiped(@NonNull final Vector center, final float width, final float height, final float depth,
						  final float x_angle, final float y_angle, final float z_angle) {
		// Generate vertices based on width, height, and depth
		float half_width = width / 2.0f;
		float half_height = height / 2.0f;
		float half_depth = depth / 2.0f;

		vertices = new float[]{
				// Front face
				-half_width, -half_height, half_depth,
				half_width, -half_height, half_depth,
				half_width, half_height, half_depth,
				-half_width, -half_height, half_depth,
				half_width, half_height, half_depth,
				-half_width, half_height, half_depth,

				// Back face
				-half_width, -half_height, -half_depth,
				-half_width, half_height, -half_depth,
				half_width, half_height, -half_depth,
				-half_width, -half_height, -half_depth,
				half_width, half_height, -half_depth,
				half_width, -half_height, -half_depth,

				// Left face
				-half_width, -half_height, -half_depth,
				-half_width, -half_height, half_depth,
				-half_width, half_height, half_depth,
				-half_width, -half_height, -half_depth,
				-half_width, half_height, half_depth,
				-half_width, half_height, -half_depth,

				// Right face
				half_width, -half_height, -half_depth,
				half_width, half_height, -half_depth,
				half_width, half_height, half_depth,
				half_width, -half_height, -half_depth,
				half_width, half_height, half_depth,
				half_width, -half_height, half_depth,

				// Top face
				-half_width, half_height, -half_depth,
				-half_width, half_height, half_depth,
				half_width, half_height, half_depth,
				-half_width, half_height, -half_depth,
				half_width, half_height, half_depth,
				half_width, half_height, -half_depth,

				// Bottom face
				-half_width, -half_height, -half_depth,
				half_width, -half_height, -half_depth,
				half_width, -half_height, half_depth,
				-half_width, -half_height, -half_depth,
				half_width, -half_height, half_depth,
				-half_width, -half_height, half_depth
		};

		int colors_floats_len = NUM_VERTICES * UtilsOpenGL.FLOATS_PER_VERTEX_COLOR;
		List<Float> colors_list = new ArrayList<>(colors_floats_len);
		for (int i = 0; i < NUM_TRIANGLES; i++) {
			colors_list.add(0.0f);
			colors_list.add(1.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);

			colors_list.add(0.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);
			colors_list.add(1.0f);

			colors_list.add(1.0f);
			colors_list.add(0.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);
		}
		colors = new float[colors_floats_len];
		for (int i = 0; i < colors_floats_len; i++) {
			colors[i] = colors_list.get(i);
		}

		rotateM(x_angle, y_angle, z_angle);
		translateM(center.x, center.y, center.z);
	}
}
