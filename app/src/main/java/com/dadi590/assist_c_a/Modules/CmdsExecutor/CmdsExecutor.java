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

import com.dadi590.assist_c_a.GlobalInterfaces.IModuleInst;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroid;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidTelephony;
import com.dadi590.assist_c_a.GlobalUtils.UtilsContext;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsNativeLibs;
import com.dadi590.assist_c_a.GlobalUtils.UtilsShell;
import com.dadi590.assist_c_a.GlobalUtils.UtilsTimeDate;
import com.dadi590.assist_c_a.Modules.AudioRecorder.AudioRecorder;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CameraManager.UtilsCameraManagerBC;
import com.dadi590.assist_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.UtilsRegistry;
import com.dadi590.assist_c_a.Modules.PreferencesManager.Registry.ValuesRegistry;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;
import com.dadi590.assist_c_a.Modules.TelephonyManagement.TelephonyManagement;
import com.dadi590.assist_c_a.ModulesList;
import com.dadi590.assist_c_a.TasksList;

import java.util.Arrays;

import ACD.ACD;

/**
 * The module that processes and executes all commands told to it (from the speech recognition or by text).
 */
public final class CmdsExecutor implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	private boolean some_cmd_detected = false;

	boolean ask_anything_else = true;

	private final class Command {
		/** The command code that comes out of the ACD. */
		@NonNull final String command_code;
		/** A description to be spoken of the command action. Complete the sentence: "This command is used to..." */
		@NonNull final String cmd_spoken_action;
		/** The {@link TasksList.Task#task_id} of the task to execute after the command is completed. */
		final int task_id;
		/** The time when the command was detected. */
		final long detection_when;

		/**
		 * <p>Initialize all to defaults.</p>
		 */
		Command() {
			command_code = "";
			cmd_spoken_action = "";
			task_id = -1;
			detection_when = 0L;
		}

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param command_code {@link #command_code}
		 * @param cmd_spoken_action {@link #cmd_spoken_action}
		 * @param what_to_do {@link #task_id}
		 */
		Command(@NonNull final String command_code, @NonNull final String cmd_spoken_action,
				@Nullable final Runnable what_to_do) {
			this.command_code = command_code;
			this.cmd_spoken_action = cmd_spoken_action;
			if (null == what_to_do) {
				task_id = -1;
			} else {
				task_id = TasksList.addTask(what_to_do);
			}
			detection_when = System.currentTimeMillis();
		}
	}
	private Command previous_cmd = new Command();

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

		some_cmd_detected = false;

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
			UtilsCmdsExecutor.speak(speak, 0, null);

			return APU_UNAVAILABLE;
		}

		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

		some_cmd_detected = false;
		ask_anything_else = true;

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
			UtilsCmdsExecutor.speak(speak, Speech2.MODE1_ALWAYS_NOTIFY, null);
			System.out.println("EXECUTOR - ERR_PROC_CMDS");

			return ERR_PROC_CMDS;
		}

		for (final String command : detected_cmds) {
			final int dot_index = command.indexOf((int) '.');
			if (-1 == dot_index) {
				// No command.
				continue;
			}

			final String cmd_id = command.substring(0, dot_index); // "14.3" --> "14"
			final String cmd_variant = command.substring(dot_index); // "14.3" --> ".3"

			String cmd_to_check = cmd_id;
			if (CmdsList.CmdAddInfo.CMDi_INF1_ASSIST_CMD.equals(CmdsList.CmdAddInfo.CMDi_INFO.get(cmd_to_check))) {
				cmd_to_check = previous_cmd.command_code;
			}
			// Keep it checking with CMDi_INF1_DO_SOMETHING and inverting the output. That way, if cmd_to_check is ""
			// (no previous command), it won't equal DO_SOMETHING and will set cmdi_only_speak to true.
			final int speech_mode2 = CmdsList.CmdAddInfo.CMDi_INF1_ONLY_SPEAK.
					equals(CmdsList.CmdAddInfo.CMDi_INFO.get(cmd_to_check)) ? Speech2.MODE2_BYPASS_NO_SND : Speech2.MODE2_DEFAULT;

			switch (cmd_id) {
				case (CmdsList.CmdIds.CMD_TOGGLE_FLASHLIGHT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
							CameraManagement.USAGE_FLASHLIGHT_ON : CameraManagement.USAGE_FLASHLIGHT_OFF);

					previous_cmd = new Command(command, "toggle flashlight", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_TIME): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "It's " + UtilsTimeDate.getTimeStr(-1L);
					UtilsCmdsExecutor.speak(speak, speech_mode2, null);

					previous_cmd = new Command(command, "ask time", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_DATE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "Today's " + UtilsTimeDate.getDateStr(-1L);
					UtilsCmdsExecutor.speak(speak, speech_mode2, null);

					previous_cmd = new Command(command, "ask date", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_WIFI): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setWifiEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Wi-Fi toggled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Wi-Fi service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Wi-Fi is already disabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLING): {
							final String speak = "The Wi-Fi is already being disabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Wi-Fi is already enabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLING): {
							final String speak = "The Wi-Fi is already being enabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "toggle wifi", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_MOBILE_DATA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setMobileDataEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Mobile Data connection toggled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Mobile Data connection.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Mobile Data connection.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "toggle mobile data connection", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_BLUETOOTH): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setBluetoothEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Bluetooth toggled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "The device does not feature a Bluetooth adapter.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error toggling the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Bluetooth is already disabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLING): {
							final String speak = "The Bluetooth is already being disabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Bluetooth is already enabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLING): {
							final String speak = "The Bluetooth is already being enabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "toggle bluetooth", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ANSWER_CALL): {
					switch (UtilsAndroidTelephony.answerPhoneCall()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Call answered.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error answering the call.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "answer call", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_END_CALL): {
					switch (UtilsAndroidTelephony.endPhoneCall()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Call ended.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Telephony service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.GEN_ERR): {
							final String speak = "Error ending the call.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "end call", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_SPEAKERS): {
					final String speak;
					if (UtilsRegistry.getValue(ValuesRegistry.Keys.CURR_PHONE_CALL_NUMBER).getData("").isEmpty()) {
						speak = "The device not in a phone call.";
					} else {
						if (UtilsAndroidTelephony.setCallSpeakerphoneEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
							speak = "Speakerphone toggled.";
						} else {
							speak = "Audio service not available on the device.";
						}
					}
					UtilsCmdsExecutor.speak(speak, speech_mode2, null);

					previous_cmd = new Command(command, "toggle speakerphone", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_AIRPLANE_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setAirplaneModeEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Airplane Mode toggled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to toggle the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_DISABLED): {
							final String speak = "The Airplane Mode is already disabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsAndroid.ALREADY_ENABLED): {
							final String speak = "The Airplane Mode is already enabled.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error toggling the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "toggle airplane mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_BATTERY_PERCENT): {
					if (!only_returning) {
						final Boolean battery_present = UtilsRegistry.
								getValue(ValuesRegistry.Keys.BATTERY_PRESENT).getData();
						if (null != battery_present && !battery_present) {
							final String speak = "There is no battery present on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);
						}
					}

					some_cmd_detected = true;
					if (only_returning) continue;

					final Integer battery_percentage = UtilsRegistry
							.getValue(ValuesRegistry.Keys.BATTERY_PERCENT).getData();
					final String speak;
					if (null == battery_percentage) {
						speak = "Battery percentage not available yet.";
					} else {
						speak = "Battery percentage: " + battery_percentage + "%.";
					}
					UtilsCmdsExecutor.speak(speak, speech_mode2, null);

					previous_cmd = new Command(command, "ask battery percentage", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_POWER_SHUT_DOWN): {
					some_cmd_detected = true;
					if (only_returning) continue;

					// Don't say anything if it's successful - he will already say "Shutdown detected".
					// EDIT: sometimes he doesn't say that. Now it says something anyway.

					switch (UtilsAndroidPower.shutDownDevice()) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Shutting down the device...";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							ask_anything_else = false;

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to shut down the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error shutting down the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "shut down device", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_POWER_REBOOT): {
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
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Rebooting the device...";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							ask_anything_else = false;

							break;
						}
						case (UtilsAndroid.NOT_AVAILABLE): {
							final String speak = "Power service not available on the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						case (UtilsShell.ErrCodes.PERM_DENIED): {
							final String speak = "No permission to reboot the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
						default: {
							final String speak = "Unspecified error rebooting the device.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, null);

							break;
						}
					}

					previous_cmd = new Command(command, "reboot device", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TAKE_PHOTO): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(cmd_variant.equals(CmdsList.CmdRetIds.RET_15_REAR) ?
							CameraManagement.USAGE_TAKE_REAR_PHOTO : CameraManagement.USAGE_TAKE_FRONTAL_PHOTO);

					previous_cmd = new Command(command, "take photo", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_RECORD_MEDIA): {
					switch (cmd_variant) {
						case (CmdsList.CmdRetIds.RET_16_AUDIO_1):
						case (CmdsList.CmdRetIds.RET_16_AUDIO_2): {
							if (!only_returning) {
								if (!(boolean) ModulesList.getElementValue(
										ModulesList.getElementIndex(AudioRecorder.class), ModulesList.ELEMENT_SUPPORTED)) {
									final String speak = "Audio recording is not supported on this device through " +
											"either hardware or application permissions limitations.";
									UtilsCmdsExecutor.speak(speak, speech_mode2, null);

									continue;
								}
							}

							// Can only start recording when the commands speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								some_cmd_detected = true;
								if (only_returning) continue;

								ask_anything_else = false;

								UtilsSpeechRecognizersBC.stopRecognition(new Runnable() {
									@Override
									public void run() {
										UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);
									}
								});

								previous_cmd = new Command(command, "record audio", null);
							}

							break;
						}
						case (CmdsList.CmdRetIds.RET_16_VIDEO_1):
						case (CmdsList.CmdRetIds.RET_16_VIDEO_2): {
							// todo
							// todo Also missing the record frontal and rear video commands (this one is generic)

							break;
						}
					}

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

					previous_cmd = new Command(command, "repeat last speech", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CALL_CONTACT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final int contact_index = (int) ACD.getSubCmdIndex(cmd_variant);
					final String[][] contacts_list = TelephonyManagement.getContactsList();
					final String contact_name = contacts_list[contact_index][0];
					final String contact_number = contacts_list[contact_index][1];

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
											UtilsCmdsExecutor.speak(speak, speech_mode2, null);

											break;
										}
										case (UtilsAndroid.NO_CALL_ANY): {
											final String speak = "Insufficient privileges to call numbers. The number " +
													"was instead only dialed and requires your manual confirmation " +
													"to proceed the call.";
											UtilsCmdsExecutor.speak(speak, speech_mode2, null);

											break;
										}
										case (UtilsAndroid.NOT_AVAILABLE): {
											final String speak = "Phone calls not supported on the device.";
											UtilsCmdsExecutor.speak(speak, speech_mode2, null);

											break;
										}
									}
								}
							};

							final String speak = "Calling " + contact_name + " now, sir.";
							UtilsCmdsExecutor.speak(speak, speech_mode2, runnable);
						}
					};

					final String spoken_action = "phone call " + contact_name;
					requestConfirmation(spoken_action, speech_mode2);

					previous_cmd = new Command(command, spoken_action, do_after_confirm);
					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_RECORD_MEDIA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					boolean stop_audio = false;
					boolean stop_video = false;

					switch (cmd_variant) {
						case CmdsList.CmdRetIds.RET_20_AUDIO: {
							stop_audio = true;

							break;
						}
						case CmdsList.CmdRetIds.RET_20_VIDEO: {
							stop_video = true;

							break;
						}
						case CmdsList.CmdRetIds.RET_20_ANY: {
							stop_audio = true;
							stop_video = true;

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

					previous_cmd = new Command(command, "stop recording media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_POWER_SAVER_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						final String speak = "Battery Saver Mode not available below Android Lollipop.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					} else {
						switch (UtilsAndroidPower.setBatterySaverEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
							case (UtilsShell.ErrCodes.NO_ERR): {
								final String speak = "Battery Saver Mode toggled.";
								UtilsCmdsExecutor.speak(speak, speech_mode2, null);

								break;
							}
							case (UtilsShell.ErrCodes.PERM_DENIED): {
								final String speak = "No permission to toggle the Battery Saver Mode.";
								UtilsCmdsExecutor.speak(speak, speech_mode2, null);

								break;
							}
							default: {
								final String speak = "Unspecified error toggling the Battery Saver Mode.";
								UtilsCmdsExecutor.speak(speak, speech_mode2, null);

								break;
							}
						}
					}

					previous_cmd = new Command(command, "toggle power saver mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CONTROL_MEDIA): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (null == audioManager) {
							UtilsCmdsExecutor.speak("No audio available on the device.", speech_mode2, null);

							break;
						}

						switch (cmd_variant) {
							case (CmdsList.CmdRetIds.RET_21_PLAY): {
								UtilsCmdsExecutor.speak("Playing now.", speech_mode2, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_PAUSE): {
								UtilsCmdsExecutor.speak("Paused sir.", speech_mode2, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_STOP): {
								UtilsCmdsExecutor.speak("Stopped sir.", speech_mode2, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));


								break;
							}
							case (CmdsList.CmdRetIds.RET_21_NEXT): {
								UtilsCmdsExecutor.speak("Next one sir.", speech_mode2, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));

								break;
							}
							case (CmdsList.CmdRetIds.RET_21_PREVIOUS): {
								UtilsCmdsExecutor.speak("Previous one sir.", speech_mode2, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));

								break;
							}
						}
					} else {
						final String speak = "Feature only available on Android KitKat on newer.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					}

					previous_cmd = new Command(command, "stop media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CONFIRM):
				case (CmdsList.CmdIds.CMD_REJECT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (previous_cmd.task_id < 0 ||
							(previous_cmd.detection_when > (System.currentTimeMillis() + 60_000L))) {
						// No runnable to execute (no command needing confirmation then) or the previous command was
						// more than a minute ago.
						final String speak = "There is nothing to confirm or reject, sir.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					} else {
						if (cmd_id.equals(CmdsList.CmdIds.CMD_CONFIRM)) {
							new Thread(TasksList.removeTask(previous_cmd.task_id).runnable).start();
						} else {
							UtilsCmdsExecutor.speak(previous_cmd.cmd_spoken_action + " rejected, sir.", speech_mode2, null);
						}
					}

					previous_cmd = new Command();
					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_LISTENING): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (UtilsRegistry.getValue(ValuesRegistry.Keys.POCKETSPHINX_REQUEST_STOP).getData(false)) {
						final String speak = "Background hot-word recognition already stopped.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					} else {
						UtilsRegistry.setValue(ValuesRegistry.Keys.POCKETSPHINX_REQUEST_STOP, true);
						UtilsSpeechRecognizersBC.stopRecognition(null);

						final String speak = "Background hot-word recognition stopped.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					}

					previous_cmd = new Command(command, "stop hot-word listening in the background", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_START_LISTENING): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (UtilsRegistry.getValue(ValuesRegistry.Keys.POCKETSPHINX_REQUEST_STOP).getData(false)) {
						UtilsRegistry.setValue(ValuesRegistry.Keys.POCKETSPHINX_REQUEST_STOP, false);
						// We could wait for the controller to restart it, but this way it's faster.
						UtilsSpeechRecognizersBC.startPocketSphinxRecognition();

						final String speak = "Background hot-word recognition started.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					} else {
						final String speak = "The background hot-word recognition is not stopped.";
						UtilsCmdsExecutor.speak(speak, speech_mode2, null);
					}

					previous_cmd = new Command(command, "start hot-word listening in the background", null);
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
						UtilsCmdsExecutor.speak(speak, 0, null);
					}
				}
			} else if (!something_said) {
				System.out.println("EXECUTOR - NOTHING_EXECUTED");

				return NOTHING_EXECUTED;
			}
		}*/

		if (some_cmd_detected) {
			if (ask_anything_else) {
				final String speak = "Anything else sir?";
				UtilsCmdsExecutor.speak(speak, 0, UtilsSpeech2.CALL_COMMANDS_RECOG);
			}

			return SOMETHING_EXECUTED;
		} else {
			return NOTHING_EXECUTED;
		}
	}

	/**
	 * <p>Makes VISOR speak "Do you confirm to [action]?".</p>
	 *
	 * @param action the action mentioned above
	 * @param mode same as in {@link Speech2#speak(String, int, int)}
	 */
	private void requestConfirmation(@NonNull final String action, final int mode) {
		ask_anything_else = false;

		final String speak = "Do you confirm to " + action + "?";
		UtilsCmdsExecutor.speak(speak, mode, UtilsSpeech2.CALL_COMMANDS_RECOG);
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
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
