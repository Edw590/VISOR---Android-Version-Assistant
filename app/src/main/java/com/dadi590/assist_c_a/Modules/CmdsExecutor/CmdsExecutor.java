/*
 * Copyright 2023 DADi590
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

package com.dadi590.assist_c_a.Modules.CmdsExecutor;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroid;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidTelephony;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNativeLibs;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;
import com.dadi590.assist_c_a.GlobalUtils.UtilsTimeDate;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CameraManager.UtilsCameraManagerBC;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.dadi590.assist_c_a.Modules.Speech.CONSTS_BC_Speech;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.TelephonyManagement;
import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ACD.ACD;

/**
 * The module that processes and executes all commands told to it (from the speech recognition or by text).
 */
public final class CmdsExecutor implements IModuleInst {

	// This variable can't be local. It must memorize the last value, so they must always remain in memory.
	// Also, because of that, the instance of this class must also remain in memory, as it's done in the ModulesList.
	// The variable is static to be able to be changed without needing the instance of the module (the module utils).
	private static boolean some_cmd_detected = false;

	static final List<Runnable> after_speak_runnables = new ArrayList<>(10);

	private static final String NO_CMD = "";
	private final class Command {
		@NonNull String command_str;
		@NonNull String cmd_action;
		@Nullable Runnable runnable;
		long time_ms_cmd_detection;

		Command() {
			command_str = "";
			cmd_action = "";
			runnable = null;
			time_ms_cmd_detection = 0L;
		}

		void resetFields(@NonNull final String command_str, @NonNull final String cmd_spoken_action,
						 @Nullable final Runnable what_to_do) {
			this.command_str = command_str;
			this.cmd_action = cmd_spoken_action;
			this.runnable = what_to_do;
			time_ms_cmd_detection = System.currentTimeMillis();
		}
	}
	private Command previous_cmd = new Command();

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private Handler main_handler = null;

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
			UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
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
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		// Static variable. If the module is restarted, this must be reset.
		some_cmd_detected = false;

