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

public final class Rectangle extends Object {
	private Triangle[] triangles = null;

	public Rectangle(@NonNull final Vector center, final float width, final float height,
					 final float x_angle, final float y_angle, final float z_angle) {
		this.center = center;

		Vector bottom_left = new Vector(center.x - width / 2, center.y - height / 2, center.z);
		Vector top_right = new Vector(bottom_left.x + width, bottom_left.y + height, bottom_left.z);

		triangles = new Triangle[]{
				new Triangle(bottom_left, width, height, 90, 0, 0, 0),
				new Triangle(top_right, -width, -height, 90, 0, 0, 0)
		};

		rotate(null, x_angle, y_angle, z_angle);
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
