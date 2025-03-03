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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsMedia;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import GPTComm.GPTComm;

/**
 * <p>Captures a picture in the background (without need for a UI).</p>
 * <p>Use up to API 20 (even though it may work on API 22 like it does on my OnePlus X).</p>
 * <br>
 * <p>Ideas taken from <a href="https://stackoverflow.com/a/24849344">this StackOverflow answer</a>, from
 * <p><a href="https://stackoverflow.com/users/1312796">Ibrahim AbdelGawad</a> - now heavily modified to suit this app's
 * needs (for example by removing checks for API levels lower than 15), but also to improve its code. Also, parts of
 * this class (static methods) were put in a separate utility class: {@link UtilsCameraManager}.</p>
 */
public final class TakePictureOld implements SurfaceHolder.Callback {









	// todo Set the resolution maybe always to have a ratio of 16:9, so the quality is better! Remember BV9500 and OPX...













	static final Bitmap.CompressFormat PICTURE_FORMAT = Bitmap.CompressFormat.PNG;

	final boolean rear_picture_requested;
	final String chosen_flash_mode;
	final int chosen_quality_mode;
	SurfaceView surfaceView = null;
	/** <p>Only true or false if Auto Focus is enabled, null otherwise.</p> */
	@Nullable Boolean focus_success = null;
	/** <p>True if it's to take 2 pictures, one with the flash off and the other with it on; false otherwise. This is
	 * used to know when the submodule is ready to take the other picture. It stays false until the on picture is taken,
	 * then it becomes true, and it's back to false after the off picture is taken (false in the end to mean it's not to
	 * take 2 pictures anymore (or any, anyway).</p> */
	boolean flash_off_on;

	public static final String FLASH_MODE_OFF_ON = "FLASH_MODE_OFF_ON";
	/**
	 * <p>Main class constructor.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #FLASH_MODE_OFF_ON} --> for {@code chosen_flash_mode}: first take a picture with the flash off and then
	 * another one with it on. Might be useful in case the user didn't select a flash mode, and also because the auto
	 * flash is not implemented, at least yet. So one of the pictures taken by this method will be good, hopefully.</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param rear_picture_requested true to take a rear picture, false to take a frontal one
	 * @param chosen_flash_mode either {@link Camera.Parameters#FLASH_MODE_ON}, {@link Camera.Parameters#FLASH_MODE_OFF},
	 *                          or one of the constants
	 * @param chosen_quality_mode goes from 0 (minimum quality and file size) to 100 (maximum quality and file size)
	 */
	public TakePictureOld(final boolean rear_picture_requested, @NonNull final String chosen_flash_mode,
						  final int chosen_quality_mode) {
		this.rear_picture_requested = rear_picture_requested;
		this.chosen_flash_mode = chosen_flash_mode;
		this.chosen_quality_mode = chosen_quality_mode;

		flash_off_on = FLASH_MODE_OFF_ON.equals(chosen_flash_mode);

		final WindowManager windowManager = (WindowManager) UtilsContext.getSystemService(Context.WINDOW_SERVICE);
		assert windowManager != null; // Module supported

		final WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		layoutParams.gravity = Gravity.TOP | Gravity.START;
		layoutParams.width = 1;
		layoutParams.height = 1;
		layoutParams.x = 0;
		layoutParams.y = 0;

		surfaceView = new SurfaceView(UtilsContext.getContext());

		windowManager.addView(surfaceView, layoutParams);
		final SurfaceHolder surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
	}

