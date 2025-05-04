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

public abstract class Object {
	@NonNull public float[] scale_matrix = new float[16];
	@NonNull public float[] translation_matrix = new float[16];
	@NonNull public float[] rotation_matrix = new float[16];

	public Object() {
		Matrix.setIdentityM(scale_matrix, 0);
		Matrix.setIdentityM(translation_matrix, 0);
		Matrix.setIdentityM(rotation_matrix, 0);
	}

	public abstract void draw(@Nullable final float[] parent_model_matrix);

	public final void rotateM(final float x_angle, final float y_angle, final float z_angle) {
		Matrix.rotateM(rotation_matrix, 0, x_angle, 1.0f, 0.0f, 0.0f);
		Matrix.rotateM(rotation_matrix, 0, y_angle, 0.0f, 1.0f, 0.0f);
		Matrix.rotateM(rotation_matrix, 0, z_angle, 0.0f, 0.0f, 1.0f);
	}

	public final void translateM(final float x_offset, final float y_offset, final float z_offset) {
		Matrix.translateM(translation_matrix, 0, x_offset, y_offset, z_offset);
	}

	public final void scaleM(final float x_scale, final float y_scale, final float z_scale) {
		Matrix.scaleM(scale_matrix, 0, x_scale, y_scale, z_scale);
	}

	@NonNull
	public final float[] getModelMatrix() {
		float[] model_matrix = new float[16];
		Matrix.multiplyMM(model_matrix, 0, rotation_matrix, 0, scale_matrix, 0);
		Matrix.multiplyMM(model_matrix, 0, translation_matrix, 0, model_matrix, 0);

		return model_matrix;
	}
}
