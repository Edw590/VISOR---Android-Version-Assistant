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

package com.dadi590.assist_c_a;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsPermissions;
import com.dadi590.assist_c_a.GlobalUtils.UtilsProcesses;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.BatteryProcessor.BatteryProcessor;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CameraManager.UtilsCameraManager;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsExecutor;
import com.dadi590.assist_c_a.Modules.ModulesManager.ModulesManager;
import com.dadi590.assist_c_a.Modules.ProtectedLockScr.ProtectedLockScrSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.CONSTS_SpeechRecog;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.SpeechRecognitionCtrl;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.UtilsSpeechRecognizers;
import com.dadi590.assist_c_a.Modules.Telephony.PhoneCallsProcessor.PhoneCallsProcessor;
import com.dadi590.assist_c_a.Modules.Telephony.SmsMsgsProcessor.SmsMsgsProcessor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * <p>The list of modules of the assistant plus information about their status.</p>
 * <p>Various modules just need to be instantiated to start working. The references to those instances are stored here
 * too statically, and there's no problem with that because all those the modules are instantiated inside the main app
 * process.</p>
 * <p>Check the module index before sending it into any of the functions of this class. The index is not checked inside
 * them! They will throw exceptions when they try to use the invalid index!</p>
 */
public final class ModulesList {


	// todo The 'disable_mod' parameter is not being used at all yet


	// In case it's not a module and it's just wanted to check if it's running or not. Could be a submodule. For example
	// the Google and PocketSphinx speech recognizers. That's why the constant is private - it's not to be used externally.
	private static final int TYPE1_SERVICE_CHK_ONLY = -1;
	public static final int TYPE1_SERVICE = 0;
	public static final int TYPE1_INSTANCE = 1;

	public static final int TYPE2_NOT_SPECIAL = 0;
	public static final int TYPE2_MIC_INPUT = 1;
	public static final int TYPE2_BATTERY_READER = 2;
	public static final int TYPE2_CAMERA_USAGE = 3;
	public static final int TYPE2_LOCATION_READER = 4;
	public static final int TYPE2_AUDIO_OUTPUT = 5;
	public static final int TYPE2_PHONE_CALL_READER = 6;
	public static final int TYPE2_SMS_READER = 7;
	public static final int TYPE2_SPEECH_RECOGNITION_CTRL = 8;

	// To disable a module, just comment its line here and be sure you disable its usages everywhere else or pray the
	// app won't crash because of negative index from getModuleIndex() in case it's used for the disabled module.
	private static final ModuleObj[] modules_list = {
			new ModuleObj(ModulesManager.class, "Modules Manager", TYPE1_INSTANCE, TYPE2_NOT_SPECIAL, true), // supported = true in the initialization because it must always run, so it's made not to need anything special
			//new ModuleObj(SomeValuesUpdater.class, "Some Values Updater", TYPE1_INSTANCE, TYPE2_NOT_SPECIAL, false),
			new ModuleObj(Speech2.class, "Speech", TYPE1_INSTANCE, TYPE2_AUDIO_OUTPUT, true), // supported = true because it's the communication module, so it must always run too (internally it checks if there's audio support or not to use TTS or notifications)
			//new ModuleObj(DeviceLocator.class, TYPE1_INSTANCE, TYPE2_LOCATION_READER, "Device Locator", false),
			new ModuleObj(BatteryProcessor.class, "Battery Processor", TYPE1_INSTANCE, TYPE2_BATTERY_READER, false),
			new ModuleObj(PhoneCallsProcessor.class, "Phone Calls Processor", TYPE1_INSTANCE, TYPE2_PHONE_CALL_READER, false),
			new ModuleObj(SmsMsgsProcessor.class, "SMS Messages Processor", TYPE1_INSTANCE, TYPE2_SMS_READER, false),
			new ModuleObj(AudioRecorder.class, "Audio Recorder", TYPE1_INSTANCE, TYPE2_MIC_INPUT, false),
			new ModuleObj(CameraManagement.class, "Camera Manager", TYPE1_INSTANCE, TYPE2_CAMERA_USAGE, false),
			new ModuleObj(CmdsExecutor.class, "Commands Executor", TYPE1_INSTANCE, TYPE2_NOT_SPECIAL, false),
			new ModuleObj(SpeechRecognitionCtrl.class, "Speech Recognition Control", TYPE1_INSTANCE, TYPE2_SPEECH_RECOGNITION_CTRL, false),
			new ModuleObj(CONSTS_SpeechRecog.POCKETSPHINX_RECOG_CLASS, "- Hotword recognizer", TYPE1_SERVICE_CHK_ONLY, TYPE2_NOT_SPECIAL, false), // Supported or not is said by the one above
			new ModuleObj(CONSTS_SpeechRecog.GOOGLE_RECOG_CLASS, "- Commands recognizer", TYPE1_SERVICE_CHK_ONLY, TYPE2_NOT_SPECIAL, false), // Same comment as the above
			new ModuleObj(ProtectedLockScrSrv.class, "Protected Lock Screen", TYPE1_SERVICE_CHK_ONLY, TYPE2_NOT_SPECIAL, false),
	};
	public static final int modules_list_length = modules_list.length;
	/**
	 * <p>Class to use to represent each module of the assistant.</p>
	 * <p>Check the constructor for more information.</p>
	 */
	public static class ModuleObj {