		registerReceiver();
	}

	public static final int NOTHING_EXECUTED = 0;
	public static final int SOMETHING_EXECUTED = 1;
	public static final int ERR_PROC_CMDS = -1;
	public static final int APU_UNAVAILABLE = -2;
	/**
	 * <p>This function checks and executes all tasks included in a string.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NOTHING_EXECUTED} --> for the returning value: if no task was detected</p>
	 * <p>- {@link #SOMETHING_EXECUTED} --> for the returning value: if some task was detected</p>
	 * <p>- {@link #ERR_PROC_CMDS} --> for the returning value: if there was an internal error with
	 * {@link ACD#main(String, String)}</p>
	 * <p>- {@link #APU_UNAVAILABLE} --> for the returning value: if the Assistant Platforms Unifier module is not
	 * available</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param sentence_str the string to be analyzed for commands
	 * @param partial_results true if the function is being called by partial recognition results (onPartialResults()),
	 *                        false otherwise (onResults(); other, like a text input).
	 * @param only_returning true if one wants nothing but the return value, false to also execute all the tasks in the
	 *                       string.
	 *
	 * @return one of the constants
	 */
	int processTask(@NonNull final String sentence_str, final boolean partial_results,
						  final boolean only_returning) {
		if (!UtilsNativeLibs.isPrimaryNativeLibAvailable(UtilsNativeLibs.ACD_LIB_NAME)) {
			final String speak = "ATTENTION - Commands detection is not available. APU's correct library file was not " +
					"detected.";
			UtilsCmdsExecutor.speak(speak, false, null);

			return APU_UNAVAILABLE;
		}

		final AudioManager audioManager = (AudioManager) UtilsGeneral.getSystemService(Context.AUDIO_SERVICE);

		some_cmd_detected = false;

		final String detected_cmds_str = ACD.main(sentence_str, false, true);
		final String[] detected_cmds = detected_cmds_str.split(ACD.CMDS_SEPARATOR);

		System.out.println("*****************************");
		System.out.println(sentence_str);
		System.out.println(Arrays.toString(detected_cmds));
		System.out.println("*****************************");

		if (detected_cmds_str.startsWith(ACD.ERR_CMD_DETECT)) {
			// PS: until he stops listening himself, the "You said" part is commented out, or he'll process what was
			// said that generated the error --> infinite loop.
			// EDIT: now this restarts PocketSphinx, which will make it not hear the "You said" part.
			UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
			final String speak = "WARNING! There was a problem processing the commands sir. This needs a fix. " +
					"The error was the following: " + detected_cmds_str + ". You said: " + sentence_str;
			UtilsCmdsExecutor.speak(speak, false, null);
			System.out.println("EXECUTOR - ERR_PROC_CMDS");

			return ERR_PROC_CMDS;
		}

		for (final String command : detected_cmds) {
			final int dot_index = command.indexOf('.');
			if (-1 == dot_index) {
				// No command.
				continue;
			}

			final String cmd_id = command.substring(0, dot_index); // "14.3" --> "14"
			final String cmd_variant = command.substring(dot_index); // "14.3" --> ".3"

			String cmd_to_check = cmd_id;
			if (CmdsList.CmdAddInfo.CMDi_INF1_ASSIST_CMD.equals(CmdsList.CmdAddInfo.CMDi_INFO.get(cmd_to_check))) {
				cmd_to_check = previous_cmd.command_str;
			}
			// Keep it checking with CMDi_INF1_DO_SOMETHING and inverting the output. That way, if cmd_to_check is ""
			// (no previous command), it won't equal DO_SOMETHING and will set cmdi_only_speak to true.
			final boolean cmdi_only_speak = !CmdsList.CmdAddInfo.CMDi_INF1_DO_SOMETHING.
					equals(CmdsList.CmdAddInfo.CMDi_INFO.get(cmd_to_check));

			switch (cmd_id) {
				case (CmdsList.CmdIds.CMD_TOGGLE_FLASHLIGHT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
							CameraManagement.USAGE_FLASHLIGHT_ON : CameraManagement.USAGE_FLASHLIGHT_OFF);

					previous_cmd.resetFields(command, "toggle flashlight", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_TIME): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "It's " + UtilsTimeDate.getTimeStr(-1L);
					UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

					previous_cmd.resetFields(command, "ask time", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_DATE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "Today's " + UtilsTimeDate.getDateStr(-1L);
					UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

					previous_cmd.resetFields(command, "ask date", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_WIFI): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setWifiEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Wi-Fi toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Wi-Fi service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to toggle the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_DISABLED): {
							final String speak = "The Wi-Fi is already disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_DISABLING): {
							final String speak = "The Wi-Fi is already being disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_ENABLED): {
							final String speak = "The Wi-Fi is already enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_ENABLING): {
							final String speak = "The Wi-Fi is already being enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "toggle wifi", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_MOBILE_DATA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setMobileDataEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.NO_ERR): {
							final String speak = "Mobile Data connection toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to toggle the Mobile Data connection.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Mobile Data connection.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "toggle mobile data connection", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_BLUETOOTH): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setBluetoothEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Bluetooth toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "The device does not feature a Bluetooth adapter.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.GEN_ERR): {
							final String speak = "Error toggling the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to toggle the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (BluetoothAdapter.STATE_OFF): {
							final String speak = "The Bluetooth is already disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (BluetoothAdapter.STATE_TURNING_OFF): {
							final String speak = "The Bluetooth is already being disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (BluetoothAdapter.STATE_ON): {
							final String speak = "The Bluetooth is already enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (BluetoothAdapter.STATE_TURNING_ON): {
							final String speak = "The Bluetooth is already being enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "toggle bluetooth", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ANSWER_CALL): {
					switch (UtilsAndroidTelephony.answerPhoneCall()) {
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Call answered.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.GEN_ERR): {
							final String speak = "Error answering the call.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "answer call", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_END_CALL): {
					switch (UtilsAndroidTelephony.endPhoneCall()) {
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Call ended.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.GEN_ERR): {
							final String speak = "Error ending the call.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "end call", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_SPEAKERS): {
					final String speak;
					if (UtilsAndroidTelephony.setCallSpeakerphoneEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						speak = "Speakerphone toggled.";
					} else {
						speak = "Audio service not available on the device.";
					}
					UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

					previous_cmd.resetFields(command, "toggle speakerphone", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_AIRPLANE_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setAirplaneModeEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.NO_ERR): {
							final String speak = "Airplane Mode toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to toggle the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Airplane Mode is already disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Airplane Mode is already enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "toggle airplane mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_BATTERY_PERCENT): {
					if (!only_returning) {
						final Boolean battery_present = ValuesStorage.
								getValueObj(ValuesStorage.Keys.battery_present).getValue();
						if (null != battery_present && !battery_present) {
							final String speak = "There is no battery present on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
						}
					}

					some_cmd_detected = true;
					if (only_returning) continue;

					final Integer battery_percentage = ValuesStorage
							.getValueObj(ValuesStorage.Keys.battery_percent).getValue();
					final String speak;
					if (null == battery_percentage) {
						speak = "Battery percentage not available yet.";
					} else {
						speak = "Battery percentage: " + battery_percentage + "%.";
					}
					UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

					previous_cmd.resetFields(command, "ask battery percentage", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_SHUT_DOWN_DEVICE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					// Don't say anything if it's successful - he will already say "Shutdown detected".
					// EDIT: sometimes he doesn't say that. Now it says something anyway.

					switch (UtilsAndroidPower.shutDownDevice()) {
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Shutting down the device...";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to shut down the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error shutting down the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "shut down device", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_REBOOT_DEVICE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					// Don't say anything if it's successful - he will already say "Shutdown detected".
					// EDIT: sometimes he doesn't say that. Now it says something anyway.

					final int reboot_mode;
					switch (cmd_variant) {
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
						case (UtilsAndroid.NO_ERR): {
							final String speak = "Rebooting the device...";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.PERM_DENIED): {
							final String speak = "No permission to reboot the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						default: {
							final String speak = "Unspecified error rebooting the device.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					previous_cmd.resetFields(command, "reboot device", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TAKE_PHOTO): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(cmd_variant.equals(CmdsList.CmdRetIds.RET_15_REAR) ?
							CameraManagement.USAGE_TAKE_REAR_PHOTO : CameraManagement.USAGE_TAKE_FRONTAL_PHOTO);

					previous_cmd.resetFields(command, "take photo", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_RECORD_MEDIA): {
					switch (cmd_variant) {
						case (CmdsList.CmdRetIds.RET_16_AUDIO): {
							if (!only_returning) {
								if (!(boolean) ModulesList.getElementValue(
										ModulesList.getElementIndex(AudioRecorder.class), ModulesList.ELEMENT_SUPPORTED)) {
									final String speak = "Audio recording is not supported on this device through either " +
											"hardware or application permissions limitations.";
									UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

									continue;
								}
							}

							// Can only start recording when the Google speech recognition has finished. Not before, or
							// other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								some_cmd_detected = true;
								if (only_returning) continue;

								UtilsSpeechRecognizersBC.stopRecognition();
								UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);
							}

							break;
						}
						case (CmdsList.CmdRetIds.RET_16_VIDEO_REAR):
						case (CmdsList.CmdRetIds.RET_16_VIDEO_FRONTAL): {
							// todo

							break;
						}
					}

					previous_cmd.resetFields(command, "record media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_SAY_AGAIN): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsSpeech2BC.sayAgain();

					// todo Save speeches on an ArrayList or something to be possible to say the second-last thing or
					// one or two more (humans have limited memory --> "I don't know what I said 3 minutes ago!").
					// Also make sure if there are things with higher priority on the lists that the last thing said is
					// the last thing said when it was requested.

					previous_cmd.resetFields(command, "repeat last speech", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CALL_CONTACT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final int contact_index = (int) ACD.getSubCmdIndex(cmd_variant);
					final String contact_name = TelephonyManagement.ALL_CONTACTS[contact_index][0];
					final String contact_number = TelephonyManagement.ALL_CONTACTS[contact_index][1];

					final Runnable do_after_confirm = new Runnable() {
						@Override
						public void run() {
							System.out.println("CALL NUMBER: " + contact_number);

							final Runnable runnable = new Runnable() {
								@Override
								public void run() {
									final int return_code = UtilsAndroidTelephony.makePhoneCall(contact_number);

									switch (return_code) {
										case (UtilsAndroid.NO_CALL_EMERGENCY): {
											final String speak = "Insufficient privileges to call " + contact_number +
													", since it is an emergency number. " +
													"Instead, it was only dialed and requires your manual confirmation " +
													"to proceed the call.";
											UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

											break;
										}
										case (UtilsAndroid.NO_CALL_ANY): {
											final String speak = "Insufficient privileges to call numbers. The number " +
													"was instead only dialed and requires your manual confirmation " +
													"to proceed the call.";
											UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

											break;
										}
										case (UtilsAndroid.NOT_AVAILABLE): {
											final String speak = "Phone calls not supported on the device.";
											UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

											break;
										}
									}

									UtilsCmdsExecutor.removeRunnableFromList(this.hashCode());
								}
							};
							UtilsCmdsExecutor.addRunnableToList(runnable);

							final String speak = "Calling " + contact_name + " now, sir.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, runnable.hashCode());
						}
					};

					final String spoken_action = "phone call " + contact_name;
					UtilsCmdsExecutor.requestConfirmation(spoken_action, cmdi_only_speak);

					previous_cmd.resetFields(command, spoken_action, do_after_confirm);
					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_RECORD_MEDIA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					boolean stop_audio = false;
					boolean stop_video = false;

					if (cmd_variant.equals(CmdsList.CmdRetIds.RET_20_AUDIO)) {
						stop_audio = true;
					} else if (cmd_variant.equals(CmdsList.CmdRetIds.RET_20_VIDEO)) {
						stop_video = true;
					} else if (cmd_variant.equals(CmdsList.CmdRetIds.RET_20_ANY)) {
						stop_audio = true;
						stop_video = true;
					} else {
						continue;
					}

					if (stop_audio) {
						UtilsAudioRecorderBC.recordAudio(false, -1, true);
					}
					if (stop_video) {
						// todo
					}

					previous_cmd.resetFields(command, "stop recording media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_POWER_SAVER_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						final String speak = "Battery Saver Mode not available below Android Lollipop.";
						UtilsCmdsExecutor.speak(speak, true, null);
					} else {
						switch (UtilsAndroidPower.setBatterySaverModeEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
							case (UtilsShell.NO_ERR): {
								final String speak = "Battery Saver Mode toggled.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.PERM_DENIED): {
								final String speak = "No permission to toggle the Battery Saver Mode.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							default: {
								final String speak = "Unspecified error toggling the Battery Saver Mode.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

					previous_cmd.resetFields(command, "toggle power saver mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_MEDIA_STOP): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", cmdi_only_speak, null);

							break;
						}

						if (audioManager.isMusicActive()) {
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));
						} else {
							UtilsCmdsExecutor.speak("Already stopped sir.", cmdi_only_speak, null);
						}
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					previous_cmd.resetFields(command, "stop media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_MEDIA_PAUSE): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", cmdi_only_speak, null);

							break;
						}

						if (audioManager.isMusicActive()) {
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
						} else {
							UtilsCmdsExecutor.speak("Already paused sir.", cmdi_only_speak, null);
						}
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					previous_cmd.resetFields(command, "pause media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_MEDIA_PLAY): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", cmdi_only_speak, null);

							break;
						}

						if (audioManager.isMusicActive()) {
							UtilsCmdsExecutor.speak("Already playing sir.", cmdi_only_speak, null);
						} else {
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
							audioManager.dispatchMediaKeyEvent(
									new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
						}
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					previous_cmd.resetFields(command, "play media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_MEDIA_NEXT): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", cmdi_only_speak, null);

							break;
						}

						UtilsCmdsExecutor.speak("Next one sir.", cmdi_only_speak, null);
						audioManager.dispatchMediaKeyEvent(
								new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
						audioManager.dispatchMediaKeyEvent(
								new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					previous_cmd.resetFields(command, "next media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_MEDIA_PREVIOUS): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", cmdi_only_speak, null);

							break;
						}

						UtilsCmdsExecutor.speak("Previous one sir.", cmdi_only_speak, null);
						audioManager.dispatchMediaKeyEvent(
								new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
						audioManager.dispatchMediaKeyEvent(
								new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					previous_cmd.resetFields(command, "previous media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CONFIRM):
				case (CmdsList.CmdIds.CMD_REJECT): {
					if (null == previous_cmd.runnable ||
							(previous_cmd.time_ms_cmd_detection > (System.currentTimeMillis() + 60_000L))) {
						// No runnable to execute (no command needing confirmation then) or the previous command was
						// more than a minute ago.
						final String speak = "There is nothing to confirm or reject, sir.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					} else {
						if (cmd_id.equals(CmdsList.CmdIds.CMD_CONFIRM)) {
							previous_cmd.runnable.run();
						} else {
							UtilsCmdsExecutor.speak(previous_cmd.cmd_action + " rejected, sir.", cmdi_only_speak, null);
						}
					}

					previous_cmd.resetFields(NO_CMD, "", null);
					break;
				}
			}
		}


		/*if (detected_cmds.length == 0) {
			System.out.println("EXECUTOR - NOTHING_EXECUTED");

			return NOTHING_EXECUTED;
		} else {
			if (something_done) {
				if (!something_said) {
					if (!only_returning) {
						final String speak = "Done.";
						UtilsCmdsExecutor.speak(speak, false, null);
					}
				}
			} else if (!something_said) {
				System.out.println("EXECUTOR - NOTHING_EXECUTED");

				return NOTHING_EXECUTED;
			}
		}*/

		if (some_cmd_detected) {
			// Vibrate to indicate it did something.
			UtilsGeneral.vibrateDeviceOnce(200L);

			return SOMETHING_EXECUTED;
		} else {
			return NOTHING_EXECUTED;
		}
	}



	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(CONSTS_BC_Speech.ACTION_AFTER_SPEAK_CODE);
		intentFilter.addAction(CONSTS_BC_CmdsExec.ACTION_CALL_PROCESS_TASK);

		try {
			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-Executor - " + intent.getAction());

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
					processTask(sentence_str, partial_results, only_returning);

					break;
				}

				case CONSTS_BC_Speech.ACTION_AFTER_SPEAK_CODE: {
					final int after_speak_code = intent.getIntExtra(
							CONSTS_BC_Speech.EXTRA_AFTER_SPEAK_CODE, -1);
					for (final Runnable runnable : after_speak_runnables) {
						if (runnable.hashCode() == after_speak_code) {
							runnable.run();

							return;
						}
					}

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
