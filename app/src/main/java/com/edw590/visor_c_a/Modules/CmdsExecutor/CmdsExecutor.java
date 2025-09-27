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

package com.edw590.visor_c_a.Modules.CmdsExecutor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroid;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.edw590.visor_c_a.GlobalUtils.AndroidSystem.UtilsAndroidTelephony;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsLogging;
import com.edw590.visor_c_a.GlobalUtils.UtilsNativeLibs;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.GlobalUtils.UtilsTimeDate;
import com.edw590.visor_c_a.Modules.AudioRecorder.AudioRecorder;
import com.edw590.visor_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.edw590.visor_c_a.Modules.CameraManager.CameraManagement;
import com.edw590.visor_c_a.Modules.CameraManager.UtilsCameraManagerBC;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.UtilsCmdsList;
import com.edw590.visor_c_a.Modules.ScreenRecorder.ScreenRecorder;
import com.edw590.visor_c_a.Modules.ScreenRecorder.UtilsScreenRecorderBC;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.edw590.visor_c_a.Modules.TelephonyManagement.TelephonyManagement;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;

import ACD.ACD;
import GMan.GMan;
import GPTComm.GPTComm;
import OICComm.OICComm;
import UtilsSWA.UtilsSWA;

/**
 * The module that processes and executes all commands told to it (from the speech recognition or by text).
 */
