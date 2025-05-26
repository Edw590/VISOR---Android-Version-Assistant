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

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

class HandDetector {

	void detect(@NonNull final Mat frame) {
		Mat gray = new Mat();
		Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
		Imgproc.GaussianBlur(gray, gray, new Size(15, 15), 0);
		Mat thresh = new Mat();
		Imgproc.threshold(gray, thresh, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

		List<MatOfPoint> contours = new ArrayList<>(10);
		Mat hierarchy = new Mat();
		Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		double maxArea = 0;
		int maxIdx = -1;
		for (int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));
			if (area > maxArea) {
				maxArea = area;
				maxIdx = i;
			}
		}

		MatOfInt hull = new MatOfInt();
		Imgproc.convexHull(contours.get(maxIdx), hull);

		MatOfInt4 defects = new MatOfInt4();
		Imgproc.convexityDefects(contours.get(maxIdx), hull, defects);

		int fingerCount = 0;
		if (defects.rows() > 0) {
			List<Integer> defectList = defects.toList();
			for (int i = 0; i < defectList.size(); i += 4) {
				double depth = defectList.get(i + 3) / 256.0;
				if (depth > 10) { // tune this value
					fingerCount++;
				}
			}
		}

		String gesture;
		if (fingerCount >= 4) {
			gesture = "Open Hand";
		} else {
			gesture = "Closed Hand";
		}

		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		System.out.println("Gesture: " + gesture);
	}
}
