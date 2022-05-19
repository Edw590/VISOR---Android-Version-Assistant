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
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidConnectivity;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidPower;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroid;
import com.dadi590.assist_c_a.GlobalUtils.AndroidSystem.UtilsAndroidTelephony;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.GlobalUtils.UtilsTimeDate;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CameraManager.UtilsCameraManagerBC;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.UtilsSpeechRecognizersBC;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS_ValueStorage;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.util.Arrays;

import CommandsDetection_APU.CommandsDetection_APU;

/**
 * The module that processes and executes all commands told to it (from the speech recognition or by text).
 */
public class CmdsExecutor implements IModule {
	// This variable can't be local. It must memorize the last value, so they must always remain in memory.
	// Also, because of that, the instance of this class must also remain in memory, as it's done in the ModulesList.
	// The variable is static to be able to be changed without needing the instance of the module (the module utils).
	private static boolean some_cmd_detected = false;

	///////////////////////////////////////////////////////////////
	// IModule stuff
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
		try {
			UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		is_module_destroyed = true;
	}
	// IModule stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public CmdsExecutor() {
		// Static variable. If the module is restarted, this must be reset.
		some_cmd_detected = false;

		registerReceiver();
	}

	public static final int NOTHING_EXECUTED = 0;
	public static final int SOMETHING_EXECUTED = 1;
	public static final int ERR_PROC_CMDS = -1;
	public static final int UNAVAILABLE_APU = -2;
	/**
	 * <p>This function checks and executes all tasks included in a string.</p>
	 * <br>
	 * <p>Note: the {@code only_returning} parameter is currently implemented only for partial results.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #NOTHING_EXECUTED} --> for the returning value: if no task was detected</p>
	 * <p>- {@link #SOMETHING_EXECUTED} --> for the returning value: if some task was detected</p>
	 * <p>- {@link #ERR_PROC_CMDS} --> for the returning value: if there was an internal error with
	 * {@link CommandsDetection_APU#main(String, String)}</p>
	 * <p>- {@link #UNAVAILABLE_APU} --> for the returning value: if the Assistant Platforms Unifier module is not
	 * available, and only on API levels below {@link Build.VERSION_CODES#M}</p>
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
	public final int processTask(@NonNull final String sentence_str, final boolean partial_results,
						  final boolean only_returning) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && UtilsGeneral.platformUnifierVersion() == null) {
			System.out.println("EXECUTOR - UNAVAILABLE_APU");
			return UNAVAILABLE_APU;
		}

		some_cmd_detected = false;

		final String detected_cmds_str = CommandsDetection_APU.main(sentence_str, CommandsDetection_APU.generateListAllCmds());
		final String[] detected_cmds = detected_cmds_str.split(CommandsDetection_APU.CMDS_SEPARATOR);

		System.out.println("*****************************");
		System.out.println(Arrays.toString(detected_cmds));
		System.out.println("*****************************");

		if (detected_cmds_str.startsWith(CommandsDetection_APU.ERR_CMD_DETECT)) {
			// PS: until he stops listening himself, the "You said" part is commented out, or he'll process what was
			// said that generated the error --> infinite loop.
			final String speak = "WARNING! There was a problem processing the commands sir. Please fix this. " +
					"The error was the following: " + detected_cmds_str/* + ". You said: " + frase_str*/;
			UtilsCmdsExecutor.speak(speak, false, null);
			System.out.println("EXECUTOR - ERR_PROC_CMDS");
			return ERR_PROC_CMDS;
		}

		for (final String command : detected_cmds) {
			final String cmd_constant = command.split("\\.")[0];

			final boolean cmdi_only_speak = CommandsDetection_APU.CMDi_INF1_ONLY_SPEAK.equals(CommandsDetection_APU.
					getCmdAdditionalInfo(cmd_constant, CommandsDetection_APU.CMDi_INDEX_INF1));

			switch (cmd_constant) {
				case (CommandsDetection_APU.CMD_TOGGLE_MEDIA): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext().
								getSystemService(Context.AUDIO_SERVICE);
						switch (command) {
							case (CommandsDetection_APU.RET_1_STOP): {
								some_cmd_detected = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));
								} else {
									UtilsCmdsExecutor.speak("Already stopped sir.", cmdi_only_speak, null);
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_PAUSE): {
								some_cmd_detected = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
								} else {
									UtilsCmdsExecutor.speak("Already paused sir.", cmdi_only_speak, null);
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_PLAY): {
								some_cmd_detected = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									UtilsCmdsExecutor.speak("Already playing sir.", cmdi_only_speak, null);
								} else {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_NEXT): {
								UtilsCmdsExecutor.speak("Next one sir.", cmdi_only_speak, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));

								break;
							}
							case (CommandsDetection_APU.RET_1_PREVIOUS): {
								some_cmd_detected = true;
								if (only_returning) continue;

								UtilsCmdsExecutor.speak("Previous one sir.", cmdi_only_speak, null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));

								break;
							}
						}
				} else {
					UtilsCmdsExecutor.speak("Feature only available on Android KitKat on newer.", cmdi_only_speak, null);
				}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_TIME): {
					if (CommandsDetection_APU.RET_2.equals(command)) {
						some_cmd_detected = true;
						if (only_returning) continue;

						final String speak = "It's " + UtilsTimeDate.getTimeStr();
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_DATE): {
					if (CommandsDetection_APU.RET_3.equals(command)) {
						some_cmd_detected = true;
						if (only_returning) continue;

						final String speak = "Today's " + UtilsTimeDate.getDateStr();
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_WIFI): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setWifiEnabled(CommandsDetection_APU.RET_4_ON.equals(command))) {
						case (UtilsAndroid.NO_ERRORS): {
							final String speak = "Wi-Fi toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ERROR): {
							final String speak = "Error toggling the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NO_ROOT): {
							final String speak = "Error toggling the Wi-Fi state - no root user rights available nor " +
									"app installed as system app.";
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
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_MOBILE_DATA): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setMobileDataEnabled(CommandsDetection_APU.RET_5_ON.equals(command))) {
						case (UtilsAndroid.NO_ERRORS): {
							final String speak = "Mobile Data connection toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ERROR): {
							final String speak = "Error toggling the Mobile Data connection.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NO_ROOT): {
							final String speak = "Error toggling the Mobile Data connection - no root user rights " +
									"available nor app installed as system app.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_BLUETOOTH): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setBluetoothEnabled(CommandsDetection_APU.RET_6_ON.equals(command))) {
						case (UtilsAndroid.NO_ERRORS): {
							final String speak = "Bluetooth toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ERROR): {
							final String speak = "Error toggling the Bluetooth.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroidConnectivity.NO_BLUETOOTH_ADAPTER): {
							final String speak = "The device does not feature a Bluetooth adapter.";
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
					}

					break;
				}
				case (CommandsDetection_APU.CMD_ANSWER_CALL): {
					if (CommandsDetection_APU.RET_7.equals(command)) {
						switch (UtilsAndroidTelephony.answerPhoneCall()) {
							case (UtilsAndroid.NO_ERRORS): {
								final String speak = "Call answered.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.ERROR): {
								final String speak = "Error answering the call.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_FLASHLIGHT): {
					some_cmd_detected = true;
					if (only_returning) continue;

					UtilsCameraManagerBC.useCamera(CommandsDetection_APU.RET_8_ON.equals(command) ?
							CameraManagement.USAGE_FLASHLIGHT_ON : CameraManagement.USAGE_FLASHLIGHT_OFF);

					break;
				}
				case (CommandsDetection_APU.CMD_END_CALL): {
					if (CommandsDetection_APU.RET_9.equals(command)) {
						switch (UtilsAndroidTelephony.endPhoneCall()) {
							case (UtilsAndroid.NO_ERRORS): {
								final String speak = "Call ended.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.ERROR): {
								final String speak = "Error ending the call.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_SPEAKERS): {
					UtilsAndroidTelephony.setCallSpeakerphoneEnabled(CommandsDetection_APU.RET_10_ON.equals(command));

					final String speak = "Speakerphone toggled.";
					UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_AIRPLANE_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					switch (UtilsAndroidConnectivity.setAirplaneModeEnabled(CommandsDetection_APU.RET_11_ON.equals(command))) {
						case (UtilsAndroid.NO_ERRORS): {
							final String speak = "Airplane Mode toggled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.ERROR): {
							final String speak = "Error toggling the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroid.NO_ROOT): {
							final String speak = "Error toggling the Airplane Mode - no root user rights available " +
									"nor app installed as system app.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroidConnectivity.ALREADY_DISABLED): {
							final String speak = "The Airplane Mode is already disabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
						case (UtilsAndroidConnectivity.ALREADY_ENABLED): {
							final String speak = "The Airplane Mode is already enabled.";
							UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

							break;
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_BATTERY_PERCENT): {
					if (CommandsDetection_APU.RET_12.equals(command)) {
						some_cmd_detected = true;
						if (only_returning) continue;

						final String speak = "Battery percentage: " + ValuesStorage.getValue(CONSTS_ValueStorage.battery_percentage) +
								"%.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_SHUT_DOWN_DEVICE): {
					if (CommandsDetection_APU.RET_13.equals(command)) {
						some_cmd_detected = true;
						if (only_returning) continue;

						String speak = "Shutting down the device, sir.";
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

						switch (UtilsAndroidPower.shutDownDevice()) {
							//case (UtilsAndroid.NO_ERRORS): - can't exist, since the function may not return
							case (UtilsAndroid.ERROR): {
								speak = "Error shutting down the device.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.NO_ROOT): {
								speak = "Error shutting down the device - no root user rights available nor app " +
										"installed as system app.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_REBOOT_DEVICE): {
					if (CommandsDetection_APU.RET_14_NORMAL.equals(command) ||
							CommandsDetection_APU.RET_14_SAFE_MODE.equals(command) ||
							CommandsDetection_APU.RET_14_RECOVERY.equals(command)) {
						some_cmd_detected = true;
						if (only_returning) continue;

						String speak;
						if (CommandsDetection_APU.RET_14_NORMAL.equals(command)) {
							speak = "Rebooting the device, sir.";
						} else if (CommandsDetection_APU.RET_14_SAFE_MODE.equals(command)) {
							speak = "Rebooting the device into safe mode, sir.";
						} else {
							speak = "Rebooting the device into the recovery, sir.";
						}
						UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

						final int reboot_mode;
						if (CommandsDetection_APU.RET_14_NORMAL.equals(command)) {
							reboot_mode = UtilsAndroidPower.MODE_NORMAL;
						} else if (CommandsDetection_APU.RET_14_SAFE_MODE.equals(command)) {
							reboot_mode = UtilsAndroidPower.MODE_SAFE;
						} else {
							reboot_mode = UtilsAndroidPower.MODE_RECOVERY;
						}
						switch (UtilsAndroidPower.rebootDevice(reboot_mode)) {
							//case (UtilsAndroid.NO_ERRORS): - can't exist, since the function may not return
							case (UtilsAndroid.ERROR): {
								speak = "Error rebooting down the device.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.NO_ROOT): {
								speak = "Error rebooting down the device - no root user rights available nor app " +
										"installed as system app.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TAKE_PHOTO): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_RECORD_MEDIA): {
					switch (command) {
						case (CommandsDetection_APU.RET_16_AUDIO): {
							// Can only start recording when the Google speech recognition has finished. Not before, or
							// other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								some_cmd_detected = true;
								if (only_returning) continue;

								UtilsSpeechRecognizersBC.stopRecognition();
								UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC);
							}

							break;
						}
						case (CommandsDetection_APU.RET_16_VIDEO_REAR):
						case (CommandsDetection_APU.RET_16_VIDEO_FRONTAL): {
							// todo

							break;
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_SAY_AGAIN): {
					UtilsSpeech2BC.sayAgain();

					// todo Save speeches on an ArrayList or something to be possible to say the second-last thing or
					// one or two more (humans have limited memory --> "I don't know what I said 3 minutes ago!").
					// Also make sure if there are things with higher priority on the lists that the last thing said is
					// the last thing said when it was requested.

					break;
				}
				case (CommandsDetection_APU.CMD_MAKE_CALL): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_POWER_SAVER_MODE): {
					some_cmd_detected = true;
					if (only_returning) continue;

					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
						final String speak = "Battery Saver Mode not available below Android Lollipop.";
						UtilsCmdsExecutor.speak(speak, true, null);
					} else {
						switch (UtilsAndroidPower.setBatterySaverModeEnabled(CommandsDetection_APU.RET_19_ON.equals(command))) {
							case (UtilsAndroid.NO_ERRORS): {
								final String speak = "Battery Saver Mode toggled.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.ERROR): {
								final String speak = "Error toggling the Battery Saver Mode.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
							case (UtilsAndroid.NO_ROOT): {
								final String speak = "Error toggling the Battery Saver Mode - no root user rights " +
										"available nor app installed as system app.";
								UtilsCmdsExecutor.speak(speak, cmdi_only_speak, null);

								break;
							}
						}
					}

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
			// Twice because vibrate once only is when he's listening for commands.
			UtilsGeneral.vibrateDeviceOnce(200L);

			System.out.println("EXECUTOR - SOMETHING_EXECUTED");

			return SOMETHING_EXECUTED;
		} else {
			System.out.println("EXECUTOR - NOTHING_EXECUTED");

			return NOTHING_EXECUTED;
		}
	}



	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	final void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(CONSTS_BC_CmdsExec.ACTION_CALL_PROCESS_TASK);

		try {
			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, intentFilter);
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
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