	/**
	 * <p>Calls {@link #takeImage()}.</p>
	 */
	final class TakeImage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			takeImage();

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			// No need to implement.
		}
	}

	/**
	 * <p>Takes a picture with the chosen settings.</p>
	 */
	void takeImage() {
		final Camera camera = UtilsCameraManager.openCamera(rear_picture_requested);
		if (camera == null) {
			final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_ERR_CANT_OPEN_CAM);
			UtilsApp.sendInternalBroadcast(broadcast_intent);

			return;
		}

		if (!configureCamera(camera, rear_picture_requested)) {
			// If the flash mode is not supported, just abort and warn about it. No taking with the default one.
			final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_ERR_UNSUPPORTED_FLASH_MODE);
			UtilsApp.sendInternalBroadcast(broadcast_intent);

			camera.release();

			return;
		}

		camera.startPreview();
		if (Camera.Parameters.FOCUS_MODE_AUTO.equals(camera.getParameters().getFocusMode())) {
			camera.autoFocus(autoFocusCallback);
		} else {
			camera.takePicture(shutterCallback, null, pictureCallback);
		}
	}

	final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(@NonNull final byte[] data, @NonNull final Camera camera) {
			// decode the data obtained by the camera into a Bitmap
			////Log.id("ImageTakin", "Done");
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			@Nullable Bitmap bitmap = UtilsCameraManager.decodeBitmap(data);
			bitmap.compress(PICTURE_FORMAT, chosen_quality_mode, bytes);
			bitmap.recycle();
			bitmap = null;

			final File image_file = UtilsMedia.getOutputMediaFile(UtilsMedia.PHOTO);
			if (!image_file.mkdirs()) {
				final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_ERR_CANT_CREATE_FILE);
				UtilsApp.sendInternalBroadcast(broadcast_intent);

				camera.release();

				return;
			}

			// write the bytes in file
			try (final FileOutputStream fileOutputStream = new FileOutputStream(image_file)) {
				fileOutputStream.write(bytes.toByteArray());
			} catch (final FileNotFoundException ignored) {
				final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_ERR_FILE_DELETED);
				UtilsApp.sendInternalBroadcast(broadcast_intent);

				camera.release();

				return;
			} catch (final IOException ignored) {
				final Intent broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_ERR_WRITING_PIC_TO_FILE);
				UtilsApp.sendInternalBroadcast(broadcast_intent);

				camera.release();

				return;
			}

			MediaScannerConnection.scanFile(UtilsContext.getContext(), new String[]{image_file.toString()}, null, null);

			camera.stopPreview();

			if (flash_off_on) {
				flash_off_on = false;

				final Camera.Parameters parameters = camera.getParameters();
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				camera.setParameters(parameters);
				camera.startPreview();
				camera.autoFocus(autoFocusCallback);

				// No need to call the takeImage() again and rerun all checks since as a start, the camera is already
				// open, and then all the parameters are still in use here. We just changed the flash mode. The rest is
				// all exactly the same.
			}

			camera.release();

			final Intent broadcast_intent;
			if (focus_success != null && !focus_success) { // Why these checks? Docstring of the variable.
				// So warn only that there was no focus if auto focus is even possible.
				broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_PICTURE_TAKEN_NO_FOCUS);
			} else {
				broadcast_intent = new Intent(CONSTS_BC_CameraManag.ACTION_PICTURE_TAKEN);
			}
			UtilsApp.sendInternalBroadcast(broadcast_intent);
		}
	};

	@Override
	public void surfaceCreated(@NonNull final SurfaceHolder holder) {
		new TakeImage().execute();
	}

	@Override
	public void surfaceChanged(@NonNull final SurfaceHolder holder, final int format, final int width, final int height) {
		// No need to implement.
	}

	@Override
	public void surfaceDestroyed(@NonNull final SurfaceHolder holder) {
		// No need to implement.
	}

	final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		@Override
		public void onAutoFocus(final boolean success, @NonNull final Camera camera) {
			focus_success = success;
			try {
				// todo Utils_general.playMp3_pequeno(getApplicationContext(), RawFiles.CAMERA_FOCUS[2]);
				// The sound above doesn't work on OnePlus X Android 5.1, but maybe it works on others. The phone plays
				// a sound by itself - others may not play one of themselves, so leave this sound here.
				camera.takePicture(shutterCallback, null, pictureCallback);
			} catch (final RuntimeException ignored) {
				final String speak = "Error taking the picture, sir.";
				UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, GPTComm.SESSION_TYPE_TEMP, false, null);
			}
		}
	};

	final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			 // todo Utils_general.playMp3_pequeno(getApplicationContext(), RawFiles.CAMERA_SHUTTER[2]);

		}
	};

	/**
	 * <p>Configures the camera to be used with the chosen parameters and various default ones for optimal quality.</p>
	 *
	 * @param camera the Camera instance to configure
	 * @param rear_camera true if it's to use the rear camera, false otherwise
	 *
	 * @return true if everything went fine and the camera is ready to be used, false if the chosen flash mode is not
	 * supported by the camera and no parameter was set
	 */
	boolean configureCamera(@NonNull final Camera camera, final boolean rear_camera) {
		final Camera.Parameters camera_parameters = camera.getParameters();

		final String flash_mode_to_use;
		if (flash_off_on) {
			flash_mode_to_use = Camera.Parameters.FLASH_MODE_ON;
		} else {
			flash_mode_to_use = chosen_flash_mode;
		}
		if (!camera_parameters.getSupportedFlashModes().contains(flash_mode_to_use)) {
			return false;
		}
		camera_parameters.setFlashMode(flash_mode_to_use);

		try {
			camera.setPreviewDisplay(surfaceView.getHolder());
		} catch (final IOException ignored) {
			// Won't happen - the SurfaceView is suitable and is available (hopefully). So no need to broadcast any
			// error.
			return false;
		}


		final List<int[]> fps_range = camera_parameters.getSupportedPreviewFpsRange();
		if (fps_range != null) {
			// NOTE: if needed to use the FPS for other purposes, don't forget they have been multiplied by 1000!
			// No idea what this means? Read getSupportedPreviewFpsRange()'s doc.
			int max_fps = 0;
			int min_fps = 0;
			final int fps_range_size = fps_range.size();
			for (int i = 0; i < fps_range_size; ++i) {
				if (fps_range.get(i)[1] > max_fps) {
					min_fps = fps_range.get(i)[0];
					max_fps = fps_range.get(i)[1];
				}
			}
			camera_parameters.setPreviewFpsRange(min_fps, max_fps);
		}

		// set biggest picture
		final Camera.Size picture_size = UtilsCameraManager.getHighestPictureSize(camera_parameters);
		camera_parameters.setPictureSize(picture_size.width, picture_size.height);

		final String[] useful_focus_modes = {Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_EDOF};
		final List<String> supported_focus_modes = camera_parameters.getSupportedFocusModes();
		String focus_mode_to_use = "";
		for (final String focus_mode : useful_focus_modes) {
			if (supported_focus_modes.contains(focus_mode)) {
				focus_mode_to_use = focus_mode;
			}
		}

		if (camera_parameters.isAutoExposureLockSupported()) {
			camera_parameters.setAutoExposureLock(false);
		}
		if (camera_parameters.isAutoWhiteBalanceLockSupported()) {
			camera_parameters.setAutoWhiteBalanceLock(false);
		}
		camera_parameters.setFocusMode(focus_mode_to_use);
		camera_parameters.setJpegQuality(chosen_quality_mode);
		camera_parameters.setJpegThumbnailQuality(chosen_quality_mode);
		camera_parameters.setPictureFormat(ImageFormat.JPEG);

		final WindowManager windowManager = (WindowManager) UtilsContext.getSystemService(Context.WINDOW_SERVICE);
		assert windowManager != null; // If the camera is available and this is how it's used, then this is also available

		final int display_rotation = windowManager.getDefaultDisplay().getRotation();
		final int[][] rotation_to_degrees = {{Surface.ROTATION_0, 0}, {Surface.ROTATION_90, 90},
				{Surface.ROTATION_180, 180}, {Surface.ROTATION_270, 270}};
		for (final int[] rotation_to_degree : rotation_to_degrees) {
			if (display_rotation == rotation_to_degree[0]) {
				// Why subtract and sum 90 depending on the camera? After tests with the OnePlus X on Android 5.1 that's
				// what I came up with (on it, the picture was with wrong rotation). Hopefully this is configuration is
				// universal...
				final int rotation_degrees = rotation_to_degree[1];
				final int final_degrees = rear_camera ? rotation_degrees + 90 : rotation_degrees - 90;
				camera_parameters.setRotation(final_degrees);
				camera.setDisplayOrientation(final_degrees);

				break;
			}
		}

		// set camera parameters
		camera.setParameters(camera_parameters);

		return true;
	}
}
