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
import com.edw590.visor_c_a.TasksList;

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

	private boolean some_cmd_detected = false;

	boolean ask_anything_else = true;

	private String last_it = "";
	private long last_it_when = 0;
	private String last_and = "";
	private long last_and_when = 0;

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
			detection_when = 0;
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
			if (what_to_do == null) {
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
		// Prepare the Advanced Commands Detection module commands array
		ACD.reloadCmdsArray(UtilsCmdsList.prepareCommandsString());

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
	 * false otherwise (onResults(); other, like a text input).
	 * @param only_returning true if one wants nothing but the return value, false to also execute all the tasks in the
	 * string.
	 * @param internal_usage true if the function is being called for internal usage such as turning on mobile data to
	 * do some user-requested task like get the weather, false if it's being called by the user.
	 *
	 * @return one of the constants
	 */
	int processTask(@NonNull final String sentence_str, final boolean partial_results,
					final boolean only_returning, final boolean internal_usage) {
		if (!UtilsNativeLibs.isPrimaryNativeLibAvailable(UtilsNativeLibs.ACD_LIB_NAME)) {
			final String speak = "ATTENTION - Commands detection is not available. APU's correct library file was " +
					"not detected.";
			UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, GPTComm.SESSION_TYPE_TEMP, false, null);

			return APU_UNAVAILABLE;
		}

		final int speech_priority;
		if (internal_usage) {
			speech_priority = Speech2.PRIORITY_MEDIUM;
		} else {
			speech_priority = Speech2.PRIORITY_USER_ACTION;
		}

		final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

		some_cmd_detected = false;
		ask_anything_else = true;

		if (System.currentTimeMillis() > last_it_when + 60*1000) {
			last_it = "";
		}
		if (System.currentTimeMillis() > last_and_when + 60*1000) {
			last_and = "";
		}

		final String cmds_info_str = ACD.main(sentence_str, false, true, last_it + "|" + last_and);
		final String[] cmds_info = cmds_info_str.split(ACD.INFO_CMDS_SEPARATOR);
		boolean send_to_GPT = false;
		if (cmds_info.length < 2) {
			sendToGPT(sentence_str);

			return NOTHING_EXECUTED;
		}
		final String[] prev_cmd_info = cmds_info[0].split("\\" + ACD.PREV_CMD_INFO_SEPARATOR);
		final String[] detected_cmds = cmds_info[1].split(ACD.CMDS_SEPARATOR);

		if (!prev_cmd_info[0].isEmpty()) {
			last_it = prev_cmd_info[0];
			last_it_when = System.currentTimeMillis();
		}
		if (!prev_cmd_info[1].isEmpty()) {
			last_and = prev_cmd_info[1];
			last_and_when = System.currentTimeMillis();
		}

		if (cmds_info_str.startsWith(ACD.ERR_CMD_DETECT)) {
			// PS: until he stops listening himself, the "You said" part is commented out, or he'll process what was
			// said that generated the error --> infinite loop.
			// EDIT: now this restarts PocketSphinx, which will make it not hear the "You said" part.
			UtilsSpeechRecognizersBC.startPocketSphinxRecognition();
			final String speak = "WARNING! There was a problem processing the commands sir. This needs a fix. " +
					"The error was the following: " + cmds_info_str + ". You said: " + sentence_str;
			UtilsSpeech2BC.speak(speak, speech_priority, Speech2.MODE1_ALWAYS_NOTIFY, UtilsSpeech2BC.SESSION_TYPE_NONE,
					false, null);

			return ERR_PROC_CMDS;
		}

		if (!partial_results) {
			if (detected_cmds.length == 0) {
				send_to_GPT = true;
			} else {
				send_to_GPT = true;
				for (final String command : detected_cmds) {
					float num = Float.parseFloat(command);
					if (num >= 1) {
						send_to_GPT = false;

						break;
					}
				}
			}
		}
		if (send_to_GPT) {
			sendToGPT(sentence_str);

			return NOTHING_EXECUTED;
		}

		for (final String command : detected_cmds) {
			final int dot_index = command.indexOf((int) '.');
			if (dot_index == -1) {
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
					equals(CmdsList.CmdAddInfo.CMDi_INFO.get(cmd_to_check)) ?
					Speech2.MODE2_BYPASS_NO_SND : Speech2.MODE_DEFAULT;

			switch (cmd_id) {
				case (CmdsList.CmdIds.CMD_TOGGLE_FLASHLIGHT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
							CameraManagement.USAGE_FLASHLIGHT_ON : CameraManagement.USAGE_FLASHLIGHT_OFF);

					if (cmd_variant.equals(CmdsList.CmdRetIds.RET_ON)) {
						final String speak = "Flashlight turned on.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						final String speak = "Flashlight turned off.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					previous_cmd = new Command(command, "toggle flashlight", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_TIME): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "It's " + UtilsTimeDate.getTimeStr(-1);
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					previous_cmd = new Command(command, "ask time", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_DATE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					final String speak = "Today's " + UtilsTimeDate.getDateStr(-1);
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					previous_cmd = new Command(command, "ask date", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_WIFI): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setWifiEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Wi-Fi turned " + (cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
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

					previous_cmd = new Command(command, "toggle wifi", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_MOBILE_DATA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setMobileDataEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Mobile Data connection turned " +
									(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ? "on." : "off.");
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

					previous_cmd = new Command(command, "toggle mobile data connection", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_BLUETOOTH): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setBluetoothEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Bluetooth turned " + (cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
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

					previous_cmd = new Command(command, "toggle bluetooth", null);
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

					previous_cmd = new Command(command, "answer call", null);
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

					previous_cmd = new Command(command, "end call", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_SPEAKERS): {
					final String speak;
					if (((String) UtilsRegistry.getData(RegistryKeys.K_CURR_PHONE_CALL_NUMBER, true)).isEmpty()) {
						speak = "The device not in a phone call.";
					} else {
						if (UtilsAndroidTelephony.setCallSpeakerphoneEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
							speak = "Speakerphone turned " + (cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
									"on." : "off.");
						} else {
							speak = "Audio service not available on the device.";
						}
					}
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

					previous_cmd = new Command(command, "toggle speakerphone", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_AIRPLANE_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setAirplaneModeEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
						case (UtilsShell.ErrCodes.NO_ERR): {
							final String speak = "Airplane Mode turned " + (cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ?
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

					previous_cmd = new Command(command, "toggle airplane mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_BATTERY_PERCENT): {
					if (!only_returning) {
						final boolean battery_present = (boolean) UtilsRegistry.
								getData(RegistryKeys.K_BATTERY_PRESENT, true);
						if (!battery_present) {
							final String speak = "There is no battery present on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);
						}
					}

					some_cmd_detected = true;
					if (only_returning) continue;

					final int battery_percentage = (int) UtilsRegistry.getData(RegistryKeys.K_BATTERY_LEVEL, true);
					final String speak = "Battery percentage: " + battery_percentage + "%.";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

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

					previous_cmd = new Command(command, "reboot device", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TAKE_PHOTO): {
					some_cmd_detected = true;
					if (only_returning) continue;

					boolean rear_pic = cmd_variant.equals(CmdsList.CmdRetIds.RET_15_REAR);

					UtilsCameraManagerBC.useCamera(rear_pic ? CameraManagement.USAGE_TAKE_REAR_PHOTO :
							CameraManagement.USAGE_TAKE_FRONTAL_PHOTO);

					String speak = "Taking a " + (rear_pic ? "rear" : "frontal") + " picture...";
					UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false, null);

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
									UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2,
											GPTComm.SESSION_TYPE_TEMP, false, null);

									continue;
								}
							}

							// Can only start recording when the commands speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								some_cmd_detected = true;
								if (only_returning) continue;

								ask_anything_else = false;

								UtilsSpeechRecognizersBC.stopRecognition(() -> {
									UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC, false);
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
						case (CmdsList.CmdRetIds.RET_16_SCREEN_1):
						case (CmdsList.CmdRetIds.RET_16_SCREEN_2): {
							if (!only_returning) {
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
							}

							// Can only start recording when the commands speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								some_cmd_detected = true;
								if (only_returning) continue;

								ask_anything_else = false;

								UtilsScreenRecorderBC.recordScreen(true, false);

								previous_cmd = new Command(command, "record audio", null);
							}

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

					previous_cmd = new Command(command, "phone call " + contact_name, null);
					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_RECORD_MEDIA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					boolean stop_audio = false;
					boolean stop_video = false;
					boolean stop_screen = false;

					switch (cmd_variant) {
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

					previous_cmd = new Command(command, "stop recording media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TOGGLE_POWER_SAVER_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						final String speak = "Battery Saver Mode not available below Android Lollipop.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					} else {
						switch (UtilsAndroidPower.setBatterySaverEnabled(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON))) {
							case (UtilsShell.ErrCodes.NO_ERR): {
								final String speak = "Battery Saver Mode turned " +
										(cmd_variant.equals(CmdsList.CmdRetIds.RET_ON) ? "on." : "off.");
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

					previous_cmd = new Command(command, "toggle power saver mode", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_CONTROL_MEDIA): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						some_cmd_detected = true;
						if (only_returning) continue;

						if (audioManager == null) {
							String speak = "No audio available on the device.";
							UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
									null);

							break;
						}

						switch (cmd_variant) {
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

					previous_cmd = new Command(command, "stop media", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_STOP_LISTENING): {
					some_cmd_detected = true;
					if (only_returning) continue;

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

					previous_cmd = new Command(command, "stop hot-word listening in the background", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_START_LISTENING): {
					some_cmd_detected = true;
					if (only_returning) continue;

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

					previous_cmd = new Command(command, "start hot-word listening in the background", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TELL_WEATHER): {
					some_cmd_detected = true;
					if (only_returning) continue;

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

					previous_cmd = new Command(command, "tell the weather", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_TELL_NEWS): {
					some_cmd_detected = true;
					if (only_returning) continue;

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

					previous_cmd = new Command(command, "tell the news", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_ASK_EVENTS): {
					some_cmd_detected = true;
					if (only_returning) continue;

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
						String[] events_ids = GMan.getEventsIdsList().split("\\|");
						String[] tasks_ids = GMan.getTasksIdsList().split("\\|");

						if (!data_was_enabled) {
							UtilsAndroidConnectivity.setMobileDataEnabled(false);
						}
						if (!wifi_was_enabled) {
							UtilsAndroidConnectivity.setWifiEnabled(false);
						}

						speak = GManUtils.getEventsList(events_ids, cmd_variant);

						if (cmd_variant.equals(CmdsList.CmdRetIds.RET_31_TODAY) ||
								cmd_variant.equals(CmdsList.CmdRetIds.RET_31_TOMORROW)) {
							speak += " " + GManUtils.getTasksList(tasks_ids, cmd_variant);
						}

						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_ACTIVE, true,
								null);
					} else {
						speak = "Not connected to the server to get the tasks and events.";
						UtilsSpeech2BC.speak(speak, speech_priority, speech_mode2, GPTComm.SESSION_TYPE_TEMP, false,
								null);
					}

					previous_cmd = new Command(command, "tell events and tasks", null);
					break;
				}
				case (CmdsList.CmdIds.CMD_GONNA_SLEEP): {
					some_cmd_detected = true;
					if (only_returning) continue;

					//UtilsAndroidConnectivity.setAirplaneModeEnabled(true); todo Needs root commands implementation
					//UtilsRegistry.setValue(ValuesRegistry.Keys.IS_USER_SLEEPING, true);

					previous_cmd = new Command(command, "get ready to sleep", null);
					break;
				}
			}
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

		if (some_cmd_detected) {
			/*if (ask_anything_else && !internal_usage) {
				final String speak = "Anything else sir?";
				UtilsSpeech2BC.speak(speak, speech_priority, 0, true, UtilsSpeech2.CALL_COMMANDS_RECOG);
			}*/

			return SOMETHING_EXECUTED;
		} else {
			return NOTHING_EXECUTED;
		}
	}

	private void sendToGPT(final String txt_to_send) {
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
					final boolean internal_usage = intent.getBooleanExtra(CONSTS_BC_CmdsExec.EXTRA_CALL_PROCESS_TASK_4,
							false);
					processTask(sentence_str, partial_results, only_returning, internal_usage);

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
