package com.edw590.visor_c_a.OpenGL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.OpenGL.Objects.Rectangle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public final class OpenCV implements CameraBridgeViewBase.CvCameraViewListener2 {

	private float window_width = 0.0f;
	private float window_height = 0.0f;

	private Mat rgba_frame = null;

	private Point[][] detected_rects_array = null;

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
		detectAndDrawFlatSurfaces(rgba_frame);

		return null;
	}

	private void detectAndDrawFlatSurfaces(@NonNull final Mat frame) {
		Mat gray = new Mat();
		Imgproc.cvtColor(frame, gray, Imgproc.COLOR_RGBA2GRAY);

		// Blacken the frame
		//frame.setTo(new Scalar(0, 0, 0, 255));

		Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
		Mat edges = new Mat();
		Imgproc.Canny(gray, edges, 50, 150);

		List<MatOfPoint> contours = new ArrayList<>(50);
		Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		// Clear previous detected quads
		List<Point[]> detected_rects_list = new ArrayList<>(10);
		for (final MatOfPoint contour : contours) {
			MatOfPoint2f contour_2f = new MatOfPoint2f(contour.toArray());
			double peri = Imgproc.arcLength(contour_2f, true);
			MatOfPoint2f approx = new MatOfPoint2f();
			Imgproc.approxPolyDP(contour_2f, approx, 0.02 * peri, true);

			MatOfPoint approx_points = new MatOfPoint(approx.toArray());
			if (approx_points.total() == 4 && Imgproc.contourArea(approx_points) > 1000 && Imgproc.isContourConvex(approx_points)) {
				Rect rect = Imgproc.boundingRect(contour);

				// Store the 2 corners of the rectangle
				detected_rects_list.add(new Point[]{
						rect.tl(),
						rect.br()
				});

				/*float ratio = (float) rect.width / rect.height;
				Scalar color;
				if (ratio > 0.8 && ratio < 1.2) {
					// Draw the rectangle
					color = new Scalar(0, 255, 0, 255); // Green
				} else {
					// Draw the square
					color = new Scalar(255, 0, 0, 255); // Red
				}
				//Imgproc.drawContours(frame, Collections.singletonList(approx_points), -1, color, 2);

				Imgproc.rectangle(frame, rect.tl(), rect.br(), color, -1);*/
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
		float ndc_z = -3.1f;
		float max_x = UtilsOpenGL.getMaxX(ndc_z);
		float max_y = UtilsOpenGL.getMaxY(ndc_z);
		float view_width = max_x * 2;
		float view_height = max_y * 2;
		for (final Point[] points : detected_rects_array) {
			float width = (float) ((points[1].x - points[0].x) / window_width * view_width);
			float height = (float) ((points[1].y - points[0].y) / window_height * view_height);
			float ndc_x = (float) ((points[0].x + points[1].x)/2 / window_width * view_width - max_x);
			float ndc_y = (float) -((points[0].y + points[1].y)/2 / window_height * view_height - max_y);

			rectangles.add(new Rectangle(
					new Vector(ndc_x, ndc_y, ndc_z),
					width, height,
					0.0f, 0.0f, 0.0f
			));
		}

		return rectangles.toArray(new Rectangle[0]);
	}
}
