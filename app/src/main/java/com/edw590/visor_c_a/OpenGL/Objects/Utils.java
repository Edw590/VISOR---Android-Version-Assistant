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

public class Utils {

	static void applyRotation(@NonNull final Vector point, @NonNull final Vector origin,
										final float pitch, final float roll, final float yaw) {
		// Translate point to origin
		float x = point.x - origin.x;
		float y = point.y - origin.y;
		float z = point.z - origin.z;

		double yaw_rad = Math.toRadians(yaw);
		double pitch_rad = Math.toRadians(pitch);
		double roll_rad = Math.toRadians(roll);

		// Apply yaw (rotation around Z-axis)
		float tempX = x * (float) StrictMath.cos(yaw_rad) - y * (float) StrictMath.sin(yaw_rad);
		float tempY = x * (float) StrictMath.sin(yaw_rad) + y * (float) StrictMath.cos(yaw_rad);
		x = tempX;
		y = tempY;

		// Apply pitch (rotation around X-axis)
		float tempZ = z * (float) StrictMath.cos(pitch_rad) - y * (float) StrictMath.sin(pitch_rad);
		tempY = z * (float) StrictMath.sin(pitch_rad) + y * (float) StrictMath.cos(pitch_rad);
		z = tempZ;
		y = tempY;

		// Apply roll (rotation around Y-axis)
		tempX = x * (float) StrictMath.cos(roll_rad) + z * (float) StrictMath.sin(roll_rad);
		tempZ = -x * (float) StrictMath.sin(roll_rad) + z * (float) StrictMath.cos(roll_rad);
		x = tempX;
		z = tempZ;

		// Translate point back
		point.x = x + origin.x;
		point.y = y + origin.y;
		point.z = z + origin.z;
	}
}
