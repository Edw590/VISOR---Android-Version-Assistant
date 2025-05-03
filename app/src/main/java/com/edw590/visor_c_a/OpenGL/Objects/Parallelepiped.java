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

public final class Parallelepiped extends Object {
	private Rectangle[] rectangles = null;

	public Parallelepiped(@NonNull final Vector center, final float width, final float height, final float depth,
						  final float x_angle, final float y_angle, final float z_angle) {
		this.center = center;

		rectangles = new Rectangle[6];
		rectangles[0] = new Rectangle(new Vector(center.x, center.y, center.z + depth / 2), width, height, 0, 0, 0);
		rectangles[1] = new Rectangle(new Vector(center.x, center.y, center.z - depth / 2), width, height, 0, 0, 90);
		rectangles[2] = new Rectangle(new Vector(center.x + width / 2, center.y, center.z), height, depth, 0, 90, 0);
		rectangles[3] = new Rectangle(new Vector(center.x - width / 2, center.y, center.z), height, depth, 0, 90, 90);
		rectangles[4] = new Rectangle(new Vector(center.x, center.y + height / 2, center.z), width, depth, 90, 0, 0);
		rectangles[5] = new Rectangle(new Vector(center.x, center.y - height / 2, center.z), width, depth, 90, 0, 90);

		rotate(null, x_angle, y_angle, z_angle);
	}

	@Override
	public void translate(final float x_offset, final float y_offset, final float z_offset) {
		super.translate(x_offset, y_offset, z_offset);

		for (final Rectangle rectangle : rectangles) {
			rectangle.translate(x_offset, y_offset, z_offset);
		}
	}

	@Override
	public void rotate(@Nullable final Vector center, final float x_angle, final float y_angle, final float z_angle) {
		Vector used_center = center;
		if (center == null) {
			used_center = this.center;
		}

		for (final Rectangle rectangle : rectangles) {
			rectangle.rotate(used_center, x_angle, y_angle, z_angle);
		}
	}

	@Override
	public void scale(final float x_scale, final float y_scale, final float z_scale) {
		for (final Rectangle rectangle : rectangles) {
			rectangle.scale(x_scale, y_scale, z_scale);
		}
	}

	@Override
	public void draw() {
		for (final Rectangle rectangle : rectangles) {
			rectangle.draw();
		}
	}
}
