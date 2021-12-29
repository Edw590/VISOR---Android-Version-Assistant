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
import com.dadi590.assist_c_a.GlobalUtils.UtilsAndroidSettings;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.dadi590.assist_c_a.Modules.CameraManager.CameraManagement;
import com.dadi590.assist_c_a.Modules.CameraManager.UtilsCameraManagerBC;
import com.dadi590.assist_c_a.Modules.SpeechRecognition.UtilsSpeechRecognizersBC;
import com.dadi590.assist_c_a.ValuesStorage.CONSTS;
import com.dadi590.assist_c_a.ValuesStorage.ValuesStorage;

import java.util.Arrays;

import CommandsDetection_APU.CommandsDetection_APU;

/**
 * The module that processes and executes all commands told to it (from the speech recognition or by text).
 */
public class CmdsExecutor implements IModule {
	// None of these variables can be local. They must memorize the last value, so they must always remain in memory.
	// Also, because of that, the instance of this class must also remain in memory, as it's done in the Main Service.
	// The variables are static to be able to be changed without needing the instance of the module.
	private static boolean something_done = false;
	static boolean something_said = false;

	private boolean is_module_alive = true;
	@Override
	public final boolean isModuleWorkingProperly() {
		if (!is_module_alive) {
			return false;
		}

		return true;
	}
	@Override
	public final void destroyModule() {
		UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		is_module_alive = false;
	}