		@NonNull public final Class<?> mod_class;
		public final int mod_type1;
		public final int mod_type2;
		@NonNull public final String mod_name;
		public boolean mod_supported;

		@Nullable public Object mod_instance = null;
		public boolean disable_mod = false;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param mod_class the class of the module
		 * @param mod_name the name of the module to present to users
		 * @param mod_type1 the type 1 of the module
		 * @param mod_type2 the type 2 of the module
		 * @param mod_supported true if the module is supported by the device hardware, false otherwise
		 */
		public ModuleObj(@NonNull final Class<?> mod_class, @NonNull final String mod_name, final int mod_type1,
						 final int mod_type2, final boolean mod_supported) {
			this.mod_class = mod_class;
			this.mod_type1 = mod_type1;
			this.mod_type2 = mod_type2;
			this.mod_name = mod_name;
			this.mod_supported = mod_supported;
		}
	}

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ModulesList() {
	}

	// Put final parameters with smaller numbers than the first non-final parameter (reason on the get function's code).
	public static final int MODULE_CLASS = 0;
	public static final int MODULE_NAME = 1;
	public static final int MODULE_TYPE1 = 2;
	public static final int MODULE_TYPE2 = 3;
	public static final int MODULE_INSTANCE = 4;
	public static final int MODULE_SUPPORTED = 5;
	public static final int MODULE_DISABLE = 6;
	/**
	 * <p>Get the value on the {@link #modules_list} associated with the given key, for a specific module.</p>
	 * <br>
	 * <p>WARNING: This method does not have @Nullable on it, but it CAN return null. Though, having @Nullable would
	 * only get in the way, because it can return null only if the required value can be null. If an int is requested,
	 * it won't be null. If it's the module instance, it can be null. So it's not necessary to check every time for
	 * nullability (as would make sense).</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #MODULE_CLASS} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_NAME} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_TYPE1} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_TYPE2} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_INSTANCE} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_SUPPORTED} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p>- {@link #MODULE_DISABLE} --> for {@code key}: read {@link ModuleObj}'s documentation</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param module_index the index of the module
	 * @param key the key of the value to get (one of the constants)
	 *
	 * @return the value associated with the given key, which may or may not be null, depending on type of the wanted
	 * value as listed on {@link #modules_list}'s documentation
	 */
	public static Object getModuleValue(final int module_index, final int key) {
		switch (key) {
			case MODULE_CLASS: {
				return modules_list[module_index].mod_class;
			}
			case MODULE_NAME: {
				return modules_list[module_index].mod_name;
			}
			case MODULE_TYPE1: {
				return modules_list[module_index].mod_type1;
			}
			case MODULE_TYPE2: {
				return modules_list[module_index].mod_type2;
			}
			case MODULE_INSTANCE: {
				return modules_list[module_index].mod_instance;
			}
			case MODULE_SUPPORTED: {
				return modules_list[module_index].mod_supported;
			}
			case MODULE_DISABLE: {
				return modules_list[module_index].disable_mod;
			}
		}

		// Won't happen - always implement the constants on the switch.
		// Don't put null, or Android Studio will think it's a @Nullable method, and then warn about 'boolean' null
		// values...
		return "";
	}

