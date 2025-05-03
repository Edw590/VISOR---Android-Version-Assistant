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

public abstract class Object {
	@NonNull public Vector center;

	public void translate(final float x_offset, final float y_offset, final float z_offset) {
		center.x += x_offset;
		center.y += y_offset;
		center.z += z_offset;
	}

	public abstract void rotate(@Nullable final Vector center, final float x_angle, final float y_angle,
								final float z_angle);

	public abstract void scale(final float x_scale, final float y_scale, final float z_scale);

	public abstract void draw();
}
