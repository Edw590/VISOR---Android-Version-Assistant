/*
 * Copyright 2021 DADi590
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

package com.dadi590.assist_c_a.Modules.CameraManager;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AndroidException;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.util.List;

/**
 * <p>The module that manages the device camera, to take photos, record a video, or toggle the flashlight.</p>
 */
public class CameraManagement implements IModule {

	@Nullable private Camera camera_old = null;

	// Only used from Marshmallow onwards, else it's null.
	@Nullable final CameraManager.TorchCallback torchCallback;

	String main_camera_id = "";

	boolean flashlight_was_on_before_pic = false;

	private boolean is_module_destroyed = false;
	@Override
	public final boolean isModuleFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return true;
	}
	@Override
	public final void destroyModule() {
		UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		is_module_destroyed = true;
	}

	/**
	 * <p>Main class constructor.</p>
	 */
	public CameraManagement() {
		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC.ACTION_USE_CAMERA);

			intentFilter.addAction(CONSTS_BC.ACTION_PICTURE_TAKEN);
			intentFilter.addAction(CONSTS_BC.ACTION_PICTURE_TAKEN_NO_FOCUS);
			intentFilter.addAction(CONSTS_BC.ACTION_ERR_CANT_OPEN_CAM);
			intentFilter.addAction(CONSTS_BC.ACTION_ERR_CANT_CREATE_FILE);
			intentFilter.addAction(CONSTS_BC.ACTION_ERR_FILE_DELETED);
			intentFilter.addAction(CONSTS_BC.ACTION_ERR_WRITING_PIC_TO_FILE);
			intentFilter.addAction(CONSTS_BC.ACTION_ERR_UNSUPPORTED_FLASH_MODE);

			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, new IntentFilter(intentFilter));
		} catch (final IllegalArgumentException ignored) {
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
			final Handler handler =  new Handler(Looper.getMainLooper());

			final CameraManager mCameraManager = (CameraManager) UtilsGeneral.getContext().
					getSystemService(Context.CAMERA_SERVICE);

			torchCallback = new CameraManager.TorchCallback() {
				@Override
				public void onTorchModeChanged(final String cameraId, final boolean enabled){
					super.onTorchModeChanged(cameraId, enabled);

					if (!main_camera_id.isEmpty() && main_camera_id.equals(cameraId)) {
						// Update the values on the ValuesStorage
						ValuesStorage.updateValue(CONSTS.main_flashlight_enabled, Boolean.toString(enabled));
					}
				}
			};

			mCameraManager.registerTorchCallback(torchCallback, handler);
		} else {
			torchCallback = null;
		}
	}

	public static final int USAGE_FLASHLIGHT_ON = 0;
	public static final int USAGE_FLASHLIGHT_OFF = 1;
	public static final int USAGE_TAKE_REAR_PHOTO = 2;
	public static final int USAGE_TAKE_FRONTAL_PHOTO = 3;
	public static final int USAGE_RECORD_REAR_VIDEO = 4;
	public static final int USAGE_RECORD_FRONTAL_VIDEO = 5;
	public static final int NOTHING_DONE = 0;
	public static final int NO_CAMERAS = 1;
	public static final int CAMERA_DISABLED_ADMIN = 2;
	public static final int NO_CAMERA_FLASH = 3;
	public static final int CAMERA_IN_USAGE = 4;
	/**
	 * <p>Function to call to use any camera service, like the flashlight, taking photos or recording a video.</p>
	 * <p>For example, in case the flashlight is turned on and a photo or a video recording is requested, the flashlight
	 * will first turn the flashlight off automatically.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #USAGE_FLASHLIGHT_ON} --> for {@code usage}: turn on the flashlight</p>
	 * <p>- {@link #USAGE_FLASHLIGHT_OFF} --> for {@code usage}: turn ff the flashlight</p>
	 * <p>- {@link #USAGE_TAKE_REAR_PHOTO} --> for {@code usage}: take a rear photo</p>
	 * <p>- {@link #USAGE_TAKE_FRONTAL_PHOTO} --> for {@code usage}: take a frontal photo</p>
	 * <p>- {@link #USAGE_RECORD_REAR_VIDEO} --> for {@code usage}: record a rear video</p>
	 * <p>- {@link #USAGE_RECORD_FRONTAL_VIDEO} --> for {@code usage}: record a frontal video</p>
	 * <p>---------------</p>
	 * <p>- {@link #NOTHING_DONE} --> for the returning value: in case nothing was done due to wrong parameters passed
	 * to the function</p>
	 * <p>- {@link #NO_CAMERAS} --> for the returning value: the device does not feature any camera</p>
	 * <p>- {@link #CAMERA_DISABLED_ADMIN} --> for the returning value: the camera has been disabled by Device
	 * Administrators</p>
	 * <p>- {@link #NO_CAMERA_FLASH} --> for the returning value: the device does not feature camera flash</p>
	 * <p>- {@link #CAMERA_IN_USAGE} --> for the returning value: the camera is already being used</p>
	 * <br>
	 * <p>- {@link #FLASHLIGHT_SET_ON} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_SET_OFF} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_ON} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_OFF} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #ERROR_ACCESSING_CAMERA} --> for the returning value: same as in {@link #flashlightNew(boolean)}</p>
	 * <p>- {@link #ERROR_ACCESSING_FLASH} --> for the returning value: same as in {@link #flashlightNew(boolean)}</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param usage one of the constants
	 *
	 * @return one of the constants
	 */
	final int useCamera(final int usage) {
		final Context context = UtilsGeneral.getContext();

		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
			final DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.
					getSystemService(Context.DEVICE_POLICY_SERVICE);
			if (devicePolicyManager.getCameraDisabled(null)) {
				return CAMERA_DISABLED_ADMIN;
			}

			if (USAGE_FLASHLIGHT_ON == usage || USAGE_FLASHLIGHT_OFF == usage) {
				if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
					final boolean turn_on_flashlight = FLASHLIGHT_SET_ON == usage;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						return flashlightNew(turn_on_flashlight);
					} else {
						// Below Marshmallow, one needs to use the old Camera API, even on Lollipop (so ignore the
						// deprecation warning - it's because of being running on that its complaining, but that's the
						// official way).
						if (turn_on_flashlight) {
							if (null == camera_old) {
								// Request the back camera, as that's the one that has the flashlight on it.
								camera_old = Camera.open();
								if (null == camera_old) {
									return CAMERA_IN_USAGE;
								}
							}
						}

						return flashlightOld(turn_on_flashlight ? FUNCTION_SET_ON : FUNCTION_SET_OFF);
					}
				} else {
					return NO_CAMERA_FLASH;
				}
			}

			/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.L) {
				if (USAGE_PHOTO == usage) {
					// todo
				} else if (USAGE_VIDEO == usage) {
					// todo
				}
			} else {
				if (USAGE_PHOTO == usage) {
					// todo
				} else if (USAGE_VIDEO == usage) {
                    // todo
                }
			}*/
		} else {
			return NO_CAMERAS;
		}

		return NOTHING_DONE;
	}

	public static final int FUNCTION_SET_ON = 0;
	public static final int FUNCTION_SET_OFF = 1;
	public static final int FUNCTION_CHECK_STATE = 2;
	public static final int FLASHLIGHT_SET_ON = 0;
	public static final int FLASHLIGHT_SET_OFF = 1;
	public static final int FLASHLIGHT_ALREADY_ON = 2;
	public static final int FLASHLIGHT_ALREADY_OFF = 3;
	public static final int FLASHLIGHT_STATE_ON = 4;
	public static final int FLASHLIGHT_STATE_OFF = 5;
	/**
	 * <p>Function to call to perform functions on the flashlight through the old API.</p>
	 * <br>
	 * <p>ATTENTION: use only up until API 22. As of API 23, use {@link #flashlightNew(boolean)}.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #FUNCTION_SET_ON} --> for {@code function}: turn on the flashlight</p>
	 * <p>- {@link #FUNCTION_SET_OFF} --> for {@code function}: turn off the flashlight</p>
	 * <p>- {@link #FUNCTION_CHECK_STATE} --> for {@code function}: check the state of the flashlight (one of the STATE
	 * constants)</p>
	 * <br>
	 * <p>- {@link #FLASHLIGHT_SET_ON} --> for the returning value: the flashlight has been enabled</p>
	 * <p>- {@link #FLASHLIGHT_SET_OFF} --> for the returning value: the flashlight has been disabled</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_ON} --> for the returning value: the flashlight is already enabled</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_OFF} --> for the returning value: the flashlight is already disabled</p>
	 * <p>- {@link #FLASHLIGHT_STATE_ON} --> for the returning value: the flashlight state is On (returned only when
	 * checking)</p>
	 * <p>- {@link #FLASHLIGHT_STATE_OFF} --> for the returning value: the flashlight state is Off (returned only when
	 * checking)</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param function tone of the constants
	 *
	 * @return one of the constants if it's to toggle the state, all can be returned
	 */
	private int flashlightOld(final int function) {
		if (camera_old == null) {
			if (FUNCTION_CHECK_STATE == function) {
				// If it's to check if it was turned on, if it's null, then it's not turned on.
				return FLASHLIGHT_STATE_OFF;
			} else {
				// If it's not to check and it's yes to toggle it, in this case off with it already off, then say it's
				// already turned off.
				return FLASHLIGHT_ALREADY_OFF;
			}
		}

		final Camera.Parameters parameters = camera_old.getParameters();

		String mode_to_use = "";

		final List<String> supportedFlashModes = parameters.getSupportedFlashModes();
		final String[] useful_flash_modes_turn_on = {Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON,
				Camera.Parameters.FLASH_MODE_RED_EYE}; // Useful flash modes for the On state in order of preference.
		for (final String flash_mode : useful_flash_modes_turn_on) {
			if (supportedFlashModes.contains(flash_mode)) {
				mode_to_use = flash_mode;
			}
		}

		if (FUNCTION_CHECK_STATE == function) {
			return parameters.getFlashMode().equals(mode_to_use) ? FLASHLIGHT_STATE_ON : FLASHLIGHT_STATE_OFF;
		} else {
			if (FUNCTION_SET_ON == function) {
				if (parameters.getFlashMode().equals(mode_to_use)) {
					return FLASHLIGHT_ALREADY_ON;
				}
				parameters.setFlashMode(mode_to_use);
				camera_old.setParameters(parameters);
				camera_old.startPreview();

				return FLASHLIGHT_SET_ON;
			} else {
				camera_old.stopPreview();
				camera_old.release();
				camera_old = null;

				if (parameters.getFlashMode().equals(mode_to_use)) {
					return FLASHLIGHT_ALREADY_OFF;
				}

				return FLASHLIGHT_SET_OFF;
			}
		}
	}

	public static final int ERROR_ACCESSING_CAMERA = 4;
	public static final int ERROR_ACCESSING_FLASH = 5;
	/**
	 * <p>Function to call to perform functions on the flashlight through the new API.</p>
	 * <br>
	 * <p>ATTENTION: use only from API 23. Up until API 22, use {@link #flashlightOld(int)}.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #FLASHLIGHT_SET_ON} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_SET_OFF} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_ON} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #FLASHLIGHT_ALREADY_OFF} --> for the returning value: same as in {@link #flashlightOld(int)}</p>
	 * <p>- {@link #ERROR_ACCESSING_CAMERA} --> for the returning value: if there was an error accessing the camera
	 * <p>- {@link #ERROR_ACCESSING_FLASH} --> for the returning value: if there was an error accessing the camera
	 * flash unit by one of the reasons listed in {@link CameraManager#setTorchMode(String, boolean)}</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param set_enabled true to turn on the flashlight, false to turn it off
	 *
	 * @return one of the constants
	 */
	@RequiresApi(api = Build.VERSION_CODES.M) // Because of setTorchMode()
	private int flashlightNew(final boolean set_enabled) {
		// The API warnings here are wrong, I think - I believe the condition is correct. Either API >= 21 or 23.
		final CameraManager camera_new = (CameraManager) UtilsGeneral.getContext().getSystemService(Context.CAMERA_SERVICE);
		try {
			main_camera_id = camera_new.getCameraIdList()[0];
		} catch (final AndroidException ignored) {
			// It's supposed to be CameraAccessException, but it can't. The exception appeared only on API
			// 21 and it seems that prior to API 19, even loading this CameraManagement class will crash the
			// app... So I've replaced it by its direct "superior": AndroidException.

			return ERROR_ACCESSING_CAMERA;
		}

		final Boolean flashlight_new_on = (Boolean) ValuesStorage.getValue(CONSTS.main_flashlight_enabled);
		// No check with the state being null is done. This is for the assistant to try anyway even if it doesn't know
		// the state.
		if (set_enabled) {
			if (flashlight_new_on != null && flashlight_new_on) {
				return FLASHLIGHT_ALREADY_ON;
			}
		} else {
			if (flashlight_new_on != null && !flashlight_new_on) {
				return FLASHLIGHT_ALREADY_OFF;
			}
		}

		try {
			camera_new.setTorchMode(main_camera_id, set_enabled);

			return set_enabled ? FLASHLIGHT_SET_ON : FLASHLIGHT_SET_OFF;
		} catch (final AndroidException ignored) {
			// Was supposed to be CameraAccessException. Same reason as above.
			// This also means we can't get the reason of the error - since we can't even load the class...

			return ERROR_ACCESSING_FLASH;
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (null == intent || null == intent.getAction()) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-CameraManagement - " + intent.getAction());

			final String intent_action = intent.getAction();
			switch (intent_action) {
				case CONSTS_BC.ACTION_USE_CAMERA: {
					final int usage = intent.getIntExtra(CONSTS_BC.EXTRA_USE_CAMERA_1, -1);
					useCamera(usage);

					break;
				}

				case CONSTS_BC.ACTION_PICTURE_TAKEN:
				case CONSTS_BC.ACTION_PICTURE_TAKEN_NO_FOCUS: {
					if (flashlight_was_on_before_pic) {
						flashlight_was_on_before_pic = false;
						useCamera(USAGE_FLASHLIGHT_ON);
					}

					if (CONSTS_BC.ACTION_PICTURE_TAKEN_NO_FOCUS.equals(intent_action)) {
						final String speak = "Notice - It was not possible to focus the camera, but the picture was" +
								"still take.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);
					}

					break;
				}
				case CONSTS_BC.ACTION_ERR_FILE_DELETED: {
					final String speak = "Error - The picture file has been deleted before it could start being written to.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);

					break;
				}
				case CONSTS_BC.ACTION_ERR_CANT_CREATE_FILE: {
					final String speak = "Error - It was not possible to create a picture file.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);

					break;
				}
				case CONSTS_BC.ACTION_ERR_CANT_OPEN_CAM: {
					final String speak = "Error - It was not possible to open the camera.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);

					break;
				}
				case CONSTS_BC.ACTION_ERR_WRITING_PIC_TO_FILE: {
					final String speak = "Error - An error occurred while writing to the picture file.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, null);

					break;
				}
			}
		}
	};
}
