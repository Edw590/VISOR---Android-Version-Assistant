package com.edw590.visor_c_a.OpenGL;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.OpenGL.Objects.Rectangle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public final class OpenCV implements CameraBridgeViewBase.CvCameraViewListener2 {

	private Mat rgba_frame = null;

	private Point[][] detected_rects_array = null;

	@Override
	public void onCameraViewStarted(final int width, final int height) {
		rgba_frame = new Mat();
	}

	@Override
	public void onCameraViewStopped() {
		rgba_frame.release();
	}

	@Override
	@NonNull
	public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
		rgba_frame = cvCameraViewFrame.rgba();
		detectAndDrawFlatSurfaces(rgba_frame);

		return rgba_frame;
	}

	private void detectAndDrawFlatSurfaces(@NonNull final Mat frame) {
		Mat gray = new Mat();
		Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);
		Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
		Mat edges = new Mat();
		Imgproc.Canny(gray, edges, 50, 150);

		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		// Clear previous detected quads
		List<Point[]> detected_rects_list = new ArrayList<>(10);
		for (MatOfPoint contour : contours) {
			MatOfPoint2f contour_2f = new MatOfPoint2f(contour.toArray());
			double peri = Imgproc.arcLength(contour_2f, true);
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(contour_2f, approx, 0.02 * peri, true);

			MatOfPoint approx_points = new MatOfPoint(approx.toArray());
			if (approx_points.total() == 4 && Imgproc.contourArea(approx_points) > 1000 && Imgproc.isContourConvex(approx_points)) {
				Rect rect = Imgproc.boundingRect(contour);

				detected_rects_list.add(new Point[]{
						rect.tl(),
						rect.br()
				}); // Store the 2 corners of the rectangle

				float ratio = (float) rect.width / rect.height;
				Scalar color;
				if (ratio > 0.8 && ratio < 1.2) {
					// Draw the rectangle
					color = new Scalar(0, 255, 0, 255); // Green
				} else {
					// Draw the square
					color = new Scalar(255, 0, 0, 255); // Red
				}
				//Imgproc.drawContours(frame, Collections.singletonList(approx_points), -1, color, 2);

				Imgproc.rectangle(frame, rect.tl(), rect.br(), color, -1);
			}
		}

		detected_rects_array = detected_rects_list.toArray(new Point[0][]);
	}

	@NonNull
	public Rectangle[] getDetectedRectangles() {
		if (detected_rects_array == null) {
			return new Rectangle[0];
		}

		List<Rectangle> rectangles = new ArrayList<>(10);
		for (final Point[] points : detected_rects_array) {
			float ndc_x = (float) (1.0 - (points[0].y / rgba_frame.height()) * 2.0);
			float ndc_y = (float) -((points[0].x / rgba_frame.width()) * 2.0 - 1.0);
			float ndc_z = -3.0f;
			float width = (float) (2.0 * (points[1].x - points[0].x) / rgba_frame.width());
			float height = (float) (2.0 * (points[1].y - points[0].y) / rgba_frame.height());
			rectangles.add(new Rectangle(
					new Vector(ndc_x, ndc_y, ndc_z),
					width, height,
					0.0f, 0.0f, 0.0f
			));

			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
			System.out.println("X: " + ndc_x + " Y: " + ndc_y + " Z: " + ndc_z);
			System.out.println("Width: " + width + " Height: " + height);
		}

		return rectangles.toArray(new Rectangle[0]);
	}
}