	/**
	 * <p>Set the value on the {@link #modules_list} associated with the given key, for a specific module.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #MODULE_SUPPORTED} --> for {@code key}: the 5th index on {@link #modules_list}'s doc</p>
	 * <p>- {@link #MODULE_DISABLE} --> for {@code key}: the 6th index on {@link #modules_list}'s doc</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param module_index the index of the module
	 * @param key the key of the value to get (one of the constants)
	 * @param value the value associated with the given key
	 */
	public static void setModuleValue(final int module_index, final int key, @NonNull final Object value) {
		if (key < MODULE_SUPPORTED) {
			// This just is to avoid setting wrong parameters by mistake.
			// The instance could be here too, but that's only for internal class usage.
			return;
		}

		switch (key) {
			case MODULE_SUPPORTED: {
				modules_list[module_index].mod_supported = (boolean) value;

				break;
			}
			case MODULE_DISABLE: {
				modules_list[module_index].disable_mod = (boolean) value;

				break;
			}
		}
	}

	/**
	 * <p>Checks if the given module is running.</p>
	 *
	 * @param module_index the index of the module
	 *
	 * @return true if it's running, false otherwise
	 */
	public static boolean isModuleRunning(final int module_index) {
		switch ((int) getModuleValue(module_index, MODULE_TYPE1)) {
			case (TYPE1_SERVICE):
			case (TYPE1_SERVICE_CHK_ONLY): {
				return UtilsServices.isServiceRunning((Class<?>) getModuleValue(module_index, MODULE_CLASS));
			}
			case (TYPE1_INSTANCE): {
				return null != getModuleValue(module_index, MODULE_INSTANCE);
			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>Checks if the given module is fully working.</p>
	 * <br>
	 * <p><strong>Attention:</strong></p>
	 * <p>Please check if the module is running with {@link #isModuleRunning(int)} before calling this function,
	 * otherwise the result may not be correct and should not be trusted.</p>
	 *
	 * @param module_index the index of the module
	 *
	 * @return true if it's fully working, false otherwise
	 */
	public static boolean isModuleFullyWorking(final int module_index) {
		final IModule iModule = getIModuleInstance(module_index);
		if (null == iModule) {
			// Assume it's fully working if the class doesn't implement IModule (no way of knowing, yet at least).
			// The getIModuleInstance() also returns null if the module is not running, but it's supposed to be checked
			// if it's running before calling this function, so all should be good.
			return true;
		} else {
			return iModule.isModuleFullyWorking();
		}
	}

	/**
	 * <p>The index of the module in the {@link #modules_list}.</p>
	 *
	 * @param module_class the class of the module
	 *
	 * @return the index of the module
	 */
	public static int getModuleIndex(@NonNull final Class<?> module_class) {
		for (int i = 0; i < modules_list_length; ++i) {
			if ((Class<?>) getModuleValue(i, MODULE_CLASS) == module_class) {
				return i;
			}
		}

		return -1; // Won't ever get here - just supply a valid module by not using a string and calling .class on the
		// class directly.
	}

	/**
	 * <p>Starts the specified module in case it has not been already started.</p>
	 * <p>In case it's a Service, it will be started in background (not foreground), as the Main Service is already in
	 * foreground - one is enough according to the Android policies.</p>
	 * <p>In case it's just to start the module and keep a reference to it, its reference will be stored on the
	 * {@link #modules_list}.</p>
	 *
	 * @param module_index the index of the module to start
	 *
	 * @return true if the module was started, false if it was already running
	 */
	public static boolean startModule(final int module_index) {
		final Class<?> module_class = (Class<?>) getModuleValue(module_index, MODULE_CLASS);
		switch ((int) getModuleValue(module_index, MODULE_TYPE1)) {
			case (ModulesList.TYPE1_SERVICE): {

				// The call to startService() will already check if the service is running or not.
				return UtilsServices.startService(module_class, null, false);
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (!ModulesList.isModuleRunning(module_index)) {
					try {
						modules_list[module_index].mod_instance = module_class.getConstructor().newInstance();

						return true;
					} catch (final NoSuchMethodException e) {
						e.printStackTrace();
					} catch (final IllegalAccessException e) {
						e.printStackTrace();
					} catch (final InstantiationException e) {
						e.printStackTrace();
					} catch (final InvocationTargetException e) {
						e.printStackTrace();
					}
				}

				return false;
			}

			default: {
				return false;
			}
		}
	}

	/**
	 * <p>Stops the specified module in case it's running.</p>
	 * <p>In case it's of type {@link #TYPE1_SERVICE}, its PID will be terminated. If it's of type
	 * {@link #TYPE1_INSTANCE}, {@link IModule#destroyModule()} will be called on it and its reference will be set
	 * to null.</p>
	 *
	 * @param module_index the index of the module to stop
	 *
	 * @return true if the module was stopped, false if it was already stopped
	 */
	public static boolean stopModule(final int module_index) {
		switch ((int) getModuleValue(module_index, MODULE_TYPE1)) {
			case (ModulesList.TYPE1_SERVICE): {

				final Class<?> module_class = (Class<?>) getModuleValue(module_index, MODULE_CLASS);
				// The call to startService() will already check if the service is running or not.
				if (UtilsServices.isServiceRunning(module_class)) {
					UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(module_class));

					return true;
				} else {
					return false;
				}
			}
			case (ModulesList.TYPE1_INSTANCE): {
				if (ModulesList.isModuleRunning(module_index)) {
					((IModule) getModuleValue(module_index, MODULE_INSTANCE)).destroyModule();
					modules_list[module_index].mod_instance = null;

					return true;
				} else {
					return false;
				}
			}
		}

		// Won't ever get here.
		return false;
	}

	/**
	 * <p>Restarts the specified module, whether it was running or not.</p>
	 * <p>Only 2 functions are called here: first {@link #stopModule(int)} and then {@link #startModule(int)}.</p>
	 *
	 * @param module_index the index of the module to restart
	 */
	public static void restartModule(final int module_index) {
		stopModule(module_index);
		startModule(module_index);
	}

	/**
	 * <p>Get the module instance masked as an {@link IModule} in case the module implements it.</p>
	 * <br>
	 * <p><strong>Attention:</strong></p>
	 * <p>Please check if the module is running with {@link #isModuleRunning(int)} before calling this function,
	 * otherwise the result may not be correct and should not be trusted.</p>
	 *
	 * @param module_index the index of the module to get as an {@link IModule}
	 *
	 * @return the {@link IModule} type instance; null in case the module does not implement said interface
	 */
	@Nullable
	private static IModule getIModuleInstance(final int module_index) {
		if ((IModule.class.isAssignableFrom((Class<?>) getModuleValue(module_index, MODULE_CLASS)))) {
			// iI the module implements the interface but it's not running, its instance will be null. Though it's
			// assumed the module is running when this function is called (as recommended in the documentation), so all
			// should be good.
			return (IModule) getModuleValue(module_index, MODULE_INSTANCE);
		}

		return null;
	}

	/**
	 * <p>Checks if the device running the app supports the module.</p>
	 * <p>An example of this is the CameraManager and the Telephony modules. If the device has no camera (??? well,
	 * maybe a car or something), why start the module and waste resources? Exactly, non-sense. Or on a tablet where we
	 * can't make phone calls or send SMS messages (no telephony stuff at all), why start those methods? Non-sense
	 * again. This function ensures the device has hardware to support the module's features.</p>
	 *
	 * @param module_index the index of the module to check
	 *
	 * @return true if the module is supported by the device, false otherwise
	 */
	public static boolean deviceSupportsModule(final int module_index) {
		return deviceSupportsModType((int) getModuleValue(module_index, MODULE_TYPE2));
	}

	/**
	 * <p>Checks if the device running the app supports a module type.</p>
	 *
	 * @param module_type one of the TYPE2_-started constants in this class
	 *
	 * @return true if the module type is supported by the device, false otherwise
	 */
	public static boolean deviceSupportsModType(final int module_type) {
		final Context context = UtilsGeneral.getContext();

		// Here module_type is not one of the TYPE1_-started constants. Instead, the TYPE2_-started constants.
		switch (module_type) {
			case TYPE2_NOT_SPECIAL:
			case TYPE2_BATTERY_READER: { // Can we check if a device has a battery plugged in??
				return true;
			}
			case TYPE2_SPEECH_RECOGNITION_CTRL:
			case TYPE2_MIC_INPUT: {
				if (TYPE2_SPEECH_RECOGNITION_CTRL == module_type) {
					if (!UtilsSpeechRecognizers.isGoogleRecogAvailable()) {
						return false;
					}
				}

				final PackageManager packageManager = context.getPackageManager();

				return packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
			}
			case TYPE2_CAMERA_USAGE: {
				return UtilsCameraManager.deviceHasAnyCamera();
			}
			case TYPE2_LOCATION_READER: {
				// todo This needs GPS checking, possibly mobile data too, WiFi, Bluetooth... (just one or various need
				//  to be true?)
				return false;
			}
			case TYPE2_PHONE_CALL_READER:
			case TYPE2_SMS_READER: {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
						UtilsPermissions.checkSelfPermission(Manifest.permission.MODIFY_PHONE_STATE)) {
					// TelecomManager.getAllPhoneAccounts() needs MODIFY_PHONE_STATE (by experience).
					final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
					final List<PhoneAccount> phoneAccount_list = telecomManager.getAllPhoneAccounts();
					for (final PhoneAccount phoneAccount : phoneAccount_list) {
						if (0 == (phoneAccount.getCapabilities() & PhoneAccount.CAPABILITY_PLACE_EMERGENCY_CALLS)) {
							// If it can place emergency calls, it can make at least that type of call. So start the
							// module, as any call type counts.
							// Though, as CAPABILITY_PLACE_EMERGENCY_CALLS's documentation says, phone accounts BY
							// DEFAULT can place emergency calls ("by default").
							return true;
						}
					}
				}

				final PackageManager packageManager = context.getPackageManager();
				if (!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY) &&
						!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_CDMA)) {
					return false;
				}

				// If the device has a telephony radio, check if it can specifically send SMS messages or make a phone
				// call by reacting to an Android scheme, for SMS messages defined in (at least - there are more places
				// where there is a definition, though always outside of the Android system code, I believe):
				// https://android.googlesource.com/platform/packages/apps/Messaging/+/master/src/com/android/messaging/util/UriUtil.java.
				final String scheme;
				final String intent_action;
				if (TYPE2_SMS_READER == module_type) {
					intent_action = Intent.ACTION_SENDTO;
					scheme = "smsto";
				} else {
					intent_action = Intent.ACTION_CALL;
					scheme = "tel";
				}
				final Intent intent = new Intent(intent_action, Uri.fromParts(scheme, "+351000000000", null));

				// No problem in MATCH_ALL being available only from API 23 onwards. Below that it will just be ignored.
				return UtilsGeneral.isIntentActionAvailable(intent, PackageManager.MATCH_ALL);
			}
			case TYPE2_AUDIO_OUTPUT: {
				final PackageManager packageManager = context.getPackageManager();
				final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

				Boolean has_audio_output_feature = null;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					has_audio_output_feature = packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT);
				}

				// If the device is below Lollipop or the method returned false, check if anything else is connected to
				// the device that can output sound. If yes, cool. If not, then there's probably no way of playing sound
				// on the device.
				// Why would the FEATURE_AUDIO_OUTPUT return false with audio output available?
				// "I tested this feature on my MOTO 360 (no speaker), it don't has this feature, and Ticwatch (with
				// speaker) do have this feature. But when I connected a Bluetooth headset to the MOTO 360, it still
				// don't have this feature, this confused me." --> https://stackoverflow.com/a/32903108/8228163.
				final boolean any_audio_device_connected = audioManager.isBluetoothA2dpOn() ||
						audioManager.isBluetoothScoOn() || audioManager.isWiredHeadsetOn() ||
						audioManager.isSpeakerphoneOn();

				if (null == has_audio_output_feature) {
					// Assume there's always a speaker below Lollipop. I have a tablet with KitKat which does have
					// speakers and can have headphones, but with wired headphones connected or not, nothing works, so
					// whatever.
					return true;
				} else {
					if (has_audio_output_feature) {
						return true;
					} else {
						return any_audio_device_connected;
					}
				}
			}
		}

		// Won't happen - use your grey mass and don't insert wtf values (or don't implement new values...)
		return false;
	}
}
