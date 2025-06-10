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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.AugmentedReality.OpenGL.Objects.Rectangle;
import com.edw590.visor_c_a.AugmentedReality.OpenGL.UtilsOpenGL;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public final class OpenCV implements CameraBridgeViewBase.CvCameraViewListener2 {

	private float window_width = 0.0f;
	private float window_height = 0.0f;

	private Mat rgba_frame = null;

	private final RectangleDetector rectangle_detector = new RectangleDetector();
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

	private static void debugMatrix(float[] m) {
		// Column-major indices
		float[] x = { m[0],  m[1],  m[2]  };
		float[] y = { m[4],  m[5],  m[6]  };
		float[] z = { m[8],  m[9],  m[10] };

		float lx = length(x), ly = length(y), lz = length(z);
		float xy = dot(x,y),   yz = dot(y,z),   zx = dot(z,x);

		Log.i("MDBG", String.format("lens: %.3f %.3f %.3f   dots: %.3f %.3f %.3f",
				lx, ly, lz, xy, yz, zx));
	}

	private static float length(float[] v){
		return (float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
	}
	private static float dot(float[] a,float[] b){
		return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
	}

	private native float[] CVTest(long matAddr);

	@Override
	@Nullable
	public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
		rgba_frame = cvCameraViewFrame.rgba();

		//rectangle_detector.detect(rgba_frame);
		//hand_detector.detect(rgba_frame);

		float[] pose_matrix = CVTest(rgba_frame.getNativeObjAddr());

		if (pose_matrix.length != 0) {
			double[][] pose = new double[4][4];
			System.out.println("one posematrix is below========");
			for (int i = 0; i < pose_matrix.length / 4; i++) {
				for (int j = 0; j < 4; j++) {

					if (j == 3 && i != 3) {
						pose[i][j] = pose_matrix[i * 4 + j] * 20; // Scale translation
					} else {
						pose[i][j] = pose_matrix[i * 4 + j];
					}
					System.out.print(pose[i][j] + "\t ");
				}

				System.out.print("\n");
			}

			double[][] R = new double[3][3];
			double[] T = new double[3];

			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					R[i][j] = pose[i][j];
				}
			}
			for (int i = 0; i < 3; i++) {
				T[i] = pose[i][3];
			}
			RealMatrix rotation = new Array2DRowRealMatrix(R);
			RealMatrix translation = new Array2DRowRealMatrix(T);

			final double d[][]={
					{1,0,0},
					{0,-1,0},
					{0,0,-1}
			};
			RealMatrix rx=new Array2DRowRealMatrix(d);
			rotation=rx.multiply(rotation);
			translation=rx.multiply(translation);
			double R1[][]= rotation.getData();
			double T1[][]=translation.getData();

			float[] model_view_matrix = new float[16];
			model_view_matrix[0]=(float) R1[0][0];
			model_view_matrix[1]=(float) R1[1][0];
			model_view_matrix[2]=(float) R1[2][0];
			model_view_matrix[3]=0.0f;

			model_view_matrix[4]=(float) R1[0][1];
			model_view_matrix[5]=(float) R1[1][1];
			model_view_matrix[6]=(float) R1[2][1];
			model_view_matrix[7]=0.0f;

			model_view_matrix[8]=(float) R1[0][2];
			model_view_matrix[9]=(float) R1[1][2];
			model_view_matrix[10]=(float) R1[2][2];
			model_view_matrix[11]=0.0f;

			model_view_matrix[12]=(float) T1[0][0];
			model_view_matrix[13]=(float) T1[1][0];
			model_view_matrix[14]=(float) T1[2][0];
			model_view_matrix[15]=1.0f;

			UtilsOpenGL.setViewMatrix(model_view_matrix);
		}

		return rgba_frame;
	}

	@NonNull
	public Rectangle[] getDetectedRectangles() {
		return rectangle_detector.getDetected(window_width, window_height);
	}
}