public final class CmdsExecutor implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String)
			ModulesList.getElementValue(element_index, ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	boolean ask_anything_else = true;

	DialogMan.HandleInputResult handle_input_result = null;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread);
	}
	@Override
	public void destroy() {
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		return true;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public CmdsExecutor() {
		// Prepare the Advanced Commands Detection module commands array
		ACD.reloadCmdsArray(UtilsCmdsList.prepareCommandsString());
		DialogMan.DialogMan.clearIntentsList();
		for (final DialogMan.Intent intent : CmdsList.getIntentList()) {
			DialogMan.DialogMan.addToIntentList(intent);
		}

		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		registerReceiver();
	}

	/**
	 * <p>This function checks and executes all tasks included in a string.</p>
	 *
	 * @param sentence the string to be analyzed for commands
	 * @param partial_results true if the function is being called by partial recognition results (onPartialResults()),
	 * false otherwise (onResults(); other, like a text input).
	 * @param internal_usage true if the function is being called for internal usage such as turning on mobile data to
	 * do some user-requested task like get the weather, false if it's being called by the user.
	 *
	 * @return one of the constants
	 */
	void processTask(@NonNull final String sentence, final boolean partial_results, final boolean internal_usage) {
		if (!UtilsNativeLibs.isPrimaryNativeLibAvailable(UtilsNativeLibs.ACD_LIB_NAME)) {
			final String speak = "ATTENTION - Commands detection is not available. VISOR Libraries file was not " +
					"detected.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

			return;
		}

		final int speech_priority;
		if (internal_usage) {
			speech_priority = Speech2.PRIORITY_MEDIUM;
		} else {
			speech_priority = Speech2.PRIORITY_USER_ACTION;
		}

		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

		ask_anything_else = true;

		handle_input_result = DialogMan.DialogMan.handleInput(sentence, handle_input_result);
		UtilsLogging.logLnDebug("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
		UtilsLogging.logLnDebug(handle_input_result);
		if (handle_input_result == null) {
			sendToGPT(sentence);

			return;
		}

		if (!handle_input_result.getResponse().isEmpty()) {
			UtilsSpeech2BC.speak(handle_input_result.getResponse(), speech_priority, Speech2.MODE_DEFAULT,
					UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		}

		boolean any_intent_detected = false;
		DialogMan.Intent[] intents = {
				handle_input_result.getIntent0(), handle_input_result.getIntent1(), handle_input_result.getIntent2(),
				handle_input_result.getIntent3(), handle_input_result.getIntent4(), handle_input_result.getIntent5(),
				handle_input_result.getIntent6(), handle_input_result.getIntent7(), handle_input_result.getIntent8(),
				handle_input_result.getIntent9()
		};
		for (final DialogMan.Intent intent : intents) {
			if (intent == null) {
				break;
			}
			any_intent_detected = true;

			// Keep it checking with CMDi_INF1_DO_SOMETHING and inverting the output. That way, if cmd_to_check is ""
			// (no previous command), it won't equal DO_SOMETHING and will set cmdi_only_speak to true.
			final int speech_mode2 = CmdsList.CmdAddInfo.CMDi_INF1_ONLY_SPEAK.
					equals(CmdsList.CmdAddInfo.CMDi_INFO.get(intent.getAcd_cmd_id())) ?
					Speech2.MODE2_BYPASS_NO_SND : Speech2.MODE_DEFAULT;

			switch (intent.getAcd_cmd_id()) {
				case (CmdsList.CmdIds.CMD_TOGGLE_FLASHLIGHT): {
					UtilsCameraManagerBC.useCamera(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ?
							CameraManagement.USAGE_FLASHLIGHT_ON : CameraManagement.USAGE_FLASHLIGHT_OFF);

					if (intent.getValue().equals(CmdsList.CmdRetIds.RET_ON)) {
						final String speak = "Flashlight turned on.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						final String speak = "Flashlight turned off.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_TIME): {
					final String speak = "It's " + UtilsTimeDate.getTimeStr(-1);
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_DATE): {
					final String speak = "Today's " + UtilsTimeDate.getDateStr(-1);
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_WIFI): {
					switch (UtilsAndroidConnectivity.setWifiEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Wi-Fi turned " + (intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ?
									"on." : "off.");
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE,
									false, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Wi-Fi service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Wi-Fi.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Wi-Fi is already disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLING): {
							final String speak = "The Wi-Fi is already being disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Wi-Fi is already enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLING): {
							final String speak = "The Wi-Fi is already being enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to toggle the Wi-Fi.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_MOBILE_DATA): {
					switch (UtilsAndroidConnectivity.setMobileDataEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Mobile Data connection turned " +
									(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ? "on." : "off.");
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE,
									false, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Mobile Data connection.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Mobile Data is already disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Mobile Data is already enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to toggle the Mobile Data connection.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_BLUETOOTH): {
					switch (UtilsAndroidConnectivity.setBluetoothEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Bluetooth turned " + (intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ?
									"on." : "off.");
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "The device does not feature a Bluetooth adapter.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error toggling the Bluetooth.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Bluetooth.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Bluetooth is already disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLING): {
							final String speak = "The Bluetooth is already being disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Bluetooth is already enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLING): {
							final String speak = "The Bluetooth is already being enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to toggle the Bluetooth.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_ANSWER_CALL): {
					switch (UtilsAndroidTelephony.answerPhoneCall()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Call answered.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error answering the call.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_END_CALL): {
					switch (UtilsAndroidTelephony.endPhoneCall()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Call ended.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error ending the call.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_SPEAKERS): {
					final String speak;
					if (((String) UtilsRegistry.getData(RegistryKeys.K_CURR_PHONE_CALL_NUMBER, true)).isEmpty()) {
						speak = "The device not in a phone call.";
					} else {
						if (UtilsAndroidTelephony.setCallSpeakerphoneEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
							speak = "Speakerphone turned " + (intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ?
									"on." : "off.");
						} else {
							speak = "Audio service not available on the device.";
						}
					}
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_AIRPLANE_MODE): {
					switch (UtilsAndroidConnectivity.setAirplaneModeEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Airplane Mode turned " + (intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ?
									"on." : "off.");
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Airplane Mode.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Airplane Mode is already disabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Airplane Mode is already enabled.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to toggle the Airplane Mode.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_BATTERY_PERCENT): {
					final boolean battery_present = (boolean) UtilsRegistry.
							getData(RegistryKeys.K_BATTERY_PRESENT, true);
					if (!battery_present) {
						final String speak = "There is no battery present on the device.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					final int battery_percentage = (int) UtilsRegistry.getData(RegistryKeys.K_BATTERY_LEVEL, true);
					final String speak = "Battery percentage: " + battery_percentage + "%.";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					break;
				}
				case (CmdsList.CmdIds.CMD_POWER_SHUT_DOWN): {
					// Don't say anything if it's successful - he will already say "Shutdown detected".
					// EDIT: sometimes he doesn't say that. Now it says something anyway.

					switch (UtilsAndroidPower.shutDownDevice()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Shutting down the device...";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							ask_anything_else = false;

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to shut down the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to shut down the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_POWER_REBOOT): {
					// Don't say anything if it's successful - he will already say "Shutdown detected".
					// EDIT: sometimes he doesn't say that. Now it says something anyway.

					final int reboot_mode;
					switch (intent.getValue()) {
						case CmdsList.CmdRetIds.RET_14_NORMAL: {
							reboot_mode = UtilsAndroid.MODE_NORMAL;
							break;
						}
						case CmdsList.CmdRetIds.RET_14_SAFE_MODE: {
							reboot_mode = UtilsAndroid.MODE_SAFE;
							break;
						}
						case CmdsList.CmdRetIds.RET_14_RECOVERY: {
							reboot_mode = UtilsAndroid.MODE_RECOVERY;
							break;
						}
						case CmdsList.CmdRetIds.RET_14_BOOTLOADER: {
							reboot_mode = UtilsAndroid.MODE_BOOTLOADER;
							break;
						}
						case CmdsList.CmdRetIds.RET_14_FAST: {
							reboot_mode = UtilsAndroid.MODE_FAST;
							break;
						}
						default: {
							continue;
						}
					}

					switch (UtilsAndroidPower.rebootDevice(reboot_mode)) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Rebooting the device...";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							ask_anything_else = false;

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to reboot the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
						default: {
							final String speak = "Unspecified error attempting to reboot the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TAKE_PHOTO): {
					boolean rear_pic = intent.getValue().equals(CmdsList.CmdRetIds.RET_15_REAR);

					UtilsCameraManagerBC.useCamera(rear_pic ? CameraManagement.USAGE_TAKE_REAR_PHOTO :
							CameraManagement.USAGE_TAKE_FRONTAL_PHOTO);

					String speak = "Taking a " + (rear_pic ? "rear" : "frontal") + " picture...";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					break;
				}
				case (CmdsList.CmdIds.CMD_RECORD_MEDIA): {
					switch (intent.getValue()) {
						case (CmdsList.CmdRetIds.RET_16_AUDIO_1):
						case (CmdsList.CmdRetIds.RET_16_AUDIO_2): {
							if (!(boolean) ModulesList.getElementValue(
									ModulesList.getElementIndex(AudioRecorder.class), ModulesList.ELEMENT_SUPPORTED)) {
								final String speak = "Audio recording is not supported on this device through " +
										"either hardware or application permissions limitations.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2,
										GPTComm.SESSION_TYPE_TEMP, false, null);

								continue;
							}

							// Can only start recording when the commands speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								ask_anything_else = false;

								UtilsSpeechRecognizersBC.stopRecognition(() -> {
									UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);
								});
							}

							break;
						}
						case (CmdsList.CmdRetIds.RET_16_VIDEO_1):
						case (CmdsList.CmdRetIds.RET_16_VIDEO_2): {
							// todo
							// todo Also missing the record frontal and rear video commands (this one is generic)

							break;
						}
						case (CmdsList.CmdRetIds.RET_16_SCREEN_1):
						case (CmdsList.CmdRetIds.RET_16_SCREEN_2): {
							if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
								String speak = "Screen recording is not available below Android 5.0 Lollipop.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2,
										GPTComm.SESSION_TYPE_TEMP, false, null);

								continue;
							}

							if (!(boolean) ModulesList.getElementValue(
									ModulesList.getElementIndex(ScreenRecorder.class), ModulesList.ELEMENT_SUPPORTED)) {
								final String speak = "Screen recording is not supported on this device through " +
										"either hardware or application permissions limitations.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2,
										GPTComm.SESSION_TYPE_TEMP, false, null);

								continue;
							}

							// Can only start recording when the commands speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								ask_anything_else = false;

								UtilsScreenRecorderBC.recordScreen(true, false);
							}

							break;
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_SAY_AGAIN): {
					UtilsSpeech2BC.sayAgain();

					// todo Save speeches on an ArrayList or something to be possible to say the second-last thing or
					// one or two more (humans have limited memory --> "I don't know what I said 3 minutes ago!").
					// Also make sure if there are things with higher priority on the lists that the last thing said is
					// the last thing said when it was requested.

					break;
				}
				case (CmdsList.CmdIds.CMD_CALL_CONTACT): {
					final int contact_index = (int) ACD.getSubCmdIndex(intent.getValue());
					final String[][] contacts_list = TelephonyManagement.getContactsList();
					final String contact_name = contacts_list[contact_index][0];
					final String contact_number = contacts_list[contact_index][1];

					final Runnable runnable = () -> {
						final int return_code = UtilsAndroidTelephony.makePhoneCall(contact_number);

						switch (return_code) {
							case (UtilsAndroid.NO_CALL_EMERGENCY): {
								final String speak = "Insufficient privileges to call " + contact_number +
										", since it is an emergency number. " +
										"Instead, it was only dialed and requires your manual confirmation " +
										"to proceed the call.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
							case (UtilsAndroid.NO_CALL_ANY): {
								final String speak = "Insufficient privileges to call numbers. The number " +
										"was instead only dialed and requires your manual confirmation " +
										"to proceed the call.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
							case (UtilsAndroid.NOT_AVAILABLE): {
								final String speak = "Phone calls not supported on the device.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
						}
					};

					final String speak = "Calling " + contact_name + " now, sir.";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE, false,
							runnable);

					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_RECORD_MEDIA): {
					boolean stop_audio = false;
					boolean stop_video = false;
					boolean stop_screen = false;

					switch (intent.getValue()) {
						case CmdsList.CmdRetIds.RET_20_AUDIO: {
							stop_audio = true;

							break;
						}
						case CmdsList.CmdRetIds.RET_20_VIDEO: {
							stop_video = true;

							break;
						}
						case CmdsList.CmdRetIds.RET_20_SCREEN: {
							stop_screen = true;

							break;
						}
						case CmdsList.CmdRetIds.RET_20_ANY: {
							stop_audio = true;
							stop_video = true;
							stop_screen = true;

							break;
						}
						default: {
							continue;
						}
					}

					if (stop_audio) {
						UtilsAudioRecorderBC.recordAudio(false, -1, true);
					}
					if (stop_video) {
						// todo
					}
					if (stop_screen) {
						UtilsScreenRecorderBC.recordScreen(false, true);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_POWER_SAVER_MODE): {
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						final String speak = "Battery Saver Mode not available below Android Lollipop.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						switch (UtilsAndroidPower.setBatterySaverEnabled(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON))) {
							case (UtilsShell.ErrCodes.NO_ERR): {
								final String speak = "Battery Saver Mode turned " +
										(intent.getValue().equals(CmdsList.CmdRetIds.RET_ON) ? "on." : "off.");
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
							case (UtilsShell.ErrCodes.PERM_DENIED): {
								final String speak = "No permission to toggle the Battery Saver Mode.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
							default: {
								final String speak = "Unspecified error attempting to toggle the Battery Saver Mode.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								break;
							}
						}
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_CONTROL_MEDIA): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						if (audioManager == null) {
							String speak = "No audio available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}

						switch (intent.getValue()) {
							case (CmdsList.CmdRetIds.RET_21_PLAY): {
								String speak = "Playing now, Sir.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_PAUSE): {
								String speak = "Paused, Sir.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_STOP): {
								String speak = "Stopped, Sir.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));


								break;
							}
							case (CmdsList.CmdRetIds.RET_21_NEXT): {
								String speak = "Next one, Sir.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_PREVIOUS): {
								String speak = "Previous one, Sir.";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));

								break;
							}
						}
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_LISTENING): {
					if ((boolean) UtilsRegistry.getData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, true)) {
						final String speak = "Background hot-word recognition already stopped.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						UtilsRegistry.setData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, true, false);
						UtilsSpeechRecognizersBC.stopRecognition(null);

						final String speak = "Background hot-word recognition stopped.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_START_LISTENING): {
					if ((boolean) UtilsRegistry.getData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, true)) {
						UtilsRegistry.setData(RegistryKeys.K_POCKETSPHINX_REQUEST_STOP, false, false);
						// We could wait for the controller to restart it, but this way it's faster.
						UtilsSpeechRecognizersBC.startPocketSphinxRecognition();

						final String speak = "Background hot-word recognition started.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						final String speak = "The background hot-word recognition is not stopped.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TELL_WEATHER): {
					String speak = "Obtaining the weather...";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE, false,
							null);

					final boolean data_was_enabled = UtilsAndroidConnectivity.getMobileDataEnabled();
					final boolean wifi_was_enabled = UtilsAndroidConnectivity.getWifiEnabled();
					if ((int) UtilsRegistry.getData(RegistryKeys.K_CURR_NETWORK_TYPE, true) == -1) {
						UtilsAndroidConnectivity.setMobileDataEnabled(true);
						UtilsAndroidConnectivity.setWifiEnabled(true);
					}
					if (UtilsSWA.waitForNetwork(10)) {
						String[] weather_locs = OICComm.getWeatherLocationsList().split("\\|");
						for (final String weather_loc : weather_locs) {
							final ModsFileInfo.Weather weather = OICComm.getWeather(weather_loc);

							if (!data_was_enabled) {
								UtilsAndroidConnectivity.setMobileDataEnabled(false);
							}
							if (!wifi_was_enabled) {
								UtilsAndroidConnectivity.setWifiEnabled(false);
							}

							if (weather == null) {
								UtilsSpeech2BC.speak("I'm sorry Sir, but I couldn't get the weather information.",
										speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

								break;
							}

							if (weather.getTemperature().isEmpty()) {
								// One being empty means the whole weather is empty
								speak = "There was a problem obtaining the weather for " + weather.getLocation() + ".";
								UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP,
										false, null);

								continue;
							}

							String status_part = " is ";
							if (!weather.getStatus().equals("ERROR")) {
								status_part += weather.getStatus() + " with ";
							}

							speak = "The weather in " + weather.getLocation() + status_part + weather.getTemperature() +
									" degrees, a high of " + weather.getMax_temp() + " degrees and a low of " +
									weather.getMin_temp() + " degrees. The mean precipitation is of " +
									weather.getPrecipitation() + ", mean humidity of " + weather.getHumidity() +
									", and mean wind of " + weather.getWind() + ".";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_ACTIVE,
									false, null);
						}
					} else {
						speak = "Not connected to the server to get the weather.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_TELL_NEWS): {
					String speak = "Obtaining the latest news...";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE, false,
							null);

					final boolean data_was_enabled = UtilsAndroidConnectivity.getMobileDataEnabled();
					final boolean wifi_was_enabled = UtilsAndroidConnectivity.getWifiEnabled();
					if ((int) UtilsRegistry.getData(RegistryKeys.K_CURR_NETWORK_TYPE, true) == -1) {
						UtilsAndroidConnectivity.setMobileDataEnabled(true);
						UtilsAndroidConnectivity.setWifiEnabled(true);
					}
					if (UtilsSWA.waitForNetwork(10)) {
						String[] news_locs = OICComm.getNewsLocationsList().split("\\|");
						for (final String news_loc : news_locs) {
							final ModsFileInfo.News news = OICComm.getNews(news_loc);

							if (!data_was_enabled) {
								UtilsAndroidConnectivity.setMobileDataEnabled(false);
							}
							if (!wifi_was_enabled) {
								UtilsAndroidConnectivity.setWifiEnabled(false);
							}

							if (news == null) {
								UtilsSpeech2BC.speak("I'm sorry Sir, but I couldn't get the news information.",
										speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

								break;
							}

							speak = "News in " + news.getLocation() + ". ";

							String[] news_info = news.getNewsList().split("\\|");
							final int news_len = news_info.length;
							for (int i = 1; i < news_len; ++i) {
								speak += news_info[i] + ". ";
							}
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_ACTIVE,
									false, null);
						}
					} else {
						speak = "Not connected to the server to get the news.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_EVENTS): {
					String speak = "Obtaining the tasks and events...";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, UtilsSpeech2BC.SESSION_TYPE_NONE, false,
							null);

					final boolean data_was_enabled = UtilsAndroidConnectivity.getMobileDataEnabled();
					final boolean wifi_was_enabled = UtilsAndroidConnectivity.getWifiEnabled();
					if ((int) UtilsRegistry.getData(RegistryKeys.K_CURR_NETWORK_TYPE, true) == -1) {
						UtilsAndroidConnectivity.setMobileDataEnabled(true);
						UtilsAndroidConnectivity.setWifiEnabled(true);
					}
					if (UtilsSWA.waitForNetwork(10)) {
						String[] events_ids = GMan.getEventsIdsList(true).split("\\|");
						String[] tasks_ids = GMan.getTasksIdsList().split("\\|");

						if (!data_was_enabled) {
							UtilsAndroidConnectivity.setMobileDataEnabled(false);
						}
						if (!wifi_was_enabled) {
							UtilsAndroidConnectivity.setWifiEnabled(false);
						}

						speak = GManUtils.getEventsList(events_ids, intent.getValue());

						if (intent.getValue().equals(CmdsList.CmdRetIds.RET_31_TODAY) ||
								intent.getValue().equals(CmdsList.CmdRetIds.RET_31_TOMORROW)) {
							speak += " " + GManUtils.getTasksList(tasks_ids, intent.getValue());
						}

						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_ACTIVE, true,
								null);
					} else {
						speak = "Not connected to the server to get the tasks and events.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					break;
				}
				case (CmdsList.CmdIds.CMD_GONNA_SLEEP): {
					//UtilsAndroidConnectivity.setAirplaneModeEnabled(true); todo Needs root commands implementation
					//UtilsRegistry.setValue(ValuesRegistry.Keys.IS_USER_SLEEPING, true);

					break;
				}
			}
		}
		if (!any_intent_detected) {
			sendToGPT(sentence);
		}


		/*if (detected_cmds.length == 0) {
			return NOTHING_EXECUTED;
		} else {
			if (something_done) {
				if (!something_said) {
					if (!only_returning) {
						final String speak = "Done.";
						UtilsSpeech2BC.speak(speak, speech_priority, 0, null);
					}
				}
			} else if (!something_said) {
				return NOTHING_EXECUTED;
			}
		}*/

		/*if (ask_anything_else && !internal_usage) {
			final String speak = "Anything else sir?";
			UtilsSpeech2BC.speak(speak, speech_priority, 0, true, UtilsSpeech2.CALL_COMMANDS_RECOG);
		}*/
	}

	/**
	 * <p>Send text to the GPT.</p>
	 *
	 * @param txt_to_send The text to send.
	 */
	private static void sendToGPT(final String txt_to_send) {
		if (!UtilsSWA.isCommunicatorConnectedSERVER()) {
			String speak = "GPT unavailable. Not connected to the server.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);

			return;
		}


		String speak = "";
		switch (GPTComm.sendText(txt_to_send, GPTComm.SESSION_TYPE_ACTIVE, GPTComm.ROLE_USER, false)) {
			case (ModsFileInfo.ModsFileInfo.MOD_7_STATE_STOPPED): {
				speak = "The GPT is stopped. Text on hold.";

				break;
			}
			case (ModsFileInfo.ModsFileInfo.MOD_7_STATE_STARTING): {
				speak = "The GPT is starting up. Text on hold.";

				break;
			}
			case (ModsFileInfo.ModsFileInfo.MOD_7_STATE_BUSY): {
				speak = "The GPT is busy. Text on hold.";

				break;
			}
		}
		if (!speak.isEmpty() && !txt_to_send.equals("/stop")) {
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_USER_ACTION, 0, UtilsSpeech2BC.SESSION_TYPE_NONE, false, null);
		}
	}



	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(CONSTS_BC_CmdsExec.ACTION_CALL_PROCESS_TASK);

		try {
			UtilsContext.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			UtilsLogging.logLnInfo("PPPPPPPPPPPPPPPPPP-Executor - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (CONSTS_BC_CmdsExec.ACTION_CALL_PROCESS_TASK): {
					final String sentence_str = intent.getStringExtra(CONSTS_BC_CmdsExec.EXTRA_CALL_PROCESS_TASK_1);
					final boolean partial_results = intent.getBooleanExtra(CONSTS_BC_CmdsExec.EXTRA_CALL_PROCESS_TASK_2,
							false);
					final boolean only_returning = intent.getBooleanExtra(CONSTS_BC_CmdsExec.EXTRA_CALL_PROCESS_TASK_3,
							false);
					final boolean internal_usage = intent.getBooleanExtra(CONSTS_BC_CmdsExec.EXTRA_CALL_PROCESS_TASK_4,
							false);
					processTask(sentence_str, partial_results, internal_usage);

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
