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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.opengl.Matrix;

import androidx.annotation.NonNull;

public class GyroRotationCorrection {
	private static final float DRIFT_THRESHOLD = (float) Math.toRadians(3);

	private final float[] accel_data = new float[3];
	private final float[] magnet_data = new float[3];
	private float[] gyro_rotation_matrix = new float[9];
	private long last_gyro_timestamp = 0;

	// At startup: collect gyro samples over ~1 second
	private int gyro_sample_count = 0;
	private final float[] gyro_bias = new float[3];
	private boolean calibrated = false;

	private final float[] view_matrix = new float[16];

	private boolean gyro_in_use = false;

	public GyroRotationCorrection() {
		reset();
	}

	public void reset() {
		// Identity matrix
		gyro_rotation_matrix = new float[]{
				1, 0, 0,
				0, 1, 0,
				0, 0, 1,
		};
		last_gyro_timestamp = 0;
	}

	@NonNull
	public float[] onSensorChanged(@NonNull final SensorEvent event) {
		switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, accel_data, 0, 3);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				System.arraycopy(event.values, 0, magnet_data, 0, 3);
				break;
			case Sensor.TYPE_GYROSCOPE:
				handleGyro(event);
				break;
			default:
				// Ignore other sensor types
				return view_matrix;
		}

		correctGyro();

		return view_matrix;
	}

	private void calibrateGyro(SensorEvent event) {
		for (int i = 0; i < 3; i++) {
			gyro_bias[i] += event.values[i];
		}
		gyro_sample_count++;

		if (gyro_sample_count >= 100) {
			for (int i = 0; i < 3; i++) {
				gyro_bias[i] /= gyro_sample_count;
			}
			calibrated = true;
		}
	}

	private void handleGyro(SensorEvent event) {
		if (!calibrated) {
			calibrateGyro(event);

			return;
		}

		float[] corrected_gyro = new float[3];
		for (int i = 0; i < 3; i++) {
			corrected_gyro[i] = event.values[i] - gyro_bias[i];
		}

		if (last_gyro_timestamp == 0) {
			last_gyro_timestamp = event.timestamp;

			return;
		}

		float dt = (event.timestamp - last_gyro_timestamp) * 1.0f / 1_000_000_000.0f;
		last_gyro_timestamp = event.timestamp;

		float[] delta_vector = new float[4];
		getRotationVectorFromGyro(corrected_gyro, delta_vector, dt);
		float[] delta_matrix = new float[9];
		SensorManager.getRotationMatrixFromVector(delta_matrix, delta_vector);
		gyro_rotation_matrix = matrixMultiply(gyro_rotation_matrix, delta_matrix);

		gyro_in_use = true;
	}

	/**
	 * <p>Corrects the gyroscope rotation matrix using accelerometer and magnetometer data.</p>
	 * <p>The gyroscope can drift (a lot) from its original position if the device moves too fast, so we need to correct
	 * it - this is done by comparing the gyroscope rotation matrix with the one obtained from the accelerometer and
	 * magnetometer and if the difference is too big, we reset the gyroscope rotation matrix to the one obtained from
	 * the accelerometer and magnetometer.</p>
	 */
	private void correctGyro() {
		float[] acc_mag_matrix = new float[9];
		if (!SensorManager.getRotationMatrix(acc_mag_matrix, null, accel_data, magnet_data)) {
			return;
		}

		// gyro_rotation_matrix is your rotation from gyro integration
		// acc_mag_matrix is the one from accelerometer + magnetometer

		if (!gyro_in_use) {
			// If gyro is not in use (maybe not available on the device, or just no data yet), just use the
			// acc_mag_matrix.
			convertToOpenGLMatrix(acc_mag_matrix, view_matrix);

			return;
		}

		// Convert both matrices to quaternions
		float[] gyro_quat = rotationMatrixToQuaternion(gyro_rotation_matrix);
		float[] acc_mag_quat = rotationMatrixToQuaternion(acc_mag_matrix);

		// Compute angle difference (in radians) between the two quaternions
		float angle_diff = quaternionAngleDifference(gyro_quat, acc_mag_quat);

		if (angle_diff > DRIFT_THRESHOLD) {
			// Reset gyro drift
			System.arraycopy(acc_mag_matrix, 0, gyro_rotation_matrix, 0, 9);
		}

		// Done! This is your view matrix now
		convertToOpenGLMatrix(gyro_rotation_matrix, view_matrix);
	}

	private void getRotationVectorFromGyro(float[] gyro, float[] delta_vector, float dt) {
		float omega_magnitude = (float) StrictMath.sqrt(gyro[0]*gyro[0] + gyro[1]*gyro[1] + gyro[2]*gyro[2]);

		if (omega_magnitude > 0.0001f) {
			gyro[0] /= omega_magnitude;
			gyro[1] /= omega_magnitude;
			gyro[2] /= omega_magnitude;
		}

		float theta_over_two = omega_magnitude * dt / 2.0f;
		float sin = (float) StrictMath.sin(theta_over_two);
		float cos = (float) StrictMath.cos(theta_over_two);

		delta_vector[0] = sin * gyro[0];
		delta_vector[1] = sin * gyro[1];
		delta_vector[2] = sin * gyro[2];
		delta_vector[3] = cos;
	}

	private float[] matrixMultiply(float[] A, float[] B) {
		float[] result = new float[9];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				result[3*i + j] =
						A[3*i + 0] * B[0 + j] +
								A[3*i + 1] * B[3 + j] +
								A[3*i + 2] * B[6 + j];
			}
		}

		return result;
	}

	private static float[] rotationMatrixToQuaternion(float[] R) {
		float[] quat = new float[4];
		float trace = R[0] + R[4] + R[8];

		if (trace > 0) {
			float s = (float) Math.sqrt(trace + 1.0f) * 2f;
			quat[3] = 0.25f * s;
			quat[0] = (R[7] - R[5]) / s;
			quat[1] = (R[2] - R[6]) / s;
			quat[2] = (R[3] - R[1]) / s;
		} else if ((R[0] > R[4]) && (R[0] > R[8])) {
			float s = (float) Math.sqrt(1.0f + R[0] - R[4] - R[8]) * 2f;
			quat[3] = (R[7] - R[5]) / s;
			quat[0] = 0.25f * s;
			quat[1] = (R[1] + R[3]) / s;
			quat[2] = (R[2] + R[6]) / s;
		} else if (R[4] > R[8]) {
			float s = (float) Math.sqrt(1.0f + R[4] - R[0] - R[8]) * 2f;
			quat[3] = (R[2] - R[6]) / s;
			quat[0] = (R[1] + R[3]) / s;
			quat[1] = 0.25f * s;
			quat[2] = (R[5] + R[7]) / s;
		} else {
			float s = (float) Math.sqrt(1.0f + R[8] - R[0] - R[4]) * 2f;
			quat[3] = (R[3] - R[1]) / s;
			quat[0] = (R[2] + R[6]) / s;
			quat[1] = (R[5] + R[7]) / s;
			quat[2] = 0.25f * s;
		}

		return quat;
	}

	private float quaternionAngleDifference(float[] q1, float[] q2) {
		// dot product gives cos(theta/2), so angle = 2 * acos(dot)
		float dot = 0;
		for (int i = 0; i < 4; i++) {
			dot += q1[i] * q2[i];
		}
		dot = Math.max(-1.0f, Math.min(1.0f, dot)); // clamp to avoid NaN

		return (float)(2.0 * Math.acos(Math.abs(dot))); // radians
	}

	private static void convertToOpenGLMatrix(@NonNull final float[] rotation_matrix_3x3,
											  @NonNull final float[] opengl_matrix_4x4) {
		// Clear to identity
		Matrix.setIdentityM(opengl_matrix_4x4, 0);

		opengl_matrix_4x4[0] = rotation_matrix_3x3[0];
		opengl_matrix_4x4[1] = rotation_matrix_3x3[1];
		opengl_matrix_4x4[2] = rotation_matrix_3x3[2];

		opengl_matrix_4x4[4] = rotation_matrix_3x3[3];
		opengl_matrix_4x4[5] = rotation_matrix_3x3[4];
		opengl_matrix_4x4[6] = rotation_matrix_3x3[5];

		opengl_matrix_4x4[8] = rotation_matrix_3x3[6];
		opengl_matrix_4x4[9] = rotation_matrix_3x3[7];
		opengl_matrix_4x4[10] = rotation_matrix_3x3[8];

		opengl_matrix_4x4[15] = 1.0f;
	}
}
