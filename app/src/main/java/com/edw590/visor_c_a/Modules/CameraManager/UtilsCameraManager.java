/*
 * Copyright 2021-2024 Edw590
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

package com.edw590.visor_c_a.Modules.CameraManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * <p>Camera Manager related utilities.</p>
 */
final class UtilsCameraManager {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsCameraManager() {
	}

	/**
	 * <p>Open the camera.</p>
	 *
	 * @param back_camera true to request the back camera, false to request the front camera
	 *
	 * @return the opened Camera instance, or null in case it was not possible to open a camera
	 */
	@Nullable
	static Camera openCamera(final boolean back_camera) {
		final int camera_facing = back_camera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
		Camera camera = null;
		final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		final int cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; ++camIdx) {
			if (camera_facing == cameraInfo.facing) {
				try {
					camera = Camera.open(camIdx);
				} catch (final RuntimeException e) {
					e.printStackTrace();
				}
			}
		}

		return camera;
	}

	/**
	 * <p>Returns the highest camera resolution available for pictures.</p>
	 *
	 * @param parameters an instance of the camera parameters
	 *
	 * @return the highest picture size available
	 */
	@NonNull
	static Camera.Size getHighestPictureSize(final Camera.Parameters parameters) {
		Camera.Size result = null;

		for (final Camera.Size size : parameters.getSupportedPictureSizes()) {
			if (result == null) {
				result = size;
			} else {
				final int resultArea = result.width * result.height;
				final int newArea = size.width * size.height;

				if (newArea > resultArea) {
					result = size;
				}
			}
		}

		// It's never null - read the documentation of getSupportedPictureSizes()
		assert result != null;
		return result;
	}

	@Nullable
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	static Size getHighestResolution(final CameraManager manager, final String cameraId) {
		try {
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
			StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
			if (map != null) {
				Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
				return Collections.max(Arrays.asList(sizes), new Comparator<Size>() {
					@Override
					public int compare(final Size s1, final Size s2) {
						return Long.compare((long) s1.getWidth() * s1.getHeight(), (long) s2.getWidth() * s2.getHeight());
					}
				});
			}
		} catch (final Exception ignored) {
		}

		return null;
	}

	/**
	 * <p>Calls {@link BitmapFactory#decodeByteArray(byte[], int, int, BitmapFactory.Options)} on {@code data} with
	 * specific options.</p>
	 *
	 * @param data the data coming from {@link Camera.PictureCallback#onPictureTaken(byte[], Camera)}
	 *
	 * @return the decoded Bitmap
	 */
	@NonNull
	static Bitmap decodeBitmap(@NonNull final byte[] data) {
		final BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
		// memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be
		// used to recover the Bitmap data
		// after being clear, when it will
		// be used in the future
		bfOptions.inTempStorage = new byte[64]; // todo See if this works properly - "32 * 1024" was here

		return BitmapFactory.decodeByteArray(data, 0, data.length, bfOptions);
	}
}
