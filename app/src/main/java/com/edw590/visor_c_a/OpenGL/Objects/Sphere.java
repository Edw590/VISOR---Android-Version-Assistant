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

import java.util.ArrayList;
import java.util.List;

public final class Sphere extends Object {
	/**
	 * Creates a sphere using spherical coordinates.
	 *
	 * @param center center of the sphere
	 * @param radius radius of the sphere
	 * @param stacks number of vertical segments (latitude)
	 * @param sectors number of horizontal segments (longitude)
	 */
	public Sphere(@NonNull final Vector center, final float radius, final int stacks, final int sectors) {
		List<Float> vertices_list = new ArrayList<>(1500);
		double stack_step = Math.PI / stacks;
		double sector_step = 2 * Math.PI / sectors;
		for (int i = 0; i <= stacks; ++i) {
			double stack_angle = Math.PI / 2 - i * stack_step;
			double xy = radius * StrictMath.cos(stack_angle);
			double z = radius * StrictMath.sin(stack_angle);

			for (int j = 0; j <= sectors; ++j) {
				double sector_angle = j * sector_step;

				double x = xy * StrictMath.cos(sector_angle);
				double y = xy * StrictMath.sin(sector_angle);

				vertices_list.add((float) x);
				vertices_list.add((float) y);
				vertices_list.add((float) z);
			}
		}
		// This exists because the original way was with indices - and glitches would appear.
		// They seem to be gone having this below and no indices, so I'm leaving it like this.
		List<Float> final_vertices = new ArrayList<>(7500);
		for (int i = 0; i < stacks; ++i) {
			for (int j = 0; j < sectors; ++j) {
				int k1 = i * (sectors + 1) + j;
				int k2 = k1 + sectors + 1;

				int[] tri = {
						k1, k2, k1 + 1,
						k1 + 1, k2, k2 + 1
				};

				for (final int k : tri) {
					int index = k * 3;
					final_vertices.add(vertices_list.get(index));
					final_vertices.add(vertices_list.get(index + 1));
					final_vertices.add(vertices_list.get(index + 2));
				}
			}
		}
		vertices = new float[final_vertices.size()];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = final_vertices.get(i);
		}

		List<Float> colors_list = new ArrayList<>(10000);
		for (int i = 0; i < final_vertices.size() / (3 * 3); i++) {
			colors_list.add(1.0f);
			colors_list.add(0.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);

			colors_list.add(0.0f);
			colors_list.add(1.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);

			colors_list.add(0.0f);
			colors_list.add(0.0f);
			colors_list.add(1.0f);
			colors_list.add(1.0f);
		}
		colors = new float[colors_list.size()];
		for (int i = 0; i < colors_list.size(); i++) {
			colors[i] = colors_list.get(i);
		}

		translateM(center.x, center.y, center.z);
	}

	private static Vector sphericalToCartesian(final float radius, final float phi, final float theta) {
		float x = (float) (radius * StrictMath.sin(phi) * StrictMath.cos(theta));
		float y = (float) (radius * StrictMath.cos(phi));
		float z = (float) (radius * StrictMath.sin(phi) * StrictMath.sin(theta));

		return new Vector(x, y, z);
	}
}
