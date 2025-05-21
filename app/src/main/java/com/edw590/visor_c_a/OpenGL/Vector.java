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

package com.edw590.visor_c_a.OpenGL;

import androidx.annotation.NonNull;

public final class Vector {
	public final float x;
	public final float y;
	public final float z;
	public final float w;

	public Vector(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		w = 0.0f;
	}

	public Vector(final float x, final float y, final float z, final float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@NonNull
	public Vector subtract(final float dx, final float dy, final float dz) {
		return new Vector(x - dx, y - dy, z - dz);
	}
}
