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

public class Icosahidral extends Object {
	public Icosahidral(@NonNull final Vector center, final float radius) {
		super(center);

		// Golden ratio
		float phi = (1 + (float) Math.sqrt(5)) / 2;

		// Normalize to radius
		float a = radius / (float) Math.sqrt(1 + phi * phi);
		float b = a * phi;

		// Vertices of the icosahedron
		vertices = new float[]{
				-a,  b,  0,
				 a,  b,  0,
				-a, -b,  0,

				 a, -b,  0,
				 0, -a,  b,
				 0,  a,  b,

				 0, -a, -b,
				 0,  a, -b,
				 b,  0, -a,

				 b,  0,  a,
				-b,  0, -a,
				-b,  0,  a
		};

		colors = new float[]{
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,

				1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,
				0.0f, 1.0f, 0.0f, 1.0f,

				0.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f, 0.0f, 1.0f,

				0.0f, 1.0f, 0.0f, 1.0f,
				0.0f, 0.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 1.0f,
		};

		// Faces of the icosahedron (20 triangles)
		indexes = new short[]{
				0, 11, 5,
				0, 5, 1,
				0, 1, 7,
				0, 7, 10,
				0, 10, 11,
				1, 5, 9,
				5, 11, 4,
				11, 10, 2,
				10, 7, 6,
				7, 1, 8,
				3, 9, 4,
				3, 4, 2,
				3, 2, 6,
				3, 6, 8,
				3, 8, 9,
				4, 9, 5,
				2, 4, 11,
				6, 2, 10,
				8, 6, 7,
				9, 8, 1
		};

		translateM(center.x, center.y, center.z);
	}
}