	/**
	 * <p>Main class constructor.</p>
	 */
	public CmdsExecutor() {
		super();
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
	 * <p>- {@link #UNAVAILABLE_APU} --> for the returning value: if the Platforms Unifier module is not available</p>
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
		if (UtilsGeneral.platformUnifierVersion() == null) {
			System.out.println("EXECUTOR - UNAVAILABLE_APU");
			return UNAVAILABLE_APU;
		}

		something_said = false;
		something_done = false;

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
			UtilsCmdsExecutor.speak(speak, null);
			System.out.println("EXECUTOR - ERR_PROC_CMDS");
			return ERR_PROC_CMDS;
		}

		for (final String command : detected_cmds) {
			switch (command.split("\\.")[0]) {
				case (CommandsDetection_APU.CMD_TOGGLE_MEDIA): {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext().getSystemService(Context.AUDIO_SERVICE);
						switch (command) {
							case (CommandsDetection_APU.RET_1_STOP): {
								something_done = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP));
								} else {
									UtilsCmdsExecutor.speak("Already stopped sir.", null);
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_PAUSE): {
								something_done = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
								} else {
									UtilsCmdsExecutor.speak("Already paused sir.", null);
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_PLAY): {
								something_done = true;
								if (only_returning) continue;

								if (audioManager.isMusicActive()) {
									UtilsCmdsExecutor.speak("Already playing sir.", null);
								} else {
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
									audioManager.dispatchMediaKeyEvent(
											new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
								}

								break;
							}
							case (CommandsDetection_APU.RET_1_NEXT): {
								UtilsCmdsExecutor.speak("Next one sir.", null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT));

								break;
							}
							case (CommandsDetection_APU.RET_1_PREVIOUS): {
								something_done = true;
								if (only_returning) continue;

								UtilsCmdsExecutor.speak("Previous one sir.", null);
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
								audioManager.dispatchMediaKeyEvent(
										new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS));

								break;
							}
						}
				} else {
					UtilsCmdsExecutor.speak("Feature only available on Android KitKat on newer.", null);
				}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_TIME): {
					if (command.equals(CommandsDetection_APU.RET_2)) {
						something_done = true;
						if (only_returning) continue;

						final String speak = "It's " + ValuesStorage.getValue(CONSTS.current_time);
						UtilsCmdsExecutor.speak(speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_DATE): {
					if (command.equals(CommandsDetection_APU.RET_3)) {
						something_done = true;
						if (only_returning) continue;

						final String speak = "Today's " + ValuesStorage.getValue(CONSTS.current_date);
						UtilsCmdsExecutor.speak(speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_WIFI): {
					something_done = true;
					if (only_returning) continue;

					switch (UtilsAndroidSettings.setWifiEnabled(command.equals(CommandsDetection_APU.RET_4_ON))) {
						case (UtilsAndroidSettings.NO_ERRORS): {
							final String speak = "Wi-Fi toggled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.ERROR): {
							final String speak = "Error toggling the Wi-Fi.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.NO_ROOT): {
							final String speak = "The Wi-Fi cannot be toggled without root user rights due to " +
									"Android security politics, and such rights are not available to the app.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_DISABLED): {
							final String speak = "The Wi-Fi is already disabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_DISABLING): {
							final String speak = "The Wi-Fi is already being disabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_ENABLED): {
							final String speak = "The Wi-Fi is already enabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (WifiManager.WIFI_STATE_ENABLING): {
							final String speak = "The Wi-Fi is already being enabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_MOBILE_DATA): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_BLUETOOTH): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_ANSWER_CALL): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_FLASHLIGHT): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_END_CALL): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_SPEAKERS): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TOGGLE_AIRPLANE_MODE): {
					something_done = true;
					if (only_returning) continue;

					switch (UtilsAndroidSettings.setAirplaneModeEnabled(command.equals(CommandsDetection_APU.RET_11_ON))) {
						case (UtilsAndroidSettings.NO_ERRORS): {
							final String speak = "Airplane Mode toggled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.ERROR): {
							final String speak = "Error toggling the Airplane Mode.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.NO_ROOT): {
							final String speak = "The Airplane Mode cannot be toggled without root user rights due to " +
									"Android security politics, and such rights are not available to the app.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.ALREADY_DISABLED): {
							final String speak = "The Airplane Mode is already disabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
						case (UtilsAndroidSettings.ALREADY_ENABLED): {
							final String speak = "The Airplane Mode is already enabled.";
							UtilsCmdsExecutor.speak(speak, null);

							break;
						}
					}

					break;
				}
				case (CommandsDetection_APU.CMD_ASK_BATTERY_PERCENT): {
					if (command.equals(CommandsDetection_APU.RET_12)) {
						something_done = true;
						if (only_returning) continue;

						final String speak = "Battery percentage: " + ValuesStorage.getValue(CONSTS.battery_percentage) +
								"%.";
						UtilsCmdsExecutor.speak(speak, null);
					}

					break;
				}
				case (CommandsDetection_APU.CMD_SHUT_DOWN_PHONE): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_REBOOT_PHONE): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_TAKE_PHOTO): {
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_RECORD_MEDIA): {
					switch (command) {
						case (CommandsDetection_APU.RET_16_AUDIO): {
							// Can only start recording when the Google speech recognition has finished. Not before,
							// or other things the user might want to say will be ignored (not cool).
							if (!partial_results) {
								something_done = true;
								if (only_returning) continue;

								UtilsSpeechRecognizersBC.stopRecognition();
								UtilsAudioRecorderBC.recordAudio(true, MediaRecorder.AudioSource.MIC);
							}

							break;
						}
						case (CommandsDetection_APU.RET_16_VIDEO_FRONTAL): {
							// todo

							break;
						}
						case (CommandsDetection_APU.RET_16_VIDEO_REAR): {
							// todo

							break;
						}
					}
					// todo

					break;
				}
				case (CommandsDetection_APU.CMD_SAY_AGAIN): {
					// todo
					// You must implement a way inside Speech2 class to say something that was said before. Save
					// speeches on an ArrayList or something. A broadcast can't return a value immediately...
					// Also make sure if there are things with higher priority on the lists that the last thing said is
					// the last thing said when it was requested.
					/*final String speak = "I said " + UtilsSpeech2BC.;
					UtilsExecutor.speak(speak, null);*/

					break;
				}
				case (CommandsDetection_APU.CMD_MAKE_CALL): {
					// todo

					break;
				}
			}
		}


		if (detected_cmds.length == 0) {
			System.out.println("EXECUTOR - NOTHING_EXECUTED");

			return NOTHING_EXECUTED;
		} else {
			if (something_done) {
				if (!something_said) {
					if (!only_returning) {
						final String speak = "Done.";
						UtilsCmdsExecutor.speak(speak, null);
					}
				}
			} else if (!something_said) {
				System.out.println("EXECUTOR - NOTHING_EXECUTED");

				return NOTHING_EXECUTED;
			}
		}

		// Vibrate to indicate it did something.
		// Twice because vibrate once only is when he's listening for commands.
		UtilsGeneral.vibrateDeviceOnce(200L);

		System.out.println("EXECUTOR - SOMETHING_EXECUTED");

		return SOMETHING_EXECUTED;
	}



	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	final void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(CONSTS_BC.ACTION_CALL_PROCESS_TASK);

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
				case (CONSTS_BC.ACTION_CALL_PROCESS_TASK): {
					final String sentence_str = intent.getStringExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_1);
					final boolean partial_results = intent.getBooleanExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_2,
							false);
					final boolean only_returning = intent.getBooleanExtra(CONSTS_BC.EXTRA_CALL_PROCESS_TASK_3,
							false);
					processTask(sentence_str, partial_results, only_returning);
				}
			}
		}
	};
}
