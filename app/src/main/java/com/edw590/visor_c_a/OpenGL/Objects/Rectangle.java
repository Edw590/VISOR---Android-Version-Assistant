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

import com.edw590.visor_c_a.OpenGL.Vector;

public final class Rectangle extends Object {

	public Rectangle(@NonNull final Vector center, final float width, final float height,
					 final float x_angle, final float y_angle, final float z_angle) {
		// Generate vertices based on width, length, and depth
		float half_width = width / 2.0f;
		float half_length = height / 2.0f;

		vertices = new float[]{
				-half_width, -half_length, 0,
				half_width, -half_length, 0,
				half_width, half_length, 0,
				-half_width, -half_length, 0,
				half_width, half_length, 0,
				-half_width, half_length, 0,
		};

		colors = new float[]{
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
		};

		rotateM(x_angle, y_angle, z_angle);
		translateM(center.x, center.y, center.z);
	}
}
