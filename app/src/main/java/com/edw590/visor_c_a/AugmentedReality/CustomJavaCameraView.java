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

package com.edw590.visor_c_a.AugmentedReality;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.lang.reflect.Field;

// Class taken from https://stackoverflow.com/a/74056690/8228163.

public final class CustomJavaCameraView extends JavaCameraView {

	public enum Orientation {
		LANDSCAPE_LEFT,
		PORTRAIT,
		LANDSCAPE_RIGHT;
		boolean isLandscape() {
			return this == LANDSCAPE_LEFT || this == LANDSCAPE_RIGHT;
		}
		boolean isLandscapeRight() {
			return this == LANDSCAPE_RIGHT;
		}
	};

	// scale camera by this coefficient - using mScale seems to more performant than upsizing frame Mat
	// orientation is immutable because every attempt to change it dynamically failed with 'null pointer dereference' and similar exceptions
	// tip: re-creating camera from the outside should allow changing orientation
	private final Orientation orientation;

	public CustomJavaCameraView(@NonNull final Context context, final int cameraId, @NonNull final Orientation orientation) {
		super(context, cameraId);
		this.orientation = orientation;
	}

	@Override
	protected void AllocateCache() {
		if (orientation.isLandscape()) {
			super.AllocateCache();
			return;
		}
		try {
			Field privateField = CameraBridgeViewBase.class.getDeclaredField("mCacheBitmap");
			privateField.setAccessible(true);
			privateField.set(this, Bitmap.createBitmap(mFrameHeight, mFrameWidth, Bitmap.Config.ARGB_8888));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final class CvCameraViewFrameImplHack implements CvCameraViewFrame {
		private final Mat rgbMat;

		CvCameraViewFrameImplHack(@NonNull final Mat rgbMat) {
			this.rgbMat = rgbMat;
		}
		@Override
		@NonNull
		public Mat rgba() {
			return rgbMat;
		}
		@Nullable
		@Override
		public Mat gray() {
			return null;
		}
	}

	@NonNull
	private static Mat rotateToPortrait(@NonNull final Mat mat) {
		Mat transposed = mat.t();
		Mat flipped = new Mat();
		Core.flip(transposed, flipped, 1);
		transposed.release();
		return flipped;
	}

	@NonNull
	private static Mat rotateToLandscapeRight(@NonNull final Mat mat) {
		Mat flipped = new Mat();
		Core.flip(mat, flipped, -1);
		return flipped;
	}

	@Override
	protected void deliverAndDrawFrame(final CvCameraViewFrame frame) {
		Mat frameMat = frame.rgba();
		Mat rotated;
		if (orientation.isLandscape()) {
			if (orientation.isLandscapeRight()) {
				rotated = rotateToLandscapeRight(frameMat);
			} else {
				rotated = frameMat;
			}
			mScale = (float)getWidth() / frameMat.width();
		} else {
			rotated = rotateToPortrait(frameMat);
			mScale = (float)getHeight() / rotated.height();
		}
		CvCameraViewFrame hackFrame = new CvCameraViewFrameImplHack(rotated);

		super.deliverAndDrawFrame(hackFrame);
	}
}
