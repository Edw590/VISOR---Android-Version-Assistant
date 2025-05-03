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

import java.util.ArrayList;
import java.util.List;

public final class Sphere extends Object {
	private final List<Triangle> triangles = new ArrayList<>(30);

	/**
	 * Generates a sphere using spherical coordinates.
	 *
	 * @param center center of the sphere
	 * @param radius radius of the sphere
	 * @param stacks number of vertical segments (latitude)
	 * @param slices number of horizontal segments (longitude)
	 */
	public Sphere(@NonNull final Vector center, final float radius, final int stacks, final int slices) {
		this.center = center;

		for (int i = 0; i < stacks; i++) {
			float phi1 = (float) Math.PI * i / stacks;
			float phi2 = (float) Math.PI * (i + 1) / stacks;

			for (int j = 0; j < slices; j++) {
				float theta1 = (float) (2 * Math.PI) * j / slices;
				float theta2 = (float) (2 * Math.PI) * (j + 1) / slices;

				// Calculate points
				Vector p1 = sphericalToCartesian(center, radius, phi1, theta1);
				Vector p2 = sphericalToCartesian(center, radius, phi2, theta1);
				Vector p3 = sphericalToCartesian(center, radius, phi2, theta2);
				Vector p4 = sphericalToCartesian(center, radius, phi1, theta2);

				// Two triangles per quad
				triangles.add(new Triangle(p1, p2, p3));
				triangles.add(new Triangle(p1, p3, p4));
			}
		}
	}

	private static Vector sphericalToCartesian(@NonNull final Vector center, final float radius, final float phi, final float theta) {
		float x = (float) (radius * StrictMath.sin(phi) * StrictMath.cos(theta)) + center.x;
		float y = (float) (radius * StrictMath.cos(phi)) + center.y;
		float z = (float) (radius * StrictMath.sin(phi) * StrictMath.sin(theta)) + center.z;

		return new Vector(x, y, z);
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
