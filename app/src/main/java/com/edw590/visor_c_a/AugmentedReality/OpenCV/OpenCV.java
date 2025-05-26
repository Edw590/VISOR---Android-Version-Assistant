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

package com.edw590.visor_c_a.AugmentedReality.OpenCV;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects.Rectangle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public final class OpenCV implements CameraBridgeViewBase.CvCameraViewListener2 {

	private float window_width = 0.0f;
	private float window_height = 0.0f;

	private Mat rgba_frame = null;

	private final RectangleDetefctor rectangle_detector = new RectangleDetefctor();
	private final HandDetector hand_detector = new HandDetector();

	@Override
	public void onCameraViewStarted(final int width, final int height) {
		rgba_frame = new Mat();
		window_width = width;
		window_height = height;
	}

	@Override
	public void onCameraViewStopped() {
		rgba_frame.release();
	}

	@Override
	@Nullable
	public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
		rgba_frame = cvCameraViewFrame.rgba();

		rectangle_detector.detect(rgba_frame);
		//hand_detector.detect(rgba_frame);

		return rgba_frame;
	}

	@NonNull
	public Rectangle[] getDetectedRectangles() {
		return rectangle_detector.getDetected(window_width, window_height);
	}
}
