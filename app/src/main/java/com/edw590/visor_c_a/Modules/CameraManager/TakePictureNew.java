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

package com.edw590.visor_c_a.Modules.CameraManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.IBinder;
import android.util.Size;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.ActivitiesFragments.Activities.ActFlash;
import com.edw590.visor_c_a.GlobalUtils.GPath;
import com.edw590.visor_c_a.GlobalUtils.UtilsFilesDirs;
import com.edw590.visor_c_a.GlobalUtils.UtilsMedia;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;

import GPTComm.GPTComm;

@RequiresApi(api = 21)
public final class TakePictureNew extends Service {

	private static final int CAMERA_CALIBRATION_DELAY = 500;
	static long cameraCaptureStartTime = 0;
	CameraDevice cameraDevice = null;
	CameraCaptureSession session = null;
	ImageReader imageReader = null;

	boolean use_flash = false;
	boolean rear_pic = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		rear_pic = intent.getBooleanExtra("rear_pic", true);
		use_flash = intent.getBooleanExtra("flash_on", false);

		if (!rear_pic && use_flash) {
			new Thread(() -> {
				if (use_flash && !rear_pic) {
					Intent intent1 = new Intent(this, ActFlash.class);
					intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent1);
				}
			}).start();
		}

		readyCamera();

		return START_NOT_STICKY;
	}

	void readyCamera() {
		CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
		try {
			String pickedCamera = getCamera(manager);
			if (rear_pic && use_flash && !isFlashAvailable(manager, pickedCamera)) {
				return;
			}

			manager.openCamera(pickedCamera, cameraStateCallback, null);

			Size maxSize = UtilsCameraManager.getHighestResolution(manager, pickedCamera);
			if (maxSize == null) {
				return;
			} else {
				imageReader = ImageReader.newInstance(maxSize.getWidth(), maxSize.getHeight(), ImageFormat.JPEG, 1);
			}

			imageReader.setOnImageAvailableListener(onImageAvailableListener, null);
		} catch (final CameraAccessException ignored) {
		}
	}

	CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
		@Override
		public void onOpened(@NonNull final CameraDevice camera) {
			cameraDevice = camera;
			actOnReadyCameraDevice();
		}

		@Override
		public void onDisconnected(@NonNull final CameraDevice camera) {
			String speak = "Error taking the picture - the camera was disconnected";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
		}

		@Override
		public void onError(@NonNull final CameraDevice camera, final int error) {
			String speak = "Generic error taking the picture";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
		}
	};

	CameraCaptureSession.StateCallback sessionStateCallback = new CameraCaptureSession.StateCallback() {

		@Override
		public void onReady(final CameraCaptureSession session) {
			TakePictureNew.this.session = session;

			if (cameraDevice == null) {
				return;
			}

			try {
				CaptureRequest.Builder focusRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
				focusRequest.addTarget(imageReader.getSurface());

				// Set auto-focus trigger
				focusRequest.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

				if (!rear_pic && use_flash) {
					Thread.sleep(2000);
				}

				session.capture(createCaptureRequest(), new CameraCaptureSession.CaptureCallback() {
					@Override
					public void onCaptureCompleted(@NonNull final CameraCaptureSession session,
												   @NonNull final CaptureRequest request,
												   @NonNull final TotalCaptureResult result) {
						closeCamera();
					}
				}, null);
				cameraCaptureStartTime = System.currentTimeMillis();
			} catch (final Exception ignored) {
			}
		}


		@Override
		public void onConfigured(final CameraCaptureSession session) {

		}

		@Override
		public void onConfigureFailed(@NonNull final CameraCaptureSession session) {
		}
	};

	ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
		@Override
		public void onImageAvailable(final ImageReader reader) {
			Image img = reader.acquireLatestImage();
			if (img != null) {
				if (System.currentTimeMillis() > cameraCaptureStartTime + CAMERA_CALIBRATION_DELAY) {
					processImage(img);
				}
				img.close();
			}
		}
	};

	String getCamera(final CameraManager manager) {
		try {
			for (final String cameraId : manager.getCameraIdList()) {
				CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
				int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
				if (cOrientation == (rear_pic ? CameraMetadata.LENS_FACING_BACK : CameraMetadata.LENS_FACING_FRONT)) {
					return cameraId;
				}
			}
		} catch (final CameraAccessException ignored) {
		}

		return null;
	}

	void actOnReadyCameraDevice() {
		try {
			cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), sessionStateCallback, null);
		} catch (final CameraAccessException ignored) {
			String speak = "Error taking the picture";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
		}
	}

	@Override
	public void onDestroy() {
		try {
			session.abortCaptures();
		} catch (final Exception ignored) {
		}
		session.close();
	}


	void processImage(final Image image) {
		//Process image data
		ByteBuffer buffer;
		byte[] bytes;
		File file = UtilsMedia.getOutputMediaFile(UtilsMedia.PHOTO);
		GPath file_parent = new GPath(false, Objects.requireNonNull(file.getParent()));
		UtilsFilesDirs.createDirectory(file_parent);
		FileOutputStream output = null;

		if (image.getFormat() == ImageFormat.JPEG) {

			buffer = image.getPlanes()[0].getBuffer();
			bytes = new byte[buffer.remaining()]; // makes byte array large enough to hold image
			buffer.get(bytes); // copies image from buffer to byte array

			// Convert bytes to Bitmap
			Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

			// Get the correct rotation
			int rotation = 0;
			try {
				rotation = getRotationCompensation(cameraDevice.getId(), this);
			} catch (final CameraAccessException ignoreed) {
			}

			// Rotate the image
			Bitmap rotatedBitmap = rotateBitmap(originalBitmap, rotation);

			// Save the rotated image
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
			} catch (final FileNotFoundException ignored) {
			} catch (final IOException ignored) {
			}
		}
	}

	private int getRotationCompensation(final String cameraId, final Context context) throws CameraAccessException {
		CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
		CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

		int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

		int deviceRotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getRotation();

		int deviceAngle;
		switch (deviceRotation) {
			case Surface.ROTATION_0: deviceAngle = 0; break;
			case Surface.ROTATION_90: deviceAngle = 90; break;
			case Surface.ROTATION_180: deviceAngle = 180; break;
			case Surface.ROTATION_270: deviceAngle = 270; break;
			default: deviceAngle = 0; break;
		}

		return (sensorOrientation - deviceAngle + 360) % 360;
	}

	private Bitmap rotateBitmap(final Bitmap bitmap, final int degrees) {
		if (degrees == 0) return bitmap;
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	@Nullable
	CaptureRequest createCaptureRequest() {
		try {
			CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			builder.addTarget(imageReader.getSurface());

			builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

			if (use_flash && rear_pic) {
				builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
			} else {
				builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
			}

			return builder.build();
		} catch (final CameraAccessException ignored) {
			return null;
		}
	}

	private static boolean isFlashAvailable(final CameraManager manager, final String cameraId) {
		try {
			CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
			Boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

			return flashAvailable != null && flashAvailable;
		} catch (final CameraAccessException ignored) {
			return false;
		}
	}

	void closeCamera() {
		if (session != null) {
			try {
				session.abortCaptures();
				session.close();
			} catch (final CameraAccessException ignored) {
			}
			session = null;
		}

		if (cameraDevice != null) {
			cameraDevice.close();
			cameraDevice = null;
		}

		if (imageReader != null) {
			imageReader.close();
			imageReader = null;
		}

		stopSelf();
	}

	@Nullable
	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}
}
